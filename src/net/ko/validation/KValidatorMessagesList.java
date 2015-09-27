package net.ko.validation;

import java.util.ArrayList;
import java.util.List;

public class KValidatorMessagesList {
	private List<KValidationMessage> messages;
	private boolean hasErrors;

	public KValidatorMessagesList() {
		messages = new ArrayList<>();
		hasErrors = false;
	}

	public void addMessage(KValidationMessage message) {
		if (message.isError())
			hasErrors = true;
		messages.add(message);
	}

	public void addMessage(KValidationMessageType level, String content) {
		addMessage(new KValidationMessage(level, content));
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	public String toString() {
		String result = "";
		for (KValidationMessage msg : messages)
			result += msg.toString();
		return result;
	}

	public void clear() {
		messages.clear();
		hasErrors = false;
	}
}
