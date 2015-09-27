package net.ko.mapping;

import net.ko.utils.KString;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxUpdateOneField extends KAjaxObject {
	protected String fieldName;
	protected String fieldValue;

	public KAjaxUpdateOneField() {
		super();
		fieldName = "";
		fieldValue = "";
	}

	@Override
	public String createFunction() {
		return null;
	}

	@Override
	public boolean isValid() {
		return KString.isNotNull(fieldName) && KString.isNotNull(fieldValue);
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		fieldName = e.getAttribute("name");
		fieldValue = e.getAttribute("value");
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "name", fieldName);
		XmlUtils.setAttribute(result, "value", fieldValue);
		return result;
	}

	@Override
	public String getDisplayCaption() {
		return fieldName + "=" + fieldValue;
	}

	@Override
	public String getDebugClientId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "field";
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	@Override
	public String toString() {
		return "";
	}

}
