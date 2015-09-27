package net.ko.http.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
import net.ko.http.views.KHtmlFieldControl;
import net.ko.mapping.IAjaxObject;
import net.ko.mapping.IHasURL;
import net.ko.mapping.KAbstractFilterMapping;
import net.ko.mapping.KAjaxIncludeDialog;
import net.ko.mapping.KAjaxIncludes;
import net.ko.mapping.KAjaxJs;
import net.ko.mapping.KAjaxRequest;
import net.ko.mapping.KAjaxWithChilds;
import net.ko.mapping.KFilterMappings;
import net.ko.mapping.KMapping;
import net.ko.types.HtmlControlType;
import net.ko.utils.KString;

/**
 * Servlet implementation class KSiteMap
 */
@WebServlet(name = "KSiteMap", urlPatterns = { "/siteMap.main" })
public class KSiteMap extends KSAbstractMain {

	private static final long serialVersionUID = 1L;
	private KAjaxIncludes ajaxIncludes;
	private boolean onlyURL = false;
	private boolean reloadMappings = false;
	ArrayList<String> urls;
	ArrayList<Integer> ids;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSiteMap() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		init(request, response);
		String op = KRequest.GETPOST("_op", request, "all");
		js = "";
		KoHttp.kstart(application);
		urls = new ArrayList<>();
		ids = new ArrayList<>();
		String path = KRequest.GETPOST("path", request, "index.do");
		String breadcrumb = KRequest.GETPOST("breadcrumb", request, "");
		int depth = KRequest.GETPOST("depth", request, 0);
		onlyURL = KString.isBooleanTrue(KRequest.GETPOST("_onlyURL", request));
		reloadMappings = KRequest.GETPOST("reloadMappings", request, false);
		if (reloadMappings) {
			try {
				KoHttp.krestart(request.getServletContext(), "2");
			} catch (Exception e) {
			}
		}
		ajaxIncludes = KoHttp.kajaxIncludes();
		boolean hidePanel = KRequest.keyExists("_hidePanel", request);
		if (!isAjax)
			out.print(getPageHeader("admin.kcss"));
		if ("all".equals(op))
			out.print("<div id='mainSiteMap'>");
		out.print("<div id='siteMain'>");
		if (!hidePanel)
			out.print(getPanel(request));
		out.print("<div id='siteMap'>");
		out.print(display(path, depth, 0, 0, breadcrumb, request));
		out.print("</div>");
		if (!hidePanel)
			out.print("</div>");
		if ("all".equals(op))
			out.print("</div>");
		printJs();
		if (!isAjax)
			out.print(getPageFooter());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public String display(String url, int depth, int niveau, int margin, String breadcrumb, HttpServletRequest request) {
		if (urls.indexOf(url) != -1)
			return "";
		String sep = "";
		if (!"".equals(breadcrumb))
			sep = ";";
		String result = "";
		for (KAbstractFilterMapping filterMapping : ajaxIncludes.getItems()) {
			if (filterMapping instanceof KAjaxRequest) {
				KAjaxRequest ajaxReq = (KAjaxRequest) filterMapping;
				if (ajaxReq.matches(url, request)) {
					result += "<div>";
					result += display(ajaxReq, depth, niveau, margin, breadcrumb + sep + url, request);
					result += "</div>";
					urls.add(ajaxReq.getRequestURL());
				}
			}
		}
		return result;
	}

