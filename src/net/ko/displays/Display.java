package net.ko.displays;

import javax.servlet.http.HttpServletRequest;

import net.ko.http.views.KFieldControl;
import net.ko.interfaces.Dispayable;
import net.ko.kobject.KObject;
import net.ko.types.HtmlControlType;

public abstract class Display implements Dispayable {
	protected static Display instance;

	public static Display getDefault() {
		if (instance == null)
			instance = new DefaultDisplay();
		return instance;
	}

	protected Display() {
		instance = this;
	}

	@Override
	public KFieldControl getFcInput(KObject ko, String field, String id, String caption, HtmlControlType type, String options) {
		return getFc(ko, field, id, caption, type, options, null);
	}

	@Override
	public KFieldControl getFcLabel(KObject ko, String field, String caption, String options) {
		return getFc(ko, field, "", caption, HtmlControlType.khcLabel, options, null);
	}

	@Override
	public KFieldControl getFcList(KObject ko, String field, String id, Object listObject, String caption, HtmlControlType type, String options) {
		return getFc(ko, field, id, caption, type, options, listObject);
	}

	@Override
	public KFieldControl getFcRadio(KObject ko, String field, String id, String listObject, String caption, String options) {
		return getFc(ko, field, id, caption, HtmlControlType.khcRadio, options, listObject);
	}

	@Override
	public KFieldControl getFcCheckBox(KObject ko, String field, String id, Object listObject, String caption, String options) {
		return getFc(ko, field, id, caption, HtmlControlType.khcCheckBox, options, listObject);
	}

	public abstract String getEndLigne();

	public abstract String showWithMask(KObject ko, String mask, String sepFirst, String sepLast, HttpServletRequest request);

	public abstract String showWithMask(KObject ko, String mask, String sepFirst, String sepLast, KObjectDisplay koDisplay, HttpServletRequest request);

	public abstract String showWithMask(KObject ko, String mask, String sepFirst, String sepLast, KObjectDisplay koDisplay);
}
