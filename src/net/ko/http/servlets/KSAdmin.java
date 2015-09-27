package net.ko.http.servlets;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KoHttp;
import net.ko.http.servlets.bean.KActionBean;

/**
 * Servlet implementation class KSAdmin
 */
@WebServlet(name = "Admin", urlPatterns = { "/admin.main" })
public class KSAdmin extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see KSAbstractMain#KSAbstractMain()
	 */
	public KSAdmin() {
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
		KActionBean bean = new KActionBean(request, response);
		ServletContext application = bean.getApplication();
		String configFile = application.getInitParameter("configFile");
		if (configFile != null)
			KoHttp.configFile = configFile;
		KoHttp.kstart(application);
		if (!bean.isAjax())
			bean.printPageHeader("admin.kcss");
		bean.print("<fieldset><legend>Outils</legend>");
		bean.print("<div id='infos'></div><div id='cmd'>");
		bean.print(getButton("Redémarrer l'application", "btRestart", "Redémarrer l'application", "btn", "spReStart"));

		bean.print(getButton("Liste des classes", "bt-classes", "classes.main", "pgAdmin", "spListClasses"));
		bean.print(getButton("Imports", "bt-operations", "operations.main", "pgAdmin", "spOperations"));
		bean.print(getButton("Site Map", "bt-siteMap", "siteMap.main", "pgAdmin", "spSiteMap"));
		bean.print(getButton("Fichiers de Logs", "bt-log", "log.main", "pgAdmin", "spLogs"));
		bean.print(getButton("Vérifications", "bt-check", "check.main", "pgAdmin", "spCheck"));
		bean.print(getButton("Tests fonctionnels", "bt-tests", "tests.main", "pgAdmin", "spTests"));
		bean.print(getButton("DB updates", "bt-dbUpdates", "dbUpdates.main", "pgAdmin", "spDbUpdates"));

		bean.addJs("$addEvt($('btRestart'),'click',function(){$get('infos','restart.cmd');});");
		bean.addJs("new Forms.Selector('.pgAdmin','click','false',0,function(e){var url=Forms.Utils.getAttr($gte(e),'id').replace('bt-','');$get('mainAdm',url+'.main');},{'color':'#1C487D','fontWeight':'bolder','border':'1px dotted #AFAFB0'});");
		bean.print("</fieldset></div>");
		bean.print("<div id='mainAdm'></div>");

		bean.printJs();
		if (!bean.isAjax())
			bean.printPageFooter();
	}

	public String getButton(String caption, String id, String title, String className, String spanClassName) {
		return "<div class='" + className + "' title=\"" + title + "\" id='" + id + "'><span class='" + spanClassName + "'>" + caption + "</span></div>";
	}
}
