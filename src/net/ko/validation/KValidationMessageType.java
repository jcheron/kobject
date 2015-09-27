package net.ko.validation;

public enum KValidationMessageType {
	INFO("infoMessage"), WARNING("warningMessage"), ERROR("errMessage"), NONE("");
	protected String cssClass;

	private KValidationMessageType(String cssClass) {
		this.cssClass = cssClass;
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

}
