package net.ko.http.controls;

import net.ko.types.HtmlControlType;

public class KCtrlList extends KCtrl {

	public KCtrlList(String caption, String value, String idName) {
		super(caption, value, idName);
		control.setFieldType(HtmlControlType.khcCmb);
	}

	public void setType(HtmlControlType ctrlType) {
		control.setFieldType(ctrlType);
	}

	public void setList(Object listObject) {
		control.setListObject(listObject);
	}

}
