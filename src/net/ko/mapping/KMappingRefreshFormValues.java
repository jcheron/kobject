package net.ko.mapping;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KMappingRefreshFormValues extends KMapping {
	public KMappingRefreshFormValues() {
		super();
		responseURL = "/KSRefreshFormValues.frm";
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		responseURL = "/KSRefreshFormValues.frm";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KMappingRefreshFormValues)
			if (requestURL != null)
				return requestURL.equals(((KMappingRefreshFormValues) obj).getRequestURL());
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return requestURL != null ? requestURL.hashCode() : 0;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		return parentElement;
	}
}
