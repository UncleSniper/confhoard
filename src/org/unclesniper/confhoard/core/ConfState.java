package org.unclesniper.confhoard.core;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import org.unclesniper.confhoard.core.util.HoardSink;
import org.unclesniper.confhoard.core.util.Listeners;
import org.unclesniper.confhoard.core.security.SlotAction;
import org.unclesniper.confhoard.core.security.Credentials;

public class ConfState implements ConfStateBinding {

	public static final String DEFAULT_HASH_ALGORITHM = "SHA-256";

	private volatile ConfManagementState managementState;

	private volatile Storage storage;

	private volatile boolean fragmentsLoaded;

	private String hashAlgorithm = ConfState.DEFAULT_HASH_ALGORITHM;

	private final Map<String, Slot> slots = new HashMap<String, Slot>();

	private final Listeners<ConfStateListener> stateListeners = new Listeners<ConfStateListener>();

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

	@Override
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	public void setHashAlgorithm(String hashAlgorithm) {
		if(hashAlgorithm == null || hashAlgorithm.length() == 0)
			hashAlgorithm = ConfState.DEFAULT_HASH_ALGORITHM;
		this.hashAlgorithm = hashAlgorithm;
	}

	public Storage getLoadedStorage(ConfStateBinding outerState, Function<String, Object> parameters)
			throws IOException, ConfHoardException {
		if(storage == null)
			throw new IllegalStateException("No storage backend is configured");
		if(!fragmentsLoaded) {
			Set<Slot> loadedSlots = new HashSet<Slot>();
			storage.loadFragments(slots::get, loadedSlots::add, hashAlgorithm, parameters);
			fragmentsLoaded = true;
			for(Slot slot : loadedSlots)
				slot.fireSlotLoaded(new SlotListener.SlotLoadedEvent(slot,
						outerState == null ? this : outerState), null);
		}
		return storage;
	}

	public void addStateListener(ConfStateListener listener) {
		stateListeners.addListener(listener);
	}

	public boolean removeStateListener(ConfStateListener listener) {
		return stateListeners.removeListener(listener);
	}

	public void fireSlotUpdateSucceeded(ConfStateListener.SlotUpdateSucceededStateEvent event)
			throws IOException, ConfHoardException {
		stateListeners.confFire(listener -> listener.slotUpdateSucceeded(event), null, null);
	}

	public void fireSlotUpdateFailed(ConfStateListener.SlotUpdateFailedStateEvent event)
			throws IOException, ConfHoardException {
		stateListeners.confFire(listener -> listener.slotUpdateFailed(event), null, null);
	}

	public void fireConfStateListenerFailed(ConfStateListener.ConfStateListenerFailedStateEvent event) {
		stateListeners.fire(listener -> listener.confStateListenerFailed(event), null, null);
	}

	@Override
	public ConfState getConfState() {
		return this;
	}

	@Override
	public void setConfState(ConfState confState) {
		throw new IllegalStateException("Cannot replace configuration state in itself");
	}

	@Override
	public Set<String> getSlotKeys() {
		return Collections.unmodifiableSet(slots.keySet());
	}

	@Override
	public Collection<Slot> getSlots() {
		return Collections.unmodifiableCollection(slots.values());
	}

	@Override
	public Slot getSlot(String key) {
		return slots.get(key);
	}

	@Override
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

	@Override
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

	@Override
	public Fragment updateSlot(Slot slot, InputStream content, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters)
			throws IOException, ConfHoardException {
		return updateOwnSlot(slotBySlot(slot), content, credentials, enforceAccess, outerState, parameters);
	}

	@Override
	public Fragment updateSlot(String key, InputStream content, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters)
			throws IOException, ConfHoardException {
		return updateOwnSlot(slotByKey(key), content, credentials, enforceAccess, outerState, parameters);
	}

	private Fragment updateOwnSlot(Slot slot, InputStream content, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters)
			throws IOException, ConfHoardException {
		if(enforceAccess && !slot.mayPerformAction(SlotAction.UPDATE, credentials))
			throw new SlotAccessForbiddenException(slot, SlotAction.UPDATE);
		ConfStateBinding innerState = outerState == null ? this : outerState;
		Fragment newFragment = getLoadedStorage(innerState, parameters).newFragment(slot, content,
				hashAlgorithm, credentials, innerState, parameters);
		Fragment oldFragment = slot.getFragment();
		String hashAlgorithm = innerState.getHashAlgorithm();
		if(oldFragment != null
				&& compareHashes(oldFragment, newFragment, hashAlgorithm, credentials, innerState, parameters)) {
			newFragment.remove(credentials, innerState, parameters);
			return null;
		}
		List<SlotListener> fired = new LinkedList<SlotListener>();
		SlotListener.SlotUpdatedEvent event = new SlotListener.SlotUpdatedEvent(slot, credentials,
				innerState, oldFragment, newFragment, parameters);
		boolean setFragmentTook = false;
		try {
			slot.fireSlotUpdated(event, fired::add);
			slot.setFragment(newFragment);
			slot.fireFragmentUpdated(credentials, innerState, parameters);
			setFragmentTook = true;
		}
		catch(RuntimeException | IOException | ConfHoardException e) {
			return slotUpdateFailed(slot, oldFragment, newFragment, event, fired, e);
		}
		finally {
			if(!setFragmentTook)
				slot.setFragment(oldFragment);
		}
		if(event.shouldRollback())
			return slotUpdateFailed(slot, oldFragment, newFragment, event, fired, null);
		safeFireSlotUpdateSucceeded(new ConfStateListener.SlotUpdateSucceededStateEvent(innerState, slot,
				credentials, parameters));
		return newFragment;
	}

