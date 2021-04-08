package org.unclesniper.confhoard.core.listener;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.io.OutputStream;
import java.util.Collections;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.nio.charset.StandardCharsets;
import org.unclesniper.confhoard.core.Slot;
import org.unclesniper.confhoard.core.Fragment;
import org.unclesniper.confhoard.core.util.StdStream;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.core.util.StdOutputLine;
import org.unclesniper.confhoard.core.util.StdOutputStream;
import org.unclesniper.confhoard.core.util.StreamCopyThread;
import org.unclesniper.confhoard.core.util.LineReaderThread;

public class ExecSlotListener extends SelectingSlotListener {

	private final List<String> commandWords = new LinkedList<String>();

	private boolean fragmentAsStdin;

	private boolean failOnNonZeroStatus = true;

	private final Map<String, String> environmentVariables = new HashMap<String, String>();

	private final Set<String> environmentFilters = new HashSet<String>();

	public ExecSlotListener() {}

	public List<String> getCommandWords() {
		return Collections.unmodifiableList(commandWords);
	}

	public void addCommandWord(String word) {
		if(word == null)
			throw new IllegalArgumentException("Command word cannot be null");
		commandWords.add(word);
	}

	public boolean isFragmentAsStdin() {
		return fragmentAsStdin;
	}

	public void setFragmentAsStdin(boolean fragmentAsStdin) {
		this.fragmentAsStdin = fragmentAsStdin;
	}

	public boolean isFailOnNonZeroStatus() {
		return failOnNonZeroStatus;
	}

	public void setFailOnNonZeroStatus(boolean failOnNonZeroStatus) {
		this.failOnNonZeroStatus = failOnNonZeroStatus;
	}

	public Set<String> getEnvironmentVariables() {
		return Collections.unmodifiableSet(environmentVariables.keySet());
	}

	public String getEnvironmentVariable(String key) {
		return environmentVariables.get(key);
	}

	public void setEnvironmentVariable(String key, String value) {
		if(key == null)
			throw new IllegalArgumentException("Environment variable name cannot be null");
		if(value == null)
			environmentVariables.remove(key);
		else
			environmentVariables.put(key, value);
	}

	public boolean removeEnvironmentVariable(String key) {
		return environmentVariables.remove(key) != null;
	}

	public Set<String> getEnvironmentFilters() {
		return Collections.unmodifiableSet(environmentFilters);
	}

	public void addEnvironmentFilter(String key) {
		if(key == null)
			throw new IllegalArgumentException("Environment variable name cannot be null");
		environmentFilters.add(key);
	}

	public boolean removeEnvironmentFilter(String key) {
		return environmentFilters.remove(key);
	}

	@Override
	protected void selectedSlotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException {
		doExec(event, null);
	}

	@Override
	protected void selectedSlotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException {
		doExec(event, event::getRequestParameter);
	}

	private void doExec(SlotEvent event, Function<String, Object> parameters)
			throws IOException, ConfHoardException {
		if(commandWords.isEmpty())
			return;
		Slot slot = event.getSlot();
		Fragment fragment = slot.getFragment();
		ProcessBuilder builder = new ProcessBuilder(commandWords);
		Map<String, String> env = builder.environment();
		for(Map.Entry<String, String> evar : environmentVariables.entrySet()) {
			env.put(evar.getKey(), evar.getValue());
		}
		for(String evar : environmentFilters)
			env.remove(evar);
		Process proc;
		try {
			proc = builder.start();
		}
		catch(IOException ioe) {
			throw new ProcessStartIOException(commandWords.get(0), ioe);
		}
		try(OutputStream stdin = proc.getOutputStream()) {
			if(fragmentAsStdin && fragment != null) {
				try(InputStream content = fragment.retrieve(event.getConfState(), parameters)) {
					withStdin(proc, stdin, content);
				}
			}
			else
				withStdin(proc, stdin, null);
		}
	}

	private void withStdin(Process proc, OutputStream stdin, InputStream content)
			throws IOException, NonZeroExitException {
		StreamCopyThread stdinThread = new StreamCopyThread(content, stdin, true);
		stdinThread.start();
		List<StdOutputLine> output = new LinkedList<StdOutputLine>();
		try(InputStream stdout = proc.getInputStream()) {
			LineReaderThread stdoutThread = new LineReaderThread(new InputStreamReader(stdout,
					StandardCharsets.ISO_8859_1),
					line -> output.add(new StdOutputLine(StdOutputStream.STDOUT, line)));
			stdoutThread.start();
			try(LineReaderThread sothr = stdoutThread) {
				try(InputStream stderr = proc.getErrorStream()) {
					LineReaderThread stderrThread = new LineReaderThread(new InputStreamReader(stderr,
							StandardCharsets.ISO_8859_1),
							line -> output.add(new StdOutputLine(StdOutputStream.STDERR, line)));
					stderrThread.start();
					try(LineReaderThread sethr = stderrThread) {
						withStdStreams(proc, stdinThread, stdoutThread, stderrThread, output);
					}
				}
			}
		}
	}

	private void withStdStreams(Process proc, StreamCopyThread stdinThread, LineReaderThread stdoutThread,
			LineReaderThread stderrThread, List<StdOutputLine> output) throws IOException, NonZeroExitException {
		int status;
		for(;;) {
			try {
				status = proc.waitFor();
				break;
			}
			catch(InterruptedException ie) {}
		}
		IOException stdinError = stdinThread.safeJoin();
		IOException stdoutError = stdoutThread.safeJoin();
		IOException stderrError = stderrThread.safeJoin();
		if(stdinError != null)
			throw new StdStreamIOException(commandWords.get(0), StdStream.STDIN, stdinError);
		if(stdoutError != null)
			throw new StdStreamIOException(commandWords.get(0), StdStream.STDOUT, stdoutError);
		if(stderrError != null)
			throw new StdStreamIOException(commandWords.get(0), StdStream.STDERR, stderrError);
		if(failOnNonZeroStatus && status != 0)
			throw new NonZeroExitException(commandWords.get(0), status);
	}

}
