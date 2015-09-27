package net.ko.http.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.controller.KObjectController;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
import net.ko.http.views.KHtmlFieldControl;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.mapping.KAjaxIncludes;
import net.ko.types.HtmlControlType;
import net.ko.utils.KString;
import net.ko.utils.KoUtils;

/**
 * Servlet implementation class KSControl
 */
@WebServlet(name = "refreshControl", urlPatterns = { "/refreshControl.frm" })
public class KSControl extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String koDisplay;
	private KObjectDisplay displayInstance;
	private String className;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSControl() {
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
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		koDisplay = KRequest.GETPOST("_koDisplay", request);
		className = null;
		displayInstance = null;
		String virtualURL = KRequest.GETPOST("_virtualURL", request);
		String name = KRequest.GETPOST("_name", request);
		KHtmlFieldControl fc = null;
		if (name != null) {
			className = KRequest.GETPOST("_className", request);
			if (className != null) {
				try {
					Class<KObject> clazz = null;
					if (!className.contains("."))
						clazz = (Class<KObject>) Class.forName(Ko.kpackage() + "." + className);
					else {
						clazz = (Class<KObject>) Class.forName(className);
						className = clazz.getSimpleName();
					}
					KObject ko = Ko.getKoInstance(clazz);
					KObjectController koc = Ko.kcontroller().getObjectController(className);
					displayInstance = getDisplayInstance();
					fc = (KHtmlFieldControl) displayInstance.getControl(ko, name, koc, request);
					String filterList = KRequest.GETPOST("_filterList", request);
					if (fc != null && filterList != null) {
						filterList = KString.urlDecode(filterList);
						if (fc.getListObject() instanceof KListObject) {
							KListObject<KObject> lo = (KListObject<KObject>) fc.getListObject();
							lo = lo.select(filterList);
							fc.setListObject(lo);
						}
					}
				} catch (Exception e) {
					Ko.klogger().log(Level.WARNING, "Impossible de créer le contrôle `" + name + "`", e);
				}
			}
			if (fc == null) {
				fc = new KHtmlFieldControl();
				fc.setName(name);
				fc.setId(name);
				fc.setFieldType(HtmlControlType.khcText);
			}
			String value = KRequest.GETPOST("_value", request);
			if (value != null)
				fc.setValue(value);
			String controlType = KRequest.GETPOST("_controlType", request);
			if (controlType != null)
				fc.setControlType(controlType);
			fc.setOnlyField(true);
			out.print(fc.toString());
			if (virtualURL != null) {
				KAjaxIncludes ai = KoHttp.kajaxIncludes();
				if (ai.processByName(virtualURL))
					out.print(ai.toString());
			}
		}
	}

	public KObjectDisplay getDisplayInstance() {
		if (displayInstance == null) {
			if (koDisplay == null || "".equals(koDisplay))
				displayInstance = getKobjectDisplay();
			else {
				try {
					displayInstance = (KObjectDisplay) KoUtils.getInstance(koDisplay);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					displayInstance = Ko.defaultKoDisplay();
					Ko.klogger().log(Level.WARNING, "Impossible de trouver la classe Display :" + koDisplay, e);
				}
			}
		}
		return displayInstance;
	}

	protected KObjectDisplay getKobjectDisplay() {
		KObjectDisplay koDisplay = null;
		if (className != null) {
			koDisplay = Ko.koDisplays.get(className);
			if (koDisplay == null)
				koDisplay = Ko.defaultKoDisplay();
		} else
			koDisplay = Ko.defaultKoDisplay();
		return koDisplay;
	}
}