	private Fragment slotUpdateFailed(Slot slot, Fragment oldFragment, Fragment newFragment,
			SlotListener.SlotUpdatedEvent event, List<SlotListener> fired, Throwable cause)
			throws SlotUpdateFailedException {
		slot.setFragment(oldFragment);
		Credentials credentials = event.getCredentials();
		ConfStateBinding state = event.getConfState();
		if(event.shouldRollback()) {
			SlotListener.SlotUpdatedEvent rollbackEvent = new SlotListener.SlotUpdatedEvent(slot,
					credentials, state, newFragment, oldFragment, event::getRequestParameter);
			try {
				for(SlotListener listener : fired)
					listener.slotUpdated(rollbackEvent);
				slot.fireFragmentUpdated(credentials, state, event::getRequestParameter);
			}
			catch(RuntimeException | IOException | ConfHoardException e) {
				return throwSlotUpdateFailed(slot, rollbackEvent, e, true);
			}
		}
		else {
			try {
				slot.fireFragmentUpdated(credentials, state, event::getRequestParameter);
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
		SlotUpdateFailedException sufe = new SlotUpdateFailedException(slot, multi, rollback);
		safeFireSlotUpdateFailed(new ConfStateListener.SlotUpdateFailedStateEvent(event.getConfState(),
				event.getSlot(), event.getCredentials(), event::getRequestParameter, sufe));
		throw sufe;
	}

	private void safeFireSlotUpdateSucceeded(ConfStateListener.SlotUpdateSucceededStateEvent event) {
		try {
			fireSlotUpdateSucceeded(event);
		}
		catch(IOException | ConfHoardException e) {
			safeFireConfStateListenerFailed(new
					ConfStateListener.ConfStateListenerFailedStateEvent(event.getConfState(), e,
					event::getRequestParameter));
		}
	}

	private void safeFireSlotUpdateFailed(ConfStateListener.SlotUpdateFailedStateEvent event) {
		try {
			fireSlotUpdateFailed(event);
		}
		catch(IOException | ConfHoardException e) {
			safeFireConfStateListenerFailed(new
					ConfStateListener.ConfStateListenerFailedStateEvent(event.getConfState(), e,
					event::getRequestParameter));
		}
	}

	private void safeFireConfStateListenerFailed(ConfStateListener.ConfStateListenerFailedStateEvent event) {
		try {
			fireConfStateListenerFailed(event);
		}
		catch(RuntimeException e) {
			e.printStackTrace();
		}
	}

	private boolean compareHashes(Fragment oldFragment, Fragment newFragment, String hashAlgorithm,
			Credentials credentials, ConfStateBinding confState, Function<String, Object> parameters)
			throws IOException {
		byte[] oldHash = oldFragment.getHash(hashAlgorithm, credentials, confState, parameters);
		byte[] newHash = newFragment.getHash(hashAlgorithm, credentials, confState, parameters);
		return Arrays.equals(oldHash, newHash);
	}

	@Override
	public void retrieveSlot(String key, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters, HoardSink<InputStream> sink)
			throws IOException, ConfHoardException {
		retrieveOwnSlot(slotByKey(key), credentials, enforceAccess, outerState, parameters, sink);
	}

	@Override
	public void retrieveSlot(Slot slot, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters, HoardSink<InputStream> sink)
			throws IOException, ConfHoardException {
		retrieveOwnSlot(slotBySlot(slot), credentials, enforceAccess, outerState, parameters, sink);
	}

	private void retrieveOwnSlot(Slot slot, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters, HoardSink<InputStream> sink)
			throws IOException, ConfHoardException {
		if(sink == null)
			throw new IllegalArgumentException("Sink cannot be null");
		if(enforceAccess && !slot.mayPerformAction(SlotAction.RETRIEVE, credentials))
			throw new SlotAccessForbiddenException(slot, SlotAction.RETRIEVE);
		Fragment fragment = slot.getFragment();
		if(fragment == null)
			sink.accept(null);
		else {
			try(InputStream is = fragment.retrieve(credentials, outerState == null ? this : outerState,
					parameters)) {
				sink.accept(is);
			}
		}
	}

	@Override
	public void requireAccess(String key, SlotAction action, Credentials credentials)
			throws NoSuchSlotException, SlotAccessForbiddenException {
		Slot slot = slotByKey(key);
		if(action == null)
			throw new IllegalArgumentException("Slot action cannot be null");
		if(!slot.mayPerformAction(action, credentials))
			throw new SlotAccessForbiddenException(slot, action);
	}

	@Override
	public void requireAccess(Slot slot, SlotAction action, Credentials credentials)
			throws SlotAccessForbiddenException {
		slotBySlot(slot);
		if(action == null)
			throw new IllegalArgumentException("Slot action cannot be null");
		if(!slot.mayPerformAction(action, credentials))
			throw new SlotAccessForbiddenException(slot, action);
	}

	private Slot slotBySlot(Slot slot) {
		if(slot == null)
			throw new IllegalArgumentException("Slot cannot be null");
		if(slots.get(slot.getKey()) != slot)
			throw new IllegalArgumentException("Slot does now belong to this ConfState");
		return slot;
	}

	private Slot slotByKey(String key) throws NoSuchSlotException {
		if(key == null)
			throw new IllegalArgumentException("Slot key cannot be null");
		Slot slot;
		synchronized(slots) {
			slot = slots.get(key);
		}
		if(slot == null)
			throw new NoSuchSlotException(key);
		return slot;
	}

}
