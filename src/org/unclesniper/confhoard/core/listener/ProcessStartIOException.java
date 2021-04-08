package org.unclesniper.confhoard.core.listener;

import java.io.IOException;

public class ProcessStartIOException extends IOException {

	private final String executable;

	public ProcessStartIOException(String executable, IOException cause) {
		super("Failed to start process '" + executable + "'"
				+ (cause == null || cause.getMessage() == null || cause.getMessage().length() == 0 ? ""
				: ": " + cause.getMessage()), cause);
		if(executable == null)
			throw new IllegalArgumentException("Executable name cannot be null");
		this.executable = executable;
	}

	public String getExecutable() {
		return executable;
	}

}
