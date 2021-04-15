package org.unclesniper.confhoard.core;

import java.util.Map;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.function.Consumer;
import org.unclesniper.confhoard.core.util.Listeners;
import org.unclesniper.confhoard.core.security.SlotAction;
import org.unclesniper.confhoard.core.security.Credentials;
import org.unclesniper.confhoard.core.security.SlotSecurityConstraint;

public class Slot {

	private final String key;

	private ConfState confState;

	private Fragment fragment;

	private String mimeType;

	private String description;

	private final Listeners<SlotListener> slotListeners = new Listeners<SlotListener>();

	private final Map<SlotStorageListener, SlotStorageListener> storageListeners
			= new IdentityHashMap<SlotStorageListener, SlotStorageListener>();

	private final Map<SlotSecurityConstraint, SlotSecurityConstraint> securityConstraints
			= new IdentityHashMap<SlotSecurityConstraint, SlotSecurityConstraint>();

	private boolean skipOnRollback = true;

	public Slot(String key) {
		this(key, null);
	}

	public Slot(String key, Fragment fragment) {
		if(key == null)
			throw new IllegalArgumentException("Slot key cannot be null");
		Slot.ensureDoesNotContainLineEnding(key, "Slot key");
		this.key = key;
		this.fragment = fragment;
	}

	public String getKey() {
		return key;
	}

	public ConfState getConfState() {
		return confState;
	}

	public void setConfState(ConfState confState) {
		if(confState == this.confState)
			return;
		ConfState oldState = this.confState;
		this.confState = confState;
		if(oldState != null)
			oldState.removeSlot(this);
	}

	public Fragment getFragment() {
		return fragment;
	}

	protected final void setFragmentLocally(Fragment fragment) {
		if(fragment != null && fragment.getSlot() != this)
			throw new IllegalArgumentException("Cannot put fragment into slot '" + key
					+ "', as it belongs to slot '" + fragment.getSlot().getKey() + "'");
		this.fragment = fragment;
	}

	public void setFragment(Fragment fragment) {
		setFragmentLocally(fragment);
	}

	public String getMimeType() {
		return mimeType;
	}

	protected final void setMimeTypeLocally(String mimeType) {
		if(mimeType != null && mimeType.length() == 0)
			mimeType = null;
		this.mimeType = mimeType;
	}

	public void setMimeType(String mimeType) {
		setMimeTypeLocally(mimeType);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if(this.description != null)
			ensureDoesNotContainLineEnding(description, "Slot description");
		this.description = description;
	}

	public boolean isSkipOnRollback() {
		return skipOnRollback;
	}

	public void setSkipOnRollback(boolean skipOnRollback) {
		this.skipOnRollback = skipOnRollback;
	}

	public void addSlotListener(SlotListener listener) {
		slotListeners.addListener(listener);
	}

	public boolean removeSlotListener(SlotListener listener) {
		return slotListeners.removeListener(listener);
	}

	public void fireSlotLoaded(SlotListener.SlotLoadedEvent event, Consumer<SlotListener> fired)
			throws IOException, ConfHoardException {
		slotListeners.confFire(listener -> listener.slotLoaded(event), fired, null);
	}

	public void fireSlotUpdated(SlotListener.SlotUpdatedEvent event, Consumer<SlotListener> fired)
			throws IOException, ConfHoardException {
		slotListeners.confFire(listener -> listener.slotUpdated(event), fired,
				skipOnRollback ? event::shouldRollback : null);
	}

	public void addStorageListener(SlotStorageListener listener) {
		if(listener == null)
			return;
		synchronized(storageListeners) {
			storageListeners.put(listener, listener);
		}
	}

	public boolean removeStorageListener(SlotStorageListener listener) {
		if(listener == null)
			return false;
		synchronized(storageListeners) {
			return storageListeners.remove(listener) != null;
		}
	}

	public void fireFragmentUpdated() throws IOException {
		synchronized(storageListeners) {
			for(SlotStorageListener listener : storageListeners.keySet())
				listener.saveSlot();
		}
	}

	public void addSecurityConstraint(SlotSecurityConstraint constraint) {
		if(constraint == null)
			return;
		synchronized(securityConstraints) {
			securityConstraints.put(constraint, constraint);
		}
	}

	public boolean removeSecurityConstraint(SlotSecurityConstraint constraint) {
		synchronized(securityConstraints) {
			return securityConstraints.remove(constraint) != null;
		}
	}

	public boolean mayPerformAction(SlotAction action, Credentials credentials) {
		synchronized(securityConstraints) {
			for(SlotSecurityConstraint constraint : securityConstraints.keySet()) {
				if(constraint.mayPerform(this, action, credentials))
					return true;
			}
		}
		return false;
	}

	private static String renderBrokenKey(String key) {
		return key
				.replace("\n", "<newline>")
				.replace("\r", "<carriage return>")
				.replace("\r\n", "<carriage return, newline>");
	}

	private static void ensureDoesNotContainLineEnding(String str, String what) {
		if(str.indexOf('\n') >= 0 || str.indexOf('\r') >= 0)
			throw new IllegalArgumentException(what + " cannot contain line ending: " + Slot.renderBrokenKey(str));
	}

}
