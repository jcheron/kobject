package net.ko.validation;

public class KValidationMessage {
	private String content = "";
	private KValidationMessageType level = KValidationMessageType.INFO;

	public KValidationMessage(String content) {
		this(KValidationMessageType.INFO, content);
	}

	public KValidationMessage(KValidationMessageType level, String content) {
		this.content = content;
		this.level = level;
	}

	public boolean isError() {
		return level != KValidationMessageType.INFO;
	}

	public String toString() {
		return "<span class='" + level.getCssClass() + "'>" + content + "</span>";
	}
}
