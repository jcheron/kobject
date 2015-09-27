package net.ko.framework;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.ko.debug.KDebugClient;
import net.ko.displays.KHttpDisplay;
import net.ko.http.objects.KHttpSession;
import net.ko.inheritance.KReflectObject;
import net.ko.kobject.KSession;
import net.ko.mapping.KAjaxIncludes;
import net.ko.mapping.KXmlMappings;
import net.ko.types.KRestartType;
import net.ko.utils.KRegExpr;

/**
 * Classe Singleton de lancement d'une application Web java de type KObject
 * 
 * @author jcheron
 * 
 */
public class KoHttp extends Ko {
	private KAjaxIncludes ajaxIncludes;
	private ServletContext servletContext;

	/**
	 * @return l'instance d'application KoHttp
	 */
	public static boolean allowAjaxIncludes = true;

	public static KoHttp getInstance() {
		if (instance == null) {
			synchronized (KoHttp.class) {
				instance = new KoHttp();
			}
		}
		try {
			instance = (KoHttp) instance;
		} catch (ClassCastException ce) {
			synchronized (KoHttp.class) {
				instance = new KoHttp();
			}
		}
		return (KoHttp) instance;
	}

	/**
	 * Démarre l'application Ko, en créant si nécessaire l'instance
	 * d'application active KoHttp
	 * 
	 * @param session
	 *            session http en cours
	 */
	public static void krestart(ServletContext context) throws ClassNotFoundException, SQLException, IOException, InstantiationException, IllegalAccessException {
		Ko.kstop();
		kstart(context, true);
	}

	public static void krestart(ServletContext context, String options) throws ClassNotFoundException, SQLException, IOException, InstantiationException, IllegalAccessException {
		if (options == null || "".equals(options.replace(";", ""))) {
			krestart(context);
			KDebugConsole.print("ReDémarrage complet de KObject", "FRAMEWORK", "KoHttp.krestart");
		} else {
			String[] optionsValues = options.split(";");
			for (String option : optionsValues) {
				if (option.equals(KRestartType.rtBddConn.value)) {
					kdatabase();
					KDebugConsole.print("ReDémarrage - connexion à la BDD", "FRAMEWORK", "KoHttp.krestart");
				}
				if (option.equals(KRestartType.rtValidation.value)) {
					Ko.getInstance().setValidationFile();
					KDebugConsole.print("ReDémarrage - lecture de kox.xml", "FRAMEWORK", "KoHttp.krestart");
				}
				if (option.equals(KRestartType.rtMessages.value)) {
					Ko.getInstance().setMessagesFile();
					Ko.getInstance().setErFile();
					KDebugConsole.print("ReDémarrage - lecture des fichiers er et messages", "FRAMEWORK", "KoHttp.krestart");
				}
				if (option.equals(KRestartType.rtMappings.value)) {
					KXmlMappings.stop();
					Ko.getInstance().setMappingFile();
					KDebugConsole.print("ReDémarrage - lecture de mox.xml", "FRAMEWORK", "KoHttp.krestart");
				}
				if (option.equals(KRestartType.rtCssVars.value)) {
					Ko.styleSheets = new HashMap<>();
					Ko.getInstance().loadCssVars();
					KDebugConsole.print("ReDémarrage - chargement des css", "FRAMEWORK", "KoHttp.krestart");
				}
			}
		}
	}

	public static void kstart(ServletContext context) {
		kstart(context, false);
	}

	public static void kstart(ServletContext context, boolean noDb) {
		new KHttpDisplay();
		KoHttp inst = (KoHttp) getInstance(context);
		if (context != null) {
			inst.servletContext = context;
			String path = context.getRealPath("/db");
			path = path.replace("\\", "/");
			System.setProperty("dbPath", path);
		}
		if (!inst.isLoaded()) {
			inst.start(noDb);
			inst.setLoaded(true);
		}
		// inst.init();
	}

	private static KoHttp getInstance(ServletContext context) {
		return getInstance();
	}

	public static void setPath(String koPath) {
		getInstance().setKoPath(koPath);
	}

	public KAjaxIncludes getAjaxIncludes() {
		return ajaxIncludes;
	}

	public static KAjaxIncludes kajaxIncludes() {
		KoHttp ko = (KoHttp) getInstance();
		return ko.getAjaxIncludes();
	}

