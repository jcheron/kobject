package net.ko.http.servlets.bean;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.http.objects.KRequest;

public class KSlistClassesBean extends KActionBean {
	private String cls = null;
	private String depth = "2";
	private boolean useCache = false;
	private String field;

	public KSlistClassesBean(HttpServletRequest request, HttpServletResponse response) throws IOException {
		super(request, response);
		cls = KRequest.GETPOST("_cls", request);
		depth = KRequest.GETPOST("_depth", request, "1");
		useCache = KRequest.GETPOST("_useCache", request, false);
		field = KRequest.GETPOSTValues("_field", request, "");
	}

	public String getCls() {
		return cls;
	}

	public void setCls(String cls) {
		this.cls = cls;
	}

	public String getDepth() {
		return depth;
	}

	public void setDepth(String depth) {
		this.depth = depth;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

}
