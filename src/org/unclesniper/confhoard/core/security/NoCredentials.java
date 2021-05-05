package org.unclesniper.confhoard.core.security;

public class NoCredentials implements Credentials {

	public static final NoCredentials instance = new NoCredentials();

	public NoCredentials() {}

	@Override
	public String toString() {
		return "nobody";
	}

}
