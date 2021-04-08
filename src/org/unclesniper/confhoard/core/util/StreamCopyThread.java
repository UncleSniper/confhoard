package org.unclesniper.confhoard.core.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StreamCopyThread extends AbstractIOThread {

	private final InputStream input;

	private final OutputStream output;

	private final boolean closeOutput;

	public StreamCopyThread(InputStream input, OutputStream output, boolean closeOutput) {
		if(output == null)
			throw new IllegalArgumentException("Output stream cannot be null");
		this.input = input;
		this.output = output;
		this.closeOutput = closeOutput;
	}

	private void copyStream() throws IOException {
		byte[] buffer = new byte[512];
		for(;;) {
			int count = input.read(buffer);
			if(count <= 0)
				break;
			output.write(buffer, 0, count);
		}
	}

	@Override
	protected void runIO() throws IOException {
		if(closeOutput) {
			try(OutputStream os = output) {
				if(input != null)
					copyStream();
			}
		}
		else {
			if(input != null)
				copyStream();
		}
	}

}
