package net.ko.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.ko.displays.Display;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.kobject.KObject;
import net.ko.utils.KHash;
import net.ko.utils.KProperties;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KObjectController implements Serializable, IKoController,
		Cloneable {
	private static final long serialVersionUID = 1L;
	protected Map<String, KObjectFieldController> members;
	protected String koClass;
	protected String caption;
	protected String display;
	protected KProperties messages;
	protected KProperties er;
	protected KTransformer transformer;
	protected String validatorClassName;
	protected String validateOn;
	protected String strTransformer;
	protected boolean xmlExists;

	public void setMessages(KProperties messages) {
		if (this.messages != messages) {
			for (Map.Entry<String, KObjectFieldController> entry : members.entrySet())
				entry.getValue().setMessages(messages);
			this.messages = messages;
		}
	}

	public void setEr(KProperties er) {
		if (this.er != er) {
			for (Map.Entry<String, KObjectFieldController> entry : members.entrySet())
				entry.getValue().setEr(er);
			this.er = er;
		}
	}

	public KObjectController(String koClass) {
		this.members = new HashMap<String, KObjectFieldController>();
		this.koClass = koClass;
		this.validateOn = "";
		this.xmlExists = false;
	}

	public String getClassName() {
		return this.koClass;
	}

	public void initFromXML(Node classXMLElement) {
		xmlExists = true;
		String memberCaption = ((Element) classXMLElement).getAttribute("caption");
		if (memberCaption != null)
			caption = memberCaption;
		String memberDisplay = ((Element) classXMLElement).getAttribute("display");
		strTransformer = ((Element) classXMLElement).getAttribute("transformer");
		validatorClassName = ((Element) classXMLElement).getAttribute("validator");
		validateOn = ((Element) classXMLElement).getAttribute("validateOn");
		setTransformer(strTransformer);
		if (memberDisplay != null)
			display = memberDisplay;
		NodeList map = classXMLElement.getChildNodes();
		for (int i = 0; i < map.getLength(); i++) {
			if (map.item(i).getNodeName().equalsIgnoreCase("member")) {
				Element e = (Element) map.item(i);
				String memberName = e.getAttribute("name");
				KObjectFieldController kofc = new KObjectFieldController(memberName);
				members.put(memberName, kofc);
				kofc.initFromXml(e);
				kofc.setClassName(koClass);
				kofc.setPos(i);
				kofc.setTransformer(transformer);
				kofc.setValidatorClassName(validatorClassName);
				kofc.setKobjectController(this);
			}
		}
	}

	public Map<String, KObjectFieldController> getMembers() {
		return members;
	}

	public KObjectFieldController getFieldController(String memberName) {
		KObjectFieldController koCtrl = null;
		if (members.containsKey(memberName)) {
			koCtrl = members.get(memberName);
		} else {
			koCtrl = new KObjectFieldController(memberName);
			koCtrl.setClassName(koClass);
		}
		return koCtrl;
	}

	public String toJSON() {
		String result = "{" + KStrings.implode(",", members.values().toArray()) + "}";
		return result;
	}

	public boolean isValid(KObject ko) {
		boolean result = true;
		for (Map.Entry<String, KObjectFieldController> entry : members.entrySet())
			try {
				result = result && entry.getValue().isValid(ko.getAttribute(entry.getKey()) + "");
			} catch (Exception e) {
			}
		return result;
	}

	public Map<String, ArrayList<String>> messages(KObject ko) {
		Map<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		for (Map.Entry<String, KObjectFieldController> entry : members.entrySet())
			try {
				result.put(entry.getKey(), entry.getValue().messages(ko.getAttribute(entry.getKey()) + ""));
			} catch (Exception e) {
			}
		return result;
	}

	public String toString() {
		String result = "";
		for (Map.Entry<String, KObjectFieldController> e : members.entrySet()) {
			result += e.getKey() + " : " + e.getValue().toString() + Display.getDefault().getEndLigne();
		}
		return result;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	@SuppressWarnings("unchecked")
	public KObjectDisplay getDisplayInstance() {
		KObjectDisplay koDisp = null;
		if (display != null && !"".equals(display)) {
			try {
				Class<KObjectDisplay> dispClass = (Class<KObjectDisplay>) Class.forName(display);
				koDisp = dispClass.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				Ko.klogger().log(Level.WARNING, "Impossible de trouver ou d'instancier la classe Display :" + display, e);
				koDisp = Ko.defaultKoDisplay();
			}
		} else
			koDisp = Ko.defaultKoDisplay();
		return koDisp;
	}

	@SuppressWarnings("unchecked")
	public void setTransformer(String strTransformer) {
		if (strTransformer != null && !"".equals(strTransformer)) {
			try {
				Class<KTransformer> transformerClass = (Class<KTransformer>) Class.forName(strTransformer);
				transformer = transformerClass.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				transformer = Ko.ktransformer();
			}
		} else
			transformer = Ko.ktransformer();
	}

	public String getValidateOn() {
		return validateOn;
	}

	public void setValidateOn(String validateOn) {
		this.validateOn = validateOn;
	}

	@Override
	public String getDisplayStructure(boolean checkBoxes) {
		String copy = "";
		String id = getUniqueIdHash();
		if (checkBoxes)
			copy = "cp-";
		String result = "<div id='" + copy + id + "'>";
		if (checkBoxes)
			result += "<input type='checkbox' id='ck-" + id + "' name='ck-" + id + "'>";
		result += "<div class='kobjectController' title='" + getTitle() + "'><span>" + getDisplayCaption(koClass, "ss-" + copy + id) + "</span>";
		result += "<div id='ss-" + copy + id + "'>";
		for (Map.Entry<String, KObjectFieldController> e : members.entrySet()) {
			result += e.getValue().getDisplayStructure(checkBoxes);
		}
		result += "</div>";
		result += "</div></div>";
		return result;
	}

	private String getDisplayCaption(String caption, String id) {
		return "<a onclick='Forms.Utils.toogleText($(\"imgToogle" + id + "\"),\"" + id + "\",false);'>" + caption + "<div id='imgToogle" + id + "' class='arrow-up'></div></a>";
	}

	private String getTitle() {
		String result = getOneTitle("Caption", caption);
		result += getOneTitle("Display", display);
		result += getOneTitle("Transformer", strTransformer);
		result += getOneTitle("Validator", validatorClassName);
		result += getOneTitle("ValidateOn", validateOn);
		return result;
	}

	public String getOneTitle(String name, String value) {
		if (KString.isNotNull(value))
			return name + ": " + value + "&#13;";
		return "";
	}

	@Override
	public String getUniqueIdHash() {
		return KHash.getMD5(koClass);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KObjectController)
			return ((KObjectController) obj).getClassName().equals(koClass);
		return false;
	}

	@Override
	public int hashCode() {
		return koClass.hashCode();
	}

	public void addElement(KObjectFieldController element) {
		members.put(element.getName(), element);
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Document document = parentElement.getOwnerDocument();
		Element node = document.createElement("class");
		XmlUtils.setAttribute(node, "validateOn", validateOn);
		XmlUtils.setAttribute(node, "validator", validatorClassName);
		XmlUtils.setAttribute(node, "transformer", strTransformer);
		XmlUtils.setAttribute(node, "display", display);
		XmlUtils.setAttribute(node, "caption", caption);
		XmlUtils.setAttribute(node, "name", koClass);
		parentElement.appendChild(node);
		for (Map.Entry<String, KObjectFieldController> e : members.entrySet()) {
			e.getValue().saveAsXML(node);
		}
		return node;
	}

	public KTransformer getTransformer() {
		return transformer;
	}

	public String getStrTransformer() {
		return strTransformer;
	}

	public boolean isXmlExists() {
		return xmlExists;
	}

	public void setXmlExists(boolean xmlExists) {
		this.xmlExists = xmlExists;
	}

	public KProperties getEr() {
		return er;
	}

	public String getValidatorClassName() {
		return validatorClassName;
	}

	@Override
	public KObjectController clone() throws CloneNotSupportedException {
		KObjectController result = null;
		try {
			result = (KObjectController) super.clone();
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace(System.err);
		}
		return result;
	}

	public String getKoClass() {
		return koClass;
	}

	public void setKoClass(String koClass) {
		this.koClass = koClass;
	}

	public KProperties getMessages() {
		return messages;
	}

	public void setMembers(Map<String, KObjectFieldController> members) {
		this.members = members;
	}

	public void setTransformer(KTransformer transformer) {
		this.transformer = transformer;
	}

	public void setValidatorClassName(String validatorClassName) {
		this.validatorClassName = validatorClassName;
		for (Map.Entry<String, KObjectFieldController> e : members.entrySet()) {
			e.getValue().setValidatorClassName(validatorClassName);
		}
	}

	public void setStrTransformer(String strTransformer) {
		this.strTransformer = strTransformer;
	}

}
