package org.unclesniper.confhoard.core;

import java.util.Set;
import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;
import org.unclesniper.confhoard.core.util.HoardSink;
import org.unclesniper.confhoard.core.security.SlotAction;
import org.unclesniper.confhoard.core.security.Credentials;

public interface ConfStateBinding {

	ConfState getConfState();

	void setConfState(ConfState confState);

	String getHashAlgorithm();

	Set<String> getSlotKeys();

	Collection<Slot> getSlots();

	Slot getSlot(String key);

	void addSlot(Slot slot);

	boolean removeSlot(Slot slot);

	Fragment updateSlot(String key, InputStream content, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters)
			throws IOException, ConfHoardException;

	Fragment updateSlot(Slot slot, InputStream content, Credentials credentials, boolean enforceAccess,
			ConfStateBinding outerState, Function<String, Object> parameters)
			throws IOException, ConfHoardException;

	void retrieveSlot(String key, Credentials credentials, boolean enforceAccess, ConfStateBinding outerState,
			Function<String, Object> parameters, HoardSink<InputStream> sink)
			throws IOException, ConfHoardException;

	void retrieveSlot(Slot slot, Credentials credentials, boolean enforceAccess, ConfStateBinding outerState,
			Function<String, Object> parameters, HoardSink<InputStream> sink)
			throws IOException, ConfHoardException;

	void requireAccess(String key, SlotAction action, Credentials credentials)
			throws NoSuchSlotException, SlotAccessForbiddenException;

	void requireAccess(Slot slot, SlotAction action, Credentials credentials) throws SlotAccessForbiddenException;

}
