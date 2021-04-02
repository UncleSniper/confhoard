package org.unclesniper.confhoard.core;

public class ConfManagementState {

	private volatile ConfState confState;

	public ConfManagementState() {}

	public ConfState getConfState() {
		return confState;
	}

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

}
