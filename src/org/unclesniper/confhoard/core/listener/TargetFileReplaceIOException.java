package org.unclesniper.confhoard.core.listener;

import java.io.File;
import java.io.IOException;

public class TargetFileReplaceIOException extends IOException {

	private final File targetFile;

	private final File prepareFile;

	public TargetFileReplaceIOException(File targetFile, File prepareFile, IOException cause) {
		super("Failed to replace target file '" + (targetFile == null ? "" : targetFile.getAbsolutePath())
				+ "' with prepared file '" + (prepareFile == null ? "" : prepareFile.getAbsolutePath()) + "'"
				+ (cause == null || cause.getMessage() == null || cause.getMessage().length() == 0 ? ""
				: ": " + cause.getMessage()), cause);
		if(targetFile == null)
			throw new IllegalArgumentException("Target file cannot be null");
		if(prepareFile == null)
			throw new IllegalArgumentException("Prepare file cannot be null");
		this.targetFile = targetFile;
		this.prepareFile = prepareFile;
	}

	public File getTargetFile() {
		return targetFile;
	}

	public File getPrepareFile() {
		return prepareFile;
	}

}
