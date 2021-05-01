package org.unclesniper.confhoard.core.cli;

public interface SubCommand {

	void execute(String[] args, int argOffset, StringBuilder aggregateHumanPrefix) throws Exception;

	String getInitiator();

}
