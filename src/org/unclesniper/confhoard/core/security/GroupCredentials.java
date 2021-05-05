package org.unclesniper.confhoard.core.security;

import org.unclesniper.confhoard.core.util.SingleElementIterator;

public class GroupCredentials implements GroupBearingCredentials {

	private final String groupName;

	public GroupCredentials(String groupName) {
		if(groupName == null)
			throw new IllegalArgumentException("Group name cannot be null");
		this.groupName = groupName;
	}

	public String getGroupName() {
		return groupName;
	}

	@Override
	public Iterable<String> getGroupNames() {
		return () -> new SingleElementIterator<String>(groupName);
	}

	@Override
	public boolean hasGroup(GroupBearingCredentials group) {
		if(group == null)
			throw new IllegalArgumentException("Group cannot be null");
		for(String gn : group.getGroupNames()) {
			if(!groupName.equals(gn))
				return false;
		}
		return true;
	}

	@Override
	public boolean hasGroup(String group) {
		if(group == null)
			throw new IllegalArgumentException("Group name cannot be null");
		return groupName.equals(group);
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof GroupCredentials))
			return false;
		GroupCredentials gc = (GroupCredentials)other;
		return groupName.equals(gc.groupName);
	}

	@Override
	public int hashCode() {
		return groupName.hashCode();
	}

	@Override
	public String toString() {
		return "group '" + groupName + '\'';
	}

}
