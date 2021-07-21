package org.unclesniper.confhoard.core.listener;

import java.io.File;
import java.io.IOException;

public class TargetFileFillIOException extends IOException {

	private final File targetFile;

	public TargetFileFillIOException(File targetFile, IOException cause) {
		super("Failed to write fragment to file '" + (targetFile == null ? "" : targetFile.getAbsolutePath()) + "'"
				+ (cause == null || cause.getMessage() == null || cause.getMessage().length() == 0 ? ""
				: ": " + cause.getMessage()), cause);
		if(targetFile == null)
			throw new IllegalArgumentException("Target file cannot be null");
		this.targetFile = targetFile;
	}

	public File getTargetFile() {
		return targetFile;
	}

}
