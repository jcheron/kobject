package net.ko.mapping;

import net.ko.framework.Ko;
import net.ko.mapping.utils.MappingInterval;
import net.ko.types.KStringsType;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxInclude extends KAjaxWithChilds implements IHasURL,
		IIsAccordionCompatible, IHasTransition, IHasSelector {
	protected String method;
	protected String targetId;
	protected String targetURL;
	protected String targetParams;
	protected String formName;
	protected String condition;
	protected KStrings parameters;
	protected int timeout = -1;
	protected int timein = -1;
	protected String interval;
	protected boolean alwaysValid;
	protected String scriptToAdd;
	protected String title;
	protected String transition;

	public KAjaxInclude() {
		super();
		this.condition = "";
		alwaysValid = true;
		scriptToAdd = "";
		transition = "";
		targetParams = "";
		interval = "";
	}

	protected KAjaxInclude(String targetId, String targetURL) {
		this(targetId, targetURL, "get", "", "", "");
	}

	public KAjaxInclude(String targetId, String targetURL, String method, String targetParams, String targetFunction, String formName) {
		this();
		this.targetId = targetId;
		this.targetURL = targetURL;
		setMethod(method);
		this.targetParams = targetParams.replace(",", "&");
		this.targetFunction = targetFunction;
		this.formName = formName;
		parameters = new KStrings(this.targetParams, KStringsType.kstQueryString);
		transition = "";
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		if (method == null || "".equals(method)) {
			if ((formName != null && !"".equals(formName)) && !getClass().equals(KAjaxIncludeForm.class))
				method = "post";
			else
				method = "get";
		}
		this.method = method.toLowerCase();
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getTargetURL() {
		return targetURL;
	}

	public void setTargetURL(String targetURL) {
		this.targetURL = targetURL;
	}

	public String getTargetParams() {
		return targetParams;
	}

	public void setTargetParams(String targetParams) {
		this.targetParams = targetParams;
		parameters = new KStrings(this.targetParams, KStringsType.kstQueryString);
	}

	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}

	public String getTargetFunction() {
		return targetFunction;
	}

	public void setTargetFunction(String targetFunction) {
		this.targetFunction = targetFunction;
	}

	@Override
	public boolean isValid() {
		boolean result = true;
		result = result && (KString.isNotNull(targetURL) || !(this.getClass().equals(KAjaxInclude.class)));
		return result;
	}

	public String createParameters() {
		String paramStr = parameters.implode();
		paramStr = paramStr.replaceAll("\\+", "_plus_");
		paramStr = KString.urlDecode(paramStr);
		paramStr = paramStr.replaceAll("_plus_", "\\+");
		paramStr = quoteParams(paramStr);
		paramStr = replaceJsExpressions(paramStr);
		String sep = "";
		if (!"".equals(paramStr))
			sep = "&";
		paramStr = "'" + paramStr + sep + "'+$qs(this)";
		return paramStr;
	}

	@Override
	public String createMyOwnFunction(String childFunctions) {
		String paramStr = createParameters();
		String strTimeout = "";

		String start = "(";

		String result = "$" + method.toLowerCase() + start + "'" + targetId + "','" + targetURL + "'," + paramStr;
		if (formName != null && !"".equals(formName))
			result = "$" + method.toLowerCase() + "Form" + start + "'" + formName + "','" + targetId + "','" + targetURL + "'," + paramStr;
		if (!"".equals(childFunctions))
			result += "," + childFunctions;
		result = result + ");" + strTimeout;

		if (timein > 1) {
			result = "setTimeout(function(){" + result + "}," + timein + ");";
		}
		result += scriptToAdd;

		if (condition != null && !"".equals(condition))
			result = "if(" + condition + "){" + result + "}";

		if (KString.isNotNull(interval)) {
			MappingInterval inter = new MappingInterval(interval);
			if (inter.isValid())
				result = inter.getName() + "=window.setInterval(function(){" + result + "}," + inter.getInterval() + ");";
		}
		return result;
	}

	public String createFunction(String childFunctions) {
		String result = createMyOwnFunction(getFunctions() + childFunctions);
		result = addDeboggerExpression(result, getName() + ":" + getDebugClientId() + "->" + targetURL, createMyOwnFunction(""));
		return result;
	}

	@Override
	public String createFunction() {
		String result = createMyOwnFunction(getFunctions());
		result = addDeboggerExpression(result, getName() + ":" + getDebugClientId() + "->" + targetURL, createMyOwnFunction(""));
		return result;
	}

	@Override
	public String toString() {
		String result = createFunction();
		if (isChild)
			result = getPreludeFunction() + result + "}";
		return result;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		this.targetId = e.getAttribute("targetId");
		this.targetURL = e.getAttribute("targetURL");
		String targetParams = e.getAttribute("targetParams");
		if (targetParams != null)
			this.targetParams = targetParams.replace(",", "&");
		this.condition = e.getAttribute("condition");
		this.targetFunction = e.getAttribute("targetFunction");
		this.interval = e.getAttribute("interval");
		this.formName = e.getAttribute("formName");
		this.title = e.getAttribute("title");
		this.transition = e.getAttribute("transition");
		parameters = new KStrings(this.targetParams, KStringsType.kstQueryString);
		setMethod(e.getAttribute("method"));
		String strTimeout = e.getAttribute("timeout");
		try {
			timeout = Integer.valueOf(strTimeout);
		} catch (Exception ex) {
			timeout = -1;
		}
		String strTimein = e.getAttribute("timein");
		try {
			timein = Integer.valueOf(strTimein);
		} catch (Exception ex) {
			timein = -1;
		}
		childs = KAjaxJs.getChilds(this, item, true, isInRootFolder);
		if (timeout > -1) {
			KAjaxMessage mess = new KAjaxMessage();
			mess.setMessage("''");
			mess.setTargetId(targetId);
			mess.setTimein(timeout);
			mess.setChild(true);
			addChild(mess);
		}
		boolean breakPoint = KString.isBooleanTrue(e.getAttribute("break"));
		setBreakPoint(breakPoint);
	}

	@Override
	public String getURL() {
		return targetURL;
	}

	@Override
	public String getDisplayCaption() {
		String result = "";
		if (KString.isNotNull(title))
			result = "{" + title + "}";
		result += getTargetURL();
		if (KString.isNotNull(getDOMSelector()))
			result += "->[" + getDOMSelector() + "]";
		return result;
	}

	@Override
	public String getDisplayStructure() {
		String postFrm = "";
		String getParams = "()";
		if (formName != null && !"".equals(formName))
			postFrm = "[" + formName + "]";
		if (targetParams != null && !"".equals(targetParams))
			getParams = "(" + targetParams + ")";
		return "<fieldset class='targetId'><legend>{caption}</legend><span class='targetUrl'>" + targetURL + "</span>-><span class='method'>" + getMethod() + postFrm + getParams + "</span></fieldset>";
	}

	@Override
	public KStrings getTitles() {
		KStrings titles = super.getTitles();
		String responseURL = "";
		KMapping mapping = (KMapping) Ko.kmappings().getFirstMatches(targetURL);
		if (mapping != null) {
			responseURL = mapping.getResponseURL();
		}
		titles.put("targetURL", KString.htmlSpecialChars(targetURL) + "|" + responseURL);
		if (KString.isNotNull(formName))
			titles.put("formName", formName);
		titles.put("method", method);
		if (KString.isNotNull(condition))
			titles.put("condition", KString.htmlSpecialChars(condition));
		return titles;
	}

	public String getFormName() {
		return formName;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	@Override
	public String getDebugClientId() {
		return "#" + targetId;
	}

	@Override
	public String getName() {
		return "include";
	}

	public void addScript(String script) {
		scriptToAdd += script;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getTimein() {
		return timein;
	}

	public void setTimein(int timein) {
		this.timein = timein;
	}

	public String getTransition() {
		return transition;
	}

	public void setTransition(String transition) {
		this.transition = transition;
	}

	@Override
	public String getTransitionId() {
		return targetId;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "targetFunction", targetFunction);
		XmlUtils.setAttribute(result, "transition", transition);
		XmlUtils.setAttribute(result, "formName", formName);
		XmlUtils.setAttribute(result, "condition", condition);
		if (method != null && !"get".equalsIgnoreCase(method))
			XmlUtils.setAttribute(result, "method", method.toUpperCase());
		if (timein != -1)
			XmlUtils.setAttribute(result, "timein", timein + "");
		if (timeout != -1)
			XmlUtils.setAttribute(result, "timeout", timeout + "");
		if (new MappingInterval(interval).isValid()) {
			XmlUtils.setAttribute(result, "interval", interval);
		}
		XmlUtils.setAttribute(result, "title", title);
		XmlUtils.setAttribute(result, "targetParams", targetParams.replace("&", ","));
		XmlUtils.setAttribute(result, "targetId", targetId);
		XmlUtils.setAttribute(result, "targetURL", targetURL);
		return result;
	}

	@Override
	public String getDOMSelector() {
		if (KString.isNotNull(targetId))
			return "#" + targetId;
		return null;
	}

	public String getScriptToAdd() {
		return scriptToAdd;
	}

	public void setScriptToAdd(String scriptToAdd) {
		this.scriptToAdd = scriptToAdd;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}
}
