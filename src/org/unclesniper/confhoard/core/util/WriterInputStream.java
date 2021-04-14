package org.unclesniper.confhoard.core.util;

import java.io.Writer;
import java.io.IOException;
import java.nio.charset.Charset;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

public abstract class WriterInputStream extends OutputInputStream {

	protected final Writer intoChars;

	public WriterInputStream(String charset) throws UnsupportedEncodingException {
		if(charset == null)
			charset = "UTF-8";
		intoChars = new OutputStreamWriter(intoBytes, charset);
	}

	public WriterInputStream(Charset charset) {
		if(charset == null)
			charset = StandardCharsets.UTF_8;
		intoChars = new OutputStreamWriter(intoBytes, charset);
	}

	protected abstract void generateMoreChars() throws IOException;

	@Override
	protected void generateMoreBytes() throws IOException {
		generateMoreChars();
	}

}
