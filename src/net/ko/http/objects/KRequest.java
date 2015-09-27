package net.ko.http.objects;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.utils.KString;
import net.ko.utils.KStrings;

public class KRequest {
	public static String includeResponse(String includePage, HttpServletRequest request, HttpServletResponse response) {
		return includeResponse(includePage, request, response, null);
	}

	public static String includeResponse(String includePage, HttpServletRequest request, HttpServletResponse response, String params) {
		return includeResponse(includePage, request, response, params, request.getMethod());
	}

	public static String includeResponse(String includePage, HttpServletRequest request, HttpServletResponse response, String params, String method) {
		String result = "";
		try {
			KHttpResponseStringWrapper resp = new KHttpResponseStringWrapper(response);
			if (params != null) {
				KHttpRequestWrapper newRequest = KHttpRequestWrapper.setParameter(request, params, method);
				request.getRequestDispatcher(includePage).include(newRequest, resp);
			} else
				request.getRequestDispatcher(includePage).include(request, resp);
			result = resp.getOutput();
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible d'inclure la page : " + includePage, e);
		}
		return result;
	}

	public static boolean keyExists(String key, HttpServletRequest request) {
		return request.getParameterMap().containsKey(key);
	}

	public static boolean isPost(HttpServletRequest request) {
		return request.getMethod().equalsIgnoreCase("post");
	}

	public static String GETPOST(String key, HttpServletRequest request) {
		if (request.getParameter(key) != null)
			return request.getParameter(key);
		return null;
	}

	public static String GETPOST(String key, String method, HttpServletRequest request) {
		if (request.getMethod().equalsIgnoreCase(method))
			return GETPOST(key, request);
		else
			return null;
	}

	public static String GETPOSTValues(String key, HttpServletRequest request, String dValue) {
		String result = "";
		try {
			String[] values = request.getParameterValues(key);
			if (values != null)
				result = KStrings.implode(Ko.krequestValueSep(), values);
			else
				result = KRequest.GETPOST(key, request, dValue);
		} catch (Exception e) {
			result = KRequest.GETPOST(key, request, dValue);
		}
		return result;
	}

	public static String GETPOST(String key, HttpServletRequest request, String dValue) {
		String result = dValue;
		if (request.getParameter(key) != null)
			result = request.getParameter(key);
		return result;
	}

	public static int GETPOST(String key, HttpServletRequest request, int dValue) {
		int result = dValue;
		try {
			result = Integer.valueOf(request.getParameter(key));
		} catch (Exception e) {
		}
		return result;
	}

	public static boolean GETPOST(String key, HttpServletRequest request, boolean dValue) {
		boolean result = dValue;
		try {
			result = KString.isBooleanTrue(request.getParameter(key));
		} catch (Exception e) {
		}
		return result;
	}

	public static String GET(String key, HttpServletRequest request, String dValue) {
		String result = dValue;
		if (!isPost(request))
			result = request.getParameter(key);
		return result;
	}

	public static String GET(String key, HttpServletRequest request) {
		return GET(key, request, null);
	}

	public static int GET(String key, HttpServletRequest request, int dValue) {
		int result = dValue;
		if (!isPost(request)) {
			try {
				result = Integer.valueOf(request.getParameter(key));
			} catch (Exception e) {
			}
		}
		return result;
	}

	public static boolean GET(String key, HttpServletRequest request, boolean dValue) {
		boolean result = dValue;
		if (!isPost(request)) {
			try {
				result = Boolean.valueOf(request.getParameter(key));
			} catch (Exception e) {
			}
		}
		return result;
	}

	public static String POST(String key, HttpServletRequest request, String dValue) {
		String result = dValue;
		if (isPost(request))
			if (request.getParameter(key) != null)
				result = request.getParameter(key);
		return result;
	}

	public static int POST(String key, HttpServletRequest request, int dValue) {
		int result = dValue;
		if (isPost(request)) {
			try {
				result = Integer.valueOf(request.getParameter(key));
			} catch (Exception e) {
			}
		}
		return result;
	}

	public static boolean POST(String key, HttpServletRequest request, boolean dValue) {
		boolean result = dValue;
		if (isPost(request)) {
			try {
				result = Boolean.valueOf(request.getParameter(key));
			} catch (Exception e) {
			}
		}
		return result;
	}

	public static String POST(String key, HttpServletRequest request) {
		return POST(key, request, null);
	}

	public static KHttpRequestWrapper setParameters(HttpServletRequest request, String params) {
		return KHttpRequestWrapper.setParameter(request, params, request.getMethod());
	}

	public static KHttpRequestWrapper setParameters(HttpServletRequest request, String params, String method) {
		return KHttpRequestWrapper.setParameter(request, params, method);
	}

	public static void includeWithQueryString(String includePage, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		include(includePage, request, response, request.getQueryString());
	}

	public static void include(String includePage, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher(includePage).include(request, response);
	}

	public static void include(String includePage, HttpServletRequest request, HttpServletResponse response, String params) throws ServletException, IOException {
		include(includePage, request, response, params, request.getMethod());
	}

	public static void include(String includePage, HttpServletRequest request, HttpServletResponse response, String params, String method) throws ServletException, IOException {
		if (params != null) {
			KHttpRequestWrapper newRequest = KHttpRequestWrapper.setParameter(request, params, method);
			request.getRequestDispatcher(includePage).include(newRequest, response);
		}
		else
			request.getRequestDispatcher(includePage).include(request, response);
	}

	public static void forwardWithQueryString(String forwardPage, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		forward(forwardPage, request, response, request.getQueryString());
	}

	public static void forward(String includePage, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher(includePage).forward(request, response);
	}

	public static void forward(String forwardPage, HttpServletRequest request, HttpServletResponse response, String params) throws ServletException, IOException {
		forward(forwardPage, request, response, params, request.getMethod());
	}

	public static void forward(String forwardPage, HttpServletRequest request, HttpServletResponse response, String params, String method) throws ServletException, IOException {
		KHttpRequestWrapper newRequest = KHttpRequestWrapper.setParameter(request, params, method);
		request.getRequestDispatcher(forwardPage).forward(newRequest, response);
	}

	public static String getCompleteScriptName(HttpServletRequest request) {
		String result = request.getRequestURI();
		if (request.getQueryString() != null)
			result += "?" + request.getQueryString();
		return result;
	}

	public static String getQueryString(HttpServletRequest request) {
		String result = "";
		if (request.getQueryString() != null)
			result = request.getQueryString();
		return result;
	}

	public static String getRequestURI(HttpServletRequest request) {
		String requestUrl = "";
		if (request.getAttribute("javax.servlet.forward.servlet_path") != null) {
			requestUrl = request.getAttribute("javax.servlet.forward.servlet_path") + "";
			if (requestUrl.startsWith("/")) {
				requestUrl = requestUrl.substring(1);
			}
		} else
			requestUrl = request.getRequestURI();

		return requestUrl;
	}

	public static String getRequestURI(String requestUrl, HttpServletRequest request) {
		if ("".equals(requestUrl) || requestUrl == null) {
			requestUrl = getRequestURI(request);
		}
		return requestUrl;
	}

	public static String getClientIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public static String getAction(HttpServletRequest request) {
		String realRequestURL = request.getServletPath();
		realRequestURL = realRequestURL.substring(realRequestURL.lastIndexOf("/") + 1);
		return realRequestURL;
	}
}
