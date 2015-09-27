package net.ko.check;

public enum KCheckMessageType {
	VALID("validMessage", 3, "Ok"), TITLE("titleMessage", 2, ""), INFO("infoMessage", 2, ""), UNKNOWN("unknownMessage", 2, "Unknown"), WARNING("warningMessage", 5, "Warnings"), ERROR("errMessage", 10, "Erreurs"), NONE("", 2, "");
	protected String cssClass;
	protected int value;
	protected String description;

	private KCheckMessageType(String cssClass, int value, String description) {
		this.cssClass = cssClass;
		this.value = value;
		this.description = description;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public String toString() {
		return cssClass;
	}

	public int getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

}
