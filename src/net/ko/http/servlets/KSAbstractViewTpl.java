package net.ko.http.servlets;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
import net.ko.utils.KString;

import org.apache.catalina.Globals;

/**
 * Servlet implementation class KSAbstractViewTpl
 */
public abstract class KSAbstractViewTpl extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected String extension;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSAbstractViewTpl() {
		super();
	}

	protected String getClassName(String action) {
		action = action.replace("." + extension, "");
		action = action.substring(action.lastIndexOf("/") + 1);
		return KoHttp.kpackage() + ".K" + KString.capitalizeFirstLetter(action);
	}

	protected String getFileName(String className) {
		String result = className.substring(className.lastIndexOf(".") + 2);
		return result.toLowerCase() + "." + extension;
	}

	protected abstract void loadTemplateView(String fileName, String className, HttpServletRequest request, HttpServletResponse response);

	protected abstract void loadDefaultView(String className, HttpServletRequest request, HttpServletResponse response);

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		String action = (String) request.getAttribute(Globals.DISPATCHER_REQUEST_PATH_ATTR);
		String cls = KRequest.GETPOST("_cls", request);
		if (cls != null && !cls.equals(getClassName(action))) {
			request.getRequestDispatcher(getFileName(cls)).forward(request, response);
		}
		String fileName = request.getServletContext().getRealPath(action);

		KoHttp.kstart(request.getServletContext());
		File f = new File(fileName);
		if (f.exists()) {
			loadTemplateView(fileName, getClassName(action), request, response);
		} else {
			Ko.klogger().log(Level.WARNING, "Fichier de template non trouvé : " + fileName);
			loadDefaultView(getClassName(action), request, response);
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
