package net.ko.http.controls;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.dao.IGenericDao;
import net.ko.framework.Ko;
import net.ko.http.objects.KRequest;
import net.ko.http.views.KHtmlFieldControl;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.utils.KString;

/**
 * Servlet implementation class KAjaxListContent
 */
@WebServlet(name = "KAjaxListContent", urlPatterns = { "*.KAjaxListContent" })
public class KAjaxListContent extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KAjaxListContent() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		String field = KRequest.GETPOST("_field", request, "");
		String listMask = KRequest.GETPOST("_listMask", request, "");
		String className = KRequest.GETPOST("_cls", request, "");
		String classListName = KRequest.GETPOST("_clsList", request, "");
		String value = KRequest.GETPOST("_value", request, "");
		String originalValue = KRequest.GETPOST("_oValue", request, "");
		String search = KRequest.GETPOST("_search", request, "%");
		if (field != "" && className != "") {
			try {
				try {
					Ko.setTempConstraintDeph(0);

					PrintWriter out = response.getWriter();
					Class<KObject> clazz = (Class<KObject>) Class.forName(className);
					Class<KObject> clazzList = (Class<KObject>) Class.forName(classListName);
					IGenericDao<KObject> dao = Ko.getDao(clazzList);
					KObject ko = Ko.getKoInstance(clazz);
					KListObject<KObject> listObject = new KListObject<>(clazzList);
					String sql = KObject.makeSQLFilter_(search, clazzList, dao.quote());
					dao.setSelect(listObject, sql);
					// listObject = dao.readAll();
					value = KString.cleanStringArray(value, Ko.krequestValueSep());
					if (!"".equals(value)) {
						KListObject<KObject> selecteds = new KListObject<>(clazzList);
						selecteds = dao.readAll(value.split(Ko.krequestValueSep()));
						listObject.addAll(selecteds);
					}
					if (!"".equals(originalValue)) {
						// TODO DAO !
						listObject.deleteByKeys(originalValue, Ko.krequestValueSep());
					}
					KHtmlFieldControl fc = new KHtmlFieldControl("", value, field, field, ko.getControlType(field), listObject, "");
					if (listMask != null && !"".equals(listMask))
						fc.setListMask(listMask);
					out.print(fc.mapListToString());
				} catch (Exception e) {
				}
			} finally {
				Ko.restoreConstraintDeph();
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
