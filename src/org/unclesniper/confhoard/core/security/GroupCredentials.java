package org.unclesniper.confhoard.core.security;

import java.util.Iterator;
import org.unclesniper.confhoard.core.util.SingleElementIterator;

public class GroupCredentials implements GroupBearingCredentials, Iterable<GroupCredentials> {

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
	public Iterable<GroupCredentials> getGroups() {
		return this;
	}

	@Override
	public boolean hasGroup(Credentials group) {
		if(group == null)
			throw new IllegalArgumentException("Group cannot be null");
		if(!(group instanceof GroupBearingCredentials))
			return false;
		GroupBearingCredentials gbc = (GroupBearingCredentials)group;
		for(GroupCredentials gc : gbc.getGroups()) {
			if(!groupName.equals(gc.groupName))
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

	@Override
	public Iterator<GroupCredentials> iterator() {
		return new SingleElementIterator<GroupCredentials>(this);
	}

}
