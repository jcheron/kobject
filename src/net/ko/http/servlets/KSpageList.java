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
import net.ko.http.objects.KRequest;
import net.ko.http.views.KPageList;
import net.ko.kobject.KObject;
import net.ko.utils.KScriptTimer;

/**
 * Servlet implementation class KSpageList
 */
@WebServlet(name = "KSpageList", urlPatterns = { "/ajaxPageList.frm" })
public class KSpageList extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSpageList() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		KoHttp.kstart(request.getServletContext());
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		KScriptTimer.start("cls");
		String cls = KRequest.GETPOST("_cls", request);
		if (cls == null)
			cls = (String) request.getAttribute("cls");
		if (cls != null) {
			try {
				Class<KObject> mCls = (Class<KObject>) Class.forName(cls);
				KPageList kpage = new KPageList(mCls, request);
				kpage.setResponse(response, !KRequest.keyExists("_ajx", request));
				kpage.setListContentRefreshParams("_ajxContent", "ajaxPageList.frm");
				kpage.setEditFormParams("frm" + mCls.getSimpleName(), "ajaxPageList.frm");
				kpage.setMessageAndUpdateParams("_ajx", "ajaxPageList.frm");
				kpage.setHasNavBarre(true);
				kpage.setHasPageCounter(true);
				kpage.setRequestUrl(KRequest.getRequestURI(request));
				kpage.setMessageDelay(-1);
				kpage.setIsShowCaption(true);
				kpage.setFormModal(false);
				kpage.setEditable(true);
				kpage.keepFieldsByIndex(kpage.getBeforeField(), kpage.getAfterfield(), KRequest.GETPOST("_field", request, ""));
				kpage.getDefaultComplete();
				out.print(kpage);
				if (kpage.getMode() != 3)
					out.print("<br>" + KScriptTimer.get("cls"));
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Erreur de chargement de KSPageList : " + cls, e);
			}
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
