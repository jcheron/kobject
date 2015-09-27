package net.ko.mapping;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import net.ko.controller.IKAjaxObjectInterface;
import net.ko.debug.KDebugClient;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.inheritance.KReflectObject;
import net.ko.utils.KHash;
import net.ko.utils.KRegExpr;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class KAjaxObject implements IAjaxObject, IHasParentObject,
		Cloneable {
	protected boolean isChild;
	protected boolean isInRootFolder;
	protected String key;
	protected boolean breakPoint;
	protected boolean breaked;
	protected boolean parentSelector;
	protected Object parentObject;
	protected Node item;
	protected IKAjaxObjectInterface ajaxObjectInterfaceInstance;
	protected String ajaxObjectInterface;

	public KAjaxObject() {
		isChild = false;
		key = "";
		breaked = false;
		breakPoint = false;
		parentSelector = false;
		parentObject = null;
		ajaxObjectInterface = "";
	}

	public boolean isChild() {
		return isChild;
	}

	public void setChild(boolean isChild) {
		this.isChild = isChild;
	}

	@Override
	public String execute(HttpServletRequest request) {
		return toString();
	}

	@Override
	public String executeByName() {
		return toString();
	}

	public Node getFirstNode(Node item, String name) {
		Node result = null;
		Element e = (Element) item;
		NodeList nList = e.getElementsByTagName(name);
		if (nList != null)
			if (nList.getLength() > 0)
				result = nList.item(0);
		return result;
	}

	@Override
	public String display(int margin) {
		String structure = getDisplayStructure();
		structure = structure.replace("{caption}", getDisplayCaption());
		return "<div style='margin-left:" + (20 * margin) + "px;' class='" + getClass().getSimpleName() + "'>" + structure + "</div>";
	}

	@Override
	public String getUniqueIdHash(int niveau) {
		String parentId = "";
		if (parentObject != null)
			parentId = ((IAjaxObject) parentObject).getUniqueIdHash(niveau);
		return KHash.getMD5(niveau + getDisplayCaption() + parentId);
	}

	@Override
	public String getDisplayStructure() {
		return "{caption}";
	}

	@Override
	public boolean hasChilds() {
		return false;
	}

	@Override
	public String replaceJsExpressions(String input) {
		String output = input;
		if (input.contains("{js")) {
			output = output.replaceAll("\\{js\\:(.+?)\\}", Matcher.quoteReplacement("'+") + "$1" + Matcher.quoteReplacement("+'"));
			output = output.replaceAll("\\{jsv\\:(.+?)\\}", Matcher.quoteReplacement("'+$$(") + "$1" + Matcher.quoteReplacement(")+'"));
		}
		return output;
	}

	public String quoteParams(String parameters) {
		String result = parameters;
		ArrayList<String> groups = KRegExpr.getGroups("(.*?)\\{js(.+?)\\}(.*?)", parameters, 2);
		for (int i = 0; i < groups.size(); i++) {
			result = result.replace(groups.get(i), "_grp" + i + "_");
		}
		result = KString.jsQuote(result);
		for (int i = 0; i < groups.size(); i++) {
			result = result.replace("_grp" + i + "_", groups.get(i));
		}
		return result;
	}

	public boolean isInRootFolder() {
		return isInRootFolder;
	}

	public void setInRootFolder(boolean isInRootFolder) {
		this.isInRootFolder = isInRootFolder;
	}

	@Override
	public String getDisplayDebugClientInfo() {
		return getDebugClientId();
	}

	@Override
	public String getDebugClientInfo(int index) {
		String result = "";
		if (KDebugClient.hasOption(getName())) {
			String id = getDebugClientId();
			if (!"".equals(id)) {
				result = "$infoBulle('" + id + "','" + getDisplayDebugClientInfo() + "',{" + getClientDebugTitle() + "},'" + getName() + "',%visibility%,'" + getKey() + "','" + getSelector() + "'%parent%);";
			}
		}
		return result;
	}

	@Override
	public String getClientDebugString() {
		return "";
	}

	@Override
	public String getChildsDebugClientInfo() {
		return "";
	}

	@Override
	public String getClientDebugTitle() {
		return getTitles().implode_param(",", "\"", ":", KGlueMode.KEY_AND_VALUE, "\"", false);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSelector() {
		return "";
	}

	public KStrings getTitles() {
		return new KStrings();
	}

	@Override
	public boolean isBreakPoint() {
		return breakPoint;
	}

	public void setBreakPoint(boolean breakPoint) {
		if (breakPoint)
			KDebugConsole.print("Point d'arrêt :" + getName() + ":" + getDebugClientId() + "->" + getClientDebugString(), "CLIENTDEBUG", "KAjaxObject.setBreakPoint");
		this.breakPoint = breakPoint;
	}

	public boolean isBreaked() {
		return breaked;
	}

	public void setBreaked(boolean breaked) {
		this.breaked = breaked;
	}

	public String addDeboggerExpression(String expression, String caption, String exeExpression) {
		String result = expression;
		if (KDebugClient.isActive()) {
			String id = getDebugClientId().replaceAll("\\W", "");
			String infoBulleId = id + "-infoBulle-" + getName() + getKey();
			if (breakPoint) {
				result = "(new $debogger()).addBreakPoint(function(){" + result + "},\"" + caption + "\",\"" + infoBulleId + "\",function(){" + exeExpression + "});";
			} else if (breaked) {
				result = "(new $debogger()).add(function(){" + result + "},\"" + caption + "\",\"" + infoBulleId + "\",function(){" + exeExpression + "});";
			}
		}
		return result;
	}

	public boolean isParentSelector() {
		return parentSelector;
	}

	public void setParentSelector(boolean parentSelector) {
		this.parentSelector = parentSelector;
	}

	public String getPreludeFunction() {
		String result = "function(){";
		if (parentSelector)
			result = "function(target){";
		return result;
	}

	@Override
	public Object getParentObject() {
		return parentObject;
	}

	@Override
	public void setParentObject(Object parentObject) {
		this.parentObject = parentObject;
	}

	@Override
	public void initFromXml(Node item) {
		this.item = item;
		Element e = (Element) item;
		setAjaxObjectInterface(e.getAttribute("javaInterface"));
	}

	public Node getItem() {
		return item;
	}

	public void setItem(Node item) {
		this.item = item;
	}

	@Override
	public String displayRecursive() {
		return display(0);
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
	public boolean addElement(IAjaxObject element) {
		return false;
	}

	@Override
	public boolean removeElement(IAjaxObject element) {
		return false;
	}

	@Override
	public String createFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createFunction(HttpServletRequest request) {
		if (ajaxObjectInterfaceInstance != null) {
			KAjaxObject clone = (KAjaxObject) this.clone();
			ajaxObjectInterfaceInstance.beforeInclude(clone, request);
			StringBuilder result = new StringBuilder(clone.toString());
			ajaxObjectInterfaceInstance.afterInclude(result, request);
			return result.toString();
		}
		return createFunction();
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDisplayCaption() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDebugClientId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(getClass()))
			return ((KAjaxObject) obj).getDisplayCaption().equals(getDisplayCaption());
		return false;
	}

	@Override
	public int hashCode() {
		return getUniqueIdHash(0).hashCode();
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Document document = parentElement.getOwnerDocument();
		Element node = document.createElement(getName());
		parentElement.appendChild(node);
		XmlUtils.setAttribute(node, "javaInterface", ajaxObjectInterface);
		return node;
	}

	public static void javaExecute(HttpServletRequest request, String javaExec) {
		if (Ko.moxClass != null)
			try {
				KReflectObject.kinvoke(javaExec, Ko.moxClass, new Object[] { request });
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				Ko.klogger().log(Level.WARNING, "Impossible d'appeler la méthode " + javaExec + " sur " + Ko.moxClass, e);
			}
	}

	public Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace(System.err);
		}
		return o;
	}

	public String getAjaxObjectInterface() {
		return ajaxObjectInterface;
	}

	public void setAjaxObjectInterface(String ajaxObjectInterface) {
		this.ajaxObjectInterface = ajaxObjectInterface;
		if (KString.isNotNull(ajaxObjectInterface)) {
			try {
				Class clazz = Class.forName(ajaxObjectInterface);
				ajaxObjectInterfaceInstance = (IKAjaxObjectInterface) clazz.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				Ko.klogger().log(Level.SEVERE, "Impossible de créer une instance de type IAjaxObjectInterface : " + ajaxObjectInterface, e);
			}
		}
	}

	public IKAjaxObjectInterface getAjaxObjectInterfaceInstance() {
		return ajaxObjectInterfaceInstance;
	}
}
