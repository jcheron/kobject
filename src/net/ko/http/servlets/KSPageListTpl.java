package net.ko.http.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.http.views.KMaskSOp;
import net.ko.http.views.KPageList;
import net.ko.http.views.KPageListTpl;
import net.ko.kobject.KObject;
import net.ko.utils.KTextFile;

/**
 * Servlet implementation class KSPageListTpl
 */
@WebServlet(name = "KSPageListTpl", urlPatterns = { "*.list" })
@SuppressWarnings("unchecked")
public class KSPageListTpl extends KSAbstractViewTpl implements Servlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see KSAbstractViewTpl#KSAbstractViewTpl()
	 */
	public KSPageListTpl() {
		super();
		extension = "list";
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
		super.doPost(request, response);
	}

	@Override
	protected void loadTemplateView(String fileName, String className, HttpServletRequest request, HttpServletResponse response) {
		try {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();

			String strTemplate = KTextFile.open(fileName);
			File f = new File(fileName);
			KMaskSOp mask = new KMaskSOp(strTemplate);
			String classNameByOp = mask.getSpecificOperation("className", request);
			if (classNameByOp != null)
				className = classNameByOp;
			KDebugConsole.print("ClassName:" + className + ",fileName:" + fileName, "TEMPLATE", "KSPageList.loadTemplateView");
			String strDisplay = mask.getSpecificOperation("koDisplay", request);

			Class<KObject> clazz = (Class<KObject>) Class.forName(className);

			KPageListTpl kpage = null;
			String strSQL = mask.getSpecificOperation("sql", request);

			if (strSQL != null)
				kpage = new KPageListTpl(clazz, request, response, strSQL, mask.get(0));
			else
				kpage = new KPageListTpl(clazz, request, response, mask.get(0));

			if (strDisplay != null)
				kpage.setKoDisplay(strDisplay);
			kpage.setHasNavBarre(true);
			kpage.setHasPageCounter(true);
			kpage.setHasFiltre(true);
			kpage.setHasAddBtn(true);
			kpage.setCreateAjaxDivs(true);
			String id = clazz.getSimpleName();
			kpage.setName(id);
			kpage.setId(id);
			// kpage.setEditFormParams("frm" + id, f.getName().replace("." +
			// extension, ".view"));
			kpage.setIdForm("frm" + id);
			// kpage.setMessageAndUpdateUrl(f.getName());
			if (kpage.getMode() == 1)
				kpage.load();
			else if (kpage.getMode() == 3)
				kpage.loadForExec();
			out.print(kpage.toString());
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible de charger le template : " + fileName, e);

		}
	}

	@Override
	protected void loadDefaultView(String className, HttpServletRequest request, HttpServletResponse response) {
		try {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			KDebugConsole.print(className, "TEMPLATE", "KSPageList.loadDefaultView");
			PrintWriter out = response.getWriter();
			Class<KObject> clazz = (Class<KObject>) Class.forName(className);
			KPageList kpage = new KPageList(clazz, request);
			kpage.setCreateAjaxDivs(true);
			kpage.setEditable(true);
			kpage.setHasNavBarre(true);
			kpage.setHasPageCounter(true);
			kpage.setHasFiltre(true);
			kpage.setIdForm("frm" + clazz.getSimpleName());
			kpage.setListContentUrl(getFileName(className));
			kpage.setResponse(response, true);
			kpage.getDefaultComplete();
			out.print(kpage.toString());
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible de charger le template par défaut pour la classe : " + className, e);

		}
	}

}
