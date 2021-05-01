package org.unclesniper.confhoard.core.cli;

import java.util.Map;
import java.util.HashMap;

public class CompoundCommand extends AbstractSubCommand {

	private final String humanPrefix;

	private final Map<String, SubCommand> subCommands = new HashMap<String, SubCommand>();

	public CompoundCommand(String initiator, String humanPrefix) {
		super(initiator);
		this.humanPrefix = humanPrefix;
	}

	public String getHumanPrefix() {
		return humanPrefix;
	}

	public void addSubCommand(SubCommand command) {
		if(command == null)
			throw new IllegalArgumentException("Subcommand cannot be null");
		String initiator = command.getInitiator();
		if(initiator == null)
			throw new IllegalArgumentException("Subcommand must have initiator");
		SubCommand old = subCommands.get(initiator);
		if(old != null && old != command)
			throw new IllegalArgumentException("Duplicator initiator: " + initiator);
		subCommands.put(initiator, command);
	}

	@Override
	public void execute(String[] args, int argOffset, StringBuilder aggregateHumanPrefix) throws Exception {
		extendPrefix(humanPrefix, aggregateHumanPrefix);
		if(argOffset >= args.length)
			throw new UsageException("One more subcommand initiator required after '" + aggregateHumanPrefix + '\'');
		String initiator = args[argOffset];
		SubCommand sc = subCommands.get(initiator);
		if(sc == null)
			throw new UsageException("Unrecognized subcommand '" + initiator + "' after '"
					+ aggregateHumanPrefix + '\'');
		sc.execute(args, argOffset + 1, aggregateHumanPrefix);
	}

	public static CompoundCommand compound(String initiator, String humanPrefix, SubCommand... children) {
		CompoundCommand cc = new CompoundCommand(initiator, humanPrefix);
		for(SubCommand sc : children)
			cc.addSubCommand(sc);
		return cc;
	}

}
