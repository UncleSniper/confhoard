package org.unclesniper.confhoard.core;

import java.util.List;
import java.io.IOException;
import java.util.LinkedList;

public interface SlotListener {

	public class SlotEvent {

		private final Slot slot;

		public SlotEvent(Slot slot) {
			this.slot = slot;
		}

		public Slot getSlot() {
			return slot;
		}

	}

	public class SlotLoadedEvent extends SlotEvent {

		public SlotLoadedEvent(Slot slot) {
			super(slot);
		}

	}

	public class SlotUpdatedEvent extends SlotEvent {

		private final Fragment previousFragment;

		private final List<SlotUpdateIssue> issues = new LinkedList<SlotUpdateIssue>();

		private boolean rollbackUpdate;

		public SlotUpdatedEvent(Slot slot, Fragment previousFragment) {
			super(slot);
			this.previousFragment = previousFragment;
		}

		public Fragment getPreviousFragment() {
			return previousFragment;
		}

		public void addSlotUpdateIssue(SlotUpdateIssue issue) {
			if(issue == null)
				throw new IllegalArgumentException("Issue cannot be null");
			issues.add(issue);
		}

		public Iterable<SlotUpdateIssue> getSlotUpdateIssues() {
			return issues;
		}

		public boolean hasSlotUpdateIssues() {
			return !issues.isEmpty();
		}

		public void requireRollback() {
			rollbackUpdate = true;
		}

		public boolean shouldRollback() {
			return rollbackUpdate;
		}

	}

	void slotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException;

	void slotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException;

}
