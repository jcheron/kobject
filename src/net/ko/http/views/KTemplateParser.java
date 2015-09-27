package net.ko.http.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.bean.KFuncOrSet;
import net.ko.framework.Ko;
import net.ko.http.objects.KRequest;
import net.ko.kobject.KMask;
import net.ko.utils.KString;
import net.ko.utils.KStrings;

public abstract class KTemplateParser {
	protected String strTemplate;
	protected String resultString = "";
	protected KAbstractView view;
	protected String sepFirst = "{";
	protected String sepLast = "}";
	protected String sepOperation = "#";
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected Map<String, String> variables;
	protected Object caller;

	public abstract String getValue(String jsonFieldName);

	protected KFieldControl getFieldControl(String fieldName) {
		return view.getFieldControl(fieldName);
	}

	public static String setQueryString(String value, HttpServletRequest request) {
		String result = value;
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			String requestValue = KRequest.GETPOST(name, request, "");
			result = result.replace("%" + name + "%", requestValue);
		}
		/*
		 * ArrayList<String> qsValues=KRegExpr.getGroups("%(.*?)%", value, 1);
		 * for(String qsV:qsValues){ if(request.getParameter(qsV)!=null)
		 * result=result.replace("%"+qsV+"%", request.getParameter(qsV)); }
		 */
		return result;
	}

	protected String setQueryString(String value) {
		return setQueryString(value, request);
	}

	protected String setVariables(String value) {
		String result = value;
		for (Map.Entry<String, String> eV : variables.entrySet()) {
			result = result.replace("%" + eV.getKey() + "%", eV.getValue());
		}
		return result;
	}

	protected String getOperation(Entry<String, Object> e) {
		String result = "";
		String key = e.getKey();
		String value = (String) e.getValue();
		value = setVariables(value);
		value = setQueryString(value);
		if (key.startsWith("kobject")) {
			key = "func";
			int nbParameters = value.split(";").length;
			if (nbParameters == 2)
				value = value.substring(0, value.lastIndexOf(")")) + ";" + KRequest.getQueryString(request) + ";true)";
			value = "ksession.kloadAndShowOne" + value;
		}
		if (key.startsWith("klistobject")) {
			key = "func";
			value = "ksession.kloadAndShowMany" + value;
		}
		if (key.startsWith("klistTemplate")) {
			key = "func";
			value = "ksession.kloadList" + value.substring(0, value.lastIndexOf(")")) + ";_request;_response)";
		}
		if (key.equalsIgnoreCase("include")) {
			result = getIncludeContent(value);
		} else if (key.startsWith("set")) {
			try {
				setMemberValue(key, value);
			} catch (Exception e1) {
				Ko.klogger().log(Level.WARNING, "Impossible d'affecter " + value + " dans l'opération " + key, e1);
			}
		} else if (key.startsWith("func")) {
			try {
				result = executeOp(key, value) + "";
			} catch (Exception e1) {
				Ko.klogger().log(Level.WARNING, "Impossible d'appeler " + value + " dans l'opération " + key, e1);
			}
		} else if (key.startsWith("var")) {
			try {
				String variableName = KString.getBefore(value, "=");
				String expression = KString.getAfter(value, "=");
				String variableValue = expression;
				if (!expression.matches("^\"(.*?)\"$"))
					variableValue = executeOp(key, expression) + "";
				else
					variableValue = KString.cleanJSONValue(variableValue);
				variables.put(variableName, variableValue);
				result = "";
			} catch (Exception e1) {
				Ko.klogger().log(Level.WARNING, "Impossible d'appeler " + value + " dans l'opération " + key, e1);
			}
		}
		return result;
	}

	protected void setMemberValue(String memberName, String value) {
		KFuncOrSet fos;
		if (view == null)
			fos = new KFuncOrSet(value, caller);
		else
			fos = new KFuncOrSet(value, view);
		try {
			fos.setOp();
		} catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
			Ko.klogger().log(Level.WARNING, "Impossible d'affecter la valeur " + value + " au membre " + memberName, e);
		}
	}

	public Object executeOp(String key, String value) throws ClassNotFoundException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		KFuncOrSet fos;
		if (view == null)
			fos = new KFuncOrSet(value, caller);
		else
			fos = new KFuncOrSet(value, view);
		String result = "";
		result = fos.execute() + "";
		return result;
	}

	public String getOperation(String jsonOperation) {
		String result = "";
		if (jsonOperation.startsWith("func") || jsonOperation.contains("object") || jsonOperation.contains("Template")) {
			jsonOperation = jsonOperation.replace(";", "_pointVirgule_");
			jsonOperation = jsonOperation.replace(",", ";");
		}
		KStrings strings = new KStrings(jsonOperation);
		for (Map.Entry<String, Object> e : strings.getStrings().entrySet()) {
			result = getOperation(e);
		}
		return result;
	}

	public KTemplateParser(String strTemplate) {
		this.strTemplate = strTemplate;
		variables = new HashMap<>();
	}

	public void parse(KAbstractView view) {
		this.view = view;
		parse(view.getRequest(), view.getResponse());

	}

	public void parse(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		resultString = encodeStrTemplate(strTemplate);
		KMask mask = new KMask(resultString);
		ArrayList<String> jsonOperations = mask.getOperations(sepFirst + sepOperation, sepOperation + sepLast);

		for (int i = 0; i < jsonOperations.size(); i++) {
			String opRes = Matcher.quoteReplacement(encodeStrTemplate(getOperation(jsonOperations.get(i)) + ""));
			resultString = resultString.replaceAll("(?i)" + "\\" + sepFirst + sepOperation + Pattern.quote(jsonOperations.get(i)) + sepOperation + "\\" + sepLast, opRes);
		}
		mask = new KMask(resultString);
		ArrayList<String> jsonFieldNames = mask.getFields();
		for (int i = 0; i < jsonFieldNames.size(); i++) {
			String valueRes = Matcher.quoteReplacement(encodeStrTemplate(getValue(jsonFieldNames.get(i))) + "");
			resultString = resultString.replaceAll("(?i)" + "\\" + sepFirst + Pattern.quote(jsonFieldNames.get(i)) + "\\" + sepLast, valueRes);
		}
		resultString = setQueryString(resultString);
		resultString = decodeStrTemplate(resultString);
	}

	protected String encodeStrTemplate(String str) {
		return replace(str, "<script", "</script>", new String[] { "{", "}" }, new String[] { "_accO_", "_accF_" });
	}

	protected String decodeStrTemplate(String str) {
		return restore(str, new String[] { "{", "}" }, new String[] { "_accO_", "_accF_" });
	}

	private String restore(String inStrTemplate, String[] replacement, String[] pattern) {
		String result = inStrTemplate;
		for (int i = 0; i < pattern.length; i++) {
			if (replacement.length > i) {
				String p = Pattern.quote(pattern[i]);
				String r = Matcher.quoteReplacement(replacement[i]);
				result = result.replaceAll(p, r);
			}
		}
		return result;
	}

	private String replace(String inStrTemplate, String start, String end, String[] pattern, String[] replacement) {
		String result = inStrTemplate;
		String start1 = Pattern.quote(start);
		String end1 = Pattern.quote(end);
		String start2 = Matcher.quoteReplacement(start);
		String end2 = Matcher.quoteReplacement(end);
		for (int i = 0; i < pattern.length; i++) {
			if (replacement.length > i) {
				String p = Pattern.quote(pattern[i]);
				String r = Matcher.quoteReplacement(replacement[i]);
				Pattern pat = Pattern.compile(start1 + "(.*?)" + p + "(.*?)" + end1, Pattern.DOTALL);
				Matcher mat = pat.matcher(result);
				do {
					result = mat.replaceFirst(start2 + "$1" + r + "$2" + end2);
					mat = pat.matcher(result);
				} while (mat.find());
				// result=result.replaceAll(start1+"([\\s^"+p+"]*?)"+p+"([\\s^"+p+"]*?)"+end1,
				// start2+"$1"+r+"$2"+end2);
			}
		}
		return result;
	}

	public void parseForExec(KAbstractView view) {
		this.view = view;
		this.request = view.getRequest();
		this.response = view.getResponse();
		KMask mask = new KMask(strTemplate);
		ArrayList<String> jsonOperations = mask.getOperations(sepFirst + sepOperation, sepOperation + sepLast);

		for (int i = 0; i < jsonOperations.size(); i++) {
			getOperation(jsonOperations.get(i));
		}
	}

	protected String getIncludeContent(String url) {
		return KRequest.includeResponse(url, request, response);
	}

	public String getStrTemplate() {
		return strTemplate;
	}

	public void setStrTemplate(String strTemplate) {
		this.strTemplate = strTemplate;
	}

	public String getResultString() {
		return resultString;
	}

	public void setResultString(String resultString) {
		this.resultString = resultString;
	}

	public String getSepFirst() {
		return sepFirst;
	}

	public void setSepFirst(String sepFirst) {
		this.sepFirst = sepFirst;
	}

	public String getSepLast() {
		return sepLast;
	}

	public void setSepLast(String sepLast) {
		this.sepLast = sepLast;
	}

	@Override
	public String toString() {
		return resultString;
	}

	public Object getCaller() {
		return caller;
	}

	public void setCaller(Object caller) {
		this.caller = caller;
	}
}
