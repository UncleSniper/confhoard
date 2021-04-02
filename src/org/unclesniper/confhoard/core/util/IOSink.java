package org.unclesniper.confhoard.core.util;

import java.io.IOException;

public interface IOSink<T> extends HoardSink<T> {

	@Override
	void accept(T t) throws IOException;

}
