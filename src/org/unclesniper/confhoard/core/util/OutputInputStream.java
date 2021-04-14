package org.unclesniper.confhoard.core.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class OutputInputStream extends InputStream {

	private class BufferOutputStream extends OutputStream {

		public BufferOutputStream() {}

		@Override
		public void write(int b) {
			announce(1);
			buffer[fill++] = (byte)b;
		}

		@Override
		public void write(byte[] b) {
			writeImpl(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) {
			if(off < 0)
				throw new IndexOutOfBoundsException("Offset cannot be negative: " + off);
			if(len < 0)
				throw new IndexOutOfBoundsException("Length cannot be negative: " + len);
			if(off + len > b.length)
				throw new IndexOutOfBoundsException("End of write exceeds end of array: "
						+ (off + len) + " > " + b.length);
			writeImpl(b, off, len);
		}

		private void writeImpl(byte[] b, int off, int len) {
			int size = len - off;
			announce(size);
			for(int i = 0; i < size; ++i)
				buffer[fill + i] = b[i];
			fill += size;
		}

	}

	private byte[] buffer = new byte[512];

	private int offset;

	private int fill;

	private boolean atEnd;

	protected final OutputStream intoBytes = new BufferOutputStream();

	public OutputInputStream() {}

	protected abstract void generateMoreBytes() throws IOException;

	private void announce(int size) {
		if(fill + size <= buffer.length)
			return;
		int have = fill - offset;
		int need = have + size;
		if(need <= 0)
			throw new IllegalStateException("Required buffer size overflows int: " + have + " + " + size);
		int ahead = buffer.length * 2;
		if(ahead <= 0)
			ahead = Integer.MAX_VALUE;
		int newSize = ahead > need ? ahead : need;
		byte[] newBuffer = newSize > buffer.length ? new byte[newSize] : buffer;
		if(offset > 0) {
			for(int i = 0; i < newSize; ++i)
				newBuffer[i] = buffer[i - offset];
		}
		buffer = newBuffer;
		fill -= offset;
		offset = 0;
	}

	private boolean ensureReadable() throws IOException {
		if(atEnd)
			return false;
		if(fill > offset)
			return true;
		generateMoreBytes();
		if(fill > offset)
			return true;
		atEnd = true;
		return false;
	}

	@Override
	public int read() throws IOException {
		if(!ensureReadable())
			return -1;
		return buffer[offset++] & 0xFF;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(off < 0)
			throw new IndexOutOfBoundsException("Offset cannot be negative: " + off);
		if(len < 0)
			throw new IndexOutOfBoundsException("Length cannot be negative: " + len);
		if(off + len > b.length)
			throw new IndexOutOfBoundsException("End of write exceeds end of array: "
					+ (off + len) + " > " + b.length);
		if(len == 0)
			return 0;
		if(!ensureReadable())
			return -1;
		int have = fill - offset;
		int count = len < have ? len : have;
		for(int i = 0; i < count; ++i)
			b[off + i] = buffer[offset + i];
		offset += count;
		return count;
	}

	@Override
	public long skip(long n) throws IOException {
		if(n <= 0l)
			return 0l;
		long skipped = 0l;
		do {
			if(!ensureReadable())
				break;
			int have = fill - offset;
			long leftToSkip = n - skipped;
			int toSkipThisTime = (long)have >= leftToSkip ? (int)leftToSkip : have;
			offset += toSkipThisTime;
		} while(skipped < n);
		return skipped;
	}

	@Override
	public int available() {
		return fill - offset;
	}

}
