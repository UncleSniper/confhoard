package org.unclesniper.confhoard.core.security;

public enum SlotAction {

	RETRIEVE("retrieve", "mayRetrieve", true),
	UPDATE("update", "mayUpdate", false);

	private final String actionName;

	private final String actionProperty;

	private final boolean readOnly;

	private SlotAction(String actionName, String actionProperty, boolean readOnly) {
		this.actionName = actionName;
		this.actionProperty = actionProperty;
		this.readOnly = readOnly;
	}

	public String getActionName() {
		return actionName;
	}

	public String getActionProperty() {
		return actionProperty;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

}
