package org.unclesniper.confhoard.core.security;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;

public class MemberUserCredentials
		implements UserBearingCredentials, GroupBearingCredentials, Iterable<GroupCredentials> {

	private final UserCredentials user;

	private final Set<GroupCredentials> groups = new HashSet<GroupCredentials>();

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

	public void addGroup(GroupCredentials group) {
		if(group == null)
			throw new IllegalArgumentException("Group cannot be null");
		groups.add(group);
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public Set<GroupCredentials> getGroups() {
		return groups;
	}

	@Override
	public boolean hasGroup(Credentials group) {
		if(group == null)
			throw new IllegalArgumentException("Group cannot be null");
		if(!(group instanceof GroupBearingCredentials))
			return false;
		GroupBearingCredentials gbc = (GroupBearingCredentials)group;
		for(GroupCredentials gc : gbc.getGroups()) {
			if(!groups.contains(gc))
				return false;
		}
		return true;
	}

	@Override
	public boolean hasGroup(String group) {
		if(group == null)
			throw new IllegalArgumentException("Group name cannot be null");
		for(GroupCredentials myGroup : groups) {
			if(myGroup.getGroupName().equals(group))
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

	@Override
	public Iterator<GroupCredentials> iterator() {
		return Collections.unmodifiableSet(groups).iterator();
	}

}
