package net.ko.http.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
import net.ko.http.views.KHtmlFieldControl;
import net.ko.ksql.KParameterizedExecute;
import net.ko.types.HtmlControlType;

/**
 * Servlet implementation class KSLog
 */
@WebServlet(name = "KSDbUpdates", urlPatterns = { "/dbUpdates.main" })
public class KSDbUpdates extends KSAbstractMain {
	private static final long serialVersionUID = 1L;
	private int countByPage = 500;
	private int page = 1;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSDbUpdates() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		init(request, response);
		KoHttp.kstart(request.getServletContext());
		out.print(getAll(request, response));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		init(request, response);
		out.print(getUpdates(request, response));
	}

	protected String getAll(HttpServletRequest request, HttpServletResponse response) {
		String txtResponse = "";
		if (!isAjax)
			txtResponse += getPageHeader("admin.kcss", "log.kcss");
		txtResponse += getForm(request, response);
		txtResponse += "<div class='logs'><div id='logs'>\n";
		txtResponse += getUpdates(request, response);
		txtResponse += "</div></div>";
		if (!isAjax)
			txtResponse += getPageFooter();
		return txtResponse;
	}

	protected String getUpdates(HttpServletRequest request, HttpServletResponse response) {
		String filtre = KRequest.GETPOST("filtre", request, "");
		page = KRequest.GETPOST("activePage", request, 1);
		countByPage = KRequest.GETPOST("countByPage", request, 100);
		String _page = KRequest.GETPOST("_page", request, "autre");
		String txtResponse = "";
		if (Ko.kmemoryDaoEngine() != null) {
			List<KParameterizedExecute> queries = Ko.kmemoryDaoEngine().getQueries();

			int count = queries.size();
			int first = 0;
			int last = queries.size() - 1;
			if (count > countByPage) {
				page = getPage(page, _page, count);
				first = getFirst(page, count);
				last = getLast(page, count);
			}
			txtResponse += "<script>$('activePage').value='" + page + "';</script>";
			if ("".equals(filtre) || filtre == null) {
				for (int i = last; i >= first; i--) {
					txtResponse += "<div class='RECORD query'>" + queries.get(i).toString() + "</div>";
				}
			} else {
				for (int i = last; i >= first; i--) {
					String q = queries.get(i).toString();
					if (q.contains(filtre))
						txtResponse += "<div class='RECORD query'>" + q.replace(filtre, "<span class='search-text'>" + filtre + "</span>") + "</div>";
				}
			}
		} else {
			txtResponse = "<div class='message'>L'utilisation du cache de base de données (à partir de H2 ou HsqlDB) n'est pas activée.<br>";
			txtResponse += "Pour l'activation du cache, ajouter les lignes suivantes dans le fichier de configuration <b>config.ko</b> :<br>";
			txtResponse += "<textarea>memory.dbType=h2mem\nqueryThread.maxInterval=1200\nqueryThread.minInterval=600\nqueryThread.queriesCount=5\nqueryThread.start=true</textarea>";
			txtResponse += "<br>Ajouter également le jar du connecteur JDBC concerné dans la dossier <b>WEB-INF/lib<b></div>";
		}
		return txtResponse;
	}

	private int getLast(int page, int count) {
		int result = count - (page - 1) * countByPage;
		if (result > count - 1)
			result = count - 1;
		return result;
	}

	private int getFirst(int page, int count) {
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
		txtResponse += "<legend onclick=\"javascript:Forms.Utils.toogleText($('toogle'),'_innerFs');\">Update queries&nbsp;<div class=\"arrow-up\" id=\"toogle\"></div>&nbsp;</legend>";
		txtResponse += "<div id=\"_innerFs\">";
		KHtmlFieldControl fc = new KHtmlFieldControl("Filtre :     ", "", "filtre", "filtre", HtmlControlType.khcAjaxList);
		fc.setListObject("{INSERT,UPDATE,DELETE,WHERE}");
		txtResponse += fc + "";
		fc = new KHtmlFieldControl("Nb queries/page :", countByPage + "", "countByPage", "countByPage", HtmlControlType.khcNumber);
		txtResponse += fc + "";
		txtResponse += "<div id='boxButtons' class='boxButtons'>";
		txtResponse += getNavBarre(request.getRequestURI());
		txtResponse += "<input type='button' class='btn' value='Appliquer/Rafraîchir' onclick=\"new Ajx('logs','" + request.getRequestURI() + "','',undefined,$('ajax-loader')).postForm('_frmFiltre');\"></div>";
		txtResponse += "<div id='loader'><div id='ajax-loader' class='ajax-loader' style='display:none'></div></div>";
		txtResponse += "</div>";
		txtResponse += "</fieldset></form></div></div>";
		return txtResponse;
	}

	public String onClick(String url, String params) {
		return "new Ajx('logs','" + url + "','" + params + "',undefined,$('ajax-loader')).postForm('_frmFiltre')";
	}

	public String getNavBarre(String url) {
		String result = "<div style='display:inline-block;' id='navBarre'>";
		result += getLink(url, "Atteindre la page 1", "_page=first", cmdFirst());
		result += getLink(url, "Atteindre la page précédente", "_page=previous", cmdPrevious());
		result += "<input type='text' style='text-align:center;' class='activePage' value='" + page + "' id='activePage' name='activePage'>";
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
		return "<div class='btn navBarreBtn'><span class='glyphicon glyphicon-fast-backward' aria-hidden='true'></span></div>";
	}

	public String cmdLast() {
		return "<div class='btn navBarreBtn'><span class='glyphicon glyphicon-fast-forward' aria-hidden='true'></span></div>";
	}

	public String cmdPrevious() {
		return "<div class='btn navBarreBtn'><span class='glyphicon glyphicon-backward' aria-hidden='true'></span></div>";
	}

	public String cmdNext() {
		return "<div class='btn navBarreBtn'><span class='glyphicon glyphicon-forward' aria-hidden='true'></span></div>";
	}
}
