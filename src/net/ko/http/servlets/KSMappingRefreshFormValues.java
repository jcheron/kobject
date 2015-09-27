package net.ko.http.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.controller.KObjectController;
import net.ko.controller.KObjectFieldController;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.framework.KoSession;
import net.ko.kobject.KObject;
import net.ko.types.KStringsType;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.KoUtils;

/**
 * Servlet implementation class KSMappingRefreshFormValues
 */
@WebServlet(name = "refreshFormValues", urlPatterns = { "/refreshFormValues.frm" })
public class KSMappingRefreshFormValues extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private KObjectDisplay displayInstance;
	private String koDisplay;
	private KObject ko;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSMappingRefreshFormValues() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String className = request.getParameter("_className");
		if (KString.isNotNull(className)) {
			String keyValues = request.getParameter("_keyValues");
			String excludedFields = request.getParameter("_excludedFields");
			load(request, response, className, keyValues, excludedFields);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	@SuppressWarnings("unchecked")
	protected void load(HttpServletRequest request, HttpServletResponse response, String className, String keyValues, String excludedFields) throws IOException {
		try {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			Class<KObject> clazz = (Class<KObject>) Class.forName(Ko.kpackage() + "." + className);
			ko = KoSession.kloadOne(clazz, keyValues.split("\\W&&[^\\-]"));
			if (!ko.isLoaded()) {
				ko = clazz.newInstance();
				Ko.klogger().log(Level.WARNING, "Impossible de charger l'instance de " + className + " pour  l'inclusion refreshFormValues");
			}
			out.print(getJSON(request, keyValues, excludedFields));

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			Ko.klogger().log(Level.SEVERE, "Impossible de construire l'instance de " + className + " pour  l'inclusion refreshFormValues", e);
		}
	}

	protected String getJSON(HttpServletRequest request, String keyValues, String excludedFields) throws IllegalArgumentException, IllegalAccessException {
		List<String> excludes = Arrays.asList(excludedFields.split("\\W&&[^\\-]"));
		KStrings jsonFields = new KStrings(KStringsType.kstJSON);
		KObjectController koc = null;
		koc = Ko.kcontroller().getObjectController(ko.getClass().getSimpleName());
		KObjectDisplay display = getDisplayInstance();
		ko.refresh();
		if (display.getRefreshFields() != null) {
			for (String field : display.getRefreshFields()) {
				if (excludes.indexOf(field) == -1) {
					jsonFields.put(field, KString.encodeURIComponent(display.getRefreshValue(ko, field, koc, request)));
				}
			}
		}
		for (Map.Entry<String, KObjectFieldController> e : koc.getMembers().entrySet()) {
			String field = e.getKey();
			if (excludes.indexOf(field) == -1) {
				jsonFields.put(field, KString.encodeURIComponent(display.getRefreshValue(ko, field, koc, request)));
			}
		}
		jsonFields.put("koKeyValue", ko.getFirstKeyValue());
		return jsonFields.toJSON();
	}

	protected KObjectDisplay getKobjectDisplay() {
		KObjectDisplay koDisplay = null;
		if (ko != null) {
			Class<? extends KObject> clazz = ko.getClass();
			koDisplay = Ko.koDisplays.get(clazz.getSimpleName());
			if (koDisplay == null)
				koDisplay = Ko.defaultKoDisplay();
		} else
			koDisplay = Ko.defaultKoDisplay();
		return koDisplay;
	}

	protected KObjectDisplay getDisplayInstance() {
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
}
