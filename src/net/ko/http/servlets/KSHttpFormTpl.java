package net.ko.http.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.http.views.KHttpForm;
import net.ko.http.views.KHttpFormTpl;
import net.ko.http.views.KMaskSOp;
import net.ko.kobject.KObject;
import net.ko.utils.KTextFile;

/**
 * Servlet implementation class KSHttpFormTpl
 */
@WebServlet(name = "KSHttpFormTpl", urlPatterns = { "*.view" })
public class KSHttpFormTpl extends KSAbstractViewTpl {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSHttpFormTpl() {
		super();
		extension = "view";
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void loadDefaultView(String className, HttpServletRequest request, HttpServletResponse response) {
		try {
			KDebugConsole.print(className, "TEMPLATE", "KSHttpFormTpl.loadDefaultView");
			PrintWriter out = response.getWriter();
			Class<KObject> clazz = (Class<KObject>) Class.forName(className);
			KHttpForm form = new KHttpForm(clazz.newInstance(), request);
			form.setResponse(response, true);
			form.getDefaultComplete("", response);
			out.print(form.toString());
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible de charger le template par défaut pour la classe : " + className, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void loadTemplateView(String fileName, String className, HttpServletRequest request, HttpServletResponse response) {
		try {
			PrintWriter out = response.getWriter();
			String strTemplate = KTextFile.open(fileName);
			KMaskSOp mask = new KMaskSOp(strTemplate);
			String classNameByOp = mask.getSpecificOperation("className", request);
			if (classNameByOp != null)
				className = classNameByOp;
			KDebugConsole.print("ClassName:" + className + ",fileName:" + fileName, "TEMPLATE", "KSHttpFormTpl.loadTemplateView");
			String strDisplay = mask.getSpecificOperation("koDisplay", request);
			String strRedirect = mask.getSpecificOperation("redirectUrl", request);
			String formName = mask.getSpecificOperation("formName", request);

			Class<KObject> clazz = (Class<KObject>) Class.forName(className);
			KHttpFormTpl form = new KHttpFormTpl(clazz.newInstance(), request, response, mask.get(0));
			form.setAutoOpen(false);
			form.setAutoClose(false);
			if (strDisplay != null)
				form.setKoDisplay(strDisplay);
			if (strRedirect == null)
				strRedirect = "";
			if (formName != null)
				form.setId(formName);
			form.getDefaultComplete(strRedirect, response);
			out.print(form.toString());
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible de charger le template : " + fileName, e);

		}
	}

}
