package net.ko.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XmlUtils {
	public static String getAttribute(Element e, String name) {
		return e.getAttribute(name);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAttribute(Element e, String name, T defaultValue) {
		T result;
		String p = e.getAttribute(name);
		if (KString.isNull(p))
			result = defaultValue;
		else {
			try {
				result = (T) e.getAttribute(name);
			} catch (Exception ex) {
				result = defaultValue;
			}
		}
		return result;
	}

	public static void setAttribute(Element node, String name, String value) {
		if (KString.isNotNull(value))
			node.setAttribute(name, value);
	}

	public static void removeAttribute(Element node, String name) {
		node.removeAttribute(name);
	}

	public static Element getFirstNodeByTagName(Document document, String tagName) {
		NodeList nodeList = document.getElementsByTagName(tagName);
		if (nodeList.getLength() > 0)
			return (Element) nodeList.item(0);
		return null;
	}

	public static void removeChilds(Element e) {
		while (e.hasChildNodes())
			e.removeChild(e.getFirstChild());
	}
}
