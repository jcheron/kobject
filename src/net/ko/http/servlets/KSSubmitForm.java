package net.ko.http.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.bootstrap.BsAlert;
import net.ko.http.controls.KCtrlInputHidden;
import net.ko.http.objects.KRequest;
import net.ko.http.views.KHttpForm;
import net.ko.kobject.KObject;

/**
 * Servlet implementation class KSSubmitForm
 */
@WebServlet(name = "submitForm", urlPatterns = { "/submitForm.frm", "/updateOne.frm" })
public class KSSubmitForm extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSSubmitForm() {
		super();
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
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String cls = KRequest.GETPOST("_cls", request);
		String shortCls = cls;
		String updateMessageMask = KRequest.GETPOST("_uMM", request, "{toString} mis à jour.");
		String noUpdateMessageMask = KRequest.GETPOST("_nuMM", request, "mise à jour de {toString} annulée.");
		String errorMessageMask = KRequest.GETPOST("_eMM", request, "erreur sur la mise à jour de {toString}.");
		if (cls != null) {
			if (!cls.contains("."))
				cls = KoHttp.kpackage() + "." + cls;
			KObject ko = null;
			KHttpForm frm = null;
			try {
				Class<KObject> clazz = (Class<KObject>) Class.forName(cls);
				ko = clazz.newInstance();
				frm = new KHttpForm(ko, request);
				frm.setPartialRequest(KRequest.keyExists("_partial", request));
				if (frm.loadAndSubmit()) {
					out.print(BsAlert.success(ko._showWithMask(updateMessageMask)));
					out.print(KoHttp.kajaxIncludes(request));
					KCtrlInputHidden input = new KCtrlInputHidden("", ko.getFirstKeyValue(), "_" + ko.getFirstKey() + shortCls + "Updated");
					out.print(input.toString());
				} else
					out.print(BsAlert.info(ko._showWithMask(noUpdateMessageMask)));
			} catch (Exception e) {
				out.print(BsAlert.danger(ko._showWithMask(errorMessageMask)));
				Ko.klogger().log(Level.WARNING, "Erreur sur la mise à jour de l'objet", e);
			}
		}
	}

}
