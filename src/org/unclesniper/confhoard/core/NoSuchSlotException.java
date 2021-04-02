package org.unclesniper.confhoard.core;

public class NoSuchSlotException extends ConfHoardException {

	private final String slotKey;

	public NoSuchSlotException(String slotKey) {
		super("No such slot: " + slotKey);
		this.slotKey = slotKey;
	}

	public String getSlotKey() {
		return slotKey;
	}

}
