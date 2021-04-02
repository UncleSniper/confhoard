package org.unclesniper.confhoard.core;

import java.util.Map;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.function.Consumer;
import org.unclesniper.confhoard.core.util.Listeners;

public class Slot {

	private final String key;

	private ConfState confState;

	private Fragment fragment;

	private final Listeners<SlotListener> slotListeners = new Listeners<SlotListener>();

	private final Map<SlotStorageListener, SlotStorageListener> storageListeners
			= new IdentityHashMap<SlotStorageListener, SlotStorageListener>();

	public Slot(String key) {
		if(key == null)
			throw new IllegalArgumentException("Slot key cannot be null");
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public ConfState getConfState() {
		return confState;
	}

	public void setConfState(ConfState confState) {
		if(confState == this.confState)
			return;
		ConfState oldState = this.confState;
		this.confState = confState;
		if(oldState != null)
			oldState.removeSlot(this);
	}

	public Fragment getFragment() {
		return fragment;
	}

	public void setFragment(Fragment fragment) {
		if(fragment != null && fragment.getSlot() != this)
			throw new IllegalArgumentException("Cannot put fragment into slot '" + key
					+ "', as it belongs to slot '" + fragment.getSlot().getKey() + "'");
		this.fragment = fragment;
	}

	public void addSlotListener(SlotListener listener) {
		slotListeners.addListener(listener);
	}

	public boolean removeSlotListener(SlotListener listener) {
		return slotListeners.removeListener(listener);
	}

	void fireSlotLoaded(SlotListener.SlotLoadedEvent event, Consumer<SlotListener> fired)
			throws IOException, ConfHoardException {
		slotListeners.confFire(listener -> listener.slotLoaded(event), fired);
	}

	void fireSlotUpdated(SlotListener.SlotUpdatedEvent event, Consumer<SlotListener> fired)
			throws IOException, ConfHoardException {
		slotListeners.confFire(listener -> listener.slotUpdated(event), fired);
	}

	public void addStorageListener(SlotStorageListener listener) {
		if(listener == null)
			return;
		synchronized(storageListeners) {
			storageListeners.put(listener, listener);
		}
	}

	public boolean removeStorageListener(SlotStorageListener listener) {
		if(listener == null)
			return false;
		synchronized(storageListeners) {
			return storageListeners.remove(listener) != null;
		}
	}

	void fireFragmentUpdated() throws IOException {
		synchronized(storageListeners) {
			for(SlotStorageListener listener : storageListeners.keySet())
				listener.saveSlot();
		}
	}

}
