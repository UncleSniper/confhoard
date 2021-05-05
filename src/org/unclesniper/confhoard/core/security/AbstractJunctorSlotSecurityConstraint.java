package org.unclesniper.confhoard.core.security;

import org.unclesniper.confhoard.core.Slot;

public abstract class AbstractJunctorSlotSecurityConstraint extends AbstractMultiSlotSecurityConstraint {

	private final boolean shortCircuitMatch;

	private final boolean shortCircuitReturn;

	public AbstractJunctorSlotSecurityConstraint(boolean shortCircuitMatch, boolean shortCircuitReturn) {
		this.shortCircuitMatch = shortCircuitMatch;
		this.shortCircuitReturn = shortCircuitReturn;
	}

	@Override
	public boolean mayPerform(Slot slot, SlotAction action, Credentials credentials) {
		for(SlotSecurityConstraint child : children) {
			if(child.mayPerform(slot, action, credentials) == shortCircuitMatch)
				return shortCircuitReturn;
		}
		return !shortCircuitReturn;
	}

}
