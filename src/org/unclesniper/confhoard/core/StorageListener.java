package org.unclesniper.confhoard.core;

import java.io.IOException;
import java.util.function.Function;

public interface StorageListener {

	public abstract class StorageEvent {

		private final Storage storage;

		private final Function<String, Object> requestParameters;

		public StorageEvent(Storage storage, Function<String, Object> requestParameters) {
			this.storage = storage;
			this.requestParameters = requestParameters;
		}

		public Storage getStorage() {
			return storage;
		}

		public Object getRequestParameter(String key) {
			return key == null || requestParameters == null ? null : requestParameters.apply(key);
		}

	}

	public abstract class SlotStorageEvent extends StorageEvent {

		private final Slot slot;

		private final int fragmentCount;

		public SlotStorageEvent(Storage storage, Slot slot, int fragmentCount,
				Function<String, Object> requestParameters) {
			super(storage, requestParameters);
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

		public SlotLoadedStorageEvent(Storage storage, Slot slot, int fragmentCount,
				Function<String, Object> requestParameters) {
			super(storage, slot, fragmentCount, requestParameters);
		}

	}

	public class SlotPurgedStorageEvent extends SlotStorageEvent {

		public SlotPurgedStorageEvent(Storage storage, Slot slot, int fragmentCount,
				Function<String, Object> requestParameters) {
			super(storage, slot, fragmentCount, requestParameters);
		}

	}

	public class StorageListenerFailedStorageEvent extends StorageEvent {

		private final Exception exception;

		public StorageListenerFailedStorageEvent(Storage storage, Exception exception,
				Function<String, Object> requestParameters) {
			super(storage, requestParameters);
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
