package org.unclesniper.confhoard.core.listener;

import java.io.IOException;
import org.unclesniper.confhoard.core.util.StdStream;

public class StdStreamIOException extends IOException {

	private final String executable;

	private final StdStream stream;

	public StdStreamIOException(String executable, StdStream stream, IOException cause) {
		super("Failed to " + (stream != null && stream.isOutputStream() ? "write to" : "read from")
				+ ' ' + (stream == null ? "" : stream.getHumanReadable()) + " of process '" + executable
				+ '\'' + (cause == null || cause.getMessage() == null || cause.getMessage().length() == 0
				? "" : ": " + cause.getMessage()), cause);
		if(executable == null)
			throw new IllegalArgumentException("Executable name cannot be null");
		if(stream == null)
			throw new IllegalArgumentException("Stream type cannot be null");
		this.executable = executable;
		this.stream = stream;
	}

	public String getExecutable() {
		return executable;
	}

	public StdStream getStream() {
		return stream;
	}

}
