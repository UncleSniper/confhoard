package org.unclesniper.confhoard.core.security;

import java.util.Set;
import java.util.HashSet;
import java.util.function.Predicate;

public class SecurityUtils {

	private SecurityUtils() {}

	public static Set<String> getGroupNames(GroupBearingCredentials group) {
		if(group == null)
			throw new IllegalArgumentException("Group cannot be null");
		Set<String> names = new HashSet<String>();
		for(String gn : group.getGroupNames()) {
			if(gn != null)
				names.add(gn);
		}
		return names;
	}

	public static boolean hasGroup(GroupBearingCredentials outerGroup, GroupBearingCredentials innerGroup) {
		if(outerGroup == null)
			throw new IllegalArgumentException("Outer group cannot be null");
		if(innerGroup == null)
			throw new IllegalArgumentException("Inner group cannot be null");
		Set<String> outerNames = SecurityUtils.getGroupNames(outerGroup);
		return SecurityUtils.hasGroup(outerNames::contains, innerGroup);
	}

	public static boolean hasGroup(Iterable<? extends GroupBearingCredentials> outerGroups,
			GroupBearingCredentials innerGroup) {
		if(outerGroups == null)
			throw new IllegalArgumentException("Outer groups cannot be null");
		if(innerGroup == null)
			throw new IllegalArgumentException("Inner group cannot be null");
		Set<String> outerNames = new HashSet<String>();
		for(GroupBearingCredentials outerGroup : outerGroups) {
			if(outerGroup == null)
				continue;
			for(String gn : outerGroup.getGroupNames()) {
				if(gn != null)
					outerNames.add(gn);
			}
		}
		return SecurityUtils.hasGroup(outerNames::contains, innerGroup);
	}

	public static boolean hasGroup(Predicate<String> outerGroups, GroupBearingCredentials innerGroup) {
		if(outerGroups == null)
			throw new IllegalArgumentException("Outer groups cannot be null");
		if(innerGroup == null)
			throw new IllegalArgumentException("Inner group cannot be null");
		for(String gn : innerGroup.getGroupNames()) {
			if(gn != null && !outerGroups.test(gn))
				return false;
		}
		return true;
	}

}
