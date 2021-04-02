package org.unclesniper.confhoard.core.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingleElementIterator<T> implements Iterator<T> {

	private final T element;

	private boolean had;

	public SingleElementIterator(T element) {
		this.element = element;
	}

	@Override
	public boolean hasNext() {
		return !had;
	}

	@Override
	public T next() {
		if(had)
			throw new NoSuchElementException();
		had = true;
		return element;
	}

}
