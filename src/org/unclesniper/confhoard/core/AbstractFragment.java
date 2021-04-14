package org.unclesniper.confhoard.core;

import java.util.Arrays;
import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.security.Credentials;

public abstract class AbstractFragment implements Fragment {

	private final Object hashLock = new Object();

	private final Slot slot;

	private volatile String hashAlgorithm;

	private volatile byte[] hash;

	public AbstractFragment(Slot slot, String hashAlgorithm, byte[] hash) {
		if(slot == null)
			throw new IllegalArgumentException("Slot cannot be null");
		if(hashAlgorithm == null)
			throw new IllegalArgumentException("Hash algorithm cannot be null");
		if(hash == null)
			throw new IllegalArgumentException("Hash cannot be null");
		if(hash.length == 0)
			throw new IllegalArgumentException("Hash length cannot be zero");
		this.slot = slot;
		this.hashAlgorithm = hashAlgorithm;
		this.hash = Arrays.copyOf(hash, hash.length);
	}

	protected abstract byte[] renewHash(String algorithm, Credentials credentials, ConfStateBinding state,
			Function<String, Object> parameters) throws IOException;

	@Override
	public Slot getSlot() {
		return slot;
	}

	@Override
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	@Override
	public byte[] getHash(String algorithm, Credentials credentials, ConfStateBinding state,
			Function<String, Object> parameters) throws IOException {
		if(algorithm == null && state != null)
			algorithm = state.getHashAlgorithm();
		if(algorithm == null)
			algorithm = ConfState.DEFAULT_HASH_ALGORITHM;
		synchronized(hashLock) {
			if(!algorithm.equals(hashAlgorithm)) {
				hash = renewHash(algorithm, credentials, state, parameters);
				hashAlgorithm = algorithm;
			}
			return Arrays.copyOf(hash, hash.length);
		}
	}

}
