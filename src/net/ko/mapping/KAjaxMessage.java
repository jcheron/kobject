package net.ko.mapping;

import net.ko.design.KCssTransition;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxMessage extends KAjaxObject implements
		IIsAccordionCompatible, IHasTransition, IHasSelector {
	protected String targetId;
	protected String message;
	protected int timeout = -1;
	protected int timein = -1;
	protected String condition = "";
	protected String title = "";
	protected String transition = "";

	public KAjaxMessage() {
		super();
	}

	public KAjaxMessage(String targetId, String message) {
		super();
		this.targetId = targetId;
		this.message = message;
	}

	@Override
	public String createFunction() {
		String strTimeout = "";
		String result = "";
		if (timeout > 1) {
			strTimeout = "setTimeout(function(){$set($('" + targetId + "'),'');}," + timeout + ");";
		}
		if (isChild)
			result = "$set($('" + targetId + "')," + message + ");" + strTimeout;
		else
			result = "$set($('" + targetId + "')," + message + ");" + strTimeout;
		if (KString.isNotNull(transition)) {
			result += KCssTransition.preparedTransition(targetId, transition).createFunction();
		}
		if (timein > 1) {
			result = "setTimeout(function(){" + result + "}," + timein + ");";
		}
		if (condition != null && !"".equals(condition))
			result = "if(" + condition + "){" + result + "}";
		result = addDeboggerExpression(result, getName() + ":" + getDebugClientId() + "->" + KString.htmlSpecialChars(message), result);
		return result;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		targetId = e.getAttribute("targetId");
		message = e.getTextContent();
		if (KString.isNotNull(message))
			message = KString.removeCRLF(message);
		this.title = e.getAttribute("title");
		String strTimeout = e.getAttribute("timeout");
		try {
			timeout = Integer.valueOf(strTimeout);
		} catch (Exception ex) {
			timeout = -1;
		}
		String strTimein = e.getAttribute("timein");
		try {
			timein = Integer.valueOf(strTimein);
		} catch (Exception ex) {
			timein = -1;
		}
		transition = e.getAttribute("transition");
		condition = e.getAttribute("condition");
		boolean breakPoint = KString.isBooleanTrue(e.getAttribute("break"));
		setBreakPoint(breakPoint);
	}

	@Override
	public String toString() {
		return getPreludeFunction() + createFunction() + "}";
	}

	@Override
	public String getDisplayCaption() {
		String result = "";
		if (KString.isNotNull(title))
			result = "{" + title + "}";
		if (KString.isNotNull(targetId)) {
			if (KString.isNotNull(result))
				result += "->";
			result += "[" + getDOMSelector() + "]";
		}
		return result;
	}

	@Override
	public String getDisplayStructure() {
		return "<fieldset class='targetId'><legend>{caption}</legend><span title='" + KString.htmlSpecialChars(message) + "' class='message'>message</span></fieldset>";
	}

	@Override
	public String getDebugClientId() {
		return "#" + targetId;
	}

	@Override
	public String getName() {
		return "message";
	}

	@Override
	public KStrings getTitles() {
		KStrings titles = super.getTitles();
		titles.put("message", KString.htmlSpecialChars(message));
		if (KString.isNotNull(condition))
			titles.put("condition", KString.htmlSpecialChars(condition));
		if (timein != -1)
			titles.put("timeIn", timein + "");
		if (timeout != -1)
			titles.put("timeOut", timeout + "");
		return titles;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getTransition() {
		return transition;
	}

	public void setTransition(String transition) {
		this.transition = transition;
	}

	@Override
	public String getTransitionId() {
		return targetId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
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

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "condition", condition);
		XmlUtils.setAttribute(result, "targetId", targetId);
		XmlUtils.setAttribute(result, "title", title);
		XmlUtils.setAttribute(result, "transition", transition);
		if (timein != -1)
			XmlUtils.setAttribute(result, "timein", timein + "");
		if (timeout != -1)
			XmlUtils.setAttribute(result, "timeout", timeout + "");
		if (message != null)
			result.setTextContent(message);
		return result;
	}

	@Override
	public String getDOMSelector() {
		if (KString.isNotNull(targetId))
			return "#" + targetId;
		return null;
	}
}
