package net.ko.creator;

import javax.xml.parsers.ParserConfigurationException;

import net.ko.db.KDataTypeConverter;
import net.ko.db.KDbField;
import net.ko.utils.KFileUtils;
import net.ko.utils.KXmlFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class KControllerCreator {
	private KXmlFile xmlFile;

	public KControllerCreator() {
		xmlFile = new KXmlFile();
	}

	public Element createClassElement(String className) {
		Element parentElement;
		parentElement = xmlFile.getXmlObject();
		Document document = xmlFile.getDocument();
		Element node = document.createElement("class");
		node.setAttribute("name", className);
		parentElement.appendChild(node);
		return node;
	}

	public Element createMemberElement(Element parentElement, KDbField field) {
		Document document = xmlFile.getDocument();
		Element node = document.createElement("member");
		node.setAttribute("name", field.getName());
		parentElement.appendChild(node);
		if ((field.isPrimary() || !field.isAllowNull()) && !field.isAutoInc())
			node.setAttribute("required", "1");
		else
			node.setAttribute("required", "0");
		if (field.getColumnSize() != -1)
			node.setAttribute("max", String.valueOf(field.getColumnSize()));
		String strType = KDataTypeConverter.getControllerType(field.getJavaType());
		if (strType != null & strType.equalsIgnoreCase("boolean"))
			node.setAttribute("max", "5");
		node.setAttribute("type", field.getType());
		if (field.getType().matches("^ENUM.*$"))
			node.setAttribute("list", field.getType().replaceAll("ENUM(.*)", "$1"));
		else
			node.setAttribute("type", strType);
		return node;
	}

	public void loadFromFile(String fileName) {
		xmlFile = new KXmlFile();
		try {
			xmlFile.loadFromFile(KFileUtils.getPathName(fileName));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void saveAs(String fileName) {
		xmlFile.saveAs(fileName);
	}
}
