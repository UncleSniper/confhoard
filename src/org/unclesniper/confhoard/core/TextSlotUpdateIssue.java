package org.unclesniper.confhoard.core;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

public class TextSlotUpdateIssue implements SlotUpdateIssue {

	private final List<String> lines = new LinkedList<String>();

	public TextSlotUpdateIssue() {}

	public TextSlotUpdateIssue(String message) {
		if(message != null)
			lines.add(message);
	}

	public TextSlotUpdateIssue(Throwable cause) {
		addThrowable(cause);
	}

	public TextSlotUpdateIssue(String message, Throwable cause) {
		this(message);
		addThrowable(cause);
	}

	private void addThrowable(Throwable t) {
		boolean cause = false;
		do {
			lines.add((cause ? "Caused by: " : "") + t);
			if(t instanceof SlotUpdateIssue) {
				Iterator<String> it = ((SlotUpdateIssue)t).getMessageLines();
				while(it.hasNext())
					lines.add("  | " + it.next());
			}
			for(StackTraceElement frame : t.getStackTrace())
				lines.add("    at " + frame);
			t = t.getCause();
			cause = true;
		} while(t != null);
	}

	public void addLine(String line) {
		if(line == null)
			throw new IllegalArgumentException("Line cannot be null");
		lines.add(line);
	}

	public void addLines(Iterable<String> lines) {
		if(lines == null)
			throw new IllegalArgumentException("Line sequence cannot be null");
		for(String line : lines) {
			if(line != null)
				this.lines.add(line);
		}
	}

	@Override
	public Iterator<String> getMessageLines() {
		return lines.iterator();
	}

}
