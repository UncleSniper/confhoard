package org.unclesniper.confhoard.core.util;

import java.util.Iterator;

public class StdOutputLineIterator implements Iterator<String> {

	private final Iterator<StdOutputLine> lineIterator;

	private String headSuffix;

	private boolean hadHead;

	public StdOutputLineIterator(Iterator<StdOutputLine> lineIterator, String headSuffix) {
		if(lineIterator == null)
			throw new IllegalArgumentException("Line iterator cannot be null");
		this.lineIterator = lineIterator;
		this.headSuffix = headSuffix;
	}

	@Override
	public boolean hasNext() {
		return !hadHead || lineIterator.hasNext();
	}

	@Override
	public String next() {
		if(!hadHead) {
			hadHead = true;
			return "Output" + (headSuffix == null ? "" : headSuffix) + ':';
		}
		StdOutputLine line = lineIterator.next();
		return '[' + line.getStream().name().toLowerCase() + "] " + line.getLine();
	}

}
