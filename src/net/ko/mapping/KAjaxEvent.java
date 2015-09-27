package net.ko.mapping;

import net.ko.utils.KString;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxEvent extends KAjaxObject implements IHasSelector {
	protected String triggerId;
	protected String triggerEvent;
	protected int timein = -1;
	protected String condition;

	public KAjaxEvent() {
	}

	public KAjaxEvent(String triggerId, String triggerEvent) {
		this();
		this.triggerId = triggerId;
		this.condition = "";
		setTriggerEvent(triggerEvent);
	}

	@Override
	public String createFunction() {
		String result = "Forms.Utils.fireEvent($('" + triggerId + "'), '" + triggerEvent + "');";
		if (timein > 1) {
			result = "setTimeout(function(){" + result + "}," + timein + ");";
		}
		if (condition != null && !"".equals(condition))
			result = "if(" + condition + "){" + result + "}";
		result = replaceJsExpressions(result);
		result = addDeboggerExpression(result, getName() + ":" + getDebugClientId() + "->" + triggerEvent, result);
		return result;
	}

	@Override
	public boolean isValid() {
		return KString.isNotNull(triggerId);
	}

	public void setTriggerEvent(String triggerEvent) {
		if (KString.isNull(triggerEvent))
			triggerEvent = "click";
		if (triggerEvent.startsWith("on"))
			triggerEvent = triggerEvent.substring(2);
		triggerEvent = triggerEvent.toLowerCase();
		this.triggerEvent = triggerEvent;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		setTriggerEvent(e.getAttribute("triggerEvent"));
		String triggerId = e.getAttribute("triggerId");
		if (triggerId != null && !"".equals(triggerId))
			this.triggerId = triggerId;
		String strTimein = e.getAttribute("timein");
		try {
			timein = Integer.valueOf(strTimein);
		} catch (Exception ex) {
			timein = -1;
		}
		condition = e.getAttribute("condition");
		boolean breakPoint = KString.isBooleanTrue(e.getAttribute("break"));
		setBreakPoint(breakPoint);
	}

	@Override
	public String toString() {
		return "function(){" + createFunction() + "}";
	}

	@Override
	public String getDisplayCaption() {
		return triggerId + "." + triggerEvent + "()";
	}

	@Override
	public String getDebugClientId() {
		return triggerId;
	}

	@Override
	public String getName() {
		return "fireEvent";
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "condition", condition);
		if (timein != -1)
			XmlUtils.setAttribute(result, "timein", timein + "");
		XmlUtils.setAttribute(result, "triggerId", triggerId);
		XmlUtils.setAttribute(result, "triggerEvent", triggerEvent);
		return result;
	}

	@Override
	public String getDOMSelector() {
		return "#" + triggerId;
	}

	public int getTimein() {
		return timein;
	}

	public void setTimein(int timein) {
		this.timein = timein;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getTriggerId() {
		return triggerId;
	}

	public String getTriggerEvent() {
		return triggerEvent;
	}

	public void setTriggerId(String triggerId) {
		this.triggerId = triggerId;
	}
}
