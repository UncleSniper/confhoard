package org.unclesniper.confhoard.core;

import java.io.IOException;

public interface StorageListener {

	public abstract class StorageEvent {

		private final Storage storage;

		public StorageEvent(Storage storage) {
			this.storage = storage;
		}

		public Storage getStorage() {
			return storage;
		}

	}

	public abstract class SlotStorageEvent extends StorageEvent {

		private final Slot slot;

		private final int fragmentCount;

		public SlotStorageEvent(Storage storage, Slot slot, int fragmentCount) {
			super(storage);
			this.slot = slot;
			this.fragmentCount = fragmentCount;
		}

		public Slot getSlot() {
			return slot;
		}

		public int getFragmentCount() {
			return fragmentCount;
		}

	}

	public class SlotLoadedStorageEvent extends SlotStorageEvent {

		public SlotLoadedStorageEvent(Storage storage, Slot slot, int fragmentCount) {
			super(storage, slot, fragmentCount);
		}

	}

	public class SlotPurgedStorageEvent extends SlotStorageEvent {

		public SlotPurgedStorageEvent(Storage storage, Slot slot, int fragmentCount) {
			super(storage, slot, fragmentCount);
		}

	}

	public class StorageListenerFailedStorageEvent extends StorageEvent {

		private final Exception exception;

		public StorageListenerFailedStorageEvent(Storage storage, Exception exception) {
			super(storage);
			this.exception = exception;
		}

		public Exception getException() {
			return exception;
		}

	}

	void slotLoaded(SlotLoadedStorageEvent event) throws IOException, ConfHoardException;

	void slotPurged(SlotPurgedStorageEvent event) throws IOException, ConfHoardException;

	void storageListenerFailed(StorageListenerFailedStorageEvent event);

}
