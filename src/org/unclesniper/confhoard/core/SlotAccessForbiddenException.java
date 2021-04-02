package org.unclesniper.confhoard.core;

import org.unclesniper.confhoard.core.security.SlotAction;

public class SlotAccessForbiddenException extends ConfHoardException {

	private final Slot slot;

	public SlotAccessForbiddenException(Slot slot, SlotAction action) {
		super("No permission to " + action.getActionName() + " slot '" + slot.getKey() + '\'');
		this.slot = slot;
	}

	public Slot getSlot() {
		return slot;
	}

}
