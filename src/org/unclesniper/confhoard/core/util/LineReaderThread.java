package org.unclesniper.confhoard.core.util;

import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.function.Consumer;

public class LineReaderThread extends AbstractIOThread {

	private final Reader reader;

	private final Consumer<String> sink;

	public LineReaderThread(Reader reader, Consumer<String> sink) {
		if(reader == null)
			throw new IllegalArgumentException("Reader cannot be null");
		if(sink == null)
			throw new IllegalArgumentException("Sink cannot be null");
		this.reader = reader;
		this.sink = sink;
	}

	@Override
	protected void runIO() throws IOException {
		BufferedReader br = new BufferedReader(reader);
		for(;;) {
			String line = br.readLine();
			if(line == null)
				break;
			sink.accept(line);
		}
	}

}
