package org.unclesniper.confhoard.core.listener;

import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.ConfState;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.core.security.Credentials;

public interface ConfStateReconfigurationListener {

	public static class ReconfigurationEvent {

		private final ConfState previousState;

		private final ConfState nextState;

		private final Credentials credentials;

		private final Function<String, Object> requestParameters;

		public ReconfigurationEvent(ConfState previousState, ConfState nextState, Credentials credentials,
				Function<String, Object> requestParameters) {
			this.previousState = previousState;
			this.nextState = nextState;
			this.credentials = credentials;
			this.requestParameters = requestParameters;
		}

		public ConfState getPreviousState() {
			return previousState;
		}

		public ConfState getNextState() {
			return nextState;
		}

		public Credentials getCredentials() {
			return credentials;
		}

		public Object getRequestParameter(String key) {
			return key == null || requestParameters == null ? null : requestParameters.apply(key);
		}

	}

	void confStateReconfigured(ReconfigurationEvent event) throws IOException, ConfHoardException;

}
