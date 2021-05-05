package org.unclesniper.confhoard.core.security;

import java.util.Set;

public abstract class AbstractMultiGroupBearingCredentials implements GroupBearingCredentials {

	private boolean cacheGroupNames = true;

	private Set<String> cachedGroupNames;

	public AbstractMultiGroupBearingCredentials() {}

	public boolean isCacheGroupNames() {
		return cacheGroupNames;
	}

	public void setCacheGroupNames(boolean cacheGroupNames) {
		this.cacheGroupNames = cacheGroupNames;
	}

	protected abstract Set<String> generateGroupNames();

	protected abstract boolean hasGroupUncached(String groupName);

	protected Set<String> getOwnGroupNames() {
		if(!cacheGroupNames)
			return generateGroupNames();
		if(cachedGroupNames == null)
			cachedGroupNames = generateGroupNames();
		return cachedGroupNames;
	}

	@Override
	public Iterable<String> getGroupNames() {
		return getOwnGroupNames();
	}

	@Override
	public boolean hasGroup(GroupBearingCredentials group) {
		return SecurityUtils.hasGroup(this, group);
	}

	@Override
	public boolean hasGroup(String group) {
		if(group == null)
			throw new IllegalArgumentException("Group name cannot be null");
		if(!cacheGroupNames)
			return hasGroupUncached(group);
		if(cachedGroupNames != null)
			return cachedGroupNames.contains(group);
		return hasGroupUncached(group);
	}

}
