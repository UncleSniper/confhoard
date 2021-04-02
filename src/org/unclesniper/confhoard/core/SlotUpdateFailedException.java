package org.unclesniper.confhoard.core;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SlotUpdateFailedException extends ConfHoardException implements SlotUpdateIssue {

	private class ExceptionIterator implements Iterator<String> {

		private boolean hadMessage;

		private final Iterator<String> lineIterator = issue == null ? null : issue.getMessageLines();

		public ExceptionIterator() {}

		@Override
		public boolean hasNext() {
			return !hadMessage || (lineIterator != null && lineIterator.hasNext());
		}

		@Override
		public String next() {
			if(!hadMessage) {
				hadMessage = true;
				return getMessage();
			}
			if(lineIterator == null || !lineIterator.hasNext())
				throw new NoSuchElementException();
			return lineIterator.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private final Slot slot;

	private final SlotUpdateIssue issue;

	private final boolean rollback;

	public SlotUpdateFailedException(Slot slot, SlotUpdateIssue issue, boolean rollback) {
		super((rollback ? "Rollback" : "Update") + " of slot '" + slot.getKey() + "' was rejected");
		this.slot = slot;
		this.issue = issue;
		this.rollback = rollback;
	}

	public Slot getSlot() {
		return slot;
	}

	public SlotUpdateIssue getIssue() {
		return issue;
	}

	public boolean wasRollback() {
		return rollback;
	}

	@Override
	public Iterator<String> getMessageLines() {
		return new ExceptionIterator();
	}

}
