package org.unclesniper.confhoard.core;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.util.IdentityHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.io.UTFDataFormatException;
import org.unclesniper.confhoard.core.util.IOSink;

public class FileSystemStorage implements Storage {

	/* struct Index {
	 *   int count;
	 *   Slot slots[count];
	 * }
	 * struct Slot {
	 *   String key;
	 *   int count;
	 *   Fragment fragments[count];
	 * }
	 * struct Fragment {
	 *   long id;
	 * }
	 */

	private class FSFragment extends AbstractFragment {

		private final long id;

		public FSFragment(Slot slot, long id) {
			super(slot);
			if(id < 0)
				throw new IllegalArgumentException("Fragment ID cannot be negative: " + id);
			this.id = id;
		}

		public long getID() {
			return id;
		}

		@Override
		public InputStream retrieve() throws IOException {
			return new FileInputStream(new File(directory, id + ".frag"));
		}

		@Override
		public void remove() throws IOException {
			synchronized(getLocalLock()) {
				File file = new File(directory, id + ".frag");
				if(file.exists())
					Files.delete(file.toPath());
				Slot slot = getSlot();
				FSSlotState state = slotStates.get(slot);
				if(state != null)
					state.removeFragment(this);
				if(slot.getFragment() == this)
					slot.setFragment(null);
				saveIndex();
			}
		}

	}

	private class FSSlotState {

		private final Map<FSFragment, FSFragment> fragments = new IdentityHashMap<FSFragment, FSFragment>();

		public FSSlotState() {}

		public Set<FSFragment> getFragments() {
			return fragments.keySet();
		}

		public FSFragment internFragment(Fragment fragment) {
			return fragment == null ? null : fragments.get(fragment);
		}

		public void addFragment(FSFragment fragment) {
			fragments.put(fragment, fragment);
		}

		public void removeFragment(FSFragment fragment) {
			fragments.remove(fragment);
		}

	}

	public static class CorruptedIndexException extends IOException {

		private final File indexFile;

		public CorruptedIndexException(File indexFile, String message) {
			super("Index file '" + indexFile.getPath() + "' is corrupted"
					+ (message == null || message.length() == 0 ? "" : ": " + message));
			this.indexFile = indexFile;
		}

		public File getIndexFile() {
			return indexFile;
		}

	}

	private static final Map<String, Object> LOCKS = new HashMap<String, Object>();

	private File directory;

	private Object lock;

	private volatile boolean loaded;

	private volatile long nextFragmentID;

	private final Map<Slot, FSSlotState> slotStates = new IdentityHashMap<Slot, FSSlotState>();

	private boolean purgeOnLoad = true;

	public FileSystemStorage() {}

