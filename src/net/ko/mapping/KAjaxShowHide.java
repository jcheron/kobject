package net.ko.mapping;

import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxShowHide extends KAjaxObject implements IHasSelector {
	private String visible;
	private String targetSelector;
	private String targetContext = "document";
	private String condition;
	protected int timein = -1;

	@Override
	public String createFunction() {
		String result = "";
		if (KString.isNotNull(targetSelector))
			result = "new Forms.Elements('" + targetSelector + "'," + targetContext + ").show(" + visible + ");";
		if (condition != null && !"".equals(condition)) {
			result = "if(" + condition + "){" + result + "}";
		}
		if (timein > 1) {
			result = "setTimeout(function(){" + result + "}," + timein + ");";
		}
		return result;
	}

	public KAjaxShowHide() {
		super();
		visible = "0";
		condition = "";
	}

	public KAjaxShowHide(String targetSelector) {
		this();
		this.targetSelector = targetSelector;
	}

	@Override
	public boolean isValid() {
		return KString.isNotNull(targetSelector);
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		targetSelector = e.getAttribute("targetSelector");
		condition = e.getAttribute("condition");
		visible = e.getAttribute("visible");

		String targetContext = e.getAttribute("targetContext");
		if (KString.isNotNull(targetContext))
			this.targetContext = targetContext;

		String strTimein = e.getAttribute("timein");
		try {
			timein = Integer.valueOf(strTimein);
		} catch (Exception ex) {
			timein = -1;
		}
	}

	@Override
	public String toString() {
		return "function(){" + createFunction() + "}";
	}

	@Override
	public String getDisplayCaption() {
		String _visible = "hide";
		if (KString.isBooleanTrue(visible))
			_visible = "show";
		return targetSelector + ":" + _visible;
	}

	public String isVisible() {
		return visible;
	}

	public void setVisible(String visible) {
		this.visible = visible;
	}

	public String getTargetSelector() {
		return targetSelector;
	}

	public void setTargetSelector(String targetSelector) {
		this.targetSelector = targetSelector;
	}

	public String getTargetContext() {
		return targetContext;
	}

	public void setTargetContext(String targetContext) {
		this.targetContext = targetContext;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public int getTimein() {
		return timein;
	}

	public void setTimein(int timein) {
		this.timein = timein;
	}

	@Override
	public String getDebugClientId() {
		return targetSelector;
	}

	@Override
	public String getName() {
		return "showHide";
	}

	@Override
	public KStrings getTitles() {
		KStrings titles = super.getTitles();
		titles.put("visible", visible + "");
		if (timein != -1)
			titles.put("timeIn", timein + "");
		if (KString.isNotNull(condition))
			titles.put("condition", KString.htmlSpecialChars(condition));
		return titles;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		if (!"document".equals(targetContext))
			XmlUtils.setAttribute(result, "targetContext", targetContext);
		XmlUtils.setAttribute(result, "condition", condition);
		if (timein != -1)
			XmlUtils.setAttribute(result, "timein", timein + "");
		XmlUtils.setAttribute(result, "visible", visible + "");
		XmlUtils.setAttribute(result, "targetSelector", targetSelector);
		return result;
	}

	@Override
	public String getDOMSelector() {
		if (KString.isNotNull(targetSelector))
			return targetSelector;
		return null;
	}

	public String getVisible() {
		return visible;
	}
}
