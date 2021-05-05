package org.unclesniper.confhoard.core.security;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public class GroupCredentials extends AbstractMultiGroupBearingCredentials {

	private final String groupName;

	private final Set<GroupBearingCredentials> subgroups = new HashSet<GroupBearingCredentials>();

	public GroupCredentials(String groupName) {
		if(groupName == null)
			throw new IllegalArgumentException("Group name cannot be null");
		this.groupName = groupName;
	}

	public String getGroupName() {
		return groupName;
	}

	public Set<GroupBearingCredentials> getSubgroups() {
		return Collections.unmodifiableSet(subgroups);
	}

	public void addSubgroup(GroupBearingCredentials subgroup) {
		if(subgroup == null)
			throw new IllegalArgumentException("Subgroup cannot be null");
		subgroups.add(subgroup);
	}

	@Override
	protected Set<String> generateGroupNames() {
		Set<String> names = new HashSet<String>();
		names.add(groupName);
		for(GroupBearingCredentials group : subgroups) {
			for(String gn : group.getGroupNames()) {
				if(gn != null)
					names.add(gn);
			}
		}
		return names;
	}

	@Override
	protected boolean hasGroupUncached(String groupName) {
		if(this.groupName.equals(groupName))
			return true;
		for(GroupBearingCredentials group : subgroups) {
			if(group.hasGroup(groupName))
				return true;
		}
		return false;
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
