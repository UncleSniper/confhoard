package org.unclesniper.confhoard.core.security;

import org.unclesniper.confhoard.core.Slot;

public interface SlotSecurityConstraint {

	boolean mayPerform(Slot slot, SlotAction action, Credentials credentials);

}
