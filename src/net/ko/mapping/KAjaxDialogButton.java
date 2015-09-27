package net.ko.mapping;

import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxDialogButton extends KAjaxWithChilds implements IHasSelector {
	protected String caption;
	protected int keyCode = 0;
	protected String id;
	protected String cssClass;
	protected String glyphIcon;

	protected KAjaxMessageDialog dialog;

	public KAjaxDialogButton() {
		this("");
	}

	public KAjaxDialogButton(String caption) {
		super();
		this.caption = caption;
		this.id = "";
		keyCode = 0;
		glyphIcon = "";
	}

	@Override
	public String createMyOwnFunction(String childFunctions) {
		return childFunctions;
	}

	@Override
	public String createFunction() {
		String result = createMyOwnFunction(getFunctions());
		result = addDeboggerExpression(result, getName() + ":" + getDebugClientId() + "->" + keyCode, createMyOwnFunction(""));
		return result;
	}

	@Override
	public boolean isValid() {
		return caption != null;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		this.caption = e.getAttribute("caption");
		this.id = e.getAttribute("id");
		this.glyphIcon = e.getAttribute("glyphIcon");
		String strKeyCode = e.getAttribute("keyCode");
		if (strKeyCode != null && !"".equals(strKeyCode)) {
			try {
				this.keyCode = Integer.valueOf(strKeyCode);
			} catch (Exception ex) {
				keyCode = 0;
			}
		}
		childs = KAjaxJs.getChilds(this, item, true, isInRootFolder);
		boolean breakPoint = KString.isBooleanTrue(e.getAttribute("break"));
		setBreakPoint(breakPoint);
	}

	@Override
	public String toString() {
		String result = createFunction();
		result = getPreludeFunction() + result + "}";
		return result;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public int getKeyCode() {
		if (keyCode < 0)
			return 0;
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public String getButtons() {
		String strFunctions = getFunctions();
		boolean hasId = KString.isNotNull(id);
		String strId = "'" + id + "'";
		String strGlyh = glyphIcon;
		if (!hasId)
			strId = "undefined";
		if (KString.isNull(glyphIcon))
			strId = "undefined";
		if (!"".equals(strFunctions))
			return "{\"id\":" + strId + ",\"caption\":\"" + KString.jsQuote(caption) + "\",\"keyCode\":" + getKeyCode() + ",\"action\":" + getFunctions() + ",\"glyphIcon\":\"" + strGlyh + "\"}";
		else
			return "{\"id\":" + strId + ",\"caption\":\"" + KString.jsQuote(caption) + "\",\"keyCode\":" + getKeyCode() + ",\"glyphIcon\":\"" + strGlyh + "\"}";
	}

	@Override
	public String getDisplayCaption() {
		return "{" + caption + "}";
	}

	@Override
	public String getDebugClientId() {
		return "#" + getDOMId();
	}

	@Override
	public String getName() {
		return "button";
	}

	@Override
	public KStrings getTitles() {
		KStrings result = super.getTitles();
		result.put("caption", caption);
		result.put("keyCode", keyCode);
		return result;
	}

	public KAjaxMessageDialog getDialog() {
		return dialog;
	}

	public void setDialog(KAjaxMessageDialog dialog) {
		this.dialog = dialog;
	}

	public String getDOMId() {
		String result = "";
		if (dialog != null) {
			result = dialog.getDOMId() + "-";
		}
		result += KString.cleanHTMLAttribute(caption) + "-boxBtn";
		return result;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "caption", caption);
		if (KString.isNotNull(id))
			if (!id.equals(getDOMId()))
				XmlUtils.setAttribute(result, "id", id);
		if (keyCode > 0)
			XmlUtils.setAttribute(result, "keyCode", keyCode + "");
		if (KString.isNotNull(glyphIcon))
			XmlUtils.setAttribute(result, "glyphIcon", glyphIcon);
		return result;
	}

	@Override
	public String getDOMSelector() {
		return "#" + id;
	}

	public String getGlyphIcon() {
		return glyphIcon;
	}

	public void setGlyphIcon(String glyphIcon) {
		this.glyphIcon = glyphIcon;
	}

}
