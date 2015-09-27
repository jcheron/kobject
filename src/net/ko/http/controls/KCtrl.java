package net.ko.http.controls;

import net.ko.http.views.KHtmlFieldControl;
import net.ko.types.HtmlControlType;

public abstract class KCtrl {
	protected KHtmlFieldControl control;

	public KCtrl(String caption, String value, String idName) {
		control = new KHtmlFieldControl(caption, value, idName, idName, HtmlControlType.khcText);
	}

	@Override
	public String toString() {
		return control.toString();
	}
}
