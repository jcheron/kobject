package net.ko.displays;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.ko.http.views.KFieldControl;
import net.ko.kobject.KObject;
import net.ko.types.HtmlControlType;

public class DefaultDisplay extends Display {

	public DefaultDisplay() {
	}

	@Override
	public KFieldControl getFc(KObject ko, String field, String id, String caption,
			HtmlControlType type, String options, Object listObject) {
		return new KFieldControl() {
		};
	}

	@Override
	public KFieldControl getBestControl(KObject ko, String field) {
		// TODO Auto-generated method stub
		return new KFieldControl() {
		};
	}

	@Override
	public String getEndLigne() {
		return "\n";
	}

	@Override
	public String showWithMask(KObject ko, String mask, String sepFirst, String sepLast, HttpServletRequest request) {
		try {
			ArrayList<String> fieldNames = ko.getFieldNames();
			for (String fName : fieldNames) {
				try {
					String value = ko.getAttribute(fName) + "";
					mask = mask.replaceAll("(?i)" + "\\" + sepFirst + fName + "\\" + sepLast, value);
				} catch (Exception e2) {
				}
			}
			mask = mask.replace(sepFirst + "id" + sepLast, ko.getId() + "");
			Pattern p = Pattern.compile("\\" + sepFirst + "(.+?)" + "\\" + sepLast, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(mask);
			while (m.find())
				if (m.groupCount() > 0) {
					Object ret = new String("");
					String mName = m.group(1);
					ret = ko.invoke(mName, ko.getClass());
					mask = mask.replace(sepFirst + mName + sepLast, ret + "");
				}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return mask;
	}

	@Override
	public HtmlControlType getControlType(KObject ko, String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KFieldControl getDefaultControl(KObject ko, String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String showWithMask(KObject ko, String mask, String sepFirst,
			String sepLast, KObjectDisplay koDisplay, HttpServletRequest request) {
		return showWithMask(ko, mask, sepFirst, sepLast, request);
	}

	/*
	 * @Override public String showWithMask(KObject ko, String mask, String
	 * sepFirst, String sepLast) { // TODO Auto-generated method stub return
	 * null; }
	 */
	@Override
	public String showWithMask(KObject ko, String mask, String sepFirst, String sepLast, KObjectDisplay koDisplay) {
		// TODO Auto-generated method stub
		return null;
	}

}
