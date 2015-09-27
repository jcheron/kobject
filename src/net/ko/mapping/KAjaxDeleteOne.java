package net.ko.mapping;

import net.ko.types.KStringsType;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxDeleteOne extends KAjaxInclude {
	protected String kobjectShortClassName;
	protected String updateMessageMask;
	protected String errorMessageMask;
	protected String oUpdateMessageMask = "{toString} supprimé";
	protected String oErrorMessageMask = "Erreur de suppression";
	protected String oTargetParams;
	protected String virtualURL;
	protected String keyValues;

	public KAjaxDeleteOne(String virtualURL) {
		this();
		setTargetURL(virtualURL);
	}

	public KAjaxDeleteOne() {
		super();
		updateMessageMask = oUpdateMessageMask;
		errorMessageMask = oErrorMessageMask;
		oTargetParams = "";
	}

	protected String getDefaultJsValues() {
		return "{js:((($('list-" + kobjectShortClassName + "').selector)?$('list-" + kobjectShortClassName + "').selector.getValue():false) || (new $e('#list-" + kobjectShortClassName + " input.checkable:checked').getValuesAsString()) || (new $e('#list-" + kobjectShortClassName + " tr:hover input[type=hidden]').getValuesAsString()))}";
	}

	protected void putKeyValuesInParameter(String keyValues) {
		if (KString.isNull(keyValues)) {
			keyValues = getDefaultJsValues();
		}
		parameters.put("_keyValues", keyValues);
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;

		virtualURL = e.getAttribute("virtualURL");
		setTargetURL(virtualURL);
		this.kobjectShortClassName = e.getAttribute("kobjectShortClassName");
		if (kobjectShortClassName != null && !"".equals(kobjectShortClassName)) {
			oTargetParams = e.getAttribute("targetParams");
			if (oTargetParams != null)
				this.targetParams = oTargetParams.replace(",", "&");
			parameters = new KStrings(this.targetParams, KStringsType.kstQueryString);
			keyValues = e.getAttribute("keyValues");
			putKeyValuesInParameter(keyValues);
			parameters.put("_cls", kobjectShortClassName);
		}
		String _updateMessageMask = KString.urlEncode(e.getAttribute("updateMessageMask"));
		if (_updateMessageMask != null && !"".equals(_updateMessageMask))
			updateMessageMask = _updateMessageMask;

		String _errorMessageMask = KString.urlEncode(e.getAttribute("errorMessageMask"));
		if (_errorMessageMask != null && !"".equals(_errorMessageMask))
			errorMessageMask = _errorMessageMask;
		parameters.put("_uMM", KString.urlEncode(updateMessageMask));
		parameters.put("_eMM", KString.urlEncode(errorMessageMask));
		setMethod(e.getAttribute("method"));
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

	public String getupdateMessageMask() {
		return updateMessageMask;
	}

	public void setupdateMessageMask(String updateMessageMask) {
		this.updateMessageMask = updateMessageMask;
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
		return "deleteOne";
	}

	@Override
	public void setMethod(String method) {
		if (method == null || "".equals(method)) {
			method = "post";
		}
		this.method = method.toLowerCase();
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		result.removeAttribute("targetURL");
		XmlUtils.setAttribute(result, "targetId", targetId);
		XmlUtils.setAttribute(result, "virtualURL", virtualURL);
		XmlUtils.setAttribute(result, "targetParams", oTargetParams.replace("&", ","));
		XmlUtils.setAttribute(result, "targetFunction", targetFunction);
		XmlUtils.setAttribute(result, "transition", transition);
		XmlUtils.setAttribute(result, "formName", formName);
		XmlUtils.setAttribute(result, "condition", condition);
		if (method != null)
			XmlUtils.setAttribute(result, "method", method.toUpperCase());
		XmlUtils.setAttribute(result, "keyValues", keyValues);
		if (timein != -1)
			XmlUtils.setAttribute(result, "timein", timein + "");
		if (timeout != -1)
			XmlUtils.setAttribute(result, "timeout", timeout + "");
		XmlUtils.setAttribute(result, "title", title);
		XmlUtils.setAttribute(result, "kobjectShortClassName", kobjectShortClassName);
		if (!oErrorMessageMask.equals(errorMessageMask))
			XmlUtils.setAttribute(result, "errorMessageMask", errorMessageMask);
		if (!oUpdateMessageMask.equals(updateMessageMask))
			XmlUtils.setAttribute(result, "updateMessageMask", updateMessageMask);
		return result;
	}

	public String getUpdateMessageMask() {
		return updateMessageMask;
	}

	public void setUpdateMessageMask(String updateMessageMask) {
		this.updateMessageMask = updateMessageMask;
	}

	public String getKeyValues() {
		return keyValues;
	}

	public void setKeyValues(String keyValues) {
		this.keyValues = keyValues;
	}

	@Override
	public String getDisplayCaption() {
		String _targetId = KString.isNull(targetId) ? "" : "->[" + getDOMSelector() + "]";
		String _targetURL = KString.isNull(virtualURL) ? "" : virtualURL;
		String _op = "(delete" + (KString.isNull(kobjectShortClassName) ? "" : ":" + kobjectShortClassName) + ")";
		return _targetURL + _op + _targetId;
	}

	@Override
	public void setTargetURL(String virtualURL) {
		if (KString.isNotNull(virtualURL))
			targetURL = virtualURL;
		else
			targetURL = "/deleteOne.frm";
	}

	public void setVirtualURL(String virtualURL) {
		this.virtualURL = virtualURL;
	}

	public String getVirtualURL() {
		return virtualURL;
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
