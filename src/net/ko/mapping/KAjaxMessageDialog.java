package net.ko.mapping;

import java.util.ArrayList;
import java.util.List;

import net.ko.design.KCssTransition;
import net.ko.utils.KString;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxMessageDialog extends KAjaxWithChilds implements
		IHasTransition {
	protected String title;
	protected String message = "";
	protected String script = "";
	protected String condition;
	protected boolean modal;
	protected String transition;

	public KAjaxMessageDialog() {
		super();
		title = "";
		condition = "";
		modal = false;
		transition = "";
	}

	public KAjaxMessageDialog(String title) {
		this();
		this.title = title;
	}

	@Override
	public String createMyOwnFunction(String childFunctions) {
		String result = "";
		String begin = "", end = "";
		if (condition != null && !"".equals(condition)) {
			begin = "if(" + condition + "){";
			end = "}";
		}
		result = begin + "var lb=new Forms.MessageDialog('" + getDOMId() + "','" + KString.jsQuote(title) + "'," + message + "," + getPreludeFunction() + script + "});";
		result += "lb.modal=" + modal + ";";
		result += "lb.addButtonsWF(" + getButtons() + ");";
		result += "lb.show();";
		if (KString.isNotNull(transition)) {
			result += KCssTransition.preparedTransition(getDOMId(), transition).createFunction();
		}
		result += end;
		return result;
	}

	@Override
	public String createFunction() {
		String result = createMyOwnFunction("");
		result = addDeboggerExpression(result, getName() + ":" + getDebugClientId() + "->" + title, result);
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
		this.title = e.getAttribute("title");
		this.condition = e.getAttribute("condition");
		this.modal = KString.isBooleanTrue(e.getAttribute("modal"));
		this.transition = e.getAttribute("transition");
		Node n = getFirstNode(item, "message");
		if (n != null) {
			this.message = n.getTextContent();
			if (this.message != null)
				this.message = this.message.replace("\n", "");
		}
		childs = KAjaxJs.getChilds(this, item, false, isInRootFolder);
		for (IAjaxObject iao : childs) {
			if (iao instanceof KAjaxDialogButton) {
				KAjaxDialogButton button = (KAjaxDialogButton) iao;
				button.setDialog(this);
			}
		}
		boolean breakPoint = KString.isBooleanTrue(e.getAttribute("break"));
		setBreakPoint(breakPoint);
	}

	@Override
	public String toString() {
		String result = createFunction();
		if (isChild)
			result = "function(obj){" + result + "}";
		return result;
	}

	public String getButtons() {
		String result = "";
		for (int i = 0; i < childs.size(); i++) {
			if (childs.get(i) instanceof KAjaxDialogButton) {
				KAjaxDialogButton adb = (KAjaxDialogButton) childs.get(i);
				if ("".equals(result))
					result += adb.getButtons();
				else
					result += "," + adb.getButtons();
			}
		}
		return "[" + result + "]";
	}

	public List<IAjaxObject> getAjaxButtons() {
		List<IAjaxObject> result = new ArrayList<>();
		for (IAjaxObject obj : childs) {
			if (obj instanceof KAjaxDialogButton)
				result.add(obj);
		}
		return result;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public boolean isModal() {
		return modal;
	}

	public void setModal(boolean modal) {
		this.modal = modal;
	}

	@Override
	public String getDisplayCaption() {
		return "{" + title + "}";
	}

	@Override
	public String getDebugClientId() {
		return "#" + getDOMId();
	}

	public String getDOMId() {
		return "lb-" + KString.cleanHTMLAttribute(title) + "-box";
	}

	@Override
	public String getName() {
		return "messageDialog";
	}

	public String getTransition() {
		return transition;
	}

	public void setTransition(String transition) {
		this.transition = transition;
	}

	@Override
	public String getTransitionId() {
		return getDOMId();
	}

	public Element saveAsXML(Element parentElement, String name) {
		Document document = parentElement.getOwnerDocument();
		Element node = document.createElement(name);
		parentElement.appendChild(node);
		XmlUtils.setAttribute(node, "condition", condition);
		XmlUtils.setAttribute(node, "modal", modal + "");
		XmlUtils.setAttribute(node, "transition", transition);
		XmlUtils.setAttribute(node, "title", title);
		for (IAjaxObject child : childs) {
			if (child instanceof KAjaxDialogButton)
				child.saveAsXML(node);
		}
		if (KString.isNotNull(message)) {
			KAjaxMessage ajaxMessage = new KAjaxMessage("", message);
			ajaxMessage.saveAsXML(node);
		}
		return node;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		return saveAsXML(parentElement, getName());
	}

}
