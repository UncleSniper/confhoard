package org.unclesniper.confhoard.core.listener;

import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.Fragment;
import org.unclesniper.confhoard.core.ConfState;
import org.unclesniper.confhoard.core.ConfStateBinding;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.core.security.Credentials;

public abstract class AbstractConfStateReconfiguringSlotListener extends SelectingSlotListener {

	public AbstractConfStateReconfiguringSlotListener() {}

	private void reconfigure(SlotEvent event, Fragment fragment, Credentials credentials,
			Function<String, Object> requestParameters) throws IOException, ConfHoardException {
		ConfStateBinding outerState = event.getConfState();
		if(outerState == null)
			throw new IllegalArgumentException("No ConfStateBinding configured");
		if(fragment == null)
			return;
		ConfState newState = parseConfState(event, fragment, credentials, outerState, requestParameters);
		if(newState == null)
			return;
		newState.getLoadedStorage(outerState, requestParameters);
		outerState.setConfState(newState);
	}

	protected abstract ConfState parseConfState(SlotEvent event, Fragment fragment, Credentials credentials,
			ConfStateBinding state, Function<String, Object> requestParameters)
			throws IOException, ConfHoardException;

	@Override
	protected void selectedSlotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException {
		reconfigure(event, event.getSlot().getFragment(), null, null);
	}

	@Override
	protected void selectedSlotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException {
		reconfigure(event, event.getNextFragment(), event.getCredentials(), event::getRequestParameter);
	}

}
