package org.unclesniper.confhoard.core;

import java.util.Set;
import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;
import org.unclesniper.confhoard.core.util.HoardSink;
import org.unclesniper.confhoard.core.security.Credentials;

public class ConfManagementState implements ConfStateBinding {

	private volatile ConfState confState;

	public ConfManagementState() {}

	@Override
	public ConfState getConfState() {
		return confState;
	}

	@Override
	public void setConfState(ConfState confState) {
		if(confState == this.confState)
			return;
		ConfState oldState = this.confState;
		this.confState = confState;
		if(oldState != null && oldState.getManagementState() == this)
			oldState.setManagementState(null);
		if(confState != null)
			confState.setManagementState(this);
	}

	@Override
	public Set<String> getSlotKeys() {
		return confState.getSlotKeys();
	}

	@Override
	public Collection<Slot> getSlots() {
		return confState.getSlots();
	}

	@Override
	public Slot getSlot(String key) {
		return confState.getSlot(key);
	}

	@Override
	public void addSlot(Slot slot) {
		confState.addSlot(slot);
	}

	@Override
	public boolean removeSlot(Slot slot) {
		return confState.removeSlot(slot);
	}

	@Override
	public Fragment updateSlot(String key, InputStream content, Credentials credentials,
			ConfStateBinding outerState, Function<String, Object> parameters)
			throws IOException, ConfHoardException {
		return confState.updateSlot(key, content, credentials, outerState == null ? this : outerState, parameters);
	}

	@Override
	public void retrieveSlot(String key, Credentials credentials, ConfStateBinding outerState,
			Function<String, Object> parameters, HoardSink<InputStream> sink)
			throws IOException, ConfHoardException {
		confState.retrieveSlot(key, credentials, outerState == null ? this : outerState, parameters, sink);
	}

}
