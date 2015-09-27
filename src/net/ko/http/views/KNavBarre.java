package net.ko.http.views;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.ko.http.objects.KQueryString;
import net.ko.http.objects.KRequest;

/**
 * Barre de navigation visuelle en HTML permettant de se déplacer entre les
 * pages gérées avec un objet KPagination
 * 
 * @see net.ko.http.views.KPagination
 * @author jcheron
 * 
 */
public class KNavBarre {
	public String folder = "images/navBarre";// Dossier contenant les images de
												// la barre
	public String activePageStyle = "select";// Style css du numéro de la page
												// active
	public int pageNavCount = 5;// Nombre de pages à afficher dans la barre
	public int lastPage;// Numéro de la dernière page
	private int activePage = 1;// Numéro de la page active
	private int rowCount;
	private HttpServletRequest request;
	private String requestUrl = "";
	private boolean ajax = false;
	private String ajaxDivContentRefresh = "ajxContent";
	private String idListe = "";

	private String getButton(String img, String pDefault) {
		String realPath = null;
		ServletContext app = request.getServletContext();
		if (app != null) {
			realPath = app.getRealPath(img);
			File f = new File(realPath);
			if (f.exists()) {
				return "<img src='" + img + "' align='absmiddle' border=0>";
			}
			else
			{
				return pDefault;
			}
		} else
			return pDefault;
	}

	public String cmdFirst() {
		return cmdFirst("first.gif");
	}

	public String cmdFirst(String imgName) {
		return this.getButton(this.folder + "/" + imgName, "<div class='btn navBarreBtn'><span class='glyphicon glyphicon-fast-backward' aria-hidden='true'></span></div>");
	}

	public String cmdLast() {
		return cmdLast("last.gif");
	}

	public String cmdLast(String imgName) {
		return this.getButton(this.folder + "/" + imgName, "<div class='btn navBarreBtn'><span class='glyphicon glyphicon-fast-forward' aria-hidden='true'></span></div>");
	}

	public String cmdPrevious() {
		return cmdPrevious("previous.gif");
	}

	public String cmdPrevious(String imgName) {
		return this.getButton(this.folder + "/" + imgName, "<div class='btn navBarreBtn'><span class='glyphicon glyphicon-backward' aria-hidden='true'></span></div>");
	}

	public String cmdNext() {
		return cmdNext("next.gif");
	}

	public String cmdNext(String imgName) {
		return this.getButton(this.folder + "/" + imgName, "<div class='btn navBarreBtn'><span class='glyphicon glyphicon-forward' aria-hidden='true'></span></div>");
	}

	private int getPageD() {
		int div = Math.round(this.activePage - this.pageNavCount / 2);
		if (div < 1)
			div = 1;
		return div;
	}

	public KNavBarre(HttpServletRequest request) {
		this(request, "");
	}

	public KNavBarre(HttpServletRequest request, String folder) {
		this.request = request;
		if (!"".equals(folder))
			this.folder = folder;
	}

	public void gotoPage(int aPage) {
		this.activePage = aPage;
	}

	public String toString() {
		int previous = this.activePage - 1;
		if (previous < 1)
			previous = 1;
		int next = this.activePage + 1;
		if (next > this.lastPage)
			next = this.lastPage;
		int pageD = this.getPageD();
		int pageF = pageD + this.pageNavCount;
		if (pageF > this.lastPage)
			pageF = this.lastPage;
		if (pageF - pageD < this.pageNavCount)
			pageD = pageF - this.pageNavCount;
		if (pageD < 1)
			pageD = 1;
		int i = pageD;
		String result = "<div class='navBarre'><table><tr>";
		result += "\n<td>" + getLink(1, "_home", "Atteindre la première page", KQueryString.setValue(this.getQueryString(), "_page", "1"), this.cmdFirst()) + "</td>";
		result += "\n<td>" + getLink(previous, "_previous", "Atteindre la page n°" + previous, KQueryString.setValue(this.getQueryString(), "_page", String.valueOf(previous)), this.cmdPrevious()) + "</td>";
		while (i <= pageF) {
			String style = "";
			if (i == this.activePage)
				style = " class='activePage " + this.activePageStyle + "' ";
			result += "\n<td align='center' valign='middle'" + style + ">";
			result += "&nbsp;" + getLink(i, "_active", "Atteindre la page " + i, KQueryString.setValue(this.getQueryString(), "_page", String.valueOf(i)), i + "") + "&nbsp;</td>";
			i++;
		}

		result += "\n<td>" + getLink(next, "_next", "Atteindre la page n°" + next, KQueryString.setValue(this.getQueryString(), "_page", String.valueOf(next)), this.cmdNext()) + "</td>";
		result += "\n<td>" + getLink(lastPage, "_end", "Atteindre la dernière page", KQueryString.setValue(this.getQueryString(), "_page", String.valueOf(lastPage)), this.cmdLast()) + "</td>";
		result += "\n</tr></table></div>";
		result += "<script>Forms.Framework.addNavigationKeys('" + idListe + "'," + activePage + ");</script>";
		return result;
	}

	private String getLink(int newPage, String id, String title, String queryString, String caption) {
		String result = "";
		if (ajax)
			result = "<a id='" + id + "' title='" + title + "' onclick=\"Forms.Framework.gotoPage('" + this.ajaxDivContentRefresh + "','" + this.getRequestUrl() + "','_refresh&" + queryString + "','" + idListe + "'," + newPage + ");\">" + caption + "</a>";
		else
			result = "<a id='" + id + "' title='" + title + "' href='" + getRequestUrl() + "?" + queryString + "'>" + caption + "</a>";
		return result;
	}

	public String getRequestUrl() {
		requestUrl = KRequest.getRequestURI(requestUrl, request);
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public boolean isAjax() {
		return ajax;
	}

	public void setAjax(boolean ajax) {
		this.ajax = ajax;
	}

	public String getAjaxDivContentRefresh() {
		return ajaxDivContentRefresh;
	}

	public void setAjaxDivContentRefresh(String ajaxDivContentRefresh) {
		this.ajaxDivContentRefresh = ajaxDivContentRefresh;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public String getQueryString() {
		String result = request.getQueryString();
		result = KQueryString.setValue(result, "_nb", rowCount + "");
		return result;
	}

	public String getIdListe() {
		return idListe;
	}

	public void setIdListe(String idListe) {
		this.idListe = idListe;
	}
}
