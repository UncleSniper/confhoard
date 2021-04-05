package org.unclesniper.confhoard.core.listener;

import java.io.IOException;
import org.unclesniper.confhoard.core.SlotListener;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.core.TextSlotUpdateIssue;

public abstract class AbstractSlotListener implements SlotListener {

	private boolean throwErrors;

	public AbstractSlotListener() {}

	public boolean isThrowErrors() {
		return throwErrors;
	}

	public void setThrowErrors(boolean throwErrors) {
		this.throwErrors = throwErrors;
	}

	protected abstract void throwingSlotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException;

	@Override
	public void slotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException {
		if(event == null)
			throw new IllegalArgumentException("Slot update event cannot be null");
		try {
			throwingSlotUpdated(event);
		}
		catch(IOException | ConfHoardException | RuntimeException e) {
			if(throwErrors)
				throw e;
			event.addSlotUpdateIssue(new TextSlotUpdateIssue(e));
			event.requireRollback();
		}
	}

}
