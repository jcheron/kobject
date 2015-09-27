package net.ko.http.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KoHttp;
import net.ko.http.views.KTemplateView;
import net.ko.utils.KTextFile;

import org.apache.catalina.Globals;

/**
 * Servlet implementation class KSTplTemplate
 */
@WebServlet(name = "KSTplTemplate", urlPatterns = { "*.tpl" })
public class KSTplTemplate extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSTplTemplate() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		String action = (String) request.getAttribute(Globals.DISPATCHER_REQUEST_PATH_ATTR);

		String fileName = request.getServletContext().getRealPath(action);
		KoHttp.kstart(request.getServletContext());
		File f = new File(fileName);
		if (f.exists()) {
			String strTemplate = KTextFile.open(fileName);
			KTemplateView tplView = new KTemplateView(request, strTemplate);
			tplView.setResponse(response, true);
			out.print(tplView.toString());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
