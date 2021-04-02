package org.unclesniper.confhoard.core.util;

import java.io.IOException;

public interface IOPredicate<T> {

	boolean test(T t) throws IOException;

}
