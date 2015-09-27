package net.ko.http.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;

public class KSAbstractMain extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected String action = "";
	protected HttpSession session;
	protected PrintWriter out;
	protected ServletContext application;
	protected boolean isAjax;
	protected String js = "";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSAbstractMain() {
		super();
	}

	protected void init(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("UTF8");
		response.setContentType("text/html");
		out = response.getWriter();
		session = request.getSession();
		application = request.getServletContext();
		this.request = request;
		this.response = response;
		isAjax = KRequest.keyExists("_ajx", request);
		js = "";
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		init(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	protected String getPageHeader() {
		String txtResponse = KRequest.includeResponse(KoHttp.kHeaderURL(), request, response);
		return txtResponse.replaceFirst("(?s)" + Matcher.quoteReplacement("<!-- header -->") + ".*", "");
	}

	protected String getPageHeader(String... csss) {
		String txtResponse = getPageHeader();
		for (String css : csss) {
			txtResponse = txtResponse.replace("</head>", "<link rel='stylesheet' href='" + application.getContextPath() + "/css/" + css + "'></head>\n");
		}
		return txtResponse;
	}

	protected String getPageFooter() {
		String txtResponse = KRequest.includeResponse(KoHttp.kFooterURL(), request, response);
		return txtResponse.replaceFirst("(?s).*?" + Matcher.quoteReplacement("<!-- footer -->"), "");

	}

	protected void addJs(String js) {
		this.js += js;
	}

	protected void printJs() {
		out.print("<script>" + this.js + "</script>");
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}
}
