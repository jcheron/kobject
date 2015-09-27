package net.ko.http.views;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.controller.KObjectFieldController;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.KoHttp;
import net.ko.kobject.KObject;
import net.ko.types.HtmlControlType;

public class KHttpShow extends KobjectHttpAbstractForm {

	private String fieldSetLegend;
	private int fieldSets;

	public KHttpShow(KObject ko, HttpServletRequest request) {
		super(ko, request);
	}

	@Override
	public void init() {

	}

	public String getFormContent() {
		return fieldControls.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ko.http.views.KobjectHttpAbstractView#load()
	 */
	@Override
	public boolean load() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		return load(false, false);
	}

	@Override
	public boolean load(boolean noDb, boolean beforeSubmit) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		boolean result = super.load(noDb || insertMode, beforeSubmit);
		initFieldControls();
		return result;
	}

	public void getDefaultComplete(String redirect, HttpServletResponse response) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		this.load(false, false);
		addDefaultFieldSet().setPos(-30);
		closeDefaultFieldSet();
	}

	protected void initFieldControls() {
		setLoaded(true);
		KObjectDisplay display = getDisplayInstance();
		fieldSetLegend = display.getFormCaption(ko.getClass(), kobjectController);
		ko.refresh();
		for (Map.Entry<String, KObjectFieldController> e : kobjectController.getMembers().entrySet()) {
			String field = e.getKey();
			KFieldControl bestCtrl = display.getReadOnlyControl(ko, field, kobjectController, request);
			bestCtrl.setReadOnly(true);
			((KHtmlFieldControl) bestCtrl).setOwner(this);
			fieldControls.put(field, bestCtrl);
		}
	}

	private KFieldControl fieldSet(String legend) {
		return fieldSet(legend, "");
	}

	private KFieldControl fieldSet(String legend, String options) {
		return new KHtmlFieldControl(legend, "", "", "_fieldset", HtmlControlType.khcFieldSet, null, options);
	}

	private KFieldControl getDefaultFieldSet() {
		if (fieldSetLegend == null || "".equals(fieldSetLegend))
			return this.fieldSet(this.ko.getClass().getSimpleName());
		else
			return this.fieldSet(fieldSetLegend);
	}

	/**
	 * @return le fieldset HTML ayant pour legend le nom de la classe de l'objet
	 */
	public KFieldControl addDefaultFieldSet() {
		KFieldControl result = fieldControls.put("_fieldset", getDefaultFieldSet());
		fieldSets++;
		return result;
	}

	public KFieldControl closeDefaultFieldSet() {
		KHtmlFieldControl cfs = new KHtmlFieldControl();
		cfs.setFieldType(HtmlControlType.khcCustom);
		cfs.setValue("</fieldset>");
		KFieldControl result = fieldControls.put("/_fieldset", cfs);
		fieldSets--;
		return result;
	}

	@Override
	public String toString() {
		String ret = getFormContent();
		if (ajaxIncludes) {
			ret += KoHttp.kajaxIncludes(request);
			ret += "<script>Forms.Framework.initShowTpl();</script>";
		}
		return getPageHeader() + ret + getPageFooter();
	}

}
