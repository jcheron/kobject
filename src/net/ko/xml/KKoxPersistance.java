package net.ko.xml;

import java.util.Map;

import net.ko.controller.KObjectController;
import net.ko.controller.KXmlControllers;
import net.ko.utils.KXmlFile;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;

public class KKoxPersistance {
	private KXmlFile xmlFile;
	private KXmlControllers xmlControllers;

	public KKoxPersistance(KXmlControllers xmlControllers) {
		super();
		this.xmlControllers = xmlControllers;
		xmlFile = xmlControllers.getXmlFile();
	}

	public void saveAs(String fileName) {
		saveControllers();
		xmlFile.saveAs(fileName);
	}

	private void saveControllers() {
		Element controllersElement = XmlUtils.getFirstNodeByTagName(xmlFile.getDocument(), "controllers");
		XmlUtils.removeChilds(controllersElement);
		for (Map.Entry<String, KObjectController> entry : xmlControllers.getKobjectControllers().entrySet()) {
			entry.getValue().saveAsXML(controllersElement);
		}
	}
}
