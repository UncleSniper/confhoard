package org.unclesniper.confhoard.core.util;

import java.util.List;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.BooleanSupplier;
import org.unclesniper.confhoard.core.ConfHoardException;

public class Listeners<ListenerT> {

	public interface IOFire<ListenerT> {

		void fire(ListenerT listener) throws IOException;

	}

	public interface ConfFire<ListenerT> {

		void fire(ListenerT listener) throws IOException, ConfHoardException;

	}

	private final List<ListenerT> listeners = new LinkedList<ListenerT>();

	private volatile List<ListenerT> cache;

	public Listeners() {}

	public void addListener(ListenerT listener) {
		if(listener == null)
			return;
		synchronized(listeners) {
			listeners.add(listener);
			cache = null;
		}
	}

	public boolean removeListener(ListenerT listener) {
		if(listener == null)
			return false;
		synchronized(listener) {
			if(!listeners.remove(listener))
				return false;
			cache = null;
		}
		return true;
	}

	public void ioFire(IOFire<ListenerT> fire, Consumer<ListenerT> fired, BooleanSupplier stop) throws IOException {
		List<ListenerT> c = cache;
		if(c == null) {
			synchronized(listeners) {
				if(cache != null)
					c = cache;
				else
					cache = c = new LinkedList<ListenerT>(listeners);
			}
		}
		for(ListenerT listener : c) {
			if(stop != null && stop.getAsBoolean())
				break;
			fire.fire(listener);
			if(fired != null)
				fired.accept(listener);
		}
	}

	public void confFire(ConfFire<ListenerT> fire, Consumer<ListenerT> fired, BooleanSupplier stop)
			throws IOException, ConfHoardException {
		List<ListenerT> c = cache;
		if(c == null) {
			synchronized(listeners) {
				if(cache != null)
					c = cache;
				else
					cache = c = new LinkedList<ListenerT>(listeners);
			}
		}
		for(ListenerT listener : c) {
			if(stop != null && stop.getAsBoolean())
				break;
			fire.fire(listener);
			if(fired != null)
				fired.accept(listener);
		}
	}

}
