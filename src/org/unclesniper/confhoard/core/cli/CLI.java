package org.unclesniper.confhoard.core.cli;

import static org.unclesniper.confhoard.core.cli.CompoundCommand.compound;

public class CLI {

	private static final SubCommand ROOT_SUBCOMMAND = compound(null, null,
		compound("dump", "dump",
			new DumpFSIndexCommand()
		)
	);

	private CLI() {}

	public static void main(String[] args) throws Exception {
		CLI.ROOT_SUBCOMMAND.execute(args, 0, new StringBuilder());
	}

}
