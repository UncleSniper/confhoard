package org.unclesniper.confhoard.core.listener;

import java.io.IOException;
import org.unclesniper.confhoard.core.SlotListener;
import org.unclesniper.confhoard.core.util.Listeners;
import org.unclesniper.confhoard.core.ConfHoardException;

public class SequenceSlotListener extends AbstractSlotListener {

	private final Listeners<SlotListener> loadListeners = new Listeners<SlotListener>();

	private final Listeners<SlotListener> updateListeners = new Listeners<SlotListener>();

	private boolean skipOnRollback = true;

	public SequenceSlotListener() {}

	public boolean isSkipOnRollback() {
		return skipOnRollback;
	}

	public void setSkipOnRollback(boolean skipOnRollback) {
		this.skipOnRollback = skipOnRollback;
	}

	public void addSlotListener(SlotListener listener) {
		loadListeners.addListener(listener);
		updateListeners.addListener(listener);
	}

	public boolean removeSlotListener(SlotListener listener) {
		boolean hadLoad = loadListeners.removeListener(listener);
		boolean hadUpdate = updateListeners.removeListener(listener);
		return hadLoad || hadUpdate;
	}

	public void addSlotLoadListener(SlotListener listener) {
		loadListeners.addListener(listener);
	}

	public boolean removeSlotLoadListener(SlotListener listener) {
		return loadListeners.removeListener(listener);
	}

	public void addSlotUpdateListener(SlotListener listener) {
		updateListeners.addListener(listener);
	}

	public boolean removeSlotUpdateListener(SlotListener listener) {
		return updateListeners.removeListener(listener);
	}

	@Override
	public void slotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException {
		loadListeners.confFire(listener -> listener.slotLoaded(event), null, null);
	}

	@Override
	protected void throwingSlotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException {
		updateListeners.confFire(listener -> listener.slotUpdated(event), null,
				skipOnRollback ? event::shouldRollback : null);
	}

}
