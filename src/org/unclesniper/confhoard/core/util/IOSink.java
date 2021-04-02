package org.unclesniper.confhoard.core.util;

import java.io.IOException;

public interface IOSink<T> {

	void accept(T t) throws IOException;

}
