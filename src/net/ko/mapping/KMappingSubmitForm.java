package net.ko.mapping;

import org.w3c.dom.Node;

public class KMappingSubmitForm extends KMapping {

	public KMappingSubmitForm() {
		super();
		responseURL = "/KSSubmitForm.frm";
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		responseURL = "/KSSubmitForm.frm";
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

}
