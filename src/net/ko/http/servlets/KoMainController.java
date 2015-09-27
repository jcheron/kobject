package net.ko.http.servlets;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
import net.ko.mapping.KFilterMappings;

/**
 * Servlet implementation class KoMainController
 */
@WebServlet(name = "KoMainController", urlPatterns = { "*.do" })
public class KoMainController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Ko ko;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KoMainController() {
		super();
	}

	private void kstart(ServletContext context) {
		if (ko == null) {
			KoHttp.kstart(context, true);
			ko = KoHttp.getInstance();
			KDebugConsole.print("Ko Framework started:" + ko, "FRAMEWORK", "KoMainController.kstart");
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		service(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		kstart(request.getServletContext());
		String ajaxIncludes = KoHttp.kajaxIncludes(request, response);
		if (!"".equals(ajaxIncludes)) {
			ajaxIncludes = setRequestParametersTo(request, ajaxIncludes);
			request.setAttribute("ajaxIncludes", ajaxIncludes);
		}
		KFilterMappings mappings = KoHttp.kmappings();
		if (mappings != null)
			mappings.process(request, response);
		else {
			KDebugConsole.print("no mappings", "MAPPING", "KoMainController.service");
			super.service(request, response);
		}
	}

	protected String setRequestParametersTo(HttpServletRequest request, String ajaxIncludes) {
		String result = ajaxIncludes;
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			result = result.replace("[" + name + "]", KRequest.GETPOST(name, request, ""));
		}
		return result;
	}
}
