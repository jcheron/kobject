package net.ko.debug;

import javax.servlet.http.HttpServletRequest;

import net.ko.framework.KDebugConsole;
import net.ko.http.controls.KCtrlList;
import net.ko.types.HtmlControlType;
import net.ko.utils.KString;

public class KDebugClient {
	private String options;
	private boolean active;

	private static KDebugClient instance = null;

	private String getOptionsJSON() {
		String[] strs = options.split(",");
		int size = strs.length;
		String result = "";
		for (int i = 0; i < size - 1; i++) {
			result += strs[i] + ":" + strs[i] + ",";
		}
		result += strs[size - 1] + ":" + strs[size - 1];
		return result;
	}

	public static KDebugClient getInstance() {
		if (instance == null) {
			synchronized (KDebugClient.class) {
				instance = new KDebugClient();
			}
		}
		return instance;
	}

	private KDebugClient() {
		options = "";
		active = false;
	}

	public static String getOptions() {
		return getInstance().options;
	}

	public static void setOptions(String options) {
		if (options != null) {
			if (KString.isBooleanTrue(options))
				options = "js,include,function,submitForm,refreshControl,message,showHide,set,fireEvent,messageDialog,selector," +
						"includeDialog,button,includeForm,submitFormButton,accordion,refreshFormValues,deleteOne,updateOne";
			if (KString.isBooleanFalse(options))
				options = "";
			options = options.replaceAll("\\W", ",");
			getInstance().options = options;
		}
	}

	public static boolean hasOption(String option) {
		String options = getInstance().options;

		return ("," + options.toLowerCase() + ",").contains("," + option.toLowerCase() + ",");
	}

	public static boolean isActive() {
		return getInstance().active;
	}

	public static void setActive(boolean active) {
		getInstance().active = active;
		KDebugConsole.print("active : " + active, "FRAMEWORK", "KDebugClient.setActive");
	}

	public static String getMenu(HttpServletRequest request) {
		String result = "";
		if (!request.getRequestURI().endsWith(".main")) {
			KCtrlList list = new KCtrlList("Afficher inclusions Ajax :", "JS", "clientDebug");
			list.setType(HtmlControlType.khcCheckedList);
			list.setList("{" + getInstance().getOptionsJSON() + "}");
			result = "<div tabindex='-1' id='clientDebugMenu'><fieldset><legend>Débogage client :</legend>";
			result += list.toString();
			result += "<div class='field'><input checked type='checkbox' class='noAutoClick' id='showOnlyActiveBulles' name='showOnlyActiveBulles'><label for='showOnlyActiveBulles'>Sur éléments DOM existants</label></div>";
			result += "<hr>";

			result += "<form id='frmDebugElementSearch' onsubmit='return Forms.Framework.infoBulleSearch();'><input type='text' placeholder='Rechercher...' id='debugElementSearch'><span id='libVersion'></span></form>";
			result += "</fieldset>\n";

			result += "<fieldset id='divDebogger' style='display:none;'><legend>Exécution :</legend>";
			result += "<select multiple size='8' id='listExpressions'></select>";
			result += "<div class='boxButtons'><label><input type='checkbox' class='noAutoClick' id='hideFiltre'>non modal</label>" +
					"<label><input type='checkbox' class='noAutoClick' id='stopDebug'>stop debug</label>" +
					"<div class='btn' title='Pas à pas' id='btStepNext'><span>Next</span></div>";
			result += "<div class='btn' title='Exécuter jusqu&apos;à la fin' id='btExecute'><span>Resume</span></div>";
			result += "</div>";
			result += "</fieldset></div>";
			result += "<script>Forms.InfoBulleUtils.init();</script>";
		}
		return result;
	}
}
