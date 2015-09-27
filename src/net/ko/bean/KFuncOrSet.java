package net.ko.bean;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.ko.converters.DateFormatter;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
import net.ko.http.views.KAbstractView;
import net.ko.inheritance.KReflectObject;
import net.ko.mapping.KAjaxSet;
import net.ko.types.KTemplateOperationType;
import net.ko.utils.KRegExpr;
import net.ko.utils.KString;

public class KFuncOrSet {
	private String memberName = "";
	private Object o = null;
	private String className = "";
	private Object[] paramsValues = null;
	private String value = "";
	private KTemplateOperationType templateOperationType = KTemplateOperationType.totMethod;
	private Object caller;

	public KFuncOrSet(String initValue, Object caller) {
		parse(initValue, caller);
	}

	public Object getObject(String varName) {
		Object result = null;
		switch (varName) {
		case "this":
		case "":
			result = caller;
			break;
		case "session":
			result = getSession();
			break;
		case "request":
			result = getRequest();
			break;
		case "application":
			result = getApplication();
			break;
		case "out":
			result = getWriter();
			break;
		case "response":
			result = getResponse();
			break;
		case "Ko":
			result = Ko.class;
			break;
		case "KoHttp":
			result = KoHttp.class;
			break;
		case "KRequest":
			result = KRequest.class;
			break;
		case "DateFormatter":
			result = DateFormatter.class;
			break;
		default:
			try {
				result = Ko.getVariable(caller, className);
			} catch (InstantiationException | IllegalAccessException | SQLException | ClassNotFoundException e) {
				result = null;
			}
			break;
		}
		return result;
	}

	public void parse(String initValue, Object caller) {
		this.caller = caller;
		className = KString.getBefore(initValue, ".");
		if (className == null || className.equals(""))
			className = "this";
		String[] paramsValuesStr = null;
		o = getObject(className);
		memberName = KString.getAfter(initValue, ".");

		String paramsStr = KRegExpr.getPart("(", ")", memberName, true);
		if (!memberName.equalsIgnoreCase(paramsStr)) {
			templateOperationType = KTemplateOperationType.totMethod;
			memberName = KString.getBefore(memberName, "(");
			if ("".equals(paramsStr)) {
				paramsValuesStr = new String[] {};
				paramsValues = new String[] {};
			} else {
				paramsValuesStr = paramsStr.split(";");
				paramsValues = new Object[paramsValuesStr.length];
				for (int i = 0; i < paramsValuesStr.length; i++) {
					paramsValuesStr[i] = KString.cleanJSONValue(paramsValuesStr[i]);
					paramsValuesStr[i] = paramsValuesStr[i].replace("_pointVirgule_", ";");
					paramsValuesStr[i] = paramsValuesStr[i].replace("\"", "\\\"");
					if (paramsValuesStr[i].startsWith("_")) {
						paramsValues[i] = getObject(paramsValuesStr[i].substring(1));
					} else
						paramsValues[i] = paramsValuesStr[i];
				}
			}
		} else {
			templateOperationType = KTemplateOperationType.totSet;
			String mn = memberName;
			if (memberName.contains("=")) {
				templateOperationType = KTemplateOperationType.totSet;
				memberName = KString.getBefore(mn, "=");
				value = KString.getAfter(mn, "=");
				value = KString.cleanJSONValue(value);
			} else {
				templateOperationType = KTemplateOperationType.totGet;
			}
		}
	}

	public Object executeOp() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object result = new String("");
		if (!"".equals(memberName)) {
			if (o != null) {
				result = KReflectObject.kinvoke(memberName, o, paramsValues);
			} else if (!"".equals(className)) {
				result = KReflectObject.kinvoke(memberName, className, paramsValues);
			}
			if (result == null)
				result = new String("");
		}
		return result;
	}

	public void setOp() throws SecurityException, IllegalAccessException, InvocationTargetException {
		if (!"".equals(memberName)) {
			if (o != null) {
				KReflectObject.__setAttribute(o, memberName, value);
			} else if (!"".equals(className)) {
				KReflectObject.__setAttribute(o, memberName, value);
			}
		}
	}

	public static Object getVariable(String memberName, Object caller) {
		Object result = null;
		try {
			Field f = getField(caller.getClass(), memberName);
			f.setAccessible(true);
			result = f.get(caller);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			// TODO Auto-generated catch block
		}
		return result;
	}

	public String execute() throws ClassNotFoundException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String result = "";
		if (templateOperationType.equals(KTemplateOperationType.totMethod)) {
			result = executeOp() + "";
			result = (result + "").replace("\\\"", "\"");
		}
		else if (templateOperationType.equals(KTemplateOperationType.totSet))
			setOp();
		else
			result = getVariable(memberName, caller) + "";
		return result;
	}

	private static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}

	private HttpServletRequest getRequest() {
		HttpServletRequest result = null;
		if (caller instanceof KAbstractView)
			result = ((KAbstractView) caller).getRequest();
		else if (caller instanceof KAjaxSet)
			result = ((KAjaxSet) caller).getRequest();
		return result;
	}

	private HttpServletResponse getResponse() {
		HttpServletResponse result = null;
		if (caller instanceof KAbstractView)
			result = ((KAbstractView) caller).getResponse();
		return result;
	}

	private HttpSession getSession() {
		HttpSession result = null;
		HttpServletRequest request = getRequest();
		if (request != null)
			result = request.getSession();
		return result;
	}

	private ServletContext getApplication() {
		ServletContext result = null;
		HttpServletRequest request = getRequest();
		if (request != null)
			result = request.getServletContext();
		return result;
	}

	private PrintWriter getWriter() {
		PrintWriter result = null;
		HttpServletResponse response = getResponse();
		if (response != null) {
			try {
				result = response.getWriter();
			} catch (IOException e) {
			}
		}
		return result;
	}

	public static Object execute(String initialExpression, Object caller) throws ClassNotFoundException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object result = null;
		String[] expressions = splitExpression(initialExpression);
		if (expressions.length > 1) {
			KFuncOrSet func = new KFuncOrSet(expressions[0] + "." + expressions[1], caller);
			Object o = func.execute();
			if (expressions.length > 2)
				result = execute("this." + expressions[2], o);
		} else {
			KFuncOrSet func = new KFuncOrSet("this." + initialExpression, caller);
			result = func.execute();
		}
		return result;
	}

	public static String[] splitExpression(String initialExpression) {
		ExpressionParser ep = new ExpressionParser(initialExpression);
		return ep.parse();
	}
}
