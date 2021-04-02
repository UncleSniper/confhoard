package org.unclesniper.confhoard.core;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

public class TextSlotUpdateIssue implements SlotUpdateIssue {

	private final List<String> lines = new LinkedList<String>();

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
			for(StackTraceElement frame : t.getStackTrace())
				lines.add("    at " + frame);
			t = t.getCause();
			cause = true;
		} while(t != null);
	}

	@Override
	public Iterator<String> getMessageLines() {
		return lines.iterator();
	}

}
