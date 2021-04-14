package org.unclesniper.confhoard.core.security;

import org.unclesniper.confhoard.core.Slot;

public abstract class AbstractSlotSecurityConstraint implements SlotSecurityConstraint {

	private int permissions;

	public AbstractSlotSecurityConstraint() {}

	public void addPermission(SlotAction action) {
		if(action == null)
			throw new IllegalArgumentException("Action cannot be null");
		permissions |= 1 << action.ordinal();
	}

	public void removePermission(SlotAction action) {
		if(action == null)
			throw new IllegalArgumentException("Action cannot be null");
		permissions &= ~(1 << action.ordinal());
	}

	protected abstract boolean mayPerformAnyAction(Slot slot, Credentials credentials);

	@Override
	public boolean mayPerform(Slot slot, SlotAction action, Credentials credentials) {
		if(action == null)
			throw new IllegalArgumentException("Action cannot be null");
		if(credentials instanceof EverythingIsAllowedCredentials)
			return true;
		if((permissions & (1 << action.ordinal())) == 0)
			return false;
		return mayPerformAnyAction(slot, credentials);
	}

}
