package org.unclesniper.confhoard.core;

import java.io.InputStream;
import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.security.Credentials;

public interface Fragment {

	Slot getSlot();

	InputStream retrieve(Credentials credentials, ConfStateBinding state, Function<String, Object> parameters)
			throws IOException;

	void remove(Credentials credentials, ConfStateBinding state, Function<String, Object> parameters)
			throws IOException;

	String getHashAlgorithm();

	byte[] getHash(String algorithm, Credentials credentials,  ConfStateBinding state,
			Function<String, Object> parameters) throws IOException;

}
