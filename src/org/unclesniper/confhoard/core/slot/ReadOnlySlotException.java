package org.unclesniper.confhoard.core.slot;

import org.unclesniper.confhoard.core.Slot;
import org.unclesniper.confhoard.core.ConfHoardException;

public class ReadOnlySlotException extends ConfHoardException {

	private final Slot slot;

	public ReadOnlySlotException(Slot slot) {
		super("Slot '" + (slot == null ? "" : slot.getKey()) + "' cannot be updated, as slots of type '"
				+ (slot == null ? "" : slot.getClass().getName()) + "' are read-only");
		if(slot == null)
			throw new IllegalArgumentException("Slot cannot be null");
		this.slot = slot;
	}

	public Slot getSlot() {
		return slot;
	}

}
