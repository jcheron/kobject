package net.ko.http.controls;

import net.ko.types.HtmlControlType;

public class KCtrlInputHidden extends KCtrl {

	public KCtrlInputHidden(String caption, String value, String idName) {
		super("", value, idName);
		control.setFieldType(HtmlControlType.khcHidden);
	}

}
