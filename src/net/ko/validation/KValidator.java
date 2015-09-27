package net.ko.validation;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.http.js.KJS;
import net.ko.http.objects.KRequest;
import net.ko.utils.Converter;
import net.ko.utils.KString;

/**
 * Permet de mettre en place une validation spécifique sur les membres d'une
 * classe dans un formulaire<br>
 * Créer une classe dérivée de KValidator et y faire référence dans les
 * attributs validator de la balise <b>class</b> et <b>validate</b> de la balise
 * <b>member</b> du fichier <b>kox.xml</b>
 * 
 * @author jc
 * @since 1.0.0.23h
 * 
 */
public abstract class KValidator {
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	private PrintWriter out;
	protected String fieldName;
	protected String jsContent;
	protected KValidatorMessagesList messages;
	protected boolean onError;

	private Map<String, String[]> initialValues;

	public KValidator() {
		super();
		jsContent = "";
		messages = new KValidatorMessagesList();
		onError = false;
	}

	/**
	 * Retourne la valeur du champ de nom fieldName
	 * 
	 * @param fieldName
	 *            nom du champ
	 * @return la valeur du champ fieldName
	 */
	protected String getValue(String fieldName) {
		return KRequest.GETPOST(fieldName, request);
	}

	protected String[] getInitialValues(String fieldName) {
		return initialValues.get(fieldName);
	}

	protected String getInitialValue(String fieldName) {
		String result = null;
		String[] values = initialValues.get(fieldName);
		if (values != null && values.length > 0)
			result = values[0];
		return result;
	}

	/**
	 * Retourne la valeur du champ à valider
	 * 
	 * @param fieldName
	 *            nom du champ
	 * @return la valeur du champ à valider
	 */
	protected String getValue() {
		return getValue(fieldName);
	}

	public void setInitialValues() {
		initialValues = new HashMap<>(request.getParameterMap());
		onLoad();
	}

	protected int getIntValue(String fieldName) {
		return (int) Converter.convert(getValue(fieldName), Integer.class);
	}

	protected boolean getBooleanValue(String fieldName) {
		return KString.isBooleanTrue(getValue(fieldName));
	}

	protected boolean fieldExists(String fieldName) {
		return KRequest.keyExists(fieldName, request);
	}

	protected void displayError(String content) {
		messages.addMessage(KValidationMessageType.ERROR, content);
	}

	protected void displayWarning(String content) {
		messages.addMessage(KValidationMessageType.WARNING, content);
	}

	protected void displayInfo(String content) {
		messages.addMessage(KValidationMessageType.INFO, content);
	}

	protected void displayMessage(KValidationMessageType level, String content) {
		messages.addMessage(level, content);
	}

	protected void addDomElementAfter(String elementId, String targetId) {
		jsContent += KJS.insertAfter(elementId, targetId);
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	protected void setValue(String value) {
		setValue(fieldName, value);
	}

	protected void setValue(String fieldName, String value) {
		addJSContent("$set('" + fieldName + "','" + value + "')");
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	protected void addJSContent(String script) {
		if (!script.endsWith(";")) {
			script += ";";
		}
		jsContent += script;
	}

	protected String getJsContent() {
		String result = "";
		if (jsContent != null && !"".equals(jsContent)) {
			result = "<script type='text/javascript'>" + jsContent + "</script>";
		}
		return result;
	}

	public void flush() {
		if (messages.hasErrors() || onError)
			onError(fieldName);
		else
			onValid(fieldName);
		out.print(messages.toString());
		out.print(getJsContent());
		messages.clear();
	}

	protected void write(String message) {
		messages.addMessage(KValidationMessageType.NONE, message);
	}

	protected abstract void onError(String fieldName);

	protected abstract void onValid(String fieldName);

	protected void onLoad() {

	}

	public void setOnError(boolean onError) {
		this.onError = onError;
	}

	protected void rmClassOn(String fieldName, String className) {
		addJSContent("Forms.Utils.rmClass('" + className + "',$('" + fieldName + "'));");
	}

	protected void addClassOn(String fieldName, String className) {
		addJSContent("Forms.Utils.addClass('" + className + "',$('" + fieldName + "'));");
	}

	public void clear() {
		messages.clear();
		onError = false;
	}
}
