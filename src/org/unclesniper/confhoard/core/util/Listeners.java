package org.unclesniper.confhoard.core.util;

import java.util.List;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.BooleanSupplier;
import org.unclesniper.confhoard.core.ConfHoardException;

public class Listeners<ListenerT> {

	public interface Fire<ListenerT> {

		void fire(ListenerT listener);

	}

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

	private List<ListenerT> getFireList() {
		List<ListenerT> c = cache;
		if(c != null)
			return c;
		synchronized(listeners) {
			if(cache != null)
				return cache;
			return cache = new LinkedList<ListenerT>(listeners);
		}
	}

	public void fire(Fire<ListenerT> fire, Consumer<ListenerT> fired, BooleanSupplier stop) {
		for(ListenerT listener : getFireList()) {
			if(stop != null && stop.getAsBoolean())
				break;
			fire.fire(listener);
			if(fired != null)
				fired.accept(listener);
		}
	}

	public void ioFire(IOFire<ListenerT> fire, Consumer<ListenerT> fired, BooleanSupplier stop) throws IOException {
		for(ListenerT listener : getFireList()) {
			if(stop != null && stop.getAsBoolean())
				break;
			fire.fire(listener);
			if(fired != null)
				fired.accept(listener);
		}
	}

	public void confFire(ConfFire<ListenerT> fire, Consumer<ListenerT> fired, BooleanSupplier stop)
			throws IOException, ConfHoardException {
		for(ListenerT listener : getFireList()) {
			if(stop != null && stop.getAsBoolean())
				break;
			fire.fire(listener);
			if(fired != null)
				fired.accept(listener);
		}
	}

}