	public String display(KAjaxRequest ajaxReq, int depth, int niveau, int margin, String breadcrumb, HttpServletRequest request) {
		String result = "";
		if (niveau == 0) {
			String id = ajaxReq.getUniqueIdHash(getRandomId());
			result += getDisplayCaption(ajaxReq, id, margin);
			result += "<div id='" + id + "'>";
		}
		for (IAjaxObject ajaxReqChild : ajaxReq.getAjaxIncludes()) {
			String id = ajaxReqChild.getUniqueIdHash(getRandomId());
			if (!onlyURL || (ajaxReqChild instanceof IHasURL)) {
				result += "<div>";
				result += getDisplayCaption(ajaxReqChild, id, margin + 1);
				result += "</div>";
			}
			result += "<div id='" + id + "'>";
			if (ajaxReqChild instanceof KAjaxJs) {
				for (IAjaxObject ajaxObj : ((KAjaxJs) ajaxReqChild).getChilds()) {
					result += display(ajaxObj, depth, niveau, margin + 1, 0, breadcrumb, request);
				}
			}
			result += "</div>";
		}
		if (niveau == 0)
			result += "</div>";
		return result;
	}

	private String getDisplayCaption(IAjaxObject ajaxObj, String id, int margin) {
		String disp = ajaxObj.display(margin);
		String caption = ajaxObj.getDisplayCaption();
		if (!"".equals(caption) && ajaxObj.hasChilds())
			disp = disp.replace(caption, "<a onclick='Forms.Utils.toogleText($(\"imgToogle" + id + "\"),\"" + id + "\",false);'>" + caption + "<div id='imgToogle" + id + "' class='arrow-up'></div></a>");
		return disp;
	}

	private String getDisplayCaption(KAjaxRequest ajaxRequest, String id, int margin) {
		String disp = ajaxRequest.display(margin);
		String caption = ajaxRequest.getRequestURL();
		if (!"".equals(caption))
			disp = disp.replace(caption, "<a onclick='Forms.Utils.toogleText($(\"imgToogle" + id + "\"),\"" + id + "\",false);'>" + caption + "<div id='imgToogle" + id + "' class='arrow-up'></div></a>");
		return disp;
	}

	private String getDisplayUrl(String url, String disp, String breadcrumb, int depth, HttpServletRequest request) {
		String result = disp;
		if (!"".equals(url)) {
			String jsUrl = getJsUrl(url, depth, breadcrumb, request);
			jsUrl = "<a onclick=\"" + jsUrl + "\">" + url + "</a>";
			result = result.replace(url, jsUrl);
		}
		return result;
	}

	public String display(IAjaxObject ajaxObj, int depth, int niveau, int margin, int decalage, String breadcrumb, HttpServletRequest request) {
		String id = ajaxObj.getUniqueIdHash(getRandomId());
		String result = "<div>";
		if (!onlyURL || (ajaxObj instanceof IHasURL))
			result += getDisplayCaption(ajaxObj, id, margin + 1);
		if (ajaxObj instanceof IHasURL)
			result = getDisplayUrl(((IHasURL) ajaxObj).getURL(), result, breadcrumb, depth, request);
		result += "<div id='" + id + "'>";
		if (niveau < depth) {
			if (ajaxObj instanceof IHasURL) {
				String url = ((IHasURL) ajaxObj).getURL();
				result += display(url, depth, niveau + 1, margin + 1, breadcrumb, request);
			}
			if (ajaxObj instanceof KAjaxWithChilds) {
				ArrayList<String> childsURL = ((KAjaxWithChilds) ajaxObj).getURLs();
				for (String url : childsURL)
					result += display(url, depth, niveau + 1, margin + 1, breadcrumb, request);
			}
		}
		if (ajaxObj instanceof KAjaxWithChilds) {
			if (ajaxObj instanceof KAjaxIncludeDialog) {
				for (IAjaxObject ajaxObjChild : ((KAjaxIncludeDialog) ajaxObj).getDialogChilds()) {
					result += display(ajaxObjChild, depth, niveau, margin + 1, decalage + 1, breadcrumb, request);
				}
			} else {
				for (IAjaxObject ajaxObjChild : ((KAjaxWithChilds) ajaxObj).getChilds()) {
					result += display(ajaxObjChild, depth, niveau, margin + 1, decalage + 1, breadcrumb, request);
				}
			}
		}
		return result + "</div></div>";
	}

