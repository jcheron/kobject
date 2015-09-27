package net.ko.http.views;

/**
 * Représente un bouton en HTML
 * 
 * @author jcheron
 * 
 */
public class KHtmlButton extends KFieldControl {
	private String type = "submit";
	private String onclick = "";

	public KHtmlButton(String type, String onclick) {
		this.type = type;
		this.onclick = onclick;
	}

	public String toString() {
		if ("submit".equals(type))
			return "<div id='form-buttons'><input class='btn' type='" + type + "' onclick='" + onclick + "' value='" + caption + "'></div>";
		else
			return "<div id='form-buttons'><div class='btn' onclick='" + onclick + "'><span>" + caption + "'></span></div></div>";

	}
}
