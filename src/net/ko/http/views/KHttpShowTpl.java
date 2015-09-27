package net.ko.http.views;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.controller.KObjectController;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.kobject.KMask;
import net.ko.kobject.KObject;

public class KHttpShowTpl extends KHttpShow {
	private String strTemplate;
	private String sepFirst = "{";
	private String sepLast = "}";

	public KHttpShowTpl(KObject ko, HttpServletRequest request, HttpServletResponse response, String strTemplate) {
		super(ko, request);
		this.strTemplate = strTemplate;
		setResponse(response, true);
	}

	public KHttpShowTpl(KObject newInstance, HttpServletRequest request) {
		this(newInstance, request, null, "");
	}

	public String getStrTemplate() {
		return strTemplate;
	}

	public void setStrTemplate(String strTemplate) {
		this.strTemplate = strTemplate;
	}

	@Override
	protected void initFieldControls() {
		super.initFieldControls();
		KObjectDisplay display = getDisplayInstance();
		KObjectController koc = null;
		koc = Ko.kcontroller().getObjectController(ko.getClass().getSimpleName());
		KMask mask = new KMask(strTemplate);
		ArrayList<String> jsonFieldNames = mask.getFields();
		for (String field : jsonFieldNames) {
			if (!field.startsWith("_") && !field.startsWith("#") && !fieldControls.containsKey(field)) {
				KFieldControl bestCtrl = display.getReadOnlyControl(ko, field, koc, request);
				bestCtrl.setReadOnly(true);
				((KHtmlFieldControl) bestCtrl).setOwner(this);
				fieldControls.put(field, bestCtrl);
			}
		}
	}

	@Override
	public String getFormContent() {
		String result = fieldControls.parseTemplate(this, ko, strTemplate, sepFirst, sepLast);
		return result;
	}

	public String getSepFirst() {
		return sepFirst;
	}

	public void setSepFirst(String sepFirst) {
		this.sepFirst = sepFirst;
	}

	public String getSepLast() {
		return sepLast;
	}

	public void setSepLast(String sepLast) {
		this.sepLast = sepLast;
	}
}
