package org.unclesniper.confhoard.core.security;

public enum SlotAction {

	RETRIEVE("retrieve"),
	UPDATE("update");

	private final String actionName;

	private SlotAction(String actionName) {
		this.actionName = actionName;
	}

	public String getActionName() {
		return actionName;
	}

}
