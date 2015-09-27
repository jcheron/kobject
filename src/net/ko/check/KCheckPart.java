package net.ko.check;

public abstract class KCheckPart {
	protected String requestURL;
	protected KCheckMessage message;

	public KCheckPart(String requestURL, KCheckMessageWithChilds messageParent, String content, String uid) {
		this.requestURL = requestURL;
		this.message = messageParent.addMessage(KCheckMessageType.UNKNOWN, content);
		this.message.setId(uid);
		this.message.setHasDOMId(true);
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	protected abstract String getJsCheck();

	protected String getJsRequest() {
		return "$get('selectors','check.main','_op=" + getOperation() + "&id=" + requestURL + "',function(){" + getJsCheck() + "});";
	}

	protected abstract String getOperation();
}
