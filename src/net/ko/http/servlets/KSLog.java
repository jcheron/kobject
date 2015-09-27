package net.ko.http.servlets;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
import net.ko.http.views.KHtmlFieldControl;
import net.ko.types.HtmlControlType;
import net.ko.utils.KTextFile;

/**
 * Servlet implementation class KSLog
 */
@WebServlet(name = "KSLog", urlPatterns = { "/log.main" })
public class KSLog extends KSAbstractMain {
	private static final long serialVersionUID = 1L;
	private int countByPage = 500;
	private int page = 1;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSLog() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		init(request, response);
		KoHttp.kstart(application);
		out.print(getAll(request, response));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		init(request, response);
		out.print(getLogs(request, response));
	}

	protected String getAll(HttpServletRequest request, HttpServletResponse response) {
		String txtResponse = "";
		if (!isAjax)
			txtResponse += getPageHeader("admin.kcss");
		txtResponse += getForm(request, response);
		txtResponse += "<div class='logs'><div id='logs'>\n";
		txtResponse += getLogs(request, response);
		txtResponse += "</div></div>";
		if (!isAjax)
			txtResponse += getPageFooter();
		return txtResponse;
	}

	protected String getLogs(HttpServletRequest request, HttpServletResponse response) {
		if (KRequest.keyExists("clear", request)) {
			Ko.klogger().clear();
			Ko.klogger().log(Level.INFO, "Vidage des logs effectué", request);
		}
		String filtre = KRequest.GETPOST("filtre", request, "");
		page = KRequest.GETPOST("activePage", request, 1);
		countByPage = KRequest.GETPOST("countByPage", request, 100);
		String _page = KRequest.GETPOST("_page", request, "autre");
		String txtResponse = "";
		if (Ko.kuseLog()) {
			String logs = KTextFile.open(Ko.logFile);
			String logsTab[] = logs.split("\n\n\n\n");

			int count = logsTab.length;
			int first = 0;
			int last = logsTab.length - 1;
			if (count > countByPage) {
				page = getPage(page, _page, count);
				first = getFirst(page, count);
				last = getLast(page, count);
			}
			txtResponse += "<script>$('activePage').value='" + page + "';</script>";
			if ("".equals(filtre) || filtre == null) {
				for (int i = last; i >= first; i--) {
					txtResponse += logsTab[i];
				}
			} else {
				for (int i = last; i >= first; i--) {
					if (logsTab[i].contains(filtre))
						txtResponse += logsTab[i].replace(filtre, "<span class='search-text'>" + filtre + "</span>");
				}
			}
		}
		return txtResponse;
	}

	private int getLast(int page, int count) {
		// double lastPage=Math.ceil((double)count/countByPage);
		// page=(int) (lastPage-page+1);
		int result = count - (page - 1) * countByPage;
		if (result > count - 1)
			result = count - 1;
		return result;
	}

	private int getFirst(int page, int count) {
		/*
		 * double lastPage=Math.ceil((double)count/countByPage); page=(int)
		 * (lastPage-page+1); int result=(page-1)*countByPage;
		 */
		int result = getLast(page, count) - countByPage;
		if (result < 0)
			result = 0;
		return result;
	}

	private int getPage(int p, String _page, int count) {
		int result = 0;
		int last = (int) (Math.ceil((double) count / countByPage));
		switch (_page) {
		case "first":
			result = 1;
			break;
		case "previous":
			result = p - 1;
			break;
		case "next":
			result = p + 1;
			break;
		case "last":
			result = last;
			break;

		default:
			result = p;
			break;
		}
		if (result < 1)
			result = 1;
		if (result > last)
			result = last;
		return result;
	}

	protected String getForm(HttpServletRequest request, HttpServletResponse response) {
		String txtResponse = "<div id=\"_classes\">";
		txtResponse += "<form id=\"_frmFiltre\" name=\"_frmFiltre\">";
		txtResponse += "<fieldset id=\"_fsClasses\">";
		txtResponse += "<legend onclick=\"javascript:Forms.Utils.toogleText($('toogle'),'_innerFs');\">Fichier de logs&nbsp;<div class=\"arrow-up\" id=\"toogle\"></div>&nbsp;</legend>";
		txtResponse += "<div id=\"_innerFs\">";
		KHtmlFieldControl fc = new KHtmlFieldControl("Filtre :     ", "", "filtre", "filtre", HtmlControlType.khcAjaxList);
		fc.setListObject("{" + KDebugConsole.getOptions() + "}");
		txtResponse += fc + "";
		fc = new KHtmlFieldControl("Nb logs/page :", countByPage + "", "countByPage", "countByPage", HtmlControlType.khcNumber);
		txtResponse += fc + "";

		txtResponse += "<div class='boxButtons'><input type='button' class='btn' value='Vider les logs' onclick=\"new Ajx('logs','" + request.getRequestURI() + "','clear',undefined,$('ajax-loader')).postForm('_frmFiltre');\">";

		txtResponse += getNavBarre(request.getRequestURI());
		txtResponse += "<input type='button' class='btn' value='Appliquer/Rafraîchir' onclick=\"new Ajx('logs','" + request.getRequestURI() + "','',undefined,$('ajax-loader')).postForm('_frmFiltre');\"></div>";
		txtResponse += "<div id='loader'><div id='ajax-loader' class='ajax-loader' style='display:none'></div></div>";
		txtResponse += "</div>";
		txtResponse += "</fieldset></form></div>";
		return txtResponse;
	}

	public String onClick(String url, String params) {
		return "new Ajx('logs','" + url + "','" + params + "',undefined,$('ajax-loader')).postForm('_frmFiltre')";
	}

	public String getNavBarre(String url) {
		String result = "<div style='display:inline-block;' id='navBarre'>";
		result += getLink(url, "Atteindre la page 1", "_page=first", cmdFirst());
		result += getLink(url, "Atteindre la page précédente", "_page=previous", cmdPrevious());
		result += "<input type='text' style='text-align:center;' class='select' value='" + page + "' id='activePage' name='activePage'>";
		result += getLink(url, "Atteindre la page suivante", "_page=next", cmdNext());
		result += getLink(url, "Atteindre la dernière page", "_page=last", cmdLast());
		return result + "</div>";
	}

	private String getLink(String url, String title, String params, String caption) {
		String result = "";
		result = "<a title='" + title + "' onclick=\"" + onClick(url, params) + "\">" + caption + "</a>";
		return result;
	}

	public String cmdFirst() {
		return "<div class='navBarreBtn'>|<div class='arrow-left'></div>&nbsp;</div>";
	}

	public String cmdLast() {
		return "<div class='navBarreBtn'>&nbsp;<div class='arrow-right'></div>|</div>";
	}

	public String cmdPrevious() {
		return "<div class='navBarreBtn'>&nbsp;<div class='arrow-left'></div>&nbsp;</div>";
	}

	public String cmdNext() {
		return "<div class='navBarreBtn'>&nbsp;<div class='arrow-right'></div>&nbsp;</div>";
	}
}
