package net.ko.mapping;

import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxSelector extends KAjaxWithChilds implements IHasSelector {
	private String selector;
	private String selectedStyle;
	private String event;
	private boolean allowNull;
	private int startIndex;

	public KAjaxSelector() {
		super();
		selectedStyle = "";
		event = "click";
		allowNull = false;
		startIndex = 1;
	}

	public KAjaxSelector(String selector, String event) {
		super();
		this.selector = selector;
		this.event = event;
	}

	@Override
	public String createMyOwnFunction(String childFunctions) {
		String result = "(new $selector('" + selector + "','" + event + "'," + allowNull + "," + startIndex;
		if (childFunctions != null && !"".equals(childFunctions))
			result += "," + childFunctions;
		else
			result += ",undefined";
		if (selectedStyle != null && !"".equals(selectedStyle))
			result += "," + selectedStyle;
		result += "));";
		// result = replaceJsExpressions(result);
		return result;
	}

	@Override
	public String createFunction() {
		String functionStr = getFunctions();
		String result = createMyOwnFunction(functionStr);
		result = addDeboggerExpression(result, getName() + ":" + getDisplayCaption(), createMyOwnFunction(""));
		return result;
	}

	@Override
	public boolean isValid() {
		boolean result = true;
		result = result && KString.isNotNull(selector);
		return result;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		selector = e.getAttribute("selector");

		String selectedStyle = e.getAttribute("selectedStyle");
		if (selectedStyle != null && !"".equals(selectedStyle))
			this.selectedStyle = selectedStyle.replaceAll("\\s", "");
		String event = e.getAttribute("event");
		if (event != null && !"".equals(event))
			this.event = event;
		String targetFunction = e.getAttribute("targetFunction");
		if (targetFunction != null && !"".equals(targetFunction))
			this.targetFunction = targetFunction;
		allowNull = KString.isBooleanTrue(e.getAttribute("allowNull"));
		try {
			startIndex = Integer.valueOf(e.getAttribute("startIndex"));
		} catch (Exception ex) {
			startIndex = 1;
		}
		childs = KAjaxJs.getChilds(this, item, true, isInRootFolder);
		for (IAjaxObject child : childs) {
			((KAjaxObject) child).setParentSelector(true);
		}
		boolean breakPoint = KString.isBooleanTrue(e.getAttribute("break"));
		setBreakPoint(breakPoint);
	}

	@Override
	public String toString() {
		return "function(){" + createFunction() + "}";
	}

	@Override
	public String getDisplayCaption() {
		return getSelector() + ".on" + KString.capitalizeFirstLetter(event);
	}

	@Override
	public String getDisplayStructure() {
		return "{caption}";
	}

	@Override
	public String getDebugClientId() {
		return selector;
	}

	@Override
	public String getName() {
		return "selector";
	}

	@Override
	public KStrings getTitles() {
		KStrings titles = super.getTitles();
		titles.put("selector.event", getDisplayCaption());
		titles.put("startIndex", startIndex + "");
		return titles;
	}

	@Override
	public String getSelector() {
		return selector;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "allowNull", allowNull + "");
		XmlUtils.setAttribute(result, "selectedStyle", selectedStyle);
		XmlUtils.setAttribute(result, "startIndex", startIndex + "");
		XmlUtils.setAttribute(result, "event", event);
		XmlUtils.setAttribute(result, "selector", selector);
		return result;
	}

	@Override
	public String getDOMSelector() {
		if (KString.isNotNull(selector))
			return selector;
		return null;
	}

	public String getSelectedStyle() {
		return selectedStyle;
	}

	public void setSelectedStyle(String selectedStyle) {
		this.selectedStyle = selectedStyle;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public boolean isAllowNull() {
		return allowNull;
	}

	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}
}
