package org.unclesniper.confhoard.core.security;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;

public class MemberUserCredentials extends AbstractMultiGroupBearingCredentials implements UserBearingCredentials {

	private final UserCredentials user;

	private final Set<GroupBearingCredentials> groups = new HashSet<GroupBearingCredentials>();

	public MemberUserCredentials(UserCredentials user) {
		if(user == null)
			throw new IllegalArgumentException("User cannot be null");
		this.user = user;
	}

	public MemberUserCredentials(String username) {
		user = new UserCredentials(username);
	}

	public UserCredentials getUser() {
		return user;
	}

	public Set<GroupBearingCredentials> getGroups() {
		return Collections.unmodifiableSet(groups);
	}

	public void addGroup(GroupBearingCredentials group) {
		if(group == null)
			throw new IllegalArgumentException("Group cannot be null");
		groups.add(group);
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	protected Set<String> generateGroupNames() {
		Set<String> names = new HashSet<String>();
		for(GroupBearingCredentials group : groups) {
			for(String gn : group.getGroupNames()) {
				if(gn != null)
					names.add(gn);
			}
		}
		return names;
	}

	@Override
	protected boolean hasGroupUncached(String groupName) {
		for(GroupBearingCredentials group : groups) {
			if(group.hasGroup(groupName))
				return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof MemberUserCredentials))
			return false;
		MemberUserCredentials muc = (MemberUserCredentials)other;
		return user.equals(muc.user);
	}

	@Override
	public int hashCode() {
		return user.hashCode();
	}

	@Override
	public String toString() {
		return user.toString();
	}

}
