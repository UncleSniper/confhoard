package org.unclesniper.confhoard.core.security;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;
import org.unclesniper.confhoard.core.Slot;

public class UserSlotSecurityConstraint extends AbstractSlotSecurityConstraint {

	private final Set<UserBearingCredentials> users = new HashSet<UserBearingCredentials>();

	private final Map<String, Integer> usernames = new HashMap<String, Integer>();

	public UserSlotSecurityConstraint() {}

	public Set<UserBearingCredentials> getUsers() {
		return Collections.unmodifiableSet(users);
	}

	private String getUsernameOf(UserBearingCredentials user) {
		if(user == null)
			throw new IllegalArgumentException("User cannot be null");
		String username = user.getUsername();
		if(username == null)
			throw new IllegalArgumentException("Username cannot be null");
		return username;
	}

	public void addUser(UserBearingCredentials user) {
		String username = getUsernameOf(user);
		synchronized(users) {
			if(users.add(user)) {
				Integer oldCount = usernames.get(username);
				int newCount = (oldCount == null ? 0 : oldCount.intValue()) + 1;
				usernames.put(username, newCount);
			}
		}
	}

	public boolean removeUser(UserBearingCredentials user) {
		String username = getUsernameOf(user);
		synchronized(users) {
			boolean removed = users.remove(user);
			if(removed) {
				Integer oldCount = usernames.get(username);
				int newCount = (oldCount == null ? 0 : oldCount.intValue()) - 1;
				if(newCount < 0) {
					users.add(user);
					throw new IllegalStateException("Username count would be negative");
				}
				if(newCount == 0)
					usernames.remove(username);
				else
					usernames.put(username, newCount);
			}
			return removed;
		}
	}

	@Override
	protected boolean mayPerformAnyAction(Slot slot, Credentials credentials) {
		if(!(credentials instanceof UserBearingCredentials))
			return false;
		UserBearingCredentials ubc = (UserBearingCredentials)credentials;
		String username = ubc.getUsername();
		synchronized(users) {
			return usernames.containsKey(username);
		}
	}

}