	public FileSystemStorage(File directory) {
		this.directory = directory;
	}

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		if(loaded && (directory == null || !directory.equals(this.directory)))
			throw new IllegalStateException("Cannot change directory after fragments have been loaded");
		this.directory = directory;
	}

	public boolean isPurgeOnLoad() {
		return purgeOnLoad;
	}

	public void setPurgeOnLoad(boolean purgeOnLoad) {
		this.purgeOnLoad = purgeOnLoad;
	}

	private Object getLocalLock() throws IOException {
		if(lock == null)
			lock = FileSystemStorage.getGlobalLock(directory);
		return lock;
	}

	@Override
	public void loadFragments(Function<String, Slot> slots, Consumer<Slot> loadedSink) throws IOException {
		if(loaded)
			return;
		synchronized(getLocalLock()) {
			if(loaded)
				return;
			File indexFile = new File(directory, "index");
			if(!indexFile.exists()) {
				File newIndex = new File(directory, "index.new");
				if(!newIndex.exists()) {
					loaded = true;
					return;
				}
				Files.move(newIndex.toPath(), indexFile.toPath());
			}
			slotStates.clear();
			int slotIndex = -1;
			Set<String> seenKeys = new HashSet<String>();
			long nextID = 0L;
			try(FileInputStream fis = new FileInputStream(indexFile)) {
				DataInputStream dis = new DataInputStream(fis);
				int slotCount = dis.readInt();
				if(slotCount < 0)
					throw new CorruptedIndexException(indexFile, "Slot count is negative: " + slotCount);
				for(slotIndex = 0; slotIndex < slotCount; ++slotIndex) {
					String key = dis.readUTF();
					if(seenKeys.contains(key))
						throw new CorruptedIndexException(indexFile, "Duplicate slot key: " + key);
					seenKeys.add(key);
					Slot trueSlot = slots.apply(key);
					Slot effectiveSlot;
					if(trueSlot != null)
						effectiveSlot = trueSlot;
					else if(purgeOnLoad)
						effectiveSlot = null;
					else
						effectiveSlot = new Slot(key);
					FSSlotState state;
					if(effectiveSlot == null)
						state = null;
					else {
						state = slotStates.get(effectiveSlot);
						if(state == null) {
							state = new FSSlotState();
							slotStates.put(effectiveSlot, state);
						}
					}
					int fragmentCount = dis.readInt();
					if(fragmentCount < 0)
						throw new CorruptedIndexException(indexFile, "Fragment count is negative for slot #"
								+ slotIndex + ": " + fragmentCount);
					for(int fragmentIndex = 0; fragmentIndex < fragmentCount; ++fragmentIndex) {
						long id = dis.readLong();
						if(id < (fragmentIndex == 0 ? -1L : 0L))
							throw new CorruptedIndexException(indexFile, "Fragment ID is negative for slot '"
									+ key + "' fragment #" + fragmentIndex + ": " + id);
						if(id == -1L) {
							if(effectiveSlot != null)
								effectiveSlot.setFragment(null);
						}
						else if(effectiveSlot != null) {
							FSFragment fragment = new FSFragment(effectiveSlot, id);
							if(id >= nextID)
								nextID = id + 1L;
							state.addFragment(fragment);
							if(fragmentIndex == 0)
								effectiveSlot.setFragment(fragment);
						}
						else {
							File file = new File(directory, id + ".frag");
							if(file.exists())
								Files.delete(file.toPath());
						}
					}
					if(trueSlot != null && loadedSink != null)
						loadedSink.accept(trueSlot);
				}
				if(dis.read() >= 0)
					throw new CorruptedIndexException(indexFile, "Excess data after end of index");
			}
			catch(EOFException ee) {
				throw new CorruptedIndexException(indexFile, "Unexpected end of file");
			}
			catch(UTFDataFormatException udfe) {
				throw new CorruptedIndexException(indexFile, "Malencoded slot key for slot #" + slotIndex);
			}
			loaded = true;
		}
	}

	@Override
	public Fragment newFragment(Slot slot, InputStream content) throws IOException {
		if(slot == null)
			throw new IllegalArgumentException("Slot cannot be null");
		if(content == null)
			throw new IllegalArgumentException("Content InputStream cannot be null");
		if(!loaded)
			throw new IllegalStateException("Storage was not loaded");
		synchronized(getLocalLock()) {
			File file = new File(directory, nextFragmentID + ".frag");
			FileOutputStream fos = new FileOutputStream(file);
			try {
				try(FileOutputStream innerFOS = fos) {
					byte[] buffer = new byte[512];
					for(;;) {
						int count = content.read(buffer);
						if(count <= 0)
							break;
						innerFOS.write(buffer, 0, count);
					}
				}
			}
			catch(IOException ioe) {
				try {
					Files.delete(file.toPath());
				}
				catch(IOException ioe2) {}
				throw ioe;
			}
			FSSlotState state = slotStates.get(slot);
			if(state == null) {
				state = new FSSlotState();
				slotStates.put(slot, state);
			}
			FSFragment fragment = new FSFragment(slot, nextFragmentID);
			state.addFragment(fragment);
			++nextFragmentID;
			saveIndex();
			return fragment;
		}
	}

	private void saveIndex() throws IOException {
		synchronized(getLocalLock()) {
			File newFile = new File(directory, "index.new");
			try(FileOutputStream fos = new FileOutputStream(newFile)) {
				DataOutputStream dos = new DataOutputStream(fos);
				dos.writeInt(slotStates.size());
				for(Map.Entry<Slot, FSSlotState> slotEntry : slotStates.entrySet()) {
					Slot slot = slotEntry.getKey();
					FSSlotState state = slotEntry.getValue();
					dos.writeUTF(slot.getKey());
					FSFragment active = state.internFragment(slot.getFragment());
					Set<FSFragment> all = state.getFragments();
					dos.writeInt(all.size() + (active != null && all.contains(active) ? 0 : 1));
					dos.writeLong(active == null ? -1L : active.getID());
					for(FSFragment fragment : all) {
						if(fragment != active)
							dos.writeLong(fragment.getID());
					}
				}
				dos.flush();
				fos.getChannel().force(false);
			}
			File oldFile = new File(directory, "index");
			if(oldFile.exists())
				Files.delete(oldFile.toPath());
			Files.move(newFile.toPath(), oldFile.toPath());
		}
	}

	@Override
	public void listFragments(IOSink<Fragment> sink) throws IOException {
		if(!loaded)
			throw new IllegalStateException("Storage was not loaded");
		synchronized(getLocalLock()) {
			for(Map.Entry<Slot, FSSlotState> state : slotStates.entrySet()) {
				for(FSFragment fragment : state.getValue().getFragments())
					sink.accept(fragment);
			}
		}
	}

	private static Object getGlobalLock(File directory) throws IOException {
		if(directory == null)
			throw new IllegalStateException("No directory is configured");
		String key = directory.getCanonicalFile().getPath();
		synchronized(FileSystemStorage.LOCKS) {
			Object lock = FileSystemStorage.LOCKS.get(key);
			if(lock == null) {
				lock = new Object();
				FileSystemStorage.LOCKS.put(key, lock);
			}
			return lock;
		}
	}

}
