package org.unclesniper.confhoard.core;

import java.util.List;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Function;

public interface SlotListener {

	public class SlotEvent {

		private final Slot slot;

		private final ConfStateBinding confState;

		public SlotEvent(Slot slot, ConfStateBinding confState) {
			this.slot = slot;
			this.confState = confState;
		}

		public Slot getSlot() {
			return slot;
		}

		public ConfStateBinding getConfState() {
			return confState;
		}

	}

	public class SlotLoadedEvent extends SlotEvent {

		public SlotLoadedEvent(Slot slot, ConfStateBinding confState) {
			super(slot, confState);
		}

	}

	public class SlotUpdatedEvent extends SlotEvent {

		private final Fragment previousFragment;

		private final List<SlotUpdateIssue> issues = new LinkedList<SlotUpdateIssue>();

		private boolean rollbackUpdate;

		private final Function<String, Object> requestParameters;

		public SlotUpdatedEvent(Slot slot, ConfStateBinding confState, Fragment previousFragment,
				Function<String, Object> requestParameters) {
			super(slot, confState);
			this.previousFragment = previousFragment;
			this.requestParameters = requestParameters;
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

		public Object getRequestParameter(String key) {
			return key == null || requestParameters == null ? null : requestParameters.apply(key);
		}

	}

	void slotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException;

	void slotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException;

}
