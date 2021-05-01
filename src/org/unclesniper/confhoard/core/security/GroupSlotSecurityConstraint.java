package org.unclesniper.confhoard.core.security;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import org.unclesniper.confhoard.core.Slot;

public class GroupSlotSecurityConstraint extends AbstractSlotSecurityConstraint {

	private final Set<GroupBearingCredentials> groups = new HashSet<GroupBearingCredentials>();

	public GroupSlotSecurityConstraint() {}

	public Set<GroupBearingCredentials> getGroups() {
		return Collections.unmodifiableSet(groups);
	}

	public void addGroup(GroupBearingCredentials group) {
		if(group == null)
			throw new IllegalArgumentException("Group cannot be null");
		synchronized(groups) {
			groups.add(group);
		}
	}

	public boolean removeGroup(GroupBearingCredentials group) {
		if(group == null)
			throw new IllegalArgumentException("Group cannot be null");
		synchronized(groups) {
			return groups.remove(group);
		}
	}

	@Override
	protected boolean mayPerformAnyAction(Slot slot, Credentials credentials) {
		if(!(credentials instanceof GroupBearingCredentials))
			return false;
		GroupBearingCredentials gbc = (GroupBearingCredentials)credentials;
		synchronized(groups) {
			for(GroupBearingCredentials theirGroup : gbc.getGroups()) {
				if(theirGroup == null)
					continue;
				for(GroupBearingCredentials myGroup : groups) {
					if(myGroup != null && myGroup.hasGroup(theirGroup))
						return true;
				}
			}
		}
		return false;
	}

}
