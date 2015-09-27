package net.ko.check;

import java.util.EnumSet;

import net.ko.utils.KHash;

public class KCheckMessage {
	protected String id;
	private String tagName;
	protected String content = "";
	protected KCheckMessageType level = KCheckMessageType.INFO;
	protected boolean hasDOMId;

	public KCheckMessage(String content) {
		this(KCheckMessageType.INFO, content);
	}

	public KCheckMessage(KCheckMessageType level, String content) {
		this.content = content;
		this.level = level;
		tagName = "span";
		hasDOMId = false;
	}

	protected String getIdHash() {
		return KHash.getMD5(content + id);
	}

	public boolean isError() {
		return level != KCheckMessageType.INFO;
	}

	public String toString() {
		return displayContent(content);
	}

	public String display(EnumSet<KCheckMessageType> showOptions) {
		String result = "";
		if (showOptions.contains(level))
			result = toString();
		return result;
	}

	public String displayContent(String content) {
		String strId = "";
		if (hasDOMId)
			strId = " id='" + getDomId() + "' ";
		return "<" + tagName + " class='" + level.getCssClass() + "'" + strId + ">" + content + "</" + tagName + ">";
	}

	public void addContent(KCheckMessageType level, String content) {
		tagName = "div";
		KCheckMessage msg = new KCheckMessage(level, content);
		this.content += msg.toString();
	}

	public KCheckMessageType getLevel() {
		return level;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getId() {
		return id;
	}

	public KCheckMessageType getMessageType() {
		return level;
	}

	public String getDomId() {
		return "_" + getIdHash();
	}

	public boolean isHasDOMId() {
		return hasDOMId;
	}

	public void setHasDOMId(boolean hasDOMId) {
		this.hasDOMId = hasDOMId;
	}
}
