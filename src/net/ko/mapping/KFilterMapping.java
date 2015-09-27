package net.ko.mapping;


public abstract class KFilterMapping extends KAbstractFilterMapping {
	protected String responseURL;

	public KFilterMapping(String requestURL, String responseURL) {
		super(requestURL);
		this.responseURL = responseURL;
	}

	public String getResponseURL() {
		return responseURL;
	}

	public void setResponseURL(String responseURL) {
		this.responseURL = responseURL;
	}

	@Override
	public boolean executeByName(String name) {
		return false;
	}

	@Override
	public String displayRecursive() {
		return display(0);
	}
}
