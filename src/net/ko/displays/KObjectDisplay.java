package net.ko.displays;

import javax.servlet.http.HttpServletRequest;

import net.ko.controller.KObjectController;
import net.ko.controller.KObjectFieldController;
import net.ko.framework.Ko;
import net.ko.http.views.KFieldControl;
import net.ko.http.views.KHtmlFieldControl;
import net.ko.http.views.KPageList;
import net.ko.http.views.KobjectHttpAbstractView;
import net.ko.interfaces.IKobjectDisplay;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.types.HtmlControlType;
import net.ko.utils.KReflection;

public class KObjectDisplay implements IKobjectDisplay {

	public KObjectDisplay() {
	}

	@Override
	public String show(KObject ko) {
		return ko.toString();
	}

	@Override
	public String showInList(KObject ko, String memberName, HttpServletRequest request) {
		return KReflection.getMemberValue(memberName, ko) + "";
	}

	@Override
	public String showWithMask(KObject ko, String mask, HttpServletRequest request) {
		return ko._showWithMask(mask);
	}

	@Override
	public String getCaption(Class<? extends KObject> clazz) {
		return clazz.getSimpleName();
	}

	@Override
	public String getCaption(KObject ko, String memberName) {
		String result = memberName;
		KObjectController koc = Ko.kcontroller().getObjectController(ko.getClass().getSimpleName());
		if (koc != null) {
			KObjectFieldController fc = koc.getFieldController(memberName);
			if (fc != null)
				if (!"".equals(fc.getCaption()))
					result = fc.getCaption();
		}
		return result;
	}

	@Override
	public String getFormCaption(Class<? extends KObject> clazz, KObjectController koc) {
		String result = clazz.getSimpleName();
		if (koc != null)
			if (koc.getCaption() != null)
				result = koc.getCaption();
		return result;
	}

	@Override
	public KFieldControl getControl(KObject ko, String memberName, KObjectController koc, HttpServletRequest request) {
		KFieldControl bestCtrl = Display.getDefault().getBestControl(ko, memberName);
		String options = "";
		String caption = memberName + " :";
		String template = null;
		boolean multiple = false;
		int pos = 0;
		if (bestCtrl == null) {
			HtmlControlType htmlCT = HtmlControlType.khcText;
			if (koc != null) {
				KObjectFieldController kofc = koc.getFieldController(memberName);
				htmlCT = kofc.getHtmlControlType();
				options = " " + kofc.getOptions() + " ";
				if (!"".equals(kofc.getCaption()))
					caption = kofc.getCaption();
				pos = kofc.getPos();
				if (kofc.isMultiple())
					multiple = true;
				template = kofc.getTemplate();
			}
			bestCtrl = Display.getDefault().getFc(ko, memberName, memberName, caption, htmlCT, options, null);
			((KHtmlFieldControl) bestCtrl).setTemplate(template);
			bestCtrl.setMultiple(multiple);
			if (pos != 0)
				bestCtrl.setPos(pos * 10);
		}
		return bestCtrl;
	}

	@Override
	public KFieldControl getReadOnlyControl(KObject ko, String memberName,
			KObjectController koc, HttpServletRequest request) {
		KFieldControl fc = getControl(ko, memberName, koc, request);
		switch (fc.getFieldType()) {
		case khcListForm:
		case khcListFormMany:
		case khcPageList:
		case khcPassWord:
		case khcHidden:
		case khcReadOnlyList:
		case khcReadOnlyText:
			break;
		case khcCheckedAjaxList:
		case khcCheckedDataList:
		case khcCheckedList:
		case khcRadio:
		case khcRadioAjaxList:
		case khcRadioDataList:
		case khcCmb:
		case khcList:
		case khcDataList:
			fc.setFieldType(HtmlControlType.khcReadOnlyList);
			break;
		default:
			fc.setFieldType(HtmlControlType.khcReadOnlyText);
			break;
		}

		return fc;
	}

	@Override
	public void beforeLoading(KObject object, KobjectHttpAbstractView view,
			HttpServletRequest request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterLoading(KObject ko, KobjectHttpAbstractView view,
			HttpServletRequest request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeLoading(Class<? extends KObject> clazz, KPageList list,
			HttpServletRequest request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterLoading(KListObject<? extends KObject> kl, KPageList list,
			HttpServletRequest request) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getRefreshValue(KObject ko, String memberName, KObjectController koc, HttpServletRequest request) {
		KFieldControl bestCtrl = getControl(ko, memberName, koc, request);
		return bestCtrl.getValue();
	}

	@Override
	public String[] getRefreshFields() {
		return new String[] {};
	}

	@Override
	public String getZone(String zoneName, KPageList list, HttpServletRequest request) {
		return "{" + zoneName + "}";
	}

	@Override
	public KObjectController getKobjectController(KObjectController koc, HttpServletRequest request) {
		return koc;
	}
}
