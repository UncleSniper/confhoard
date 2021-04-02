package org.unclesniper.confhoard.core.util;

import java.io.IOException;
import org.unclesniper.confhoard.core.ConfHoardException;

public interface HoardSink<T> {

	void accept(T t) throws IOException, ConfHoardException;

}
