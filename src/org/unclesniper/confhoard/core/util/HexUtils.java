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

	public static byte[] fromHexString(String str) {
		if(str == null)
			throw new IllegalArgumentException("Hex digit string cannot be null");
		int length = str.length();
		byte[] bytes = new byte[(length + 1) / 2];
		for(int i = -(length % 2); i < length; i += 2) {
			char hi = i < 0 ? '0' : str.charAt(i);
			char lo = str.charAt(i + 1);
			bytes[(i + 1) / 2] = (byte)((HexUtils.decodeHexDigit(hi) << 4) | HexUtils.decodeHexDigit(lo));
		}
		return bytes;
	}

	private static int decodeHexDigit(char c) {
		if(c >= 'A' && c <= 'F')
			return c - 'A' + 10;
		if(c >= 'a' && c <= 'f')
			return c - 'a' + 10;
		if(c >= '0' && c <= '9')
			return c - '0';
		throw new IllegalArgumentException("Invalid hex digit: " + c);
	}

}
