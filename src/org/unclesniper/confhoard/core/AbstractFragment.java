package org.unclesniper.confhoard.core;

import java.util.Arrays;

public abstract class AbstractFragment implements Fragment {

	private final Slot slot;

	private final String hashAlgorithm;

	private final byte[] hash;

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

	@Override
	public Slot getSlot() {
		return slot;
	}

	@Override
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	@Override
	public byte[] getHash() {
		return Arrays.copyOf(hash, hash.length);
	}

}
