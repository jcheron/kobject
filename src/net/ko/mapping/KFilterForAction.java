package net.ko.mapping;

import org.w3c.dom.Element;

public class KFilterForAction extends KFilter {

	public KFilterForAction(String actName) {
		super("{#.*?/(.+?)/(.+?)\\b(.*?)#}/" + actName, "/$1/" + actName);
		this.addParameter("_action", 2);
		this.addParameter("_parameters", 3);
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		return parentElement;
	}

}
