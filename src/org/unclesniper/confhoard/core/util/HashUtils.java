package org.unclesniper.confhoard.core.util;

import java.io.InputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

	private HashUtils() {}

	public static byte[] hashStream(InputStream stream, String hashAlgorithm) throws IOException {
		if(stream == null)
			throw new IllegalStateException("Stream cannot be null");
		if(hashAlgorithm == null)
			throw new IllegalArgumentException("Hash algorithm cannot be null");
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(hashAlgorithm);
		}
		catch(NoSuchAlgorithmException nsae) {
			throw new IllegalStateException(nsae.getMessage(), nsae);
		}
		byte[] buffer = new byte[512];
		for(;;) {
			int count = stream.read(buffer);
			if(count <= 0)
				break;
			md.update(buffer, 0, count);
		}
		return md.digest();
	}

}
