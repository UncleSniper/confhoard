package org.unclesniper.confhoard.core.security;

public class UserCredentials implements UserBearingCredentials {

	private final String username;

	public UserCredentials(String username) {
		if(username == null)
			throw new IllegalArgumentException("Username cannot be null");
		this.username = username;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof UserCredentials)
			return false;
		UserCredentials uc = (UserCredentials)other;
		return username.equals(uc.username);
	}

	@Override
	public int hashCode() {
		return username.hashCode();
	}

	@Override
	public String toString() {
		return "user '" + username + '\'';
	}

}
