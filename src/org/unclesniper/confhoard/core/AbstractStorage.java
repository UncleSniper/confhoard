package org.unclesniper.confhoard.core;

import java.io.IOException;
import org.unclesniper.confhoard.core.util.Listeners;

public abstract class AbstractStorage implements Storage {

	private final Listeners<StorageListener> storageListeners = new Listeners<StorageListener>();

	public AbstractStorage() {}

	public void fireSlotLoaded(StorageListener.SlotLoadedStorageEvent event)
			throws IOException, ConfHoardException {
		storageListeners.confFire(listener -> listener.slotLoaded(event), null, null);
	}

	public void fireSlotPurged(StorageListener.SlotPurgedStorageEvent event)
			throws IOException, ConfHoardException {
		storageListeners.confFire(listener -> listener.slotPurged(event), null, null);
	}

	public void fireStorageListenerFailed(StorageListener.StorageListenerFailedStorageEvent event) {
		storageListeners.fire(listener -> listener.storageListenerFailed(event), null, null);
	}

	protected final void safeFireSlotLoaded(StorageListener.SlotLoadedStorageEvent event) {
		try {
			fireSlotLoaded(event);
		}
		catch(IOException | ConfHoardException e) {
			safeFireStorageListenerFailed(new StorageListener.StorageListenerFailedStorageEvent(this, e,
					event::getRequestParameter));
		}
	}

	protected final void safeFireSlotPurged(StorageListener.SlotPurgedStorageEvent event) {
		try {
			fireSlotPurged(event);
		}
		catch(IOException | ConfHoardException e) {
			safeFireStorageListenerFailed(new StorageListener.StorageListenerFailedStorageEvent(this, e,
					event::getRequestParameter));
		}
	}

	private void safeFireStorageListenerFailed(StorageListener.StorageListenerFailedStorageEvent event) {
		try {
			fireStorageListenerFailed(event);
		}
		catch(RuntimeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addStorageListener(StorageListener listener) {
		storageListeners.addListener(listener);
	}

	@Override
	public boolean removeStorageListener(StorageListener listener) {
		return storageListeners.removeListener(listener);
	}

}
