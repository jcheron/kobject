package net.ko.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.http.objects.KRequest;
import net.ko.utils.KHash;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KFilter extends KFilterMapping {
	private ArrayList<KParameter> parameters;

	public KFilter(String requestURL, String responseURL) {
		super(requestURL, responseURL);
		this.queryString = "";
		parameters = new ArrayList<>();
	}

	public KFilter() {
		this("", "");
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		String requestURL = ((Element) item).getAttribute("requestURL");
		if (requestURL != null)
			this.requestURL = requestURL;

		String responseURL = ((Element) item).getAttribute("responseURL");
		if (responseURL != null)
			this.responseURL = responseURL;

		String queryString = ((Element) item).getAttribute("queryString");
		if (queryString != null)
			this.queryString = queryString.replace(",", "&");

		NodeList paramsList = item.getChildNodes();
		for (int i = 0; i < paramsList.getLength(); i++) {
			if (paramsList.item(i).getNodeName().equalsIgnoreCase("parameter")) {
				Element e = (Element) paramsList.item(i);
				String name = e.getAttribute("name");
				String position = e.getAttribute("position");
				if (name != null & !"".equals(name))
					parameters.add(new KParameter(name, position, i + 1));
			}
		}
	}

	@Override
	public boolean requestURLMatches(HttpServletRequest request, boolean forward) {
		boolean result = true;
		if (requestURL != null && !"".equals(requestURL)) {
			Pattern p = Pattern.compile(getRequestRegExpr(), Pattern.CASE_INSENSITIVE);
			String realRequestURL = request.getServletPath();
			if (!requestURL.contains("/"))
				realRequestURL = realRequestURL.substring(realRequestURL.lastIndexOf("/") + 1);
			realRequestURL = Pattern.quote(realRequestURL);
			Matcher mat = p.matcher(realRequestURL);
			if (mat.find()) {
				for (int i = 0; i < parameters.size(); i++) {
					KParameter param = parameters.get(i);
					int position = param.getPosition();
					if (position <= mat.groupCount()) {
						param.setValue(mat.group(position));
					} else {
						result = false;
						break;
					}
				}
			}
			else
				result = false;
		}
		return result;
	}

	@Override
	public boolean matches(HttpServletRequest request) {
		boolean result = requestURLMatches(request, true);
		return result;
	}

	@Override
	public String getQueryString() {
		String result = "";
		for (int i = 0; i < parameters.size() - 1; i++) {
			KParameter param = parameters.get(i);
			result += param.getName() + "=" + param.getValue() + "&";
		}
		if (parameters.size() > 0) {
			KParameter param = parameters.get(parameters.size() - 1);
			result += param.getName() + "=" + param.getValue();
		}
		if (queryString != null && !"".equals(queryString)) {
			if ("".equals(result))
				result = queryString;
			else
				result += "&" + queryString;
		}
		return result;
	}

	private String getResponseUrl(HttpServletRequest request) {
		String result = responseURL;
		if (responseURL.contains("$")) {
			Pattern p = Pattern.compile(getRequestRegExpr(), 0);
			Matcher m = p.matcher(request.getServletPath());
			result = m.replaceAll(responseURL);
		}
		if (result == null || "".equals(result)) {
			String realRequestURL = request.getServletPath();
			result = realRequestURL.substring(realRequestURL.lastIndexOf("/") + 1);
		}
		return result;
	}

	@Override
	public boolean execute(HttpServletRequest request, HttpServletResponse response) {
		boolean result = true;
		String respUrl = "";
		try {
			respUrl = getResponseUrl(request);
			KRequest.forward(respUrl, request, response, getQueryString());
		} catch (ServletException | IOException e) {
			result = false;
			Ko.klogger().log(Level.WARNING, "Impossible d'effectuer le forward vers " + respUrl, e);
		}
		return result;
	}

	public void addParameter(String name, int position) {
		parameters.add(new KParameter(name, position));
	}

	@Override
	public String display(int niveau) {
		return "";
	}

	@Override
	public String getUniqueIdHash(int ecart) {
		return KHash.getMD5(getRequestURL() + getQueryString());
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Document document = parentElement.getOwnerDocument();
		Element node = document.createElement(getName());
		XmlUtils.setAttribute(node, "requestURL", requestURL);
		XmlUtils.setAttribute(node, "responseURL", responseURL);
		XmlUtils.setAttribute(node, "queryString", queryString.replace("&", ","));
		parentElement.appendChild(node);
		for (KParameter parameter : parameters) {
			Element nodeParam = document.createElement("parameter");
			XmlUtils.setAttribute(nodeParam, "name", parameter.getName());
			XmlUtils.setAttribute(nodeParam, "position", (parameter.getPosition()) + "");
			node.appendChild(nodeParam);
		}
		return node;
	}

	@Override
	public String getName() {
		return "filter";
	}

}
