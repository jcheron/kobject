package net.ko.dao;

public class KDaoSpec {
	protected String protectChar;

	public KDaoSpec(String protectChar) {
		super();
		this.protectChar = protectChar;
	}

	public String getProtectChar() {
		return protectChar;
	}

	public void setProtectChar(String protectChar) {
		this.protectChar = protectChar;
	}
}
