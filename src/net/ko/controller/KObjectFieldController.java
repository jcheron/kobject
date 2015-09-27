package net.ko.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import net.ko.framework.Ko;
import net.ko.kobject.KObject;
import net.ko.persistence.annotation.Id;
import net.ko.types.HtmlControlType;
import net.ko.utils.KHash;
import net.ko.utils.KProperties;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.KTextFile;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SuppressWarnings("serial")
public class KObjectFieldController extends KObject implements IKoController,
		Cloneable {
	private String xmlElement;
	@Id
	protected String name;
	protected String type = "";
	protected int max = -1;
	protected int min = -1;
	protected HtmlControlType htmlControlType;
	protected boolean required = false;
	protected boolean allowNull = false;
	protected String control;
	protected String list = "";
	protected String options = "";
	protected String className;
	protected String caption = "";
	protected String controllerString;
	protected String regExpr = "";
	protected String template = null;
	protected KProperties messages;
	protected KProperties er;
	protected int pos = 0;
	protected boolean multiple = false;
	protected String transform;
	protected KTransformer transformer;
	protected String validatorClassName;
	protected String validateMethodName;
	protected KObjectController kobjectController;

	public void setMessages(KProperties messages) {
		this.messages = messages;
	}

	public void setEr(KProperties er) {
		this.er = er;
	}

	public KObjectFieldController(String name) {
		this(name, "", -1, -1, HtmlControlType.khcNone, false);
	}

	public KObjectFieldController(String name, String type, int max, int min, HtmlControlType htmlControlType, boolean required) {
		super();
		this.name = name;
		this.type = type;
		this.max = max;
		this.min = min;
		this.htmlControlType = htmlControlType;
		this.required = required;
		this.controllerString = "{\"min\":\"integer\",\"max\":\"integer\"}";
		this.list = "";
		this.options = "";
		this.regExpr = "";
		this.transform = "";
		this.transformer = Ko.ktransformer();
		this.validateMethodName = "";
	}

	public void initFromXml(Element element) {
		try {
			this.max = Integer.valueOf(element.getAttribute("max"));
		} catch (Exception e) {
		}
		try {
			this.min = Integer.valueOf(element.getAttribute("min"));
		} catch (Exception e) {
		}
		this.type = element.getAttribute("type");
		this.control = element.getAttribute("control");
		this.caption = element.getAttribute("caption");
		this.list = element.getAttribute("list");
		this.required = KString.isBooleanTrue(element.getAttribute("required"));
		this.allowNull = KString.isBooleanTrue(element.getAttribute("allowNull"));
		this.options = element.getAttribute("options");
		this.regExpr = element.getAttribute("regExpr");
		this.multiple = KString.isBooleanTrue(element.getAttribute("multiple"));
		this.transform = element.getAttribute("transform");
		this.validateMethodName = element.getAttribute("validate");
		String templateUrl = element.getAttribute("templateUrl");
		setTemplateUrl(templateUrl);

		setControlType();
	}

	public void setTemplateUrl(String templateUrl) {
		try {
			template = KTextFile.open(Ko.getPath() + templateUrl);
			if ("".equals(template))
				template = null;
		} catch (Exception e) {
			template = null;
		}
	}

	private void setControlType() {
		this.htmlControlType = HtmlControlType.getType(control);
		if (this.htmlControlType == HtmlControlType.khcNone)
			this.htmlControlType = getBestControlType();
	}

	private HtmlControlType getBestControlType() {
		HtmlControlType result = HtmlControlType.khcText;
		if (this.type.equals("autoinc"))
			result = HtmlControlType.khcHidden;
		if (this.type.equals("boolean"))
			result = HtmlControlType.khcCheckBox;
		if (this.type.equals("date"))
			result = HtmlControlType.khcDateCmb;
		if (this.type.equals("datetime"))
			result = HtmlControlType.khcDateTimeCmb;
		if (this.type.equals("time"))
			result = HtmlControlType.khcTimeCmb;
		if (this.type.equals("tel"))
			result = HtmlControlType.khcTel;
		if (this.type.equals("mail"))
			result = HtmlControlType.khcEmail;
		if (this.max > 255)
			result = HtmlControlType.khcTextarea;
		if (this.list != null & !"".equals(this.list))
			result = HtmlControlType.khcList;
		return result;

	}

	public void setClassName(String koClass) {
		className = koClass;
	}

	public String getControllerString() {
		ArrayList<String> ret = new ArrayList<String>();
		if (required)
			ret.add("@");
		if (max != -1)
			ret.add("-" + max);
		if (min != -1)
			ret.add(String.valueOf(min));
		if (regExpr != null && !"".equals(regExpr))
			ret.add(regExpr);
		else if (type != null && !"".equals(type))
			ret.add(type);
		if (list != null && !list.equals(""))
			ret.add(list);
		if (control != "" && control != null)
			ret.add("_khc" + control);
		String completeValidator = getCompleteValidator();
		if (!"".equals(completeValidator))
			ret.add(completeValidator);
		return new KStrings(ret).implode(".");
	}

	public String getName() {
		return name;
	}

	public String getList() {
		return this.list;
	}

	public String getControl() {
		return control;
	}

	public String getType() {
		return this.type;
	}

	public int getMax() {
		return max;
	}

	public int getMin() {
		return min;
	}

	public HtmlControlType getHtmlControlType() {
		return htmlControlType;
	}

	public boolean isAllowNull() {
		return allowNull;
	}

	public String toString() {
		return "\"" + name + "\":\"" + getControllerString() + "\"";
	}

	public boolean ctrlRequired(String value) {
		boolean result = true;
		if (required)
			result = (value != null && !value.equals("") && !value.equalsIgnoreCase("null"));
		return result;
	}

	public boolean ctrlNull(String value) {
		boolean result = true;
		if (!allowNull)
			if (value == null)
				result = false;
		return result;
	}

	public boolean ctrlControlType(String value) {
		boolean result = true;
		if (er != null)
			result = value.matches(er.getProperty(type, ".*"));
		return result;
	}

	public boolean ctrlMax(String value) {
		boolean result = true;
		if (max != -1)
			result = max >= value.length();
		return result;
	}

	public boolean ctrlMin(String value) {
		boolean result = true;
		if (min != -1)
			result = min <= value.length();
		return result;
	}

	public boolean isValid(String value) {
		boolean result = true;
		result = ctrlRequired(value) && ctrlControlType(value) && ctrlNull(value);
		return result;
	}

	public ArrayList<String> messages(String value) {
		ArrayList<String> result = new ArrayList<String>();
		if (messages != null) {
			if (!ctrlRequired(value))
				result.add(messages.getProperty("@", "Saisie obligatoire"));
			if (!ctrlNull(value))
				result.add(messages.getProperty("notNull", "Valeur nulle interdite"));
			if (!ctrlControlType(value))
				result.add(messages.getProperty(type, "valeur non conforme au masque"));
			if (!ctrlMax(value))
				result.add(max + " " + messages.getProperty("max", "Caractères requis au maximum"));
			if (!ctrlMin(value))
				result.add(min + " " + messages.getProperty("min", "Caractères requis au minimum"));
		} else {
			if (!isValid(value))
				result.add("valeur non conforme");
		}
		return result;
	}

	/**
	 * Retourne la valeur du contrôle contenue dans les paramètres de la requête<br/>
	 * traitement spécial des checkboxes qui retournent null si elle ne sont pas
	 * cochées en java
	 * 
	 * @param request
	 *            requête Http
	 * @return valeur du contrôle
	 */
	public String getValue(HttpServletRequest request) {
		String result = null;
		if (!multiple && HtmlControlType.khcCheckBox.equals(htmlControlType)) {
			result = "false";
			if (KString.isBooleanTrue(request.getParameter(name)))
				result = "true";
		} else if (multiple)
			return KStrings.implode(Ko.krequestValueSep(), request.getParameterValues(name));
		else
			result = transformer.parse(transform, request.getParameter(name));
		return result;
	}

	public String getValue(HttpServletRequest request, int position) {
		String[] values = request.getParameterValues(name);
		String result = null;
		if (values == null) {
			if (!multiple && HtmlControlType.khcCheckBox.equals(htmlControlType))
				result = "false";
		} else {
			if (values.length > position) {
				if (!multiple && HtmlControlType.khcCheckBox.equals(htmlControlType)) {
					if (KString.isBooleanTrue(values[position]))
						result = "true";
				} else {
					result = transformer.parse(transform, values[position]);
				}
			}
		}
		return result;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public boolean isRequired() {
		return required;
	}

	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public KTransformer getTransformer() {
		return transformer;
	}

	public void setTransformer(KTransformer transformer) {
		this.transformer = transformer;
	}

	public String getValidatorClassName() {
		return validatorClassName;
	}

	public void setValidatorClassName(String validatorClassName) {
		this.validatorClassName = validatorClassName;
	}

	public String getValidateMethodName() {
		return validateMethodName;
	}

	public void setValidateMethodName(String validateMethodName) {
		this.validateMethodName = validateMethodName;
	}

	protected String getCompleteValidator() {
		String result = "";
		if (validatorClassName != null && validateMethodName != null) {
			if (!"".equals(validatorClassName) && !"".equals(validateMethodName)) {
				result = validatorClassName.replace(".", "!") + "#" + validateMethodName;
			}
		}
		return result;
	}

	public String getDisplayStructure(boolean checkBoxes) {
		String copy = "";
		String id = getUniqueIdHash();
		String className = "";
		if (allowNull)
			className += " allowNull";
		if (required)
			className += " required";
		if (checkBoxes)
			copy = "cp-";
		String result = "<div id='" + copy + id + "'>";
		if (checkBoxes)
			result += "<input type='checkbox' id='ck-" + id + "' name='ck-" + id + "'>";
		result += "<div class='fieldController' title='" + getTitle() + "'>";
		result += "<div class='member'><span class='memberType" + className + "'>" + type + " </span><span class='memberName" + className + "'>" + name + "</span><span class='otherProperties'>" + getDisplayOthers() + "</span></div>";
		result += "</div></div>";
		return result;
	}

	private String getDisplayOthers() {
		String result = "";
		if (KString.isNotNull(caption))
			result += displayOne("Caption", "[" + caption + "]");
		result += displayOne("Control", control);
		if (min != -1)
			result += displayOne("Minimum", ">" + min);
		if (max != -1)
			result += displayOne("Maximum", "<" + max);
		if (multiple)
			result += displayOne("multiple", "*");
		result += displayOne("transform", transform);
		result += displayOne("regExpr", KString.htmlSpecialChars(regExpr));
		result += displayOne("options", KString.htmlSpecialChars(options));
		result += displayOne("list", list);
		if (KString.isNotNull(validateMethodName))
			result += displayOne("validator", validatorClassName + "." + validateMethodName);
		return result;
	}

	private String displayOne(String name, String value) {
		if (KString.isNotNull(value))
			return "<span title='" + name + "' class='otherOne'> " + value + "</span>";
		return "";
	}

	private String getTitle() {
		String result = "";
		result += "Required: " + required + "&#13;";
		result += "Nullable: " + allowNull + "&#13;";
		return result;
	}

	@Override
	public String getUniqueIdHash() {
		return KHash.getMD5(className + "." + name);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KObjectFieldController)
			return ((KObjectFieldController) obj).getUniqueIdHash().equals(getUniqueIdHash());
		return false;
	}

	@Override
	public int hashCode() {
		return getUniqueIdHash().hashCode();
	}

	public String getClassName() {
		return className;
	}

	public KObjectController getKobjectController() {
		return kobjectController;
	}

	public void setKobjectController(KObjectController kobjectController) {
		this.kobjectController = kobjectController;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Document document = parentElement.getOwnerDocument();
		Element node = document.createElement("member");
		parentElement.appendChild(node);
		XmlUtils.setAttribute(node, "transform", transform);
		XmlUtils.setAttribute(node, "validate", validateMethodName);
		if (multiple)
			XmlUtils.setAttribute(node, "multiple", multiple + "");
		if (min != -1)
			XmlUtils.setAttribute(node, "min", min + "");
		if (max != -1)
			XmlUtils.setAttribute(node, "max", max + "");
		XmlUtils.setAttribute(node, "regExpr", regExpr);
		XmlUtils.setAttribute(node, "options", options);
		if (required)
			XmlUtils.setAttribute(node, "required", required + "");
		if (allowNull)
			XmlUtils.setAttribute(node, "allowNull", allowNull + "");
		XmlUtils.setAttribute(node, "list", list);
		XmlUtils.setAttribute(node, "control", control);
		XmlUtils.setAttribute(node, "type", type);
		XmlUtils.setAttribute(node, "caption", caption);
		XmlUtils.setAttribute(node, "name", name);

		return node;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setControl(String control) {
		this.control = control;
		setControlType();
	}

	public void setMax(int max) {
		this.max = max;
	}

	public void setMin(int min) {
		this.min = min;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	public String getXmlElement() {
		return xmlElement;
	}

	public void setXmlElement(String xmlElement) {
		this.xmlElement = xmlElement;
	}

	public String getRegExpr() {
		return regExpr;
	}

	public void setRegExpr(String regExpr) {
		this.regExpr = regExpr;
	}

	public String getTransform() {
		return transform;
	}

	public void setTransform(String transform) {
		this.transform = transform;
	}

	public KProperties getMessages() {
		return messages;
	}

	public KProperties getEr() {
		return er;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHtmlControlType(HtmlControlType htmlControlType) {
		this.htmlControlType = htmlControlType;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setList(String list) {
		this.list = list;
	}

	public void setControllerString(String controllerString) {
		this.controllerString = controllerString;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
}
