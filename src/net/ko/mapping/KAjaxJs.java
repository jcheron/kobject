package net.ko.mapping;

import java.util.ArrayList;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import net.ko.debug.KDebugClient;
import net.ko.design.KCssTransition;
import net.ko.http.objects.KRequest;
import net.ko.utils.KHash;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KAjaxJs extends KAjaxWithChilds {
	private String triggerSelector;
	private String triggerContext = "document";
	private String triggerEvent;
	private HttpServletRequest request;
	private int keyCode = -1;
	private boolean unique = false;

	public KAjaxJs() {
		childs = new ArrayList<>();
	}

	public KAjaxJs(String triggerSelector, String triggerContext, String triggerEvent) {
		this();
		this.triggerSelector = triggerSelector;
		this.triggerContext = triggerContext;
		setTriggerEvent(triggerEvent);
	}

	@Override
	public boolean isValid() {
		boolean result = true;
		result = result && (KString.isNotNull(triggerSelector));
		return result;
	}

	private boolean isKeyEvent() {
		return (triggerEvent.startsWith("onkey") || triggerEvent.startsWith("key")) && keyCode != -1;
	}

	@Override
	public String createFunction() {
		String result = "function(e){var target=$gte(e);$sp(e);";
		String startKc = "";
		String endKc = "";
		if (isKeyEvent()) {
			startKc = "if($gkc(e)==" + keyCode + "){";
			endKc = "return false;}";
		}
		result += startKc;
		for (IAjaxObject obj : childs) {
			if (obj instanceof KAjaxSet)
				((KAjaxSet) obj).setRequest(request);
			result += obj.createFunction();
		}
		result += endKc;
		return result + "}";
	}

	@Override
	public String createFunction(HttpServletRequest request) {
		String result = "function(e){var target=$gte(e);$sp(e);";
		String startKc = "";
		String endKc = "";
		if (isKeyEvent()) {
			startKc = "if($gkc(e)==" + keyCode + "){";
			endKc = "return false;}";
		}
		result += startKc;
		for (IAjaxObject obj : childs) {
			if (obj instanceof KAjaxSet)
				((KAjaxSet) obj).setRequest(request);
			result += obj.createFunction(request);
		}
		result += endKc;
		return result + "}";
	}

	public String getExecuteString() {
		String result = "";
		for (IAjaxObject obj : childs) {
			if (obj instanceof KAjaxSet)
				((KAjaxSet) obj).setRequest(request);
			result += obj.createFunction();
		}
		return result;
	}

	public String getExecuteString(HttpServletRequest request) {
		String result = "";
		for (IAjaxObject obj : childs) {
			if (obj instanceof KAjaxSet)
				((KAjaxSet) obj).setRequest(request);
			result += obj.createFunction(request);
		}
		return result;
	}

	@Override
	public String toString() {
		String result = "";
		String addEvent = "addEvent";
		if (unique)
			addEvent = "addUniqueEvent";
		String executeString = getExecuteString();
		if (KString.isNotNull(triggerSelector)) {
			if (triggerSelector.equalsIgnoreCase("body") && triggerEvent.contains("load"))
				result = "Forms.DOM.onReady(function(){" + executeString + "});\n";
			else {
				if (triggerEvent.contains("load"))
					result = "Forms.DOM.onReady(function(){" + executeString + "});\n";
				if (keyCode < 1)
					result += "new Forms.Elements('" + triggerSelector + "'," + triggerContext + ")." + addEvent + "('" + triggerEvent + "'," + createFunction() + ",false);\n";
				else
					result += "new Forms.Elements('" + triggerSelector + "'," + triggerContext + ")." + addEvent + "('" + triggerEvent + "'," + createFunction() + ",false," + keyCode + ");\n";
			}
		}
		result = replaceJsExpressions(result);
		return result;
	}

	public String include(HttpServletRequest request) {
		String result = "";
		String executeString = getExecuteString(request);
		String addEvent = "addEvent";
		if (unique)
			addEvent = "addUniqueEvent";

		if (KString.isNotNull(triggerSelector)) {
			if (triggerSelector.equalsIgnoreCase("body") && triggerEvent.contains("load"))
				result = "Forms.DOM.onReady(function(){" + executeString + "});\n";
			else {
				if (triggerEvent.contains("load"))
					result = "Forms.DOM.onReady(function(){" + executeString + "});\n";
				if (keyCode < 1)
					result += "new Forms.Elements('" + triggerSelector + "'," + triggerContext + ")." + addEvent + "('" + triggerEvent + "'," + createFunction(request) + ",false);\n";
				else
					result += "new Forms.Elements('" + triggerSelector + "'," + triggerContext + ")." + addEvent + "('" + triggerEvent + "'," + createFunction(request) + ",false," + keyCode + ");\n";
			}
		}
		result = replaceJsExpressions(result);
		return result;
	}

	public String getTriggerEvent() {
		return triggerEvent;
	}

	public void setTriggerEvent(String triggerEvent) {
		if (KString.isNull(triggerEvent))
			triggerEvent = "click";
		if (triggerEvent.startsWith("on"))
			triggerEvent = triggerEvent.substring(2);
		triggerEvent = triggerEvent.toLowerCase();
		this.triggerEvent = triggerEvent;
	}

	public static ArrayList<IAjaxObject> getChilds(IAjaxObject parentObject, Node item, boolean isChild, boolean isInRootFolder) {
		return getChilds(parentObject, item, isChild, isInRootFolder, false);
	}

	public static ArrayList<IAjaxObject> getChilds(IAjaxObject parentObject, Node item, boolean isChild, boolean isInRootFolder, boolean force) {
		ArrayList<IAjaxObject> result = new ArrayList<>();
		NodeList cNodes = item.getChildNodes();
		for (int i = 0; i < cNodes.getLength(); i++) {
			KAjaxObject obj = null;
			switch (cNodes.item(i).getNodeName()) {
			case "include":
				obj = new KAjaxInclude();
				obj.initFromXml(cNodes.item(i));
				break;
			case "function":
				obj = new KAjaxFunction();
				obj.initFromXml(cNodes.item(i));
				break;
			case "message":
				obj = new KAjaxMessage();
				obj.initFromXml(cNodes.item(i));
				break;
			case "showHide":
				obj = new KAjaxShowHide();
				obj.initFromXml(cNodes.item(i));
				break;
			case "selector":
				obj = new KAjaxSelector();
				obj.initFromXml(cNodes.item(i));
				break;
			case "set":
				obj = new KAjaxSet();
				obj.initFromXml(cNodes.item(i));
				break;
			case "fireEvent":
				obj = new KAjaxEvent();
				obj.initFromXml(cNodes.item(i));
				break;
			case "messageDialog":
				obj = new KAjaxMessageDialog();
				obj.initFromXml(cNodes.item(i));
				break;
			case "button":
				obj = new KAjaxDialogButton();
				obj.initFromXml(cNodes.item(i));
				break;
			case "includeDialog":
				obj = new KAjaxIncludeDialog();
				obj.initFromXml(cNodes.item(i));
				break;
			case "submitForm":
				obj = new KAjaxSubmitForm();
				obj.initFromXml(cNodes.item(i));
				break;
			case "refreshControl":
				obj = new KAjaxRefreshControl();
				obj.initFromXml(cNodes.item(i));
				break;
			case "includeForm":
				obj = new KAjaxIncludeForm();
				obj.initFromXml(cNodes.item(i));
				break;
			case "accordion":
				obj = new KAjaxAccordion();
				obj.initFromXml(cNodes.item(i));
				break;
			case "refreshFormValues":
				obj = new KAjaxRefreshFormValues();
				obj.initFromXml(cNodes.item(i));
				break;

			case "deleteOne":
				obj = new KAjaxDeleteOne();
				obj.initFromXml(cNodes.item(i));
				break;

			case "deleteMulti":
				obj = new KAjaxDeleteMulti();
				obj.initFromXml(cNodes.item(i));
				break;

			case "updateOne":
				obj = new KAjaxUpdateOne();
				obj.initFromXml(cNodes.item(i));
				break;

			case "field":
				obj = new KAjaxUpdateOneField();
				obj.initFromXml(cNodes.item(i));
				break;

			case "upload":
				obj = new KAjaxUpload();
				obj.initFromXml(cNodes.item(i));
				break;

			case "transition":
				obj = new KCssTransition();
				obj.initFromXml(cNodes.item(i));
				break;

			default:
				break;
			}
			if (obj != null) {
				if (obj.isValid() || force) {
					if (obj != null)
						obj.setInRootFolder(isInRootFolder);
					obj.setChild(isChild);
					obj.setParentObject(parentObject);
					obj.setKey(item.hashCode() + "-" + result.size());
					result.add(obj);
				}
			}
		}
		return result;
	}

	@Override
	public void initFromXml(Node item) {
		this.item = item;
		Element e = (Element) item;
		setTriggerEvent(e.getAttribute("triggerEvent"));
		String triggerSelector = e.getAttribute("triggerSelector");
		if (triggerSelector != null && !"".equals(triggerSelector))
			this.triggerSelector = triggerSelector;
		String triggerContext = e.getAttribute("triggerContext");
		if (triggerContext != null && !"".equals(triggerContext))
			this.triggerContext = triggerContext;
		String strKeyCode = e.getAttribute("keyCode");
		if (strKeyCode != null && !"".equals(strKeyCode)) {
			try {
				this.keyCode = Integer.valueOf(strKeyCode);
			} catch (Exception ex) {
				keyCode = -1;
			}
		}
		unique = KString.isBooleanTrue(e.getAttribute("unique"));
		childs = getChilds(this, item, false, isInRootFolder);
		boolean breakPoint = KString.isBooleanTrue(e.getAttribute("break"));
		setBreakPoint(breakPoint);
	}

	@Override
	public String execute(HttpServletRequest request) {
		this.request = request;
		return include(request);
	}

	@Override
	public String getDisplayCaption() {
		return triggerSelector + ".on" + KString.capitalizeFirstLetter(getTriggerEvent());
	}

	@Override
	public String display(int margin) {
		String structure = getDisplayStructure();
		structure = structure.replace("{caption}", getDisplayCaption());
		return "<div style='margin-left:" + (20 * margin) + "px;' class='" + getClass().getSimpleName() + "'>" + structure + "</div>";
	}

	@Override
	public String getUniqueIdHash(int niveau) {
		return KHash.getMD5(niveau + getDisplayCaption());
	}

	@Override
	public String getDisplayStructure() {
		return "{caption}";
	}

	@Override
	public boolean hasChilds() {
		return true;
	}

	@Override
	public String replaceJsExpressions(String input) {
		String output = input;
		if (input.contains("{js")) {
			output = output.replaceAll("\\{js\\:(.+?)\\}", Matcher.quoteReplacement("'+") + "$1" + Matcher.quoteReplacement("+'"));
			output = output.replaceAll("\\{jsv\\:(.+?)\\}", Matcher.quoteReplacement("'+$$('") + "$1" + Matcher.quoteReplacement("')+'"));
		}
		return output;
	}

	@Override
	public String executeByName() {
		return toString();
	}

	public boolean isInRootFolder() {
		return isInRootFolder;
	}

	public void setInRootFolder(boolean isInRootFolder) {
		this.isInRootFolder = isInRootFolder;
	}

	@Override
	public String getDisplayDebugClientInfo() {
		return triggerSelector + ".on" + KString.capitalizeFirstLetter(getTriggerEvent());
	}

	@Override
	public String getDebugClientInfo(int index) {
		String result = "";
		String id = getDebugClientId();
		if (KDebugClient.hasOption(getName())) {
			if (!"".equals(id)) {
				result = "$infoBulle('" + id + "','" + getDisplayDebugClientInfo() + "',{" + getClientDebugTitle() + "},'" + getName() + "',true,'" + getKey() + "');\n";
			}
		}
		result += getChildsDebugClientInfo();
		return result;

	}

	@Override
	public String getDebugClientId() {
		return triggerSelector;
	}

	@Override
	public String getName() {
		return "js";
	}

	@Override
	public String getClientDebugString() {
		return clientDebugString;
	}

	@Override
	public String getChildsDebugClientInfo() {
		String clientDebugString = "";
		String tmp = "";
		int i = 0;
		for (IAjaxObject child : childs) {
			tmp += child.getDebugClientInfo(i);
			i++;
		}
		String parent = getDebugClientId().replaceAll("\\W", "") + "-infoBulle-" + getName() + getKey();
		clientDebugString += tmp.replace("%visibility%", "false").replace("%parent%", ",'" + parent + "'");
		clientDebugString += "$addUEvt($('" + parent + "'),'click',function(e){event=Forms.Private.getEvent(e);if(event.ctrlKey==1){ Forms.Utils.stopPropagation(event);event.target.areChildsVisible=event.target.areChildsVisible?false:true;" + tmp.replace("%visibility%", "event.target.areChildsVisible").replace("%parent%", "") + "}});";
		return clientDebugString;
	}

	@Override
	public String getClientDebugTitle() {
		return getTitles().implode_param(",", "\"", ":", KGlueMode.KEY_AND_VALUE, "\"", false);
	}

	public KStrings getTitles() {
		KStrings titles = new KStrings();
		if (request != null)
			titles.put("request", KRequest.getRequestURI(request));
		if (keyCode != -1)
			titles.put("keyCode", keyCode + "");
		return titles;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public void setBreaked(boolean breaked) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isBreakPoint() {
		return breakPoint;
	}

	public void setBreakPoint(boolean breakPoint) {
		this.breakPoint = breakPoint;
		if (breakPoint) {
			for (IAjaxObject child : childs) {
				child.setBreakPoint(breakPoint);
			}
		}
	}

	@Override
	public Object getParentObject() {
		return parentObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IHasParentObject> T getParentObject(Class<T> clazz) {
		Object parent = getParentObject();
		if (parent != null) {
			if (parent.getClass().equals(clazz))
				return (T) parent;
			else
				return ((T) parent).getParentObject(clazz);
		}
		return null;
	}

	@Override
	public void setParentObject(Object parentObject) {
		this.parentObject = parentObject;
	}

	public Node getItem() {
		return item;
	}

	public void setItem(Node item) {
		this.item = item;
	}

	@Override
	public String displayRecursive() {
		String result = display(0);
		for (IAjaxObject ajaxObj : childs) {
			result += ajaxObj.displayRecursive();
		}
		return result;
	}

	@Override
	public String createMyOwnFunction(String childFunctions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		if (keyCode != 0 && keyCode != -1)
			XmlUtils.setAttribute(result, "keyCode", keyCode + "");
		XmlUtils.setAttribute(result, "triggerEvent", triggerEvent);
		if (triggerContext != null && !"document".equals(triggerContext))
			XmlUtils.setAttribute(result, "triggerContext", triggerContext);
		XmlUtils.setAttribute(result, "triggerSelector", triggerSelector);
		XmlUtils.setAttribute(result, "unique", unique + "");
		return result;
	}

	public String getTriggerSelector() {
		return triggerSelector;
	}

	public String getTriggerContext() {
		return triggerContext;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public void setTriggerSelector(String triggerSelector) {
		this.triggerSelector = triggerSelector;
	}

	public void setTriggerContext(String triggerContext) {
		this.triggerContext = triggerContext;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}
}
