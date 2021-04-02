package org.unclesniper.confhoard.core;

public abstract class AbstractFragment implements Fragment {

	private final Slot slot;

	public AbstractFragment(Slot slot) {
		if(slot == null)
			throw new IllegalStateException("Slot cannot be null");
		this.slot = slot;
	}

	@Override
	public Slot getSlot() {
		return slot;
	}

}
