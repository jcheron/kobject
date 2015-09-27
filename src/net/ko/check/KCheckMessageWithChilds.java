package net.ko.check;

import java.util.EnumSet;

public class KCheckMessageWithChilds extends KCheckMessage {
	protected KCheckMessagesList messageList;
	protected boolean visible;

	public KCheckMessageWithChilds(KCheckMessageType level, String content) {
		super(level, content);
		messageList = new KCheckMessagesList();
		visible = false;
	}

	public KCheckMessageWithChilds(String content) {
		super(content);
		messageList = new KCheckMessagesList();
		visible = false;
	}

	public void addMessage(KCheckMessage msg) {
		messageList.addMessage(msg);
	}

	@Override
	public String toString() {
		String content = this.content;
		if (messageList.count() > 0) {
			KCheckMessageType mt = getMessageType();
			if (level.getValue() < mt.getValue()) {
				content += new KCheckMessage(mt, mt.getDescription());
				if (mt.getValue() > 3)
					setVisibleForError(true);
			}
			String className = "arrow-down";
			String style = "display:none;";
			if (visible) {
				className = "arrow-up";
				style = "";
			}
			String id = getIdHash();
			String result = "<a class='toogleItem' onclick='Forms.Utils.toogleText($(\"imgToogle" + id + "\"),\"" + id + "\",false);'><div id='imgToogle" + id + "' class='" + className + "'></div>&nbsp;" + super.displayContent(content) + "</a>";
			result += "<div style='" + style + "' class='ssDiv' id='" + id + "'>";
			for (KCheckMessage msg : messageList.getMessages()) {
				result += msg;
			}
			result += "</div>";
			return result;
		}
		else
			return super.toString();
	}

	@Override
	public String display(EnumSet<KCheckMessageType> showOptions) {
		String content = this.content;
		if (messageList.count() > 0) {
			KCheckMessageType mt = getMessageType();
			if (level.getValue() < mt.getValue()) {
				content += new KCheckMessage(mt, mt.getDescription());
				if (mt.getValue() > 3)
					setVisibleForError(true);
			}
			String className = "arrow-down";
			String style = "display:none;";
			if (visible) {
				className = "arrow-up";
				style = "";
			}
			String id = getIdHash();
			String childsResult = "";
			String result = "<a class='toogleItem' onclick='Forms.Utils.toogleText($(\"imgToogle" + id + "\"),\"" + id + "\",false);'><div id='imgToogle" + id + "' class='" + className + "'></div>&nbsp;" + super.displayContent(content) + "</a>";
			result += "<div style='" + style + "' class='ssDiv' id='" + id + "'>";
			for (KCheckMessage msg : messageList.getMessages()) {
				childsResult += msg.display(showOptions);
			}
			if ("".equals(childsResult) && !showOptions.contains(getMessageType()))
				return "";
			result += childsResult;
			result += "</div>";
			return result;
		}
		else
			return super.display(showOptions);
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setVisible(boolean visible, boolean recursive) {
		if (visible && recursive) {
			this.visible = visible;
			for (KCheckMessage msg : messageList.getMessages()) {
				if (msg instanceof KCheckMessageWithChilds)
					((KCheckMessageWithChilds) msg).setVisible(visible, recursive);
			}
		}

	}

	public void setVisibleForError(boolean visible) {
		this.visible = visible;
		for (KCheckMessage msg : messageList.getMessages()) {
			if (msg instanceof KCheckMessageWithChilds)
				if (msg.getLevel().getValue() > 3)
					((KCheckMessageWithChilds) msg).setVisible(visible);
		}
	}

	public KCheckMessage addMessage(KCheckMessageType level, String content) {
		return messageList.addMessage(level, content);
	}

	public KCheckMessageWithChilds addMessageWithChild(KCheckMessageType level, String string) {
		return messageList.addMessageWithChild(level, string);
	}

	public KCheckMessage addInfoMessage(String content) {
		return messageList.addInfoMessage(content);
	}

	@Override
	public boolean isError() {
		boolean result = super.isError();
		if (result) {
			return messageList.hasErrors();
		}
		return result;
	}

	@Override
	public KCheckMessageType getMessageType() {
		KCheckMessageType msgType = KCheckMessageType.VALID;
		for (KCheckMessage msg : messageList.getMessages()) {
			if (msg.getMessageType().getValue() > msgType.getValue())
				msgType = msg.getMessageType();
		}
		return msgType;
	}

}
