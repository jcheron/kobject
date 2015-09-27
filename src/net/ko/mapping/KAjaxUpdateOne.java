package net.ko.mapping;

import net.ko.utils.KString;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxUpdateOne extends KAjaxDeleteOne {
	protected String operation;

	public KAjaxUpdateOne(String virtualURL) {
		this();
		setTargetURL(virtualURL);
	}

	public KAjaxUpdateOne() {
		super();
		operation = "add";
		oUpdateMessageMask = "{toString} mis à jour";
		updateMessageMask = oUpdateMessageMask;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		operation = e.getAttribute("operation");
		String strURL = e.getAttribute("virtualURL");
		setTargetURL(strURL);
		parameters.put("_partial", "1");
		for (IAjaxObject child : childs) {
			if (child instanceof KAjaxUpdateOneField) {
				KAjaxUpdateOneField field = (KAjaxUpdateOneField) child;
				parameters.put(field.getFieldName(), field.getFieldValue());
			}
		}
	}

	@Override
	public String getName() {
		return "updateOne";
	}

	@Override
	protected void putKeyValuesInParameter(String keyValues) {
		if (KString.isNull(keyValues) && "update".equals(operation)) {
			keyValues = getDefaultJsValues();
		}
		parameters.put("_keyValues", keyValues);
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Override
	public void setTargetURL(String virtualURL) {
		if (KString.isNotNull(virtualURL))
			targetURL = virtualURL;
		else
			targetURL = "/updateOne.frm";
	}

	@Override
	public String getDisplayCaption() {
		String _targetId = KString.isNull(targetId) ? "" : "->[" + getDOMSelector() + "]";
		String _targetURL = KString.isNull(virtualURL) ? "" : virtualURL;
		String _op = "(" + operation + (KString.isNull(kobjectShortClassName) ? "" : ":" + kobjectShortClassName) + ")";
		return _targetURL + _op + _targetId;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "operation", operation);
		if (!oUpdateMessageMask.equals(updateMessageMask))
			XmlUtils.setAttribute(result, "updateMessageMask", updateMessageMask);
		return result;
	}

	@Override
	public void addChild(KAjaxObject child) {
		child.setParentObject(this);
		int index = getFirstChildIndex(KAjaxUpdateOneField.class);
		if (child instanceof KAjaxUpdateOneField)
			childs.add(index, child);
		else {
			if (index > 0)
				index = index - 1;
			else
				index = 0;
			childs.add(index, child);
		}
	}

}
