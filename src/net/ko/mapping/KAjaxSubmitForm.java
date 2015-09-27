package net.ko.mapping;

import net.ko.types.KStringsType;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxSubmitForm extends KAjaxInclude {
	protected String kobjectShortClassName;
	protected String updateMessageMask;
	protected String noUpdateMessageMask;
	protected String errorMessageMask;
	protected String oUpdateMessageMask = "{toString} enregistré";
	protected String oNoUpdateMessageMask = "Aucune action effectuée sur {toString}";
	protected String oErrorMessageMask = "Erreur pendant l'enregistrement de {toString}";
	protected boolean validation;
	protected String virtualURL;
	protected String oCondition;
	private String oTargetParams;

	public KAjaxSubmitForm(String virtualURL) {
		this();
		setTargetURL(virtualURL);
	}

	public KAjaxSubmitForm() {
		super();
		formName = "";
		method = "post";
		oCondition = "";
		oTargetParams = "";
		updateMessageMask = oUpdateMessageMask;
		noUpdateMessageMask = oNoUpdateMessageMask;
		errorMessageMask = oErrorMessageMask;
		validation = true;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		oCondition = condition;
		oTargetParams = targetParams;
		Element e = (Element) item;
		String sep = "";
		virtualURL = e.getAttribute("virtualURL");
		setTargetURL(virtualURL);
		this.kobjectShortClassName = e.getAttribute("kobjectShortClassName");
		if (kobjectShortClassName != null && !"".equals(kobjectShortClassName)) {
			if (formName == null || "".equals(formName)) {
				formName = "frm" + kobjectShortClassName;
			}
			String strValidation = e.getAttribute("validation");
			if (KString.isBoolean(strValidation))
				validation = KString.isBooleanTrue(strValidation);
			setCondition(validation, condition);
			if (targetParams != null && !"".equals(targetParams)) {
				sep = "&";
			}
			targetParams += sep + "_cls=" + kobjectShortClassName;
		}
		if (targetParams == null || "".equals(targetParams))
			targetParams = "_keyValues={jsv:'koKeyValue'}";
		else {
			if (!targetParams.contains("{jsv:'koKeyValue'}"))
				targetParams += "&_keyValues={jsv:'koKeyValue'}";
		}
		String _updateMessageMask = e.getAttribute("updateMessageMask");
		if (_updateMessageMask != null && !"".equals(_updateMessageMask))
			updateMessageMask = _updateMessageMask;
		String _noUpdateMessageMask = e.getAttribute("noUpdateMessageMask");
		if (_noUpdateMessageMask != null && !"".equals(_noUpdateMessageMask))
			noUpdateMessageMask = _noUpdateMessageMask;
		String _errorMessageMask = e.getAttribute("errorMessageMask");
		if (_errorMessageMask != null && !"".equals(_errorMessageMask))
			errorMessageMask = _errorMessageMask;
		targetParams += "&_uMM=" + KString.urlEncode(updateMessageMask);
		targetParams += "&_nuMM=" + KString.urlEncode(noUpdateMessageMask);
		targetParams += "&_eMM=" + KString.urlEncode(errorMessageMask);
		setMethod(e.getAttribute("method"));
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

	public void initWith(String kobjectShortClassName, String formName, String condition, String targetParams, String updateMessageMask,
			String noUpdateMessageMask, String errorMessageMask, String method, boolean validation) {
		String sep = "";
		this.kobjectShortClassName = kobjectShortClassName;
		if (kobjectShortClassName != null && !"".equals(kobjectShortClassName)) {
			if (formName == null || "".equals(formName)) {
				this.formName = "frm" + kobjectShortClassName;
			}
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
		String _updateMessageMask = KString.urlEncode(updateMessageMask);
		if (_updateMessageMask != null && !"".equals(_updateMessageMask))
			this.updateMessageMask = _updateMessageMask;
		String _noUpdateMessageMask = KString.urlEncode(noUpdateMessageMask);
		if (_noUpdateMessageMask != null && !"".equals(_noUpdateMessageMask))
			this.noUpdateMessageMask = _noUpdateMessageMask;
		String _errorMessageMask = KString.urlEncode(errorMessageMask);
		if (_errorMessageMask != null && !"".equals(_errorMessageMask))
			this.errorMessageMask = _errorMessageMask;
		this.targetParams += "&_uMM=" + KString.urlEncode(this.updateMessageMask);
		this.targetParams += "&_nuMM=" + KString.urlEncode(this.noUpdateMessageMask);
		this.targetParams += "&_eMM=" + KString.urlEncode(this.errorMessageMask);
		setMethod(method);
		parameters = new KStrings(this.targetParams, KStringsType.kstQueryString);
	}

	@Override
	public boolean isValid() {
		boolean result = super.isValid() && kobjectShortClassName != null;
		return result;
	}

	public String getKobjectShortClassName() {
		return kobjectShortClassName;
	}

	public void setKobjectShortClassName(String kobjectShortClassName) {
		this.kobjectShortClassName = kobjectShortClassName;
	}

	public String getUpdateMessageMask() {
		return updateMessageMask;
	}

	public void setUpdateMessageMask(String updateMessageMask) {
		this.updateMessageMask = updateMessageMask;
	}

	public String getNoUpdateMessageMask() {
		return noUpdateMessageMask;
	}

	public void setNoUpdateMessageMask(String noUpdateMessageMask) {
		this.noUpdateMessageMask = noUpdateMessageMask;
	}

	public String getErrorMessageMask() {
		return errorMessageMask;
	}

	public void setErrorMessageMask(String errorMessageMask) {
		this.errorMessageMask = errorMessageMask;
	}

	@Override
	public KStrings getTitles() {
		KStrings titles = super.getTitles();
		titles.put("kobjectShortClassName", kobjectShortClassName);
		return titles;
	}

	@Override
	public String getName() {
		return "submitForm";
	}

	public boolean isValidation() {
		return validation;
	}

	public void setValidation(boolean validation) {
		this.validation = validation;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.removeAttribute(result, "targetURL");
		XmlUtils.setAttribute(result, "validation", validation + "");
		XmlUtils.setAttribute(result, "virtualURL", virtualURL);
		XmlUtils.setAttribute(result, "condition", oCondition);
		XmlUtils.removeAttribute(result, "targetParams");
		XmlUtils.setAttribute(result, "targetParams", oTargetParams);
		XmlUtils.setAttribute(result, "kobjectShortClassName", kobjectShortClassName);
		if (!oErrorMessageMask.equals(errorMessageMask))
			XmlUtils.setAttribute(result, "errorMessageMask", errorMessageMask);
		if (!oUpdateMessageMask.equals(updateMessageMask))
			XmlUtils.setAttribute(result, "updateMessageMask", updateMessageMask);
		if (!oNoUpdateMessageMask.equals(noUpdateMessageMask))
			XmlUtils.setAttribute(result, "noUpdateMessageMask", noUpdateMessageMask);
		return result;
	}

	public String getVirtualURL() {
		return virtualURL;
	}

	public void setVirtualURL(String virtualURL) {
		this.virtualURL = virtualURL;
	}

	public String getoCondition() {
		return oCondition;
	}

	public void setoCondition(String oCondition) {
		this.oCondition = oCondition;
	}

	@Override
	public void setTargetURL(String virtualURL) {
		if (KString.isNotNull(virtualURL))
			targetURL = virtualURL;
		else
			targetURL = "/submitForm.frm";
	}

	@Override
	public String getDisplayCaption() {
		String _targetId = KString.isNull(targetId) ? "" : "->[" + getDOMSelector() + "]";
		String _targetURL = KString.isNull(virtualURL) ? "" : virtualURL;
		String _op = "(" + (KString.isNotNull(formName) ? formName + "." : "") + method + (KString.isNull(kobjectShortClassName) ? "" : ":" + kobjectShortClassName) + ")";
		return _targetURL + _op + _targetId;
	}

	public String getoTargetParams() {
		return oTargetParams;
	}

	public void setoTargetParams(String oTargetParams) {
		if (KString.isNotNull(oTargetParams))
			oTargetParams = oTargetParams.replace("&", ",");
		this.oTargetParams = oTargetParams;
	}
}