	public static ServletContext kServletContext() {
		KoHttp ko = (KoHttp) getInstance();
		return ko.getServletContext();
	}

	public static String kajaxIncludes(HttpServletRequest request, HttpServletResponse response) {
		String debugClientStr = "";
		KoHttp ko = (KoHttp) getInstance();
		KAjaxIncludes ajaxIncludes = ko.getAjaxIncludes();
		synchronized (ajaxIncludes) {
			ajaxIncludes.process(request, response);
			if (KDebugClient.isActive()) {
				if (request.getParameter("_ajx") != null)
					debugClientStr = "<script type='text/javascript'>Forms.DOM.onReady(function(){Forms.Framework.ckInfoBulleMenuLoad();});</script>";
			}
		}
		return ajaxIncludes.toString() + debugClientStr;
	}

	public static String kajaxIncludes(HttpServletRequest request) {
		String result = "<!-- ajax-includes -->";
		if (request.getAttribute("ajaxIncludes") != null)
			result += request.getAttribute("ajaxIncludes") + "";
		if (koUseRequestVarsInMox)
			result = replaceRequestExpressions(result, request);
		return result;
	}

	public static String replaceRequestExpressions(String input, HttpServletRequest request) {
		String output = input;
		if (input != null) {
			if (input.contains("{application:")) {
				ServletContext application = request.getServletContext();
				ArrayList<String> appVars = KRegExpr.getGroups("\\{application\\:(.*?)\\}", output, 0);
				for (String appVar : appVars) {
					appVar = getRealVarName(appVar, "application");
					Object o = application.getAttribute(appVar);
					if (o != null)
						output = output.replaceAll("\\{application\\:" + appVar + "\\}", o + "");
					else
						output = output.replaceAll("\\{application\\:" + appVar + "\\}", "");
				}
			}
			if (input.contains("{session:")) {
				HttpSession session = request.getSession();
				ArrayList<String> sessionVars = KRegExpr.getGroups("\\{session\\:(.*?)\\}", output, 0);
				for (String sessionVar : sessionVars) {
					sessionVar = getRealVarName(sessionVar, "session");
					Object o = session.getAttribute(sessionVar);
					if (o != null)
						output = output.replaceAll("\\{session\\:" + sessionVar + "\\}", o + "");
					else
						output = output.replaceAll("\\{session\\:" + sessionVar + "\\}", "");
				}
			}
			if (input.contains("{request:")) {
				ArrayList<String> requestVars = KRegExpr.getGroups("\\{request\\:(.*?)\\}", output, 0);
				for (String requestVar : requestVars) {
					requestVar = getRealVarName(requestVar, "request");
					Object o = request.getAttribute(requestVar);
					if (o != null)
						output = output.replaceAll("\\{request\\:" + requestVar + "\\}", o + "");
					else
						output = output.replaceAll("\\{request\\:" + requestVar + "\\}", "");
				}
			}
			if (input.toLowerCase().contains("{moxclass:")) {
				ArrayList<String> moxVars = KRegExpr.getGroups("\\{moxclass\\:(.*?)\\}", output, 0);
				for (String moxVar : moxVars) {
					moxVar = getRealVarName(moxVar, "moxclass");
					try {
						Object o = KReflectObject.kinvoke(moxVar, Ko.moxClass, new Object[] { request });
						if (o != null)
							output = output.replaceAll("(?i)\\{moxclass\\:" + moxVar + "\\}", o + "");
						else
							output = output.replaceAll("(?i)\\{moxclass\\:" + moxVar + "\\}", "");
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						Ko.klogger().log(Level.WARNING, "Impossible d'appeler la méthode " + moxVar + " sur " + Ko.moxClass, e);
					}
				}
			}
		}
		return output;
	}

	protected static String getRealVarName(String varName, String prefix) {
		String result = varName;
		if (varName.toLowerCase().contains("{" + prefix + ":")) {
			result = varName.replaceFirst("(?i)\\{" + prefix + "\\:(.*?)\\}", "$1");
		}
		return result;
	}

	@Override
	public KXmlMappings setMappingFile(String mappingFile) {
		KXmlMappings result = super.setMappingFile(mappingFile);
		ajaxIncludes = result.getAjaxIncludes();
		return result;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public KSession getKsession(Object s) {
		return new KHttpSession((HttpSession) s);
	}

}
