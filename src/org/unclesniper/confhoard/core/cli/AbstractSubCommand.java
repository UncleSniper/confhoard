package org.unclesniper.confhoard.core.cli;

public abstract class AbstractSubCommand implements SubCommand {

	private final String initiator;

	public AbstractSubCommand(String initiator) {
		this.initiator = initiator;
	}

	protected void extendPrefix(String humanPrefix, StringBuilder aggregateHumanPrefix) {
		if(humanPrefix == null)
			return;
		if(aggregateHumanPrefix.length() > 0)
			aggregateHumanPrefix.append(' ');
		aggregateHumanPrefix.append(humanPrefix);
	}

	protected void requireLeaf(String[] args, int argOffset, StringBuilder aggregateHumanPrefix)
			throws UsageException {
		if(argOffset < args.length)
			throw new UsageException("Excess argument after '" + aggregateHumanPrefix + "': " + args[argOffset]);
	}

	protected String requireArg(String[] args, int argOffset, StringBuilder aggregateHumanPrefix, String argName)
			throws UsageException {
		if(argOffset >= args.length)
			throw new UsageException("Expected " + argName + " after '" + args[args.length - 1]
					+ "', not end of command line");
		return args[argOffset];
	}

	@Override
	public String getInitiator() {
		return initiator;
	}

}
