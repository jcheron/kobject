package net.ko.check;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class KCheckMessagesList {
	private List<KCheckMessage> messages;
	private boolean hasErrors;

	public KCheckMessagesList() {
		messages = new ArrayList<>();
		hasErrors = false;
	}

	public KCheckMessage addMessage(KCheckMessage message) {
		if (message.isError())
			hasErrors = true;
		messages.add(message);
		return message;
	}

	public KCheckMessage addMessage(KCheckMessageType level, String content) {
		KCheckMessage msg = new KCheckMessage(level, content);
		addMessage(msg);
		return msg;
	}

	public KCheckMessage addTitleMessage(String content) {
		return addMessage(new KCheckMessage(KCheckMessageType.TITLE, content));
	}

	public KCheckMessageWithChilds addMessageWithChild(KCheckMessageType level, String content) {
		KCheckMessageWithChilds msg = new KCheckMessageWithChilds(level, content);
		messages.add(msg);
		return msg;
	}

	public KCheckMessage addInfoMessage(String content) {
		return addMessage(new KCheckMessage(KCheckMessageType.INFO, content));
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	public String toString() {
		String result = "";
		for (KCheckMessage msg : messages)
			result += msg.toString();
		return result;
	}

	public String display(EnumSet<KCheckMessageType> showOptions) {
		String result = "";
		for (KCheckMessage msg : messages)
			result += msg.display(showOptions);
		return result;
	}

	public void clear() {
		messages.clear();
		hasErrors = false;
	}

	public int count() {
		return messages.size();
	}

	public List<KCheckMessage> getMessages() {
		return messages;
	}
}
