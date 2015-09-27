package net.ko.mapping;

import net.ko.types.KStringsType;
import net.ko.utils.KString;
import net.ko.utils.KStrings;

import org.w3c.dom.Element;

public class KAjaxSubmitFormButton extends KAjaxSubmitForm {
	private String buttonId;
	private String buttonCaption;
	private int buttonKeyCode;

	public KAjaxSubmitFormButton() {
		super();
		isChild = true;
		buttonKeyCode = -1;
	}

	public void initWith(String kobjectShortClassName, String formName, String condition, String targetParams, String updateMessageMask,
			String noUpdateMessageMask, String errorMessageMask, String method, boolean validation) {
		String sep = "";
		this.kobjectShortClassName = kobjectShortClassName;
		if (kobjectShortClassName != null && !"".equals(kobjectShortClassName)) {
			if (formName == null || "".equals(formName)) {
				this.formName = "frm" + kobjectShortClassName;
			} else
				this.formName = formName;
			setCondition(validation, condition);
			if (targetParams != null && !"".equals(targetParams)) {
				sep = "&";
			}
			this.targetParams = targetParams + sep + "_cls=" + this.kobjectShortClassName;
		}
		if (this.targetParams == null || "".equals(this.targetParams))
			this.targetParams = "_keyValues={jsv:'koKeyValue'}";
		else {
			if (!this.targetParams.contains("{jsv:'koKeyValue'}"))
				this.targetParams += "&_keyValues={jsv:'koKeyValue'}";
		}

		if (updateMessageMask != null && !"".equals(updateMessageMask))
			this.updateMessageMask = updateMessageMask;

		if (noUpdateMessageMask != null && !"".equals(noUpdateMessageMask))
			this.noUpdateMessageMask = noUpdateMessageMask;

		if (errorMessageMask != null && !"".equals(errorMessageMask))
			this.errorMessageMask = errorMessageMask;
		this.targetParams += "&_uMM=" + KString.urlEncode(this.updateMessageMask);
		this.targetParams += "&_nuMM=" + KString.urlEncode(this.noUpdateMessageMask);
		this.targetParams += "&_eMM=" + KString.urlEncode(this.errorMessageMask);

		setMethod(method);
		parameters = new KStrings(this.targetParams, KStringsType.kstQueryString);
	}

	public void setCondition(boolean validation, String condition) {
		if (KString.isNotNull(condition)) {
			if (validation)
				this.condition = "(v" + formName + ".validate()==true) && (" + condition + ")";
			else
				this.condition = condition;
		} else {
			if (validation)
				this.condition = "v" + formName + ".validate()==true";
			else
				this.condition = "";
		}
	}

	public String getButtonId() {
		return buttonId;
	}

	public void setButtonId(String buttonId) {
		if (buttonId == null || "".equals(buttonId))
			this.buttonId = this.formName + "-button";
		else
			this.buttonId = buttonId;
	}

	public void setButtonCaption(String buttonCaption) {
		if (buttonCaption == null || "".equals(buttonCaption))
			this.buttonCaption = "Valider";
		else
			this.buttonCaption = buttonCaption;
	}

	@Override
	public String createFunction() {
		String result = super.createFunction();
		result = "Forms.Framework.checkSubmitFormButton('" + this.formName + "','" + this.buttonId + "','" + this.buttonCaption + "');new Forms.Elements('#" + this.buttonId + "').addEvent('click'," + getPreludeFunction() + result + "},false," + buttonKeyCode + ");\n";
		result = addDeboggerExpression(result, getName() + ":" + getDebugClientId() + "->" + targetURL, result);
		return result;
	}

	public String getSubmitFormFunction() {
		return super.createFunction();
	}

	@Override
	public KStrings getTitles() {
		KStrings titles = super.getTitles();
		titles.put("buttonId", buttonId);
		titles.put("buttonCaption", buttonCaption);
		return titles;
	}

	@Override
	public String getName() {
		return "submitFormButton";
	}

	@Override
	public String getDebugClientId() {
		return "#" + buttonId;
	}

	public String getButtonCaption() {
		return buttonCaption;
	}

	public int getButtonKeyCode() {
		return buttonKeyCode;
	}

	public void setButtonKeyCode(int buttonKeyCode) {
		this.buttonKeyCode = buttonKeyCode;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		return result;
	}
}
