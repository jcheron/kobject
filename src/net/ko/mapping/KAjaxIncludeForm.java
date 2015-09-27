package net.ko.mapping;

import net.ko.framework.Ko;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxIncludeForm extends KAjaxInclude {
	private KAjaxSubmitFormButton submitFormButton;
	private String formKobjectShortClassName;

	public KAjaxIncludeForm() {
		super();
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		submitFormButton = new KAjaxSubmitFormButton();
		submitFormButton.getChilds().addAll(childs);
		childs.clear();
		Element e = (Element) item;
		String kobjectShortClassName = e.getAttribute("formKobjectShortClassName");
		String strValidation = e.getAttribute("validation");
		boolean validation = true;
		if (KString.isBoolean(strValidation))
			validation = KString.isBooleanTrue(strValidation);
		submitFormButton.initWith(kobjectShortClassName, e.getAttribute("formName"), e.getAttribute("formCondition"), e.getAttribute("formTargetParams"),
				e.getAttribute("formUpdateMessageMask"), e.getAttribute("formNoUpdateMessageMask"), e.getAttribute("formErrorMessageMask"), e.getAttribute("formMethod"), validation);
		String targetUrl = "submitForm" + kobjectShortClassName + ".do";
		submitFormButton.setParentSelector(parentSelector);
		submitFormButton.setTargetURL(targetUrl);
		submitFormButton.setTargetId(e.getAttribute("formTargetId"));
		submitFormButton.setButtonId(e.getAttribute("formButtonId"));
		submitFormButton.setButtonCaption(e.getAttribute("formButtonCaption"));
		submitFormButton.setTimein(timein);
		submitFormButton.setTimeout(timeout);
		String strKeyCode = e.getAttribute("formButtonKeyCode");
		if (strKeyCode != null && !"".equals(strKeyCode)) {
			try {
				submitFormButton.setButtonKeyCode(Integer.valueOf(strKeyCode));
			} catch (Exception ex) {
				submitFormButton.setButtonKeyCode(-1);
			}
		}
		timein = -1;
		timeout = -1;
		KMappingSubmitForm msf = new KMappingSubmitForm();
		msf.setRequestURL(targetUrl);
		Ko.kadditionalMappings().add(msf);
		childs.add(submitFormButton);
	}

	@Override
	public KStrings getTitles() {
		KStrings titles = super.getTitles();
		titles.put("kobjectShortClassName", submitFormButton.getKobjectShortClassName());
		return titles;
	}

	@Override
	public String getName() {
		return "includeForm";
	}

	@Override
	public String getDebugClientId() {
		return "#" + targetId;
	}

	public String getButtonCaption() {
		String result = "";
		if (submitFormButton != null)
			result = submitFormButton.getButtonCaption();
		return result;
	}

	public String getButtonId() {
		String result = "";
		if (submitFormButton != null)
			result = submitFormButton.getButtonId();
		return result;
	}

	public KAjaxSubmitFormButton getSubmitFormButton() {
		return submitFormButton;
	}

	public void setSubmitFormButton(KAjaxSubmitFormButton submitFormButton) {
		this.submitFormButton = submitFormButton;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "validation", submitFormButton.isValidation() + "");
		XmlUtils.setAttribute(result, "formKobjectShortClassName", submitFormButton.getKobjectShortClassName());
		XmlUtils.setAttribute(result, "formName", submitFormButton.getFormName());
		XmlUtils.setAttribute(result, "formCondition", submitFormButton.getCondition());
		if (submitFormButton.getTargetParams() != null)
			XmlUtils.setAttribute(result, "formTargetParams", submitFormButton.getTargetParams().replace("&", ","));
		XmlUtils.setAttribute(result, "formUpdateMessageMask", submitFormButton.getUpdateMessageMask());
		XmlUtils.setAttribute(result, "formNoUpdateMessageMask", submitFormButton.getNoUpdateMessageMask());
		XmlUtils.setAttribute(result, "formErrorMessageMask", submitFormButton.getErrorMessageMask());
		XmlUtils.setAttribute(result, "formMethod", submitFormButton.getMethod());
		XmlUtils.setAttribute(result, "formTargetId", submitFormButton.getTargetId());
		XmlUtils.setAttribute(result, "formButtonId", submitFormButton.getButtonId());
		XmlUtils.setAttribute(result, "formButtonCaption", submitFormButton.getButtonCaption());
		if (submitFormButton.getButtonKeyCode() != 0)
			XmlUtils.setAttribute(result, "formButtonKeyCode", submitFormButton.getButtonKeyCode() + "");
		return result;
	}
}
