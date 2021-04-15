package org.unclesniper.confhoard.core;

import java.util.Set;
import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;
import org.unclesniper.confhoard.core.util.HoardSink;
import org.unclesniper.confhoard.core.security.SlotAction;
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
	public String getHashAlgorithm() {
		return confState.getHashAlgorithm();
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
	public Fragment updateSlot(String key, InputStream content, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters)
			throws IOException, ConfHoardException {
		return confState.updateSlot(key, content, credentials, enforceAccess,
				outerState == null ? this : outerState, parameters);
	}

	@Override
	public Fragment updateSlot(Slot slot, InputStream content, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters)
			throws IOException, ConfHoardException {
		return confState.updateSlot(slot, content, credentials, enforceAccess,
				outerState == null ? this : outerState, parameters);
	}

	@Override
	public void retrieveSlot(String key, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters, HoardSink<InputStream> sink)
			throws IOException, ConfHoardException {
		confState.retrieveSlot(key, credentials, enforceAccess, outerState == null ? this : outerState,
				parameters, sink);
	}

	@Override
	public void retrieveSlot(Slot slot, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters, HoardSink<InputStream> sink)
			throws IOException, ConfHoardException {
		confState.retrieveSlot(slot, credentials, enforceAccess, outerState == null ? this : outerState,
				parameters, sink);
	}

	@Override
	public void requireAccess(String key, SlotAction action, Credentials credentials)
			throws NoSuchSlotException, SlotAccessForbiddenException {
		confState.requireAccess(key, action, credentials);
	}

	@Override
	public void requireAccess(Slot slot, SlotAction action, Credentials credentials)
			throws SlotAccessForbiddenException {
		confState.requireAccess(slot, action, credentials);
	}

}
