package org.unclesniper.confhoard.core.util;

import java.io.Writer;
import java.io.IOException;
import java.io.OutputStream;

public class WriterUtils {

	private static final byte[] EOL_BYTES = new byte[] {(byte)'\n'};

	public static final int EOL_BYTE_COUNT = EOL_BYTES.length;

	private static final char[] EOL_CHARS = new char[] {'\n'};

	public static final int EOL_CHAR_COUNT = EOL_CHARS.length;

	private WriterUtils() {}

	public static void putEOL(byte[] buffer, int offset) {
		if(buffer == null)
			throw new IllegalArgumentException("Buffer cannot be null");
		if(offset < 0)
			throw new IndexOutOfBoundsException("Offset cannot be negative: " + offset);
		if(offset + WriterUtils.EOL_BYTE_COUNT > buffer.length)
			throw new IndexOutOfBoundsException("End of write exceeds end of array: "
					+ (offset + WriterUtils.EOL_BYTE_COUNT) + " > " + buffer.length);
		for(int i = 0; i < WriterUtils.EOL_BYTE_COUNT; ++i)
			buffer[offset + i] = WriterUtils.EOL_BYTES[i];
	}

	public static void putEOL(char[] buffer, int offset) {
		if(buffer == null)
			throw new IllegalArgumentException("Buffer cannot be null");
		if(offset < 0)
			throw new IndexOutOfBoundsException("Offset cannot be negative: " + offset);
		if(offset + WriterUtils.EOL_CHAR_COUNT > buffer.length)
			throw new IndexOutOfBoundsException("End of write exceeds end of array: "
					+ (offset + WriterUtils.EOL_CHAR_COUNT) + " > " + buffer.length);
		for(int i = 0; i < WriterUtils.EOL_CHAR_COUNT; ++i)
			buffer[offset + i] = WriterUtils.EOL_CHARS[i];
	}

	public static void putEOL(OutputStream stream) throws IOException {
		stream.write(WriterUtils.EOL_BYTES);
	}

	public static void putEOL(Writer writer) throws IOException {
		writer.write(WriterUtils.EOL_CHARS);
	}

}
