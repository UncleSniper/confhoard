package org.unclesniper.confhoard.core.util;

import java.io.IOException;

public abstract class AbstractIOThread extends Thread implements AutoCloseable {

	private volatile IOException error;

	public AbstractIOThread() {}

	protected abstract void runIO() throws IOException;

	public IOException safeJoin() {
		for(;;) {
			try {
				join();
				return error;
			}
			catch(InterruptedException ie) {}
		}
	}

	@Override
	public void run() {
		try {
			runIO();
		}
		catch(IOException ioe) {
			error = ioe;
		}
	}

	@Override
	public void close() {
		safeJoin();
	}

}
