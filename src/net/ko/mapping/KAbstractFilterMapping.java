package net.ko.mapping;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.utils.KRegExpr;
import net.ko.utils.KString;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class KAbstractFilterMapping implements IHasParentObject,
		Cloneable {
	protected String requestURL;
	protected String queryString;
	protected String out = "";
	protected Object parentObject;
	protected Node item;

	public void initFromXml(Node item) {
		this.item = item;
	}

	public String getRequestRegExpr() {
		String regExpr = requestURL;
		String m = Pattern.quote(requestURL);
		ArrayList<String> strs = KRegExpr.getGroups("\\{\\#((?:\\s|.)+?)\\#\\}", m, 1);
		for (String s : strs) {
			regExpr = regExpr.replace("{#" + s + "#}", "#sep#");
		}
		String[] parts = regExpr.split("#sep#");
		regExpr = "";
		for (int i = 0; i < parts.length; i++) {
			regExpr += Pattern.quote(parts[i]);
			if (strs.size() > i)
				regExpr += strs.get(i);
		}
		return regExpr;
	}

	public KAbstractFilterMapping(String requestURL) {
		super();
		this.requestURL = requestURL;
	}

	public boolean requestURLMatches(HttpServletRequest request, boolean forward) {
		boolean result = true;
		if (requestURL != null && !"".equals(requestURL)) {
			String realRequestURL = request.getServletPath();
			;
			if (request.getAttribute("javax.servlet.forward.servlet_path") != null && forward) {
				realRequestURL = request.getAttribute("javax.servlet.forward.servlet_path") + "";
			}
			Pattern p = Pattern.compile(getRequestRegExpr(), Pattern.CASE_INSENSITIVE);
			realRequestURL = realRequestURL.replace(request.getServletContext().getContextPath(), "");
			if (!requestURL.contains("/"))
				realRequestURL = realRequestURL.substring(realRequestURL.lastIndexOf("/") + 1);
			Matcher mat = p.matcher(realRequestURL);
			result = mat.matches();
		}
		return result;
	}

	public boolean requestURLMatches(String url, HttpServletRequest request) {
		boolean result = true;
		if (requestURL != null && !"".equals(requestURL)) {
			String realRequestURL = getRealUrl(url, request);
			Pattern p = Pattern.compile(getRequestRegExpr(), Pattern.CASE_INSENSITIVE);
			if (!requestURL.contains("/"))
				realRequestURL = realRequestURL.substring(realRequestURL.lastIndexOf("/") + 1);
			Matcher mat = p.matcher(realRequestURL);
			result = mat.matches();
		}
		return result;
	}

	public boolean requestURLMatches(String url) {
		boolean result = true;
		if (requestURL != null && !"".equals(requestURL)) {
			Pattern p = Pattern.compile(getRequestRegExpr(), Pattern.CASE_INSENSITIVE);
			if (!requestURL.contains("/"))
				url = url.substring(url.lastIndexOf("/") + 1);
			Matcher mat = p.matcher(url);
			result = mat.matches();
		}
		return result;
	}

	public String getRealUrl(String url, HttpServletRequest request) {
		String result = "";
		if (url != null)
			result = url.replace(request.getServletContext().getContextPath(), "");
		return result;
	}

	public abstract boolean matches(HttpServletRequest request);

	public boolean matches(String url, HttpServletRequest request) {
		return requestURLMatches(url, request);
	}

	public abstract String display(int niveau);

	public abstract String displayRecursive();

	public abstract boolean execute(HttpServletRequest request, HttpServletResponse response);

	public abstract boolean executeByName(String name);

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		if (KString.isNotNull(queryString))
			queryString = queryString.replace("&", ",");
		this.queryString = queryString;
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public void addOut(String out) {
		this.out += out;
	}

	@Override
	public Object getParentObject() {
		return parentObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IHasParentObject> T getParentObject(Class<T> clazz) {
		Object parent = getParentObject();
		if (parent != null) {
			if (parent.getClass().equals(clazz))
				return (T) parent;
			else
				return ((T) parent).getParentObject(clazz);
		}
		return null;
	}

	@Override
	public void setParentObject(Object parentObject) {
		this.parentObject = parentObject;
	}

	public Node getItem() {
		return item;
	}

	public void setItem(Node item) {
		this.item = item;
	}

	public abstract String getUniqueIdHash(int ecart);

	@Override
	public boolean addElement(IAjaxObject element) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeElement(IAjaxObject element) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(getClass()))
			return ((KAbstractFilterMapping) obj).getUniqueIdHash(0).equals(getUniqueIdHash(0));
		return false;
	}

	@Override
	public int hashCode() {
		return getUniqueIdHash(0).hashCode();
	}

	public abstract Element saveAsXML(Element parentElement);

	public abstract String getName();

	public Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace(System.err);
		}
		return o;
	}
}
