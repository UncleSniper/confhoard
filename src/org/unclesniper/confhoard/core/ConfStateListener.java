package org.unclesniper.confhoard.core;

import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.security.Credentials;

public interface ConfStateListener {

	public abstract class StateEvent {

		private final ConfStateBinding confState;

		private final Function<String, Object> requestParameters;

		public StateEvent(ConfStateBinding confState, Function<String, Object> requestParameters) {
			this.confState = confState;
			this.requestParameters = requestParameters;
		}

		public ConfStateBinding getConfState() {
			return confState;
		}

		public Object getRequestParameter(String key) {
			return key == null || requestParameters == null ? null : requestParameters.apply(key);
		}

	}

	public abstract class SlotStateEvent extends StateEvent {

		private final Slot slot;

		private final Credentials credentials;

		public SlotStateEvent(ConfStateBinding confState, Slot slot, Credentials credentials,
				Function<String, Object> requestParameters) {
			super(confState, requestParameters);
			this.slot = slot;
			this.credentials = credentials;
		}

		public Slot getSlot() {
			return slot;
		}

		public Credentials getCredentials() {
			return credentials;
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

		public ConfStateListenerFailedStateEvent(ConfStateBinding confState, Exception exception,
				Function<String, Object> requestParameters) {
			super(confState, requestParameters);
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
