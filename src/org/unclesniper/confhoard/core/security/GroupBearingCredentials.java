package org.unclesniper.confhoard.core.security;

public interface GroupBearingCredentials extends Credentials {

	Iterable<String> getGroupNames();

	boolean hasGroup(GroupBearingCredentials group);

	boolean hasGroup(String group);

}
