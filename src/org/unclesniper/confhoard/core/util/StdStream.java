package org.unclesniper.confhoard.core.util;

public enum StdStream {

	STDIN("standard input", false),
	STDOUT("standard output", true),
	STDERR("standard error", true);

	private final String humanReadable;

	private final boolean outputStream;

	private StdStream(String humanReadable, boolean outputStream) {
		this.humanReadable = humanReadable;
		this.outputStream = outputStream;
	}

	public String getHumanReadable() {
		return humanReadable;
	}

	public boolean isOutputStream() {
		return outputStream;
	}

}
