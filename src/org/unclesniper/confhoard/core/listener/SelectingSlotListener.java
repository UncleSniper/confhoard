package org.unclesniper.confhoard.core.listener;

import java.io.IOException;
import org.unclesniper.confhoard.core.ConfHoardException;

public abstract class SelectingSlotListener extends AbstractSlotListener {

	private boolean triggerOnLoad = true;

	private boolean triggerOnUpdate = true;

	public SelectingSlotListener() {}

	public boolean isTriggerOnLoad() {
		return triggerOnLoad;
	}

	public void setTriggerOnLoad(boolean triggerOnLoad) {
		this.triggerOnLoad = triggerOnLoad;
	}

	public boolean isTriggerOnUpdate() {
		return triggerOnUpdate;
	}

	public void setTriggerOnUpdate(boolean triggerOnUpdate) {
		this.triggerOnUpdate = triggerOnUpdate;
	}

	protected abstract void selectedSlotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException;

	protected abstract void selectedSlotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException;

	@Override
	public void slotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException {
		if(triggerOnLoad)
			selectedSlotLoaded(event);
	}

	@Override
	protected void throwingSlotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException {
		if(triggerOnUpdate)
			selectedSlotUpdated(event);
	}

}
