package org.unclesniper.confhoard.core.listener;

import java.io.File;
import java.io.IOException;

public class BackupDeleteIOException extends IOException {

	private final File backupFile;

	public BackupDeleteIOException(File backupFile, IOException cause) {
		super("Failed to remove file backup '" + (backupFile == null ? "" : backupFile.getAbsolutePath()) + "'"
				+ (cause == null || cause.getMessage() == null || cause.getMessage().length() == 0 ? ""
				: ": " + cause.getMessage()), cause);
		if(backupFile == null)
			throw new IllegalArgumentException("Backup file cannot be null");
		this.backupFile = backupFile;
	}

	public File getBackupFile() {
		return backupFile;
	}

}
