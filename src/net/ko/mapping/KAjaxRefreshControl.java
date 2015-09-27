package net.ko.mapping;

import net.ko.types.KStringsType;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxRefreshControl extends KAjaxObject implements IHasSelector,
		IHasURL {
	private KAjaxInclude ajaxInclude;
	private String name;
	private String className;
	private String controlType;
	private String filterList;
	private String value;
	private String koDisplay;
	private String condition;
	private String virtualURL;
	private String targetParams;

	public KAjaxRefreshControl() {
		super();
		ajaxInclude = new KAjaxInclude();
		name = "";
		className = "";
		controlType = "";
		filterList = "";
		condition = "";
		virtualURL = "";
		koDisplay = "";
		value = "";
		targetParams = "";
	}

	public KAjaxRefreshControl(String virtualURL) {
		this();
		setVirtualURL(virtualURL);
	}

	@Override
	public String createFunction() {
		return ajaxInclude.createFunction();
	}

	@Override
	public boolean isValid() {
		return ajaxInclude.isValid();
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		name = e.getAttribute("name");
		className = e.getAttribute("kobjectShortClassName");
		value = e.getAttribute("value");
		filterList = e.getAttribute("filterList");
		controlType = e.getAttribute("controlType");
		koDisplay = e.getAttribute("koDisplay");
		condition = e.getAttribute("condition");
		virtualURL = e.getAttribute("virtualURL");
		targetParams = e.getAttribute("targetParams");
		ajaxInclude.setTargetId("field-" + name);
		ajaxInclude.setCondition(condition);
		ajaxInclude.setTargetURL(virtualURL);
		String params = "";
		params = addParam(params, "_name", name);
		params = addParam(params, "_className", className);
		params = addParam(params, "_value", value, false);
		params = addParam(params, "_filterList", filterList);
		params = addParam(params, "_controlType", controlType);
		params = addParam(params, "_koDisplay", koDisplay);
		params = addParam(params, "_virtualURL", virtualURL);
		if (KString.isNotNull(targetParams)) {
			KStrings paramsStr = new KStrings(targetParams, KStringsType.kstQueryString);
			for (String s : paramsStr) {
				params = addParam(params, s, paramsStr.get(s) + "");
			}
		}
		ajaxInclude.setTargetParams(params);
		ajaxInclude.setMethod("POST");
		// ajaxInclude.setTargetFunction("function(){Forms.Utils.fireEvent($('"
		// + name + "'),'change');}");
		ajaxInclude.setChilds(KAjaxJs.getChilds(this, item, true, isInRootFolder));
	}

	private String addParam(String params, String paramName, String paramValue) {
		return addParam(params, paramName, paramValue, true);
	}

	private String addParam(String params, String paramName, String paramValue, boolean encoding) {
		String result = params;

		if (paramValue != null && !"".equals(paramValue)) {
			if (encoding)
				paramValue = KString.urlEncode(paramValue);
			if (!"".equals(result))
				result += "&" + paramName + "=" + paramValue;
			else
				result += paramName + "=" + paramValue;
		}
		return result;
	}

	@Override
	public String getDisplayCaption() {
		String _targetId = KString.isNull(name) ? "" : "->[" + getDOMSelector() + "]";
		String _targetURL = KString.isNull(virtualURL) ? "" : virtualURL;
		String _op = "(" + controlType + (KString.isNull(className) ? "" : ":" + className) + ")";
		return _targetURL + _op + _targetId;
	}

	@Override
	public String toString() {
		ajaxInclude.setChild(isChild);
		return ajaxInclude.toString();
	}

	@Override
	public String getDebugClientId() {
		return "#field-" + name;
	}

	@Override
	public String getName() {
		return "refreshControl";
	}

	public String _getName() {
		return name;
	}

	@Override
	public KStrings getTitles() {
		KStrings titles = super.getTitles();
		titles.put("virtualURL", virtualURL);
		titles.put("className", className);
		if (KString.isNotNull(condition))
			titles.put("condition", KString.htmlSpecialChars(condition));
		if (KString.isNotNull(controlType))
			titles.put("controlType", controlType);
		if (KString.isNotNull(filterList))
			titles.put("filterList", filterList);
		if (KString.isNotNull(koDisplay))
			titles.put("koDisplay", koDisplay);
		return titles;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "name", name);
		XmlUtils.setAttribute(result, "kobjectShortClassName", className);
		XmlUtils.setAttribute(result, "condition", condition);
		XmlUtils.setAttribute(result, "controlType", controlType);
		XmlUtils.setAttribute(result, "filterList", filterList);
		XmlUtils.setAttribute(result, "koDisplay", koDisplay);
		XmlUtils.setAttribute(result, "value", value);
		XmlUtils.setAttribute(result, "virtualURL", virtualURL);
		XmlUtils.setAttribute(result, "targetParams", targetParams);
		return result;
	}

	@Override
	public String getDOMSelector() {
		if (KString.isNotNull(name))
			return "#" + name;
		return null;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getControlType() {
		return controlType;
	}

	public void setControlType(String controlType) {
		this.controlType = controlType;
	}

	public String getFilterList() {
		return filterList;
	}

	public void setFilterList(String filterList) {
		this.filterList = filterList;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKoDisplay() {
		return koDisplay;
	}

	public void setKoDisplay(String koDisplay) {
		this.koDisplay = koDisplay;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		ajaxInclude.setCondition(condition);
		this.condition = condition;
	}

	public String getVirtualURL() {
		return virtualURL;
	}

	public void setVirtualURL(String virtualURL) {
		ajaxInclude.setTargetURL(virtualURL);
		this.virtualURL = virtualURL;
	}

	public void setName(String name) {
		ajaxInclude.setTargetId(name);
		this.name = name;
	}

	@Override
	public String getURL() {
		return virtualURL;
	}

	public String getTargetParams() {
		return targetParams;
	}

	public void setTargetParams(String targetParams) {
		this.targetParams = targetParams;
	}
}
