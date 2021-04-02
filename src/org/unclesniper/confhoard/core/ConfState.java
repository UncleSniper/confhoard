package org.unclesniper.confhoard.core;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Collections;
import org.unclesniper.confhoard.core.util.HoardSink;
import org.unclesniper.confhoard.core.security.SlotAction;
import org.unclesniper.confhoard.core.security.Credentials;

public class ConfState {

	private volatile ConfManagementState managementState;

	private volatile Storage storage;

	private volatile boolean fragmentsLoaded;

	private final Map<String, Slot> slots = new HashMap<String, Slot>();

	public ConfState() {}

	public ConfManagementState getManagementState() {
		return managementState;
	}

	public void setManagementState(ConfManagementState managementState) {
		if(managementState == this.managementState)
			return;
		ConfManagementState oldManagementState = this.managementState;
		this.managementState = managementState;
		if(oldManagementState != null && oldManagementState.getConfState() == this)
			oldManagementState.setConfState(null);
		if(managementState != null)
			managementState.setConfState(this);
	}

	public Storage getStorage() {
		return storage;
	}

	public void setStorage(Storage storage) {
		if(storage != this.storage)
			fragmentsLoaded = false;
		this.storage = storage;
	}

	public Set<String> getSlotKeys() {
		return Collections.unmodifiableSet(slots.keySet());
	}

	public Slot getSlot(String key) {
		return slots.get(key);
	}

	public void addSlot(Slot slot) {
		if(slot == null)
			throw new IllegalArgumentException("Slot cannot be null");
		String key = slot.getKey();
		synchronized(slots) {
			Slot other = slots.get(key);
			if(other == null) {
				slots.put(key, slot);
				slot.setConfState(this);
			}
			else if(other != slot)
				throw new IllegalArgumentException("Slot key '" + key + "' is already registered");
		}
	}

	public boolean removeSlot(Slot slot) {
		if(slot == null)
			return false;
		String key = slot.getKey();
		synchronized(slots) {
			Slot other = slots.get(key);
			if(other != slot)
				return false;
			slots.remove(key);
			if(slot.getConfState() == this)
				slot.setConfState(null);
		}
		return true;
	}

	public Storage getLoadedStorage() throws IOException, ConfHoardException {
		if(storage == null)
			throw new IllegalStateException("No storage backend is configured");
		if(!fragmentsLoaded) {
			Set<Slot> loadedSlots = new HashSet<Slot>();
			storage.loadFragments(slots::get, loadedSlots::add);
			fragmentsLoaded = true;
			for(Slot slot : loadedSlots)
				slot.fireSlotLoaded(new SlotListener.SlotLoadedEvent(slot), null);
		}
		return storage;
	}

	public Fragment updateSlot(String key, InputStream content, Credentials credentials)
			throws IOException, ConfHoardException {
		if(key == null)
			throw new IllegalArgumentException("Slot key cannot be null");
		Slot slot;
		synchronized(slots) {
			slot = slots.get(key);
		}
		if(slot == null)
			throw new NoSuchSlotException(key);
		if(!slot.mayPerformAction(SlotAction.UPDATE, credentials))
			throw new SlotAccessForbiddenException(slot, SlotAction.UPDATE);
		Fragment newFragment = getLoadedStorage().newFragment(slot, content);
		Fragment oldFragment = slot.getFragment();
		List<SlotListener> fired = new LinkedList<SlotListener>();
		SlotListener.SlotUpdatedEvent event = new SlotListener.SlotUpdatedEvent(slot, oldFragment);
		slot.setFragment(newFragment);
		try {
			slot.fireSlotUpdated(event, fired::add);
			slot.fireFragmentUpdated();
		}
		catch(RuntimeException | IOException | ConfHoardException e) {
			return slotUpdateFailed(slot, oldFragment, event, fired, e);
		}
		if(event.shouldRollback())
			return slotUpdateFailed(slot, oldFragment, event, fired, null);
		return newFragment;
	}

	private Fragment slotUpdateFailed(Slot slot, Fragment oldFragment, SlotListener.SlotUpdatedEvent event,
			List<SlotListener> fired, Throwable cause) throws SlotUpdateFailedException {
		Fragment newFragment = slot.getFragment();
		slot.setFragment(oldFragment);
		if(event.shouldRollback()) {
			SlotListener.SlotUpdatedEvent rollbackEvent = new SlotListener.SlotUpdatedEvent(slot, newFragment);
			try {
				for(SlotListener listener : fired)
					listener.slotUpdated(rollbackEvent);
				slot.fireFragmentUpdated();
			}
			catch(RuntimeException | IOException | ConfHoardException e) {
				return throwSlotUpdateFailed(slot, rollbackEvent, e, true);
			}
		}
		else {
			try {
				slot.fireFragmentUpdated();
			}
			catch(RuntimeException | IOException e) {
				cause = e;
			}
		}
		return throwSlotUpdateFailed(slot, event, cause, false);
	}

	private Fragment throwSlotUpdateFailed(Slot slot, SlotListener.SlotUpdatedEvent event, Throwable cause,
			boolean rollback) throws SlotUpdateFailedException {
		MultiSlotUpdateIssue multi = new MultiSlotUpdateIssue(event.getSlotUpdateIssues(),
				MultiSlotUpdateIssue.DEFAULT_LIST_LINE_TRANSFORM);
		if(cause != null)
			multi.addSlotUpdateIssue(new TextSlotUpdateIssue(cause));
		throw new SlotUpdateFailedException(slot, multi, rollback);
	}

	public void retrieveSlot(String key, Credentials credentials, HoardSink<InputStream> sink)
			throws IOException, ConfHoardException {
		if(key == null)
			throw new IllegalArgumentException("Slot key cannot be null");
		if(sink == null)
			throw new IllegalArgumentException("Sink cannot be null");
		Slot slot;
		synchronized(slots) {
			slot = slots.get(key);
		}
		if(slot == null)
			throw new NoSuchSlotException(key);
		if(!slot.mayPerformAction(SlotAction.RETRIEVE, credentials))
			throw new SlotAccessForbiddenException(slot, SlotAction.RETRIEVE);
		Fragment fragment = slot.getFragment();
		if(fragment == null)
			sink.accept(null);
		else {
			try(InputStream is = fragment.retrieve()) {
				sink.accept(is);
			}
		}
	}

}
