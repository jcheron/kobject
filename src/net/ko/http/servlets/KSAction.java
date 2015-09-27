package net.ko.http.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.http.objects.KRequest;
import net.ko.inheritance.KReflectObject;

/**
 * Servlet implementation class KSAction
 */

public class KSAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected String action = "";
	protected HttpSession session;
	protected PrintWriter out;
	protected ServletContext application;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSAction() {
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
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		init(request, response);
		action = KRequest.GETPOST("_action", request, "index");
		String param = KRequest.GETPOST("_parameters", request);
		String[] params = new String[] {};
		if (param != null) {
			if (param.startsWith("/"))
				param = param.substring(1);
			params = param.split("\\/");
		}
		try {
			KDebugConsole.print("Tentative d'appel de Servlet : [" + this + "] ,Action : [" + action + "], params : [" + params + "]", "MAPPING", "KSAction");
			KReflectObject.kinvoke(action, this, params);
		} catch (Exception e) {
			Ko.klogger().log(Level.SEVERE, "L'action " + action + " est introuvable", e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * Page index
	 */
	public void index() {
		out.print(this.getClass() + " index");
	}
}
