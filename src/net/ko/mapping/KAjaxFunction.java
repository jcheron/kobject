package net.ko.mapping;

import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxFunction extends KAjaxObject {
	private String script;

	public KAjaxFunction() {
		this("");
	}

	public KAjaxFunction(String script) {
		super();
		this.script = script;
	}

	@Override
	public String createFunction() {
		if (script != null && !script.trim().endsWith(";"))
			script += ";";
		return script;
	}

	@Override
	public boolean isValid() {
		return KString.isNotNull(script);
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		script = e.getAttribute("script");

	}

	@Override
	public String toString() {
		return getPreludeFunction() + createFunction() + "}";
	}

	@Override
	public String getDisplayStructure() {
		return "<span title='" + KString.htmlSpecialChars(script) + "' class='function'>{caption}</span>";
	}

	@Override
	public String getDisplayCaption() {
		return "Script";
	}

	@Override
	public String getDebugClientId() {
		return "body";
	}

	@Override
	public String getName() {
		return "function";
	}

	@Override
	public KStrings getTitles() {
		KStrings titles = super.getTitles();
		titles.put("script", KString.htmlSpecialChars(script));
		return titles;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "script", script);
		return result;
	}
}
