package org.unclesniper.confhoard.core.listener;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import org.unclesniper.confhoard.core.SlotListener;
import org.unclesniper.confhoard.core.TextSlotUpdateIssue;

public class RejectingSlotListener implements SlotListener {

	private final List<String> messageLines = new LinkedList<String>();

	public RejectingSlotListener() {}

	public void addMessageLine(String line) {
		if(line == null)
			throw new IllegalArgumentException("Message line cannot be null");
		messageLines.add(line);
	}

	public List<String> getMessageLines() {
		return Collections.unmodifiableList(messageLines);
	}

	@Override
	public void slotLoaded(SlotLoadedEvent event) {}

	@Override
	public void slotUpdated(SlotUpdatedEvent event) {
		TextSlotUpdateIssue text = new TextSlotUpdateIssue();
		if(messageLines.isEmpty())
			text.addLine("Slot update rejected");
		else
			text.addLines(messageLines);
		event.addSlotUpdateIssue(text);
		event.requireRollback();
	}

}