	public int getRandomId() {
		int id = 0;
		do {
			Random r = new Random();
			double randomValue = 1 + (1000 - 1) * r.nextDouble();
			id = (int) Math.round(randomValue);
		} while (ids.indexOf(Integer.valueOf(id)) != -1);
		ids.add(Integer.valueOf(id));
		return id;
	}

	public String getPanel(HttpServletRequest request) {
		String path = KRequest.GETPOST("path", request, "index.do");
		int depth = KRequest.GETPOST("depth", request, 0);
		String breadcrumb = KRequest.GETPOST("breadcrumb", request, "");
		String breadcrumbDisplay = "&nbsp;";
		if (breadcrumb != null) {
			String[] breads = breadcrumb.split(";");
			String actual = "";
			for (int i = 0; i < breads.length - 1; i++) {
				if (!"".equals(breads[i])) {
					breadcrumbDisplay += "<a class='breadcrumbURL' onclick=\"" + getJsUrl(breads[i], depth, actual, request) + "\">" + breads[i] + "</a>&nbsp;>&nbsp;";
					if ("".equals(actual))
						actual = breads[i];
					else
						actual += ";" + breads[i];
				}
			}
			breadcrumbDisplay += "<a class='breadcrumbURL' onclick=\"" + getJsUrl(breads[breads.length - 1], depth, actual, request) + "\">" + breads[breads.length - 1] + "</a>";
		}
		String result = "<div id='_commandes'><form id='_frmMap' name='_frmMap'><fieldset>";
		result += "<legend>Site map</legend>";
		KHtmlFieldControl fcPath = new KHtmlFieldControl("URL", path, "path", "path", HtmlControlType.khcAjaxList);
		fcPath.setListObject("{" + getMappingsUrl() + "}");
		fcPath.setAllowNull(false);
		KHtmlFieldControl fcDepth = new KHtmlFieldControl("Profondeur", depth + "", "depth", "depth", HtmlControlType.khcRange);
		KHtmlFieldControl fcBreadcrumb = new KHtmlFieldControl("", breadcrumb + "", "breadcrumb", "breadcrumb", HtmlControlType.khcHidden);
		KHtmlFieldControl fcOnlyURL = new KHtmlFieldControl("URL seulement", onlyURL + "", "_onlyURL", "_onlyURL", HtmlControlType.khcCheckBox);
		KHtmlFieldControl fcReloadMappings = new KHtmlFieldControl("Recharger mappings", reloadMappings + "", "reloadMappings", "reloadMappings", HtmlControlType.khcCheckBox);

		result += fcPath.toString();

		result += fcBreadcrumb.toString();

		result += "<label>Fil d'ariane</label>";
		result += "<div class='breadcrumb'>" + breadcrumbDisplay + "</div>";

		result += "<div id='boxButtons' class='boxButtons'>" + fcDepth + fcOnlyURL + fcReloadMappings + "<input type='button' class='sBtn' value='Appliquer' id='btReloadSiteMap'></div>";
		addJs("$addEvt($('btReloadSiteMap'), 'click', function(){$postForm('_frmMap','mainSiteMap','siteMap.main','_op=submit');});");
		result += "</fieldset></form></div>";
		return result;
	}

	private String getJsUrl(String url, int depth, String breadcrumb, HttpServletRequest request) {
		return "$post('siteMain','" + request.getRequestURI() + "','_op=submit&path=" + url + "&depth=" + depth + "&breadcrumb=" + breadcrumb + "&_onlyURL=" + onlyURL + "');";
	}

	private String getMappingsUrl() {
		String result = "";
		KFilterMappings mappings = KoHttp.kmappings();
		if (mappings != null) {
			for (KAbstractFilterMapping afm : mappings.getItems()) {
				if (afm instanceof KMapping) {
					String url = ((KMapping) afm).getRequestURL();
					if (!url.contains("{")) {
						if ("".equals(result))
							result = url;
						else
							result += "," + url;
					}
				}
			}
		}
		return result;

	}
}
