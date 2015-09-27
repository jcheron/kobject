package net.ko.http.servlets.bean;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;

public class KActionBean {
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected String action = "";
	protected HttpSession session;
	protected PrintWriter out;
	protected ServletContext application;
	protected boolean isAjax;
	protected String js = "";

	public KActionBean(HttpServletRequest request, HttpServletResponse response) throws IOException {
		init(request, response);
	}

	private void init(HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.request = request;
		this.response = response;
		response.setCharacterEncoding("UTF8");
		response.setContentType("text/html");
		out = response.getWriter();
		session = request.getSession();
		application = request.getServletContext();
		isAjax = KRequest.keyExists("_ajx", request);
		js = "";
		action = KRequest.GETPOST("_action", request, "index");
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public HttpSession getSession() {
		return session;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	public PrintWriter getOut() {
		return out;
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}

	public ServletContext getApplication() {
		return application;
	}

	public void setApplication(ServletContext application) {
		this.application = application;
	}

	public boolean isAjax() {
		return isAjax;
	}

	public void setAjax(boolean isAjax) {
		this.isAjax = isAjax;
	}

	public String getJs() {
		return js;
	}

	public void setJs(String js) {
		this.js = js;
	}

	public String getPageHeader() {
		String txtResponse = KRequest.includeResponse(KoHttp.kHeaderURL(), request, response);
		return txtResponse.replaceFirst("(?s)" + Matcher.quoteReplacement("<!-- header -->") + ".*", "");
	}

	public String getPageHeader(String... csss) {
		String txtResponse = getPageHeader();
		for (String css : csss) {
			txtResponse = txtResponse.replace("</head>", "<link rel='stylesheet' href='" + application.getContextPath() + "/css/" + css + "'></head>\n");
		}
		return txtResponse;
	}

	public void printPageHeader() {
		out.print(getPageHeader());
	}

	public void printPageHeader(String... csss) {
		out.print(getPageHeader(csss));
	}

	public void printPageFooter() {
		out.print(getPageFooter());
	}

	public String getPageFooter() {
		String txtResponse = KRequest.includeResponse(KoHttp.kFooterURL(), request, response);
		return txtResponse.replaceFirst("(?s).*?" + Matcher.quoteReplacement("<!-- footer -->"), "");
	}

	public void addJs(String js) {
		this.js += js;
	}

	public void printJs() {
		out.print("<script>" + this.js + "</script>");
	}

	public void print(String content) {
		out.print(content);
	}

	public void flush() {
		out.flush();
	}
}
