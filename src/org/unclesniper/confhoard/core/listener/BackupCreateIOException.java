package org.unclesniper.confhoard.core.listener;

import java.io.File;
import java.io.IOException;

public class BackupCreateIOException extends IOException {

	private final File originalFile;

	private final File backupFile;

	public BackupCreateIOException(File originalFile, File backupFile, IOException cause) {
		super("Failed to rename file '" + (originalFile == null ? "" : originalFile.getAbsolutePath())
				+ "' to backup '" + (backupFile == null ? "" : backupFile.getAbsolutePath()) + "'"
				+ (cause == null || cause.getMessage() == null || cause.getMessage().length() == 0 ? ""
				: ": " + cause.getMessage()), cause);
		if(originalFile == null)
			throw new IllegalArgumentException("Original file cannot be null");
		if(backupFile == null)
			throw new IllegalArgumentException("Backup file cannot be null");
		this.originalFile = originalFile;
		this.backupFile = backupFile;
	}

	public File getOriginalFile() {
		return originalFile;
	}

	public File getBackupFile() {
		return backupFile;
	}

}
