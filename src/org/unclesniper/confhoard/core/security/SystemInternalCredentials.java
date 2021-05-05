package org.unclesniper.confhoard.core.security;

public class SystemInternalCredentials implements EverythingIsAllowedCredentials {

	public static final EverythingIsAllowedCredentials instance = new SystemInternalCredentials();

	public SystemInternalCredentials() {}

	@Override
	public String toString() {
		return "system";
	}

}
