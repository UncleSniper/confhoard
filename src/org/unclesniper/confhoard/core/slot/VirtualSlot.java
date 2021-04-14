package org.unclesniper.confhoard.core.slot;

import java.util.Arrays;
import java.io.InputStream;
import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.Slot;
import org.unclesniper.confhoard.core.Fragment;
import org.unclesniper.confhoard.core.ConfState;
import org.unclesniper.confhoard.core.SlotListener;
import org.unclesniper.confhoard.core.util.HashUtils;
import org.unclesniper.confhoard.core.ConfStateBinding;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.core.security.Credentials;

public abstract class VirtualSlot extends Slot {

	private class VirtualFragment implements Fragment {

		private final Object hashLock = new Object();

		private volatile String hashAlgorithm;

		private volatile byte[] hash;

		public VirtualFragment() {}

		@Override
		public Slot getSlot() {
			return VirtualSlot.this;
		}

		@Override
		public InputStream retrieve(Credentials credentials, ConfStateBinding state,
				Function<String, Object> parameters) throws IOException {
			return retrieveFragment(credentials, state, parameters);
		}

		@Override
		public void remove() {}

		@Override
		public String getHashAlgorithm() {
			return hashAlgorithm == null ? ConfState.DEFAULT_HASH_ALGORITHM : hashAlgorithm;
		}

		@Override
		public byte[] getHash(String algorithm, Credentials credentials, ConfStateBinding state,
				Function<String, Object> parameters) throws IOException {
			if(algorithm == null && state != null)
				algorithm = state.getHashAlgorithm();
			if(algorithm == null)
				algorithm = ConfState.DEFAULT_HASH_ALGORITHM;
			synchronized(hashLock) {
				if(hash == null || !algorithm.equals(hashAlgorithm)) {
					try(InputStream is = retrieveFragment(credentials, state, parameters)) {
						hash = HashUtils.hashStream(is, algorithm);
					}
					hashAlgorithm = algorithm;
				}
				return Arrays.copyOf(hash, hash.length);
			}
		}

	}

	private class VirtualSlotListener implements SlotListener {

		public VirtualSlotListener() {}

		@Override
		public void slotLoaded(SlotLoadedEvent event) {}

		@Override
		public void slotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException {
			updateSlot(event);
		}

	}

	public VirtualSlot(String key) {
		super(key);
		setFragmentLocally(new VirtualFragment());
		addSlotListener(new VirtualSlotListener());
	}

	protected abstract InputStream retrieveFragment(Credentials credentials, ConfStateBinding state,
			Function<String, Object> parameters) throws IOException;

	protected void updateSlot(SlotListener.SlotUpdatedEvent event) throws IOException, ConfHoardException {
		throw new ReadOnlySlotException(this);
	}

	protected String getDefaultMimeType() {
		return null;
	}

	@Override
	public void setFragment(Fragment fragment) {}

	@Override
	public String getMimeType() {
		String mimeType = super.getMimeType();
		return mimeType != null ? mimeType : getDefaultMimeType();
	}

}
