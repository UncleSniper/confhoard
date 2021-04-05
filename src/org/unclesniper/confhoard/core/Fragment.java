package org.unclesniper.confhoard.core;

import java.io.InputStream;
import java.io.IOException;
import java.util.function.Function;

public interface Fragment {

	Slot getSlot();

	InputStream retrieve(ConfStateBinding state, Function<String, Object> parameters) throws IOException;

	void remove() throws IOException;

	String getHashAlgorithm();

	byte[] getHash();

}
