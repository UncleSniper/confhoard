package org.unclesniper.confhoard.core.util;

public final class StdOutputLine {

	private final StdOutputStream stream;

	private final String line;

	public StdOutputLine(StdOutputStream stream, String line) {
		if(stream == null)
			throw new IllegalArgumentException("Stream type cannot be null");
		if(line == null)
			throw new IllegalArgumentException("Line cannot be null");
		this.stream = stream;
		this.line = line;
	}

	public StdOutputStream getStream() {
		return stream;
	}

	public String getLine() {
		return line;
	}

}
