package net.ko.mapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
import net.ko.http.objects.KResponse;
import net.ko.utils.KHash;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KMapping extends KFilterMapping {
	protected String method;
	protected boolean mainControl;
	protected String tmpClassControl;
	protected String classControl;
	protected String defaultTargetId;
	protected Map<String, Object> parameters;

	public KMapping() {
		this("", "", "", false, "", "");
	}

	public KMapping(String method, String requestURL, String responseURL, boolean mainControl, String classControl, String queryString) {
		super(requestURL, responseURL);
		this.method = method.toUpperCase();
		this.mainControl = mainControl;
		this.classControl = classControl;
		this.parameters = new HashMap<>();
		this.queryString = queryString;
		this.defaultTargetId = "";
		tmpClassControl = "";
	}

	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public boolean isMainControl() {
		return mainControl;
	}

	public void setMainControl(boolean mainControl) {
		if (this.mainControl != mainControl) {
			if (mainControl) {
				if (KString.isNotNull(classControl)) {
					tmpClassControl = this.classControl;
					classControl = "";
				}
			} else {
				if (KString.isNotNull(tmpClassControl)) {
					classControl = tmpClassControl;
					tmpClassControl = "";
				}
			}
			this.mainControl = mainControl;
		}
	}

	public String getClassControl() {
		return classControl;
	}

	public void setClassControl(String classControl) {
		if (KString.isNotNull(classControl))
			mainControl = false;
		this.classControl = classControl;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		String method = ((Element) item).getAttribute("method");
		if (method != null && !"*".equals(method))
			this.method = method;

		String requestURL = ((Element) item).getAttribute("requestURL");
		if (requestURL != null)
			this.requestURL = requestURL;

		String responseURL = ((Element) item).getAttribute("responseURL");
		if (responseURL != null)
			this.responseURL = responseURL;

		boolean mainControl = KString.isBooleanTrue(((Element) item).getAttribute("mainControl"));
		this.mainControl = mainControl;

		String classControl = ((Element) item).getAttribute("classControl");
		if (classControl != null)
			this.classControl = classControl;

		String queryString = ((Element) item).getAttribute("queryString");
		if (queryString != null)
			this.queryString = queryString.replace(",", "&");

		String defaultTargetId = ((Element) item).getAttribute("defaultTargetId");
		if (defaultTargetId != null)
			this.defaultTargetId = defaultTargetId;

		NodeList paramsList = item.getChildNodes();
		for (int i = 0; i < paramsList.getLength(); i++) {
			if (paramsList.item(i).getNodeName().equalsIgnoreCase("parameter")) {
				Element e = (Element) paramsList.item(i);
				String name = e.getAttribute("name");
				String value = e.getAttribute("value");
				if (name != null & !"".equals(name))
					parameters.put(name, value);
			}
		}

	}

	public boolean methodMatches(HttpServletRequest request) {
		boolean result = true;
		if (method != null && !"".equals(method) && !"*".equals(method)) {
			result = method.equalsIgnoreCase(request.getMethod());
		}
		return result;
	}

	private boolean paramMatches(String name, String value, HttpServletRequest request) {
		boolean result = true;
		result = request.getParameterMap().containsKey(name);
		if (result && value != null && !"".equals(value))
			result = value.equals(request.getParameter(name));
		return result;
	}

	public boolean paramsMatches(HttpServletRequest request) {
		boolean result = true;
		if (parameters.size() > 0) {
			for (Map.Entry<String, Object> e : parameters.entrySet()) {
				String name = e.getKey();
				String value = e.getValue() + "";
				result = paramMatches(name, value, request);
				if (!result)
					break;
			}
		}
		return result;
	}

	@Override
	public boolean matches(HttpServletRequest request) {
		boolean result = methodMatches(request) && requestURLMatches(request, false) && paramsMatches(request);
		return result;
	}

	@SuppressWarnings("unchecked")
	private IMappingControl getSelfControl() {
		IMappingControl result = null;
		try {
			Class<IMappingControl> clazz = (Class<IMappingControl>) Class.forName(classControl);
			result = clazz.newInstance();
		} catch (Exception e) {
		}
		return result;
	}

	public IMappingControl getMappingControl() {
		IMappingControl result = null;
		if (mainControl)
			result = Ko.kcontrolInstance();
		if (classControl != null && !"".equals(classControl)) {
			result = getSelfControl();
		}
		return result;
	}

	@Override
	public boolean execute(HttpServletRequest request, HttpServletResponse response) {
		boolean result = true;
		IMappingControl mc = getMappingControl();
		if (mc != null) {
			boolean cont = mc.beforeProcessAction(request, response);
			if (cont) {
				if (mc.isValid(request, response))
					result = process(request, response);
				else {
					KDebugConsole.print("map:" + request.getServletPath() + "->invalidControl", "MAPPING", "KMapping.execute");
					request.removeAttribute("ajaxIncludes");
					mc.onInvalidControl(request, response);
				}
			}
		} else
			result = process(request, response);
		return result;
	}

	private String getResponseURL(HttpServletRequest request) {
		String result = responseURL;
		if (responseURL.contains("$")) {
			Pattern p = Pattern.compile(getRequestRegExpr(), 0);
			Matcher m = p.matcher(request.getServletPath());
			result = m.replaceAll(responseURL);
		}
		return result;
	}

	private boolean process(HttpServletRequest request, HttpServletResponse response) {
		boolean result = true;
		String responseURL = getResponseURL(request);
		String queryString = this.queryString;
		if (Ko.koUseRequestVarsInMox) {
			responseURL = KoHttp.replaceRequestExpressions(responseURL, request);
			queryString = KoHttp.replaceRequestExpressions(queryString, request);
		}
		try {
			if (defaultTargetId != null && !"".equals(defaultTargetId)) {
				String HtmlResponse = KRequest.includeResponse(responseURL, request, response, queryString);
				// HtmlResponse = KString.HtmlQuote(HtmlResponse);
				HtmlResponse = KString.encodeURIComponent(HtmlResponse);
				PrintWriter out = KResponse.getOut(response);
				out.print("<script type='text/javascript'>Forms.Utils.setInnerHTML($(\"" + defaultTargetId + "\"),decodeURIComponent(\"" + HtmlResponse + "\"));</script>");
				KDebugConsole.print("map:" + request.getServletPath() + "->" + responseURL + "?" + queryString + ": #" + defaultTargetId, "MAPPING", "KMapping.process");
			} else
			{
				if (queryString != null && !"".equals(queryString)) {
					KDebugConsole.print("map:" + request.getServletPath() + "->" + responseURL + "?" + queryString, "MAPPING", "KMapping.process");
					KRequest.forward(responseURL, request, response, queryString);
				}
				else {
					KDebugConsole.print("map:" + request.getServletPath() + "->" + responseURL, "MAPPING", "KMapping.process");
					request.getRequestDispatcher(responseURL).forward(request, response);
				}
			}
		} catch (ServletException | IOException e) {
			result = false;
		}
		return result;
	}

	@Override
	public String display(int niveau) {
		String strQueryString = "";
		if (KString.isNotNull(queryString))
			strQueryString = "?" + queryString;
		String strDefaultTargetId = "";
		if (KString.isNotNull(defaultTargetId))
			strDefaultTargetId = "[#" + defaultTargetId + "]";
		String strInitialParams = "";
		if (parameters.size() > 0)
			strInitialParams = "?" + KStrings.implode_param(parameters, "&", "", "=", KGlueMode.KEY_AND_VALUE, false);
		String result = "<div title='method: " + method + "' style='margin-left:" + (20 * niveau) + "px;' class='" + getClass().getSimpleName() + "'>" + requestURL + strInitialParams + " -> " + responseURL + strQueryString + strDefaultTargetId + "</div>";
		return result;
	}

	public String getDefaultTargetId() {
		return defaultTargetId;
	}

	public void setDefaultTargetId(String defaultTargetId) {
		this.defaultTargetId = defaultTargetId;
	}

	@Override
	public String getUniqueIdHash(int ecart) {
		String strInitialParams = "";
		if (parameters.size() > 0)
			strInitialParams = "?" + KStrings.implode_param(parameters, "&", "", "=", KGlueMode.KEY_AND_VALUE, false);
		return KHash.getMD5(ecart + requestURL + strInitialParams + getClass().getSimpleName());
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Document document = parentElement.getOwnerDocument();
		Element node = document.createElement(getName());
		XmlUtils.setAttribute(node, "classControl", classControl);
		XmlUtils.setAttribute(node, "defaultTargetId", defaultTargetId);
		if (mainControl)
			XmlUtils.setAttribute(node, "mainControl", mainControl + "");
		XmlUtils.setAttribute(node, "method", method);
		XmlUtils.setAttribute(node, "queryString", queryString.replace("&", ","));
		XmlUtils.setAttribute(node, "responseURL", responseURL);
		XmlUtils.setAttribute(node, "requestURL", requestURL);
		parentElement.appendChild(node);
		for (Map.Entry<String, Object> e : parameters.entrySet()) {
			Element nodeParam = document.createElement("parameter");
			XmlUtils.setAttribute(nodeParam, "value", e.getValue() + "");
			XmlUtils.setAttribute(nodeParam, "name", e.getKey());
			node.appendChild(nodeParam);
		}
		return node;
	}

	@Override
	public String getName() {
		return "mapping";
	}

	public String getRequestParameters() {
		String strInitialParams = "";
		if (parameters.size() > 0)
			strInitialParams = "?" + KStrings.implode_param(parameters, "&", "", "=", KGlueMode.KEY_AND_VALUE, false);
		return strInitialParams;
	}
}
