package org.unclesniper.confhoard.core.security;

public interface GroupBearingCredentials extends Credentials {

	Iterable<GroupCredentials> getGroups();

	boolean hasGroup(Credentials group);

	boolean hasGroup(String group);

}
