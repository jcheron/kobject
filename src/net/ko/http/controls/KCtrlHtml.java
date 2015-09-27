package net.ko.http.controls;

import net.ko.http.views.KHtmlFieldControl;
import net.ko.types.HtmlControlType;

public class KCtrlHtml extends KHtmlFieldControl {

	public KCtrlHtml(String htmlContent) {
		super();
		fieldType = HtmlControlType.khcCustom;
		value = htmlContent;
	}

	@Override
	public String toString() {
		return value;
	}

}
