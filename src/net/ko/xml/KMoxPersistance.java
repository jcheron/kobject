package net.ko.xml;

import net.ko.mapping.KAbstractFilterMapping;
import net.ko.mapping.KXmlMappings;
import net.ko.utils.KXmlFile;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;

public class KMoxPersistance {
	private KXmlFile xmlFile;
	private KXmlMappings xmlMappings;

	public KMoxPersistance(KXmlMappings xmlMappings) {
		super();
		this.xmlMappings = xmlMappings;
		xmlFile = xmlMappings.getXmlFile();
	}

	private void saveMappings() {
		Element mappingsElement = xmlMappings.getMappingsNode();
		XmlUtils.removeChilds(mappingsElement);
		for (KAbstractFilterMapping mapping : xmlMappings.getMappings().getItems())
			mapping.saveAsXML(mappingsElement);
	}

	private void saveFilters() {
		Element filtersElement = xmlMappings.getFiltersNode();
		XmlUtils.removeChilds(filtersElement);
		for (KAbstractFilterMapping mapping : xmlMappings.getFilters().getItems())
			mapping.saveAsXML(filtersElement);
	}

	private void saveAjaxIncludes() {
		Element ajaxIncludesElement = xmlMappings.getAjaxIncludesNode();
		XmlUtils.removeChilds(ajaxIncludesElement);
		for (KAbstractFilterMapping mapping : xmlMappings.getAjaxIncludes().getItems())
			mapping.saveAsXML(ajaxIncludesElement);
	}

	public void saveAs(String fileName) {
		saveMappings();
		saveFilters();
		saveAjaxIncludes();
		xmlFile.saveAs(fileName);
	}

	public void save() {
		if (xmlFile.getFileName() != null)
			saveAs(xmlFile.getFileName());
	}
}
