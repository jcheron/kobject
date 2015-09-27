package net.ko.types;

public enum EscapeChar {
	ecVirgule(',',"%virgule%"),
	ecDeuxPoints(':',"%2Points%");
	private char c;
	private String replacement;
	private EscapeChar(char c,String replacement) {
		this.c=c;
		this.replacement=replacement;
	}
	public char getC() {
		return c;
	}
	public void setC(char c) {
		this.c = c;
	}
	public String getReplacement() {
		return replacement;
	}
	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
}
