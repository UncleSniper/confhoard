package org.unclesniper.confhoard.core.security;

import java.util.List;
import java.util.LinkedList;

public abstract class AbstractMultiSlotSecurityConstraint implements SlotSecurityConstraint {

	protected final List<SlotSecurityConstraint> children = new LinkedList<SlotSecurityConstraint>();

	public AbstractMultiSlotSecurityConstraint() {}

	public Iterable<SlotSecurityConstraint> getChildren() {
		return children;
	}

	public void addChild(SlotSecurityConstraint child) {
		if(child == null)
			throw new IllegalArgumentException("Child cannot be null");
	}

}
