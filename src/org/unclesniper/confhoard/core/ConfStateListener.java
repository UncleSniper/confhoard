package org.unclesniper.confhoard.core;

import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.security.Credentials;

public interface ConfStateListener {

	public abstract class StateEvent {

		private final ConfStateBinding confState;

		public StateEvent(ConfStateBinding confState) {
			this.confState = confState;
		}

		public ConfStateBinding getConfState() {
			return confState;
		}

	}

	public abstract class SlotStateEvent extends StateEvent {

		private final Slot slot;

		private final Credentials credentials;

		private final Function<String, Object> requestParameters;

		public SlotStateEvent(ConfStateBinding confState, Slot slot, Credentials credentials,
				Function<String, Object> requestParameters) {
			super(confState);
			this.slot = slot;
			this.credentials = credentials;
			this.requestParameters = requestParameters;
		}

		public Slot getSlot() {
			return slot;
		}

		public Credentials getCredentials() {
			return credentials;
		}

		public Object getRequestParameter(String key) {
			return key == null || requestParameters == null ? null : requestParameters.apply(key);
		}

	}

	public class SlotUpdateSucceededStateEvent extends SlotStateEvent {

		public SlotUpdateSucceededStateEvent(ConfStateBinding confState, Slot slot, Credentials credentials,
				Function<String, Object> requestParameters) {
			super(confState, slot, credentials, requestParameters);
		}

	}

	public class SlotUpdateFailedStateEvent extends SlotStateEvent {

		private final SlotUpdateFailedException exception;

		public SlotUpdateFailedStateEvent(ConfStateBinding confState, Slot slot, Credentials credentials,
				Function<String, Object> requestParameters, SlotUpdateFailedException exception) {
			super(confState, slot, credentials, requestParameters);
			this.exception = exception;
		}

		public SlotUpdateFailedException getException() {
			return exception;
		}

	}

	public class ConfStateListenerFailedStateEvent extends StateEvent {

		private final Exception exception;

		public ConfStateListenerFailedStateEvent(ConfStateBinding confState, Exception exception) {
			super(confState);
			this.exception = exception;
		}

		public Exception getException() {
			return exception;
		}

	}

	void slotUpdateSucceeded(SlotUpdateSucceededStateEvent event) throws IOException, ConfHoardException;

	void slotUpdateFailed(SlotUpdateFailedStateEvent event) throws IOException, ConfHoardException;

	void confStateListenerFailed(ConfStateListenerFailedStateEvent event);

}
