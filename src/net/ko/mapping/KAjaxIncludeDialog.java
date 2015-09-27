package net.ko.mapping;

import java.util.List;

import net.ko.utils.KString;
import net.ko.utils.KStrings;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxIncludeDialog extends KAjaxWithChilds implements IHasURL,
		IHasTransition {
	protected KAjaxInclude include;
	protected KAjaxMessageDialog dialog;

	public KAjaxIncludeDialog() {
		this("", "");
	}

	public KAjaxIncludeDialog(String title, String targetURL) {
		super();
		dialog = new KAjaxMessageDialog(title);
		include = new KAjaxInclude("", targetURL);
	}

	@Override
	public String createMyOwnFunction(String childFunctions) {
		String begin = "", end = "";
		String parameters = include.createParameters();
		if (childFunctions == null || "".equals(childFunctions)) {
			childFunctions = "undefined";
		}
		if (dialog.getCondition() != null && !"".equals(dialog.getCondition())) {
			begin = "if(" + dialog.getCondition() + "){";
			end = "}";
		}
		String result = begin + "var lb=new Forms.AjaxLightBox('" + dialog.getDOMId() + "','" + KString.jsQuote(dialog.getTitle()) + "','" + include.getTargetURL() + "'," + parameters + "," + childFunctions + ");";
		result += "lb.addActions(" + dialog.getButtons() + ");";
		result += "lb.modal=" + dialog.isModal() + ";";
		result += "lb." + include.getMethod().toLowerCase() + "();";
		result += end;
		return result;
	}

	@Override
	public String createFunction() {
		String result = createMyOwnFunction(include.getFunctions());
		result = addDeboggerExpression(result, getName() + ":" + getDebugClientId() + "->" + include.getTargetURL(), createMyOwnFunction(""));
		return result;
	}

	@Override
	public boolean isValid() {
		return include.getTargetURL() != null && dialog.isValid();
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		Node n = getFirstNode(item, "include");
		if (n != null) {
			include = new KAjaxInclude();
			include.initFromXml(n);
		} else {
			n = getFirstNode(item, "includeForm");
			include = new KAjaxIncludeForm();
			include.initFromXml(n);
		}
		n = getFirstNode(item, "dialog");
		if (n != null) {
			dialog.initFromXml(n);
			if (include instanceof KAjaxIncludeForm) {
				KAjaxIncludeForm includeForm = (KAjaxIncludeForm) include;
				KAjaxDialogButton button = new KAjaxDialogButton();
				button.setCaption(includeForm.getButtonCaption());
				button.setId(includeForm.getButtonId());
				KAjaxSubmitFormButton sfButton = includeForm.getSubmitFormButton();
				sfButton.addScript("return true;");
				includeForm.removeChild(sfButton);
				button.addChild(new KAjaxFunction(sfButton.getSubmitFormFunction()));
				dialog.addChild(button);
			}
		}
		include.setTransition(dialog.getTransition());
		include.setTargetId(dialog.getDOMId());
		dialog.setTransition("");
		boolean breakPoint = KString.isBooleanTrue(e.getAttribute("break"));
		setBreakPoint(breakPoint);
	}

	@Override
	public String getChildsDebugClientInfo() {
		include.setTargetId("lb-" + KString.cleanHTMLAttribute(dialog.getTitle()));
		clientDebugString = include.getDebugClientInfo(0);
		clientDebugString += dialog.getDebugClientInfo(0);
		return clientDebugString;
	}

	@Override
	public String toString() {
		String result = createFunction();
		if (isChild)
			result = getPreludeFunction() + result + "}";
		return result;
	}

	@Override
	public String getURL() {
		return include.getURL();
	}

	@Override
	public String getDisplayCaption() {
		String result = "";
		if (dialog != null)
			result = "{" + dialog.getTitle() + "}->";
		if (include != null)
			result += include.getURL();
		return result;
	}

	@Override
	public String getDisplayStructure() {
		String postFrm = "";
		String getParams = "()";
		String formName = include.getFormName();
		String targetParams = include.getTargetParams();
		if (formName != null && !"".equals(formName))
			postFrm = "[" + formName + "]";
		if (targetParams != null && !"".equals(targetParams))
			getParams = "(" + targetParams + ")";
		return "<span class='title'>" + dialog.getTitle() + "</span> : <span class='url'>{caption}</span>-><span class='method'>" + include.getMethod() + postFrm + getParams + "</span>";
	}

	public List<IAjaxObject> getDialogChilds() {
		List<IAjaxObject> result = dialog.getChilds();
		return result;
	}

	@Override
	public String getDebugClientId() {
		return "#" + dialog.getDOMId();
	}

	@Override
	public String getName() {
		return "includeDialog";
	}

	@Override
	public KStrings getTitles() {
		KStrings result = super.getTitles();
		result.put("Dialog", dialog.getTitle());
		result.put("Include", KString.htmlSpecialChars(include.getTargetURL()));
		return result;
	}

	@Override
	public String getTransition() {
		return dialog.getTransition();
	}

	@Override
	public String getTransitionId() {
		return dialog.getDOMId();
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		dialog.saveAsXML(result, "dialog");
		include.saveAsXML(result);
		return result;
	}

	public KAjaxInclude getInclude() {
		return include;
	}

	public KAjaxMessageDialog getDialog() {
		return dialog;
	}

	public void setInclude(KAjaxInclude include) {
		this.include = include;
	}

	public void setDialog(KAjaxMessageDialog dialog) {
		this.dialog = dialog;
	}
}
