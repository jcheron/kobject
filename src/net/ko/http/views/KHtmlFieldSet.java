package net.ko.http.views;


/**
 * Fieldset HTML
 * @author jcheron
 *
 */
public class KHtmlFieldSet extends KFieldControl {
	public KHtmlFieldSet() {
	}
	public KHtmlFieldSet(String legend) {
		this.caption=legend;
	}
	public String toString(){
		return "<fieldset><legend>"+caption+"</legend>";
	}
}
