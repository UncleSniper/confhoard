package org.unclesniper.confhoard.core.security;

public enum SlotAction {

	RETRIEVE("retrieve", "mayRetrieve"),
	UPDATE("update", "mayUpdate");

	private final String actionName;

	private final String actionProperty;

	private SlotAction(String actionName, String actionProperty) {
		this.actionName = actionName;
		this.actionProperty = actionProperty;
	}

	public String getActionName() {
		return actionName;
	}

	public String getActionProperty() {
		return actionProperty;
	}

}
