package org.unclesniper.confhoard.core.cli;

import java.io.File;
import java.io.FileInputStream;
import org.unclesniper.confhoard.core.util.HexUtils;
import org.unclesniper.confhoard.core.FileSystemStorage;

public class DumpFSIndexCommand extends AbstractSubCommand {

	private static final class DumpingIndexSink implements FileSystemStorage.IndexSink {

		private final File directory;

		public DumpingIndexSink(File directory) {
			this.directory = directory;
		}

		@Override
		public void enterIndex() {}

		@Override
		public void foundHashAlgorithm(String hashAlgorithm) {
			System.out.println("Hash algorithm: " + hashAlgorithm);
		}

		@Override
		public void foundSlotCount(int slotCount) {
			System.out.println(slotCount + " slots in index");
		}

		@Override
		public void enterSlot(int slotIndex, int slotCount, String key, int fragmentCount) {
			System.out.println("Slot #" + slotIndex + ": " + key + " (" + fragmentCount + " fragments)");
		}

		@Override
		public void foundFragment(int fragmentIndex, int fragmentCount, long id, byte[] hash) {
			boolean missing = id >= 0L && !new File(directory, id + ".frag").exists();
			System.out.print("    Fragment #" + fragmentIndex + " = ID " + id);
			if(missing || hash != null)
				System.out.print(" (");
			if(hash != null)
				System.out.print("hash = " + HexUtils.toHexString(hash));
			if(missing) {
				if(hash != null)
					System.out.print(", ");
				System.out.print("file missing");
			}
			if(missing || hash != null)
				System.out.print(')');
			System.out.println();
		}

		@Override
		public void leaveSlot(int slotIndex, int slotCount, String key, int fragmentCount) {}

		@Override
		public void leaveIndex(long nextID) {
			System.out.flush();
		}

	}

	public DumpFSIndexCommand() {
		super("fs");
	}

	@Override
	public void execute(String[] args, int argOffset, StringBuilder aggregateHumanPrefix) throws Exception {
		extendPrefix("fs ...", aggregateHumanPrefix);
		String sdir = requireArg(args, argOffset++, aggregateHumanPrefix, "storage directory");
		requireLeaf(args, argOffset, aggregateHumanPrefix);
		File directory = new File(sdir);
		File indexFile = new File(directory, "index");
		try(FileInputStream fis = new FileInputStream(indexFile)) {
			FileSystemStorage.readIndex(indexFile, fis, new DumpingIndexSink(directory));
		}
	}

}
