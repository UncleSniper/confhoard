package org.unclesniper.confhoard.core;

import java.util.List;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Function;
import org.unclesniper.confhoard.core.security.Credentials;
import org.unclesniper.confhoard.core.security.SystemInternalCredentials;

public interface SlotListener {

	public abstract class SlotEvent {

		private final Slot slot;

		private final ConfStateBinding confState;

		private final Function<String, Object> requestParameters;

		public SlotEvent(Slot slot, ConfStateBinding confState, Function<String, Object> requestParameters) {
			this.slot = slot;
			this.confState = confState;
			this.requestParameters = requestParameters;
		}

		public Slot getSlot() {
			return slot;
		}

		public ConfStateBinding getConfState() {
			return confState;
		}

		public Object getRequestParameter(String key) {
			return key == null || requestParameters == null ? null : requestParameters.apply(key);
		}

		public abstract Credentials getCredentials();

		public abstract void propagate(SlotListener listener) throws IOException, ConfHoardException;

	}

	public class SlotLoadedEvent extends SlotEvent {

		public SlotLoadedEvent(Slot slot, ConfStateBinding confState, Function<String, Object> requestParameters) {
			super(slot, confState, requestParameters);
		}

		@Override
		public Credentials getCredentials() {
			return SystemInternalCredentials.instance;
		}

		@Override
		public void propagate(SlotListener listener) throws IOException, ConfHoardException {
			if(listener == null)
				throw new IllegalArgumentException("Slot listener cannot be null");
			listener.slotLoaded(this);
		}

	}

	public class SlotUpdatedEvent extends SlotEvent {

		private final Fragment previousFragment;

		private final Fragment nextFragment;

		private final List<SlotUpdateIssue> issues = new LinkedList<SlotUpdateIssue>();

		private boolean rollbackUpdate;

		private final Credentials credentials;

		public SlotUpdatedEvent(Slot slot, Credentials credentials, ConfStateBinding confState,
				Fragment previousFragment, Fragment nextFragment, Function<String, Object> requestParameters) {
			super(slot, confState, requestParameters);
			this.credentials = credentials;
			this.previousFragment = previousFragment;
			this.nextFragment = nextFragment;
		}

		public Fragment getPreviousFragment() {
			return previousFragment;
		}

		public Fragment getNextFragment() {
			return nextFragment;
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

		@Override
		public Credentials getCredentials() {
			return credentials;
		}

		@Override
		public void propagate(SlotListener listener) throws IOException, ConfHoardException {
			if(listener == null)
				throw new IllegalArgumentException("Slot listener cannot be null");
			listener.slotUpdated(this);
		}

	}

	void slotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException;

	void slotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException;

}
