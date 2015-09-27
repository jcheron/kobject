package net.ko.controller;

import org.w3c.dom.Element;

public interface IKoController {
	public String getDisplayStructure(boolean checkBoxes);

	public String getUniqueIdHash();

	public Element saveAsXML(Element parentElement);
}
