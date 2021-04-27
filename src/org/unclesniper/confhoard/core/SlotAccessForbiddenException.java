package org.unclesniper.confhoard.core;

import org.unclesniper.confhoard.core.security.SlotAction;

public class SlotAccessForbiddenException extends ConfHoardException {

	private final Slot slot;

	private final SlotAction slotAction;

	public SlotAccessForbiddenException(Slot slot, SlotAction action) {
		super("No permission to " + (action == null ? "" : action.getActionName()) + " slot '"
				+ (slot == null ? "" : slot.getKey()) + '\'');
		if(slot == null)
			throw new IllegalArgumentException("Slot cannot be null");
		if(action == null)
			throw new IllegalArgumentException("Slot action cannot be null");
		this.slot = slot;
		slotAction = action;
	}

	public Slot getSlot() {
		return slot;
	}

	public SlotAction getSlotAction() {
		return slotAction;
	}

}
