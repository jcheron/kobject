package net.ko.mapping;

import net.ko.types.KStringsType;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxRefreshFormValues extends KAjaxInclude {
	protected String className;
	protected String keyValues;
	protected String excludedFields;

	public KAjaxRefreshFormValues() {
		this("", "");
	}

	public KAjaxRefreshFormValues(String targetId, String targetURL) {
		super(targetId, targetURL);
		className = "";
		keyValues = "";
		excludedFields = "";
	}

	@Override
	public boolean isValid() {
		return KString.isNotNull(keyValues) && KString.isNotNull(className);
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		className = e.getAttribute("kobjectShortClassName");
		keyValues = e.getAttribute("keyValues");
		excludedFields = e.getAttribute("excludedFields");
		condition = e.getAttribute("condition");
		targetURL = e.getAttribute("virtualURL");
		targetId = "";
		parameters = new KStrings(KStringsType.kstQueryString);
		parameters.put("_className", className);
		parameters.put("_keyValues", KString.urlEncode(keyValues));
		parameters.put("_excludedFields", KString.urlEncode(excludedFields));
		setMethod("get");
		childs = KAjaxJs.getChilds(this, item, true, isInRootFolder);
		boolean breakPoint = KString.isBooleanTrue(e.getAttribute("break"));
		setBreakPoint(breakPoint);
	}

	@Override
	public String getName() {
		return "refreshFormValues";
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		result.removeAttribute("targetURL");
		XmlUtils.setAttribute(result, "excludedFields", excludedFields);
		XmlUtils.setAttribute(result, "kobjectShortClassName", className);
		XmlUtils.setAttribute(result, "keyValues", keyValues);
		XmlUtils.setAttribute(result, "virtualURL", targetURL);
		return result;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getKeyValues() {
		return keyValues;
	}

	public void setKeyValues(String keyValues) {
		this.keyValues = keyValues;
	}

	public String getExcludedFields() {
		return excludedFields;
	}

	public void setExcludedFields(String excludedFields) {
		this.excludedFields = excludedFields;
	}

	@Override
	public String getDisplayCaption() {
		String _targetId = KString.isNull(targetId) ? "" : "->[" + getDOMSelector() + "]";
		String _targetURL = KString.isNull(targetURL) ? "" : targetURL;
		String _op = "(" + (KString.isNull(className) ? "" : className) + (KString.isNull(keyValues) ? "" : "?" + keyValues) + ")";
		return _targetURL + _op + _targetId;
	}
}
