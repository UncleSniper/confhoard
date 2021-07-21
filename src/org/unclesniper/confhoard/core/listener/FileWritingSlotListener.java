package org.unclesniper.confhoard.core.listener;

import java.io.File;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.function.Function;
import java.nio.file.StandardCopyOption;
import org.unclesniper.confhoard.core.Fragment;
import org.unclesniper.confhoard.core.SlotListener;
import org.unclesniper.confhoard.core.ConfHoardException;

public class FileWritingSlotListener extends SelectingSlotListener {

	public enum NoFragmentAction {
		SKIP,
		IGNORE,
		TRUNCATE,
		DELETE
	}

	private static final class BackupRestorer implements AutoCloseable {

		private final Path backupPath;

		private Path destinationPath;

		public BackupRestorer(Path backupPath, Path destinationPath) {
			this.backupPath = backupPath;
			this.destinationPath = destinationPath;
		}

		public void release() {
			destinationPath = null;
		}

		@Override
		public void close() throws IOException {
			if(destinationPath == null)
				return;
			try {
				Files.deleteIfExists(destinationPath);
				Files.move(backupPath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			}
			catch(IOException ioe) {
				throw new BackupRestoreIOException(backupPath.toFile(), destinationPath.toFile(), ioe);
			}
		}

	}

	public static final String DEFAULT_BACKUP_EXTENSION = ".prev";

	private File targetFile;

	private NoFragmentAction noFragmentAction;

	private String backupExtension;

	private boolean keepBackup;

	private SlotListener receivingSlotListener;

	private String prepareExtension;

	public FileWritingSlotListener() {
		setNoFragmentAction(null);
	}

	public File getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(File targetFile) {
		if(targetFile != null)
			targetFile = targetFile.getAbsoluteFile();
		this.targetFile = targetFile;
	}

	public NoFragmentAction getNoFragmentAction() {
		return noFragmentAction;
	}

	public void setNoFragmentAction(NoFragmentAction noFragmentAction) {
		this.noFragmentAction = noFragmentAction == null ? NoFragmentAction.DELETE : noFragmentAction;
	}

	public String getBackupExtension() {
		return backupExtension;
	}

	public void setBackupExtension(String backupExtension) {
		if(backupExtension != null && backupExtension.length() == 0)
			backupExtension = null;
		this.backupExtension = backupExtension;
	}

	public boolean isKeepBackup() {
		return keepBackup;
	}

	public void setKeepBackup(boolean keepBackup) {
		this.keepBackup = keepBackup;
	}

	public SlotListener getReceivingSlotListener() {
		return receivingSlotListener;
	}

	public void setReceivingSlotListener(SlotListener receivingSlotListener) {
		this.receivingSlotListener = receivingSlotListener;
	}

	public String getPrepareExtension() {
		return prepareExtension;
	}

	public void setPrepareExtension(String prepareExtension) {
		if(prepareExtension != null && prepareExtension.length() == 0)
			prepareExtension = null;
		this.prepareExtension = prepareExtension;
	}

	@Override
	protected void selectedSlotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException {
		doWrite(event, event.getSlot().getFragment(), null);
	}

	@Override
	protected void selectedSlotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException {
		doWrite(event, event.getNextFragment(), event::getRequestParameter);
	}

	private void doWrite(SlotEvent event, Fragment fragment, Function<String, Object> parameters)
			throws IOException, ConfHoardException {
		if(targetFile == null)
			throw new IllegalStateException("No target file is configured");
		if(fragment == null) {
			switch(noFragmentAction) {
				case IGNORE:
					if(receivingSlotListener != null)
						event.propagate(receivingSlotListener);
				case SKIP:
					return;
			}
		}
		Path targetPath = targetFile.toPath();
		File directory = targetFile.getParentFile();
		String backupName = targetFile.getName() + (backupExtension == null
				? FileWritingSlotListener.DEFAULT_BACKUP_EXTENSION : backupExtension);
		File backupFile = new File(directory, backupName);
		Path backupPath = backupFile.toPath();
		try {
			Files.deleteIfExists(backupPath);
		}
		catch(IOException ioe) {
			throw new BackupDeleteIOException(backupFile, ioe);
		}
		try {
			Files.move(targetPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
		}
		catch(IOException ioe) {
			throw new BackupCreateIOException(targetFile, backupFile, ioe);
		}
		try(BackupRestorer restorer = new BackupRestorer(backupPath, targetPath)) {
			if(fragment == null && noFragmentAction == NoFragmentAction.DELETE) {
				try {
					Files.deleteIfExists(targetPath);
				}
				catch(IOException ioe) {
					throw new TargetFileDeleteIOException(targetFile, ioe);
				}
			}
			else if(prepareExtension == null || fragment == null)
				doNonAtomic(event, fragment, targetPath);
			else {
				String prepareName = targetFile.getName() + prepareExtension;
				File prepareFile = new File(directory, prepareName);
				Path preparePath = prepareFile.toPath();
				doAtomic(event, fragment, targetPath, preparePath);
			}
			if(receivingSlotListener != null)
				event.propagate(receivingSlotListener);
			restorer.release();
		}
		if(!keepBackup)
			backupFile.delete();
	}

	private void doNonAtomic(SlotEvent event, Fragment fragment, Path targetPath) throws IOException {
		writeFile(event, targetPath, fragment);
	}

	private void doAtomic(SlotEvent event, Fragment fragment, Path targetPath, Path preparePath)
			throws IOException {
		try {
			writeFile(event, preparePath, fragment);
			try {
				Files.deleteIfExists(targetPath);
				Files.move(preparePath, targetPath,
						StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			}
			catch(IOException ioe) {
				throw new TargetFileReplaceIOException(targetPath.toFile(), preparePath.toFile(), ioe);
			}
		}
		finally {
			preparePath.toFile().delete();
		}
	}

	private void writeFile(SlotEvent event, Path path, Fragment fragment) throws IOException {
		try {
			try(FileOutputStream fos = new FileOutputStream(path.toFile())) {
				if(fragment != null) {
					try(InputStream is = fragment.retrieve(event.getCredentials(), event.getConfState(),
							event::getRequestParameter)) {
						byte[] buffer = new byte[512];
						for(;;) {
							int count = is.read(buffer);
							if(count <= 0)
								break;
							fos.write(buffer, 0, count);
						}
					}
				}
			}
		}
		catch(IOException ioe) {
			throw new TargetFileFillIOException(path.toFile(), ioe);
		}
	}

}
