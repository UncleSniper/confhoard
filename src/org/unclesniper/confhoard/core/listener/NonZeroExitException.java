package org.unclesniper.confhoard.core.listener;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;
import org.unclesniper.confhoard.core.SlotUpdateIssue;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.core.util.StdOutputLine;
import org.unclesniper.confhoard.core.util.StdOutputLineIterator;

public class NonZeroExitException extends ConfHoardException implements SlotUpdateIssue {

	private final String executable;

	private final int exitStatus;

	private final List<StdOutputLine> outputLines = new LinkedList<StdOutputLine>();

	public NonZeroExitException(String executable, int exitStatus) {
		super("Process '" + executable + "' returned exit status " + exitStatus);
		if(executable == null)
			throw new IllegalArgumentException("Executable name cannot be null");
		this.executable = executable;
		this.exitStatus = exitStatus;
	}

	public String getExecutable() {
		return executable;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public List<StdOutputLine> getOutputLines() {
		return Collections.unmodifiableList(outputLines);
	}

	public void addOutputLine(StdOutputLine line) {
		if(line == null)
			throw new IllegalArgumentException("Output line cannot be null");
		outputLines.add(line);
	}

	@Override
	public Iterator<String> getMessageLines() {
		return new StdOutputLineIterator(outputLines.iterator(), " of process '" + executable + "'");
	}

}
