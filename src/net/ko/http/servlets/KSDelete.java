package net.ko.http.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.dao.IGenericDao;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.framework.KoSession;
import net.ko.http.bootstrap.BsAlert;
import net.ko.http.controls.KCtrlInputHidden;
import net.ko.http.objects.KRequest;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;

/**
 * Servlet implementation class KSDelete
 */
@WebServlet(name = "KSDelete", urlPatterns = { "/deleteOne.frm", "/deleteMulti.frm" })
public class KSDelete extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSDelete() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean multiple = KRequest.keyExists("_multiple", request);
		if (multiple) {
			deleteMulti(request, response);
		} else {
			deleteOne(request, response);
		}
	}

	protected void deleteMulti(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String deleteMessageMask = KRequest.GETPOST("_uMM", request, "{toString} supprimé.");
		String errorMessageMask = KRequest.GETPOST("_eMM", request, "Suppression impossible");
		String cls = KRequest.GETPOST("_cls", request);

		if (cls != null) {
			if (!cls.contains("."))
				cls = KoHttp.kpackage() + "." + cls;
			KObject ko = null;
			try {
				Class<KObject> clazz = (Class<KObject>) Class.forName(cls);

				String strKeys = KRequest.GETPOST("_keyValues", request, "");
				String[] tValeurs = strKeys.split(",");
				KListObject<? extends KObject> objects = KoSession.kloadMany(clazz, tValeurs);
				for (KObject o : objects) {
					KoSession.delete(o);
				}

				out.print(BsAlert.success(objects.showWithMask(deleteMessageMask)));
			} catch (Exception e) {
				out.print(BsAlert.danger(ko._showWithMask(errorMessageMask)));
				Ko.klogger().log(Level.WARNING, "Erreur sur la suppression des objets", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void deleteOne(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String deleteMessageMask = KRequest.GETPOST("_uMM", request, "{toString} supprimé.");
		String errorMessageMask = KRequest.GETPOST("_eMM", request, "Suppression impossible");
		String cls = KRequest.GETPOST("_cls", request);
		String shortCls = cls;

		if (cls != null) {
			if (!cls.contains("."))
				cls = KoHttp.kpackage() + "." + cls;
			KObject ko = null;
			try {
				Class<KObject> clazz = (Class<KObject>) Class.forName(cls);
				ko = clazz.newInstance();
				String strKeys = KRequest.GETPOST("_keyValues", request, "");
				List<Object> keyValues = new ArrayList<Object>(Arrays.asList(strKeys.split(",")));
				ko.setKeyValues(keyValues);
				IGenericDao<KObject> dao = (IGenericDao<KObject>) Ko.getDao(clazz);
				dao.read(ko);
				if (ko.isLoaded()) {
					dao.delete(ko);
					out.print(BsAlert.success(ko._showWithMask(deleteMessageMask)));
					out.print(KoHttp.kajaxIncludes(request));
					KCtrlInputHidden input = new KCtrlInputHidden("", ko.getFirstKeyValue(), "_" + ko.getFirstKey() + shortCls + "Deleted");
					out.print(input.toString());
				}
			} catch (Exception e) {
				out.print(BsAlert.danger(ko._showWithMask(errorMessageMask)));
				Ko.klogger().log(Level.WARNING, "Erreur sur la suppression de l'objet", e);
			}
		}
	}
}
