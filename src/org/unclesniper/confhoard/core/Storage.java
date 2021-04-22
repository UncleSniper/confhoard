package org.unclesniper.confhoard.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.function.Consumer;
import org.unclesniper.confhoard.core.util.IOSink;

public interface Storage {

	void loadFragments(Function<String, Slot> slots, Consumer<Slot> loaded, String hashAlgorithm,
			Function<String, Object> parameters) throws IOException;

	Fragment newFragment(Slot slot, InputStream content, String hashAlgorithm) throws IOException;

	void listFragments(IOSink<Fragment> sink) throws IOException;

	void addStorageListener(StorageListener listener);

	boolean removeStorageListener(StorageListener listener);

}
