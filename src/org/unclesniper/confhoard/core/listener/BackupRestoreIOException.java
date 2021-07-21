package org.unclesniper.confhoard.core.listener;

import java.io.File;
import java.io.IOException;

public class BackupRestoreIOException extends IOException {

	private final File backupFile;

	private final File originalFile;

	public BackupRestoreIOException(File backupFile, File originalFile, IOException cause) {
		super("Failed to restore backup '" + (backupFile == null ? "" : backupFile.getAbsolutePath()) +
				"' to '" + (originalFile == null ? "" : originalFile.getAbsolutePath()) + "'"
				+ (cause == null || cause.getMessage() == null || cause.getMessage().length() == 0 ? ""
				: ": " + cause.getMessage()), cause);
		if(backupFile == null)
			throw new IllegalArgumentException("Backup file cannot be null");
		if(originalFile == null)
			throw new IllegalArgumentException("Original file cannot be null");
		this.backupFile = backupFile;
		this.originalFile = originalFile;
	}

	public File getBackupFile() {
		return backupFile;
	}

	public File getOriginalFile() {
		return originalFile;
	}

}
