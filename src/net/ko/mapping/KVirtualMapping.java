package net.ko.mapping;

import java.util.Map;

import net.ko.utils.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KVirtualMapping extends KMapping {
	private String mappingFor;

	public KVirtualMapping() {
		super();
	}

	public KVirtualMapping(String method, String requestURL, String mappingFor, boolean mainControl, String classControl, String queryString) {
		super(method, requestURL, "/" + mappingFor + ".frm", mainControl, classControl, queryString);
		this.mappingFor = mappingFor;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		mappingFor = e.getAttribute("mappingFor");
		responseURL = "/" + mappingFor + ".frm";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KMappingSubmitForm)
			if (requestURL != null)
				return requestURL.equals(((KMappingSubmitForm) obj).getRequestURL());
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return requestURL != null ? requestURL.hashCode() : 0;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Document document = parentElement.getOwnerDocument();
		Element node = document.createElement(getName());
		XmlUtils.setAttribute(node, "method", method);
		XmlUtils.setAttribute(node, "requestURL", requestURL);
		XmlUtils.setAttribute(node, "mappingFor", mappingFor);
		XmlUtils.setAttribute(node, "queryString", queryString.replace("&", ","));
		XmlUtils.setAttribute(node, "classControl", classControl);
		XmlUtils.setAttribute(node, "defaultTargetId", defaultTargetId);
		XmlUtils.setAttribute(node, "mainControl", mainControl + "");
		parentElement.appendChild(node);
		for (Map.Entry<String, Object> e : parameters.entrySet()) {
			Element nodeParam = document.createElement("parameter");
			XmlUtils.setAttribute(nodeParam, "name", e.getKey());
			XmlUtils.setAttribute(nodeParam, "value", e.getValue() + "");
			node.appendChild(nodeParam);
		}
		return node;
	}

	@Override
	public String getName() {
		return "virtualMapping";
	}

	public String getMappingFor() {
		return mappingFor;
	}

	public void setMappingFor(String mappingFor) {
		this.mappingFor = mappingFor;
	}
}
