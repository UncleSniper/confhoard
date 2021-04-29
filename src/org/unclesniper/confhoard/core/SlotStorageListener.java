package org.unclesniper.confhoard.core;

import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.security.Credentials;

public interface SlotStorageListener {

	void saveSlot(Credentials credentials, ConfStateBinding state, Function<String, Object> parameters)
			throws IOException;

}
