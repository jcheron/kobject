package net.ko.mapping;

import java.util.ArrayList;
import java.util.List;

import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxAccordion extends KAjaxWithChilds implements IHasSelector {
	protected String containerId;
	protected String parent;
	protected String type;
	protected String event;
	protected List<String> pages;
	protected int selectedIndex;
	protected String options;

	public KAjaxAccordion() {
		super();
		event = "click";
		options = "{'allowSelectNone':true}";
		parent = "";
		type = "checkbox";
	}

	public KAjaxAccordion(String containerId) {
		this();
		this.containerId = containerId;
	}

	@Override
	public boolean isValid() {
		return KString.isNotNull(containerId);
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		pages = new ArrayList<>();
		Element e = (Element) item;
		containerId = e.getAttribute("containerId");
		type = e.getAttribute("type");
		event = XmlUtils.getAttribute(e, "event", "click");
		parent = e.getAttribute("parent");
		try {
			selectedIndex = Integer.valueOf(e.getAttribute("selectedIndex"));
		} catch (Exception ex) {
			selectedIndex = -1;
		}
		String options = e.getAttribute("options");
		if (KString.isNotNull(options)) {
			this.options = "{" + KString.cleanJSONString(options) + "}";
		}
		childs = KAjaxJs.getChilds(this, item, true, isInRootFolder, true);
		int i = 0;
		for (IAjaxObject child : childs) {
			if (child instanceof IIsAccordionCompatible) {
				IIsAccordionCompatible element = (IIsAccordionCompatible) child;
				element.setTargetId(containerId + "-article-" + i);
				pages.add(element.getTitle());
				i++;
			}
		}

		boolean breakPoint = KString.isBooleanTrue(e.getAttribute("break"));
		setBreakPoint(breakPoint);
	}

	@Override
	public String getDisplayCaption() {
		return getDOMSelector() + "." + event + "()[" + type + "]";
	}

	@Override
	public String getDebugClientId() {
		return "#" + containerId;
	}

	@Override
	public String getName() {
		return "accordion";
	}

	@Override
	public String createMyOwnFunction(String childFunctions) {
		String strPages = "[" + KStrings.implode(",", pages, "'") + "]";
		String result = "new Forms.CssAccordion('" + containerId + "','" + type + "'," + strPages + ",'" + parent + "'," + selectedIndex + "," + options + ");";
		result += childFunctions;
		return result;
	}

	@Override
	public String createFunction() {
		String result = createMyOwnFunction(getFunctions());
		result = addDeboggerExpression(result, getName() + ":" + getDebugClientId() + "->CssAccordion", createMyOwnFunction(""));
		return result;
	}

	@Override
	public String toString() {
		String result = createFunction();
		if (isChild)
			result = getPreludeFunction() + result + "}";
		return result;
	}

	@Override
	public String getFunctions() {
		String result = "";
		if ("load".equals(event)) {
			result += getLoadFunction(0);
		} else {
			for (int i = 0; i < childs.size(); i++) {
				IAjaxObject child = childs.get(i);
				if (i == selectedIndex - 1) {
					result += child.createFunction();
				} else {
					result += "$addEvt($('" + containerId + "-bt-" + i + "'),'" + event + "'," + child.toString() + ");";
				}
			}
		}
		return result;
	}

	public String getLoadFunction(int pos) {
		String result = "";
		IAjaxObject ajaxObj = childs.get(pos);
		if (ajaxObj instanceof KAjaxInclude) {
			result = ((KAjaxInclude) ajaxObj).createMyOwnFunction("{functions}");
		} else {
			result = ajaxObj.createFunction();
		}
		if (pos < childs.size() - 1) {
			if (result.contains("{functions}"))
				result = result.replace("{functions}", getPreludeFunction() + getLoadFunction(pos + 1) + "}");
			else
				result += getLoadFunction(pos + 1);
		}
		if (pos == childs.size() - 1)
			result = result.replace("{functions}", "undefined");
		return result;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	@Override
	public KStrings getTitles() {
		KStrings titles = super.getTitles();
		titles.put("type", type);
		titles.put("event", event);
		titles.put("selectedIndex", selectedIndex + "");
		return titles;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public List<String> getPages() {
		return pages;
	}

	public void setPages(List<String> pages) {
		this.pages = pages;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "containerId", containerId);
		XmlUtils.setAttribute(result, "event", event);
		XmlUtils.setAttribute(result, "options", options);
		XmlUtils.setAttribute(result, "parent", parent);
		XmlUtils.setAttribute(result, "selectedIndex", selectedIndex + "");
		XmlUtils.setAttribute(result, "type", type);
		return result;
	}

	@Override
	public String getDOMSelector() {
		return "#" + containerId;
	}
}
