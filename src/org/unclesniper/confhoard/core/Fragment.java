package org.unclesniper.confhoard.core;

import java.io.InputStream;
import java.io.IOException;

public interface Fragment {

	Slot getSlot();

	InputStream retrieve(ConfStateBinding state) throws IOException;

	void remove() throws IOException;

	String getHashAlgorithm();

	byte[] getHash();

}
