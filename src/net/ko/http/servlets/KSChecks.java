package net.ko.http.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.check.KCheckConfig;
import net.ko.check.KCheckKox;
import net.ko.check.KCheckOptions;
import net.ko.check.KcheckMox;
import net.ko.framework.Ko;
import net.ko.http.objects.KRequest;
import net.ko.http.views.KHtmlFieldControl;
import net.ko.types.HtmlControlType;
import net.ko.utils.KString;
import net.ko.utils.KStrings;

/**
 * Servlet implementation class KSChecks
 */
@WebServlet(name = "Check", urlPatterns = { "/check.main" })
public class KSChecks extends KSAbstractMain {
	private static final long serialVersionUID = 1L;
	protected static Map<String, String> responsesText;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSChecks() {
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		init(request, response);

		synchronized (out) {
			String op = KRequest.GETPOST("_op", request, "all");
			switch (op) {
			case "all":
				all();
				break;
			case "run":
				responsesText = new HashMap<>();
				run();
				break;
			case "checkSelector":
				checkSelectors();
				break;
			default:
				break;
			}
		}
	}

	private void checkSelectors() {
		String id = KRequest.GETPOST("id", request);
		if (KString.isNotNull(id)) {
			if (responsesText.containsKey(id)) {
				String resp = responsesText.get(id);
				boolean ajaxIncludes = resp.contains("<!-- ajax-includes -->");
				resp = KString.getPart("<!-- header -->", "<!-- footer -->", resp);
				if (ajaxIncludes && !resp.contains("<!-- ajax-includes -->"))
					resp += "<!-- ajax-includes -->";
				out.print(resp);
			}
			else
				out.print("Réponse introuvable");
		}
	}

	private void run() {
		out.print("<fieldset><legend>Résultats de la vérification</legend>");
		KCheckConfig checkConfig = new KCheckConfig(request);
		checkConfig.setOptions(KRequest.GETPOST("opsCheck", request, ""), KRequest.GETPOST("opsAffCheck", request, ""));
		if (checkConfig.getOptions().contains(KCheckOptions.coConfig)) {
			checkConfig.setExpand(KRequest.GETPOST("_ckExpand", request, false));
			checkConfig.checkAll();
			out.print(checkConfig.getMessages());
		}
		KCheckKox checkKox = new KCheckKox(request);
		checkKox.setOptions(KRequest.GETPOST("opsCheck", request, ""), KRequest.GETPOST("opsAffCheck", request, ""));
		checkKox.setExpand(KRequest.GETPOST("_ckExpand", request, false));
		checkKox.checkAll();
		out.print(checkKox.getMessages());
		KcheckMox checkMox = new KcheckMox(request, response);
		checkMox.setOptions(KRequest.GETPOST("opsCheck", request, ""), KRequest.GETPOST("opsAffCheck", request, ""));
		checkMox.setExpand(KRequest.GETPOST("_ckExpand", request, false));
		checkMox.checkAll();
		out.print(checkMox.getMessages());
		out.print("</fieldset>");
		out.print("<div id='selectors' style='display:none;'></div>");
		if (checkMox.getOptions().contains(KCheckOptions.coAjaxIncludes))
			addJs(checkMox.getCheckSelectors().getJsRequests());
		printJs();
		out.flush();
	}

	protected void all() {
		if (!isAjax)
			out.print(getPageHeader("admin.kcss"));
		out.print("<div id='mainCheck'>");
		out.print("<div id='_commandes'><form id='frmChecks'><fieldset><legend>Vérifications</legend><div id='infos'></div>");
		KStrings listOps = new KStrings();
		listOps.put(KCheckOptions.coConfig.getCaption(), "Fichier de configuration " + Ko.configFile);
		listOps.put(KCheckOptions.coClasses.getCaption(), "Classes");
		listOps.put(KCheckOptions.coDb.getCaption(), "Structure de la base de données");
		listOps.put(KCheckOptions.coControllers.getCaption(), "Validation (" + Ko.getInstance().getValidationFile() + ")");
		listOps.put(KCheckOptions.coMappings.getCaption(), "Mappings (mox.xml)");
		listOps.put(KCheckOptions.coFilters.getCaption(), "Filters (mox.xml)");
		listOps.put(KCheckOptions.coAjaxIncludes.getCaption(), "Inclusions ajax (mox.xml)");
		KHtmlFieldControl opsCheck = new KHtmlFieldControl("Options de vérification", "", "opsCheck", "opsCheck", HtmlControlType.khcCheckedList, listOps, "");
		out.print("<div class='optBoxLeft'>" + opsCheck + "</div>");
		KStrings listOpsAff = new KStrings();
		listOpsAff.put("valid", "<span class='validMessage'>Ok</span>");
		listOpsAff.put("info", "<span class='infoMessage'>Informations</span>");
		listOpsAff.put("warning", "<span class='warningMessage'>Warnings</span>");
		listOpsAff.put("error", "<span class='errMessage'>Erreurs</span>");
		KHtmlFieldControl opsAffCheck = new KHtmlFieldControl("Affichage", "info;warning;error", "opsAffCheck", "opsAffCheck", HtmlControlType.khcCheckedList, listOpsAff, "");
		out.print("<div class='optBox'>" + opsAffCheck + "</div>");
		KHtmlFieldControl ckExpand = new KHtmlFieldControl("Expand", "", "ckExpand", "ckExpand", HtmlControlType.khcCheckBox);
		out.print("<div id='boxButtons' class='boxButtons'>" + ckExpand + getButton("Lancer la vérification", "btRunChecks", "Lancer la vérification des éléments sélectionnés", "btn", "") + "</div>");
		out.print("</fieldset></form></div>");
		out.print("<div id='checkResults'></div>");
		out.print("</div>");
		addJs("$addEvt($('btRunChecks'), 'click', function(){$postForm('frmChecks','checkResults','check.main','_op=run');});");
		addJs("$addEvt($('_ckExpand'), 'click', function(e){var t=$gte(e);\nvar s=(t.checked)?'.arrow-down':'.arrow-up';\n (new $e(s)).each(function(k,v){Forms.Utils.fireEvent(v,'click');});});");
		printJs();
		if (!isAjax)
			out.print(getPageFooter());
	}

	protected String getButton(String caption, String id, String title, String className, String spanClassName) {
		return "<div class='" + className + "' title=\"" + title + "\" id='" + id + "'><span class='" + spanClassName + "'>" + caption + "</span></div>";
	}

	public static Map<String, String> getResponsesText() {
		return responsesText;
	}
}
