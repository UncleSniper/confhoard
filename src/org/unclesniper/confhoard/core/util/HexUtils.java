package org.unclesniper.confhoard.core.util;

public class HexUtils {

	private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

	private HexUtils() {}

	public static String toHexString(byte[] bytes) {
		if(bytes == null)
			throw new IllegalArgumentException("Bytes cannot be null");
		char[] chars = new char[bytes.length * 2];
		for(int i = 0; i < bytes.length; ++i) {
			int b = (bytes[i] + 256) % 256;
			chars[2 * i] = HexUtils.HEX_CHARS[b >> 4];
			chars[2 * i + 1] = HexUtils.HEX_CHARS[b & 0xF];
		}
		return String.valueOf(chars);
	}

}
