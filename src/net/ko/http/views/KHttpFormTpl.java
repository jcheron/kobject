package net.ko.http.views;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.displays.KObjectDisplay;
import net.ko.kobject.KMask;
import net.ko.kobject.KObject;

public class KHttpFormTpl extends KHttpForm {
	private String strTemplate;
	private String sepFirst = "{";
	private String sepLast = "}";

	public KHttpFormTpl(KObject ko, HttpServletRequest request, HttpServletResponse response, String strTemplate) {
		super(ko, request);
		this.strTemplate = strTemplate;
		setResponse(response, true);
	}

	public KHttpFormTpl(KObject ko, HttpServletRequest request, HttpServletResponse response,
			boolean clientControl, String id, String strTemplate) {
		super(ko, request, clientControl, id);
		this.strTemplate = strTemplate;
		this.response = response;
	}

	public String getStrTemplate() {
		return strTemplate;
	}

	public void setStrTemplate(String strTemplate) {
		this.strTemplate = strTemplate;
	}

	@Override
	public String getFormContent() {
		String result = fieldControls.parseTemplate(this, ko, strTemplate, sepFirst, sepLast);
		return result;
	}

	@Override
	public void execBeforeSubmit() {
		fieldControls.parseTemplate(this, ko, strTemplate, sepFirst, sepLast, true);
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

	@Override
	public KFieldControl addDefaultButtons() {
		return fieldControls.put("_buttons", getDefaultButtons());
	}

	@Override
	protected void initFieldControls() {
		super.initFieldControls();
		KObjectDisplay display = getDisplayInstance();
		KMask mask = new KMask(strTemplate);
		ArrayList<String> jsonFieldNames = mask.getFields();
		for (String field : jsonFieldNames) {
			if (!field.startsWith("_") && !field.startsWith("#") && !fieldControls.containsKey(field)) {
				KFieldControl bestCtrl = display.getControl(ko, field, kobjectController, request);
				((KHtmlFieldControl) bestCtrl).setOwner(this);
				fieldControls.put(field, bestCtrl);
				if (!"".equals(bestCtrl.getId()))
					this.addValidator(((KHtmlFieldControl) bestCtrl).getValidatorString());
			}
		}
	}
}
