package net.ko.http.objects;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import net.ko.types.KStringsType;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;

import org.apache.catalina.Globals;

public class KHttpRequestWrapper extends HttpServletRequestWrapper {

	private Map<String, String[]> params;
	private boolean merged;
	private String method;

	public static KHttpRequestWrapper setParameter(HttpServletRequest request, String params) {
		return new KHttpRequestWrapper(request, params);
	}

	public static KHttpRequestWrapper setParameter(HttpServletRequest request, String params, String method) {
		return new KHttpRequestWrapper(request, params, method);
	}

	public KHttpRequestWrapper(HttpServletRequest request, String params) {
		this(request, new KStrings(params, KStringsType.kstQueryString).getMapArrayStrings());
	}

	public KHttpRequestWrapper(HttpServletRequest request, String params, String method) {
		this(request, new KStrings(params, KStringsType.kstQueryString).getMapArrayStrings(), method);
	}

	public KHttpRequestWrapper(HttpServletRequest request, Map<String, String[]> params) {
		super(request);
		this.params = params;
		for (Iterator<Map.Entry<String, String[]>> iter = params.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>) iter.next();
			Object value = entry.getValue();
			if (value instanceof String) {
				entry.setValue(new String[] { (String) value });
			}
		}
		merged = false;
		method = request.getMethod();
	}

	public KHttpRequestWrapper(HttpServletRequest request, Map<String, String[]> params, String method) {
		super(request);
		this.params = params;
		for (Iterator<Map.Entry<String, String[]>> iter = params.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>) iter.next();
			Object value = entry.getValue();
			if (value instanceof String) {
				entry.setValue(new String[] { (String) value });
			}
		}
		merged = false;
		this.method = method;
	}

	public String getParameter(String name) {
		String values[] = (String[]) this.params.get(name);
		if ((values == null) || (values.length == 0)) {
			if (merged) {
				return null;
			} else {
				return super.getParameter(name);
			}
		} else {
			return values[0];
		}
	}

	public Map<String, String[]> getParameterMap() {
		if (!merged) {
			Map<String, String[]> paramMap = new HashMap<String, String[]>(super.getParameterMap());
			paramMap.putAll(this.params);
			this.params = paramMap;
			this.merged = true;
		}
		return this.params;
	}

	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(this.getParameterMap().keySet());
	}

	public String[] getParameterValues(String name) {
		String[] result = new String[] {};
		Collection<String[]> coll = this.getParameterMap().values();
		try {
			result = (String[]) coll.toArray(new String[coll.size()]);
		} catch (Exception e) {
			result = null;
		}
		return result;
	}

	public String getMethod() {
		return this.method;
	}

	public String getQueryString() {
		Map<String, Object> tmp = new HashMap<String, Object>(params.size());
		for (Map.Entry<String, String[]> e : params.entrySet()) {
			tmp.put(e.getKey(), getParameter(e.getKey()));
		}
		return new KStrings(tmp).implode_param("&", "", "=", KGlueMode.KEY_AND_VALUE, false);
	}

	public String getRequestDispatcherPath() {
		return (String) getAttribute(Globals.DISPATCHER_REQUEST_PATH_ATTR);
	}
}
