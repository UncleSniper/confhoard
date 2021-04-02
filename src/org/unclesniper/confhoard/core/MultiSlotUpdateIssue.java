package org.unclesniper.confhoard.core;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class MultiSlotUpdateIssue implements SlotUpdateIssue {

	public interface LineTransform {

		String transformHead(String line);

		String transformTail(String line);

	}

	public static class ListLineTransform implements LineTransform {

		public static final String DEFAULT_HEAD_PREFIX = "- ";

		public static final String DEFAULT_TAIL_PREFIX = "  ";

		private String headPrefix;

		private String tailPrefix;

		public ListLineTransform() {}

		public ListLineTransform(String headPrefix, String tailPrefix) {
			this.headPrefix = headPrefix;
			this.tailPrefix = tailPrefix;
		}

		public String getHeadPrefix() {
			return headPrefix;
		}

		public void setHeadPrefix(String headPrefix) {
			this.headPrefix = headPrefix;
		}

		public String getTailPrefix() {
			return tailPrefix;
		}

		public void setTailPrefix(String tailPrefix) {
			this.tailPrefix = tailPrefix;
		}

		@Override
		public String transformHead(String line) {
			return (headPrefix == null ? ListLineTransform.DEFAULT_HEAD_PREFIX : headPrefix) + line;
		}

		@Override
		public String transformTail(String line) {
			return (tailPrefix == null ? ListLineTransform.DEFAULT_TAIL_PREFIX : tailPrefix) + line;
		}

	}

	private static class MultiIterator implements Iterator<String> {

		private final Iterator<SlotUpdateIssue> issueIterator;

		private final LineTransform lineTransform;

		private Iterator<String> lineIterator;

		private boolean head;

		public MultiIterator(Iterator<SlotUpdateIssue> issueIterator, LineTransform lineTransform) {
			this.issueIterator = issueIterator;
			this.lineTransform = lineTransform;
			nextIssue();
		}

		private void nextIssue() {
			head = true;
			while(issueIterator.hasNext()) {
				lineIterator = issueIterator.next().getMessageLines();
				if(lineIterator.hasNext())
					return;
			}
			lineIterator = null;
		}

		@Override
		public boolean hasNext() {
			return lineIterator != null;
		}

		@Override
		public String next() {
			if(lineIterator == null)
				throw new NoSuchElementException();
			String line = lineIterator.next();
			boolean wasHead = head;
			if(!lineIterator.hasNext())
				nextIssue();
			else
				head = false;
			if(lineTransform != null)
				line = wasHead ? lineTransform.transformHead(line) : lineTransform.transformTail(line);
			return line;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public static final LineTransform DEFAULT_LIST_LINE_TRANSFORM = new ListLineTransform();

	private final List<SlotUpdateIssue> issues = new LinkedList<SlotUpdateIssue>();

	private LineTransform lineTransform;

	public MultiSlotUpdateIssue() {}

	public MultiSlotUpdateIssue(LineTransform lineTransform) {
		this.lineTransform = lineTransform;
	}

	public MultiSlotUpdateIssue(Iterable<SlotUpdateIssue> issues) {
		addSlotUpdateIssues(issues);
	}

	public MultiSlotUpdateIssue(Iterable<SlotUpdateIssue> issues, LineTransform lineTransform) {
		addSlotUpdateIssues(issues);
		this.lineTransform = lineTransform;
	}

	public void addSlotUpdateIssue(SlotUpdateIssue issue) {
		if(issue == null)
			throw new IllegalArgumentException("Issue cannot be null");
		issues.add(issue);
	}

	public void addSlotUpdateIssues(Iterable<SlotUpdateIssue> issues) {
		if(issues == null)
			return;
		for(SlotUpdateIssue issue : issues) {
			if(issue != null)
				this.issues.add(issue);
		}
	}

	public Iterable<SlotUpdateIssue> getSlotUpdateIssues() {
		return issues;
	}

	public LineTransform getLineTransform() {
		return lineTransform;
	}

	public void setLineTransform(LineTransform lineTransform) {
		this.lineTransform = lineTransform;
	}

	@Override
	public Iterator<String> getMessageLines() {
		return new MultiIterator(issues.iterator(), lineTransform);
	}

}
