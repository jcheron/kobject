package net.ko.http.views;


/**
 * Représente un ensemble d'éléments HTML quelconque
 * @author jcheron
 *
 */
public class KHtmlElement extends KFieldControl {
	private String content;
	public KHtmlElement() {
		content="";
	}
	public KHtmlElement(String content){
		this.content=content;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String toString(){
		return content;
	}
}
