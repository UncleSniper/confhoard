package org.unclesniper.confhoard.core.listener;

import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.Fragment;
import org.unclesniper.confhoard.core.ConfState;
import org.unclesniper.confhoard.core.util.Listeners;
import org.unclesniper.confhoard.core.ConfStateBinding;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.core.security.Credentials;

public abstract class AbstractConfStateReconfiguringSlotListener extends SelectingSlotListener {

	private final Listeners<ConfStateReconfigurationListener> reconfigurationListeners
			= new Listeners<ConfStateReconfigurationListener>();

	public AbstractConfStateReconfiguringSlotListener() {}

	public void addReconfigurationListener(ConfStateReconfigurationListener listener) {
		reconfigurationListeners.addListener(listener);
	}

	public boolean removeReconfigurationListener(ConfStateReconfigurationListener listener) {
		return reconfigurationListeners.removeListener(listener);
	}

	public void fireConfStateReconfigured(ConfStateReconfigurationListener.ReconfigurationEvent event)
			throws IOException, ConfHoardException {
		reconfigurationListeners.confFire(listener -> listener.confStateReconfigured(event), null, null);
	}

	private void reconfigure(SlotEvent event, Fragment fragment) throws IOException, ConfHoardException {
		ConfStateBinding outerState = event.getConfState();
		if(outerState == null)
			throw new IllegalArgumentException("No ConfStateBinding configured");
		if(fragment == null)
			return;
		Credentials credentials = event.getCredentials();
		Function<String, Object> requestParameters = event::getRequestParameter;
		ConfState newState = parseConfState(event, fragment, outerState);
		if(newState == null)
			return;
		ConfState oldState = outerState.getConfState();
		newState.getLoadedStorage(credentials, outerState, requestParameters);
		fireConfStateReconfigured(new ConfStateReconfigurationListener.ReconfigurationEvent(oldState, newState,
				credentials, requestParameters));
		outerState.setConfState(newState);
	}

	protected abstract ConfState parseConfState(SlotEvent event, Fragment fragment, ConfStateBinding state)
			throws IOException, ConfHoardException;

	@Override
	protected void selectedSlotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException {
		reconfigure(event, event.getSlot().getFragment());
	}

	@Override
	protected void selectedSlotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException {
		reconfigure(event, event.getNextFragment());
	}

}
