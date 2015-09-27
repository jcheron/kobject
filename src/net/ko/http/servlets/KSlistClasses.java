package net.ko.http.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.bean.KListClass;
import net.ko.bean.KTester;
import net.ko.cache.KCache;
import net.ko.cache.KCacheType;
import net.ko.debug.KDebugClient;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
import net.ko.http.servlets.bean.KSlistClassesBean;
import net.ko.http.views.KHtmlFieldControl;
import net.ko.http.views.KHttpForm;
import net.ko.types.HtmlControlType;

/**
 * Servlet implementation class KSlistClasses
 */
@WebServlet(name = "KSlistClasses", urlPatterns = { "/listClasses.frm", "/classes.main" })
public class KSlistClasses extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSlistClasses() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		KSlistClassesBean bean = new KSlistClassesBean(request, response);
		String configFile = bean.getApplication().getInitParameter("configFile");
		if (configFile != null)
			KoHttp.configFile = configFile;
		KoHttp.kstart(request.getServletContext());

		String op = KRequest.GETPOST("op", request, "all");
		switch (op) {
		case "all":
			all(bean);
			break;
		case "liste":
			loadListe(bean);
			break;

		case "classes":
			try {
				_getListClasses(bean);
				bean.printJs();
			} catch (ClassNotFoundException | SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException | InstantiationException | SQLException e) {
				Ko.klogger().log(Level.WARNING, "Erreur de chargement", e);
			}
			break;

		default:
			break;
		}

	}

	private void all(KSlistClassesBean bean) {
		try {
			if (!bean.isAjax())
				bean.printPageHeader("admin.kcss");
			getListClasses(bean);
			restartFrm(bean);
			bean.print("<div id=\"_ajx\"></div>");
			bean.print("<div id=\"_ajxContent\">");
			bean.flush();
			loadListe(bean);
			bean.print("</div>");
			bean.printJs();
			if (!bean.isAjax())
				bean.printPageFooter();
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Erreur de chargement", e);
		}
	}

	private void loadListe(KSlistClassesBean bean) throws ServletException, IOException {
		KRequest.include("ajaxPageList.frm", bean.getRequest(), bean.getResponse(), "_ajx=true&_cls=" + bean.getCls() + "&_depth=" + bean.getDepth() + "&_useCache=" + bean.isUseCache() + "&_field=" + bean.getField());
	}

	private void getListClasses(KSlistClassesBean bean) throws IOException, ClassNotFoundException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException, SQLException {
		bean.print("<div id=\"_classes\">");
		_getListClasses(bean);
		bean.print("</div>");
	}

	private void _getListClasses(KSlistClassesBean bean) throws IOException, ClassNotFoundException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException, SQLException {
		bean.print("<form name='_frmClasse' id='_frmClasse'>");
		bean.print("<fieldset id=\"_fsClasses\">");
		bean.print("<legend onclick=\"javascript:Forms.Utils.toogleText($('toogle'),'_innerFs');\">Classes&nbsp;<div class=\"arrow-up\" id=\"toogle\"></div>&nbsp;</legend>");
		bean.print("<div id=\"_innerFs\">");

		String depth = bean.getDepth();
		String field = bean.getField();
		if (bean.isUseCache()) {
			Ko.cacheType = KCacheType.ktListAndObjects;
			KCache.loadAllCache();
		}
		try {
			Ko.ConstraintsDepth = Integer.valueOf(depth);
		} catch (Exception e) {
			Ko.ConstraintsDepth = 1;
			depth = "1";
		}
		KListClass kclasses = KListClass.kload();
		kclasses.sort();
		if (bean.getCls() == null)
			if (kclasses.count() > 0)
				bean.setCls(kclasses.get(0).getIdClasse() + "");

		KTester tester = new KTester();
		tester.set_cls(bean.getCls());
		String listFields = tester.getListFields();

		KHttpForm frmTester = new KHttpForm(tester, bean.getRequest());
		frmTester.setResponse(bean.getResponse(), false);
		frmTester.load(true, false);
		KHtmlFieldControl listCls = (KHtmlFieldControl) frmTester.getFieldControl("_cls");
		if (listCls != null) {
			listCls.setListObject(kclasses);
			listCls.setCaption("Classe :");
		}
		KHtmlFieldControl cDepth = (KHtmlFieldControl) frmTester.getFieldControl("_depth");
		KHtmlFieldControl useCache = (KHtmlFieldControl) frmTester.getFieldControl("_useCache");
		if (useCache != null)
			useCache.setCaption("useCache");
		if (cDepth != null) {
			cDepth.setCaption("Ko.constraintDepth :");
		}
		KHtmlFieldControl ctrlListFields = (KHtmlFieldControl) frmTester.getFieldControl("_field");
		if (ctrlListFields != null) {
			ctrlListFields.setCaption("Membres");
			ctrlListFields.setListObject(listFields);
			ctrlListFields.setFieldType(HtmlControlType.khcCheckedList);
			if (field == null || "".equals(field)) {
				field = tester.getListFieldsValues();
				bean.setField(field);
				ctrlListFields.setValue(field);
			}
		}
		frmTester.removeFieldControl(new String[] { "_depth", "_useCache" });

		frmTester.addHTML("<div id='boxButtons' class='boxButtons'>" + cDepth + useCache + "<input id='btShowClass' type='button' class='sBtn' value='Appliquer'></div>");
		bean.addJs("$addEvt($('_cls'),'change',function(){" +
				"$postForm('_frmClasse','_classes','classes.main','op=classes',function(){Forms.Utils.fireEvent($('btShowClass'),'click');});});");
		bean.addJs("$addEvt($('btShowClass'), 'click', function(){$postForm('_frmClasse','_ajxContent','classes.main','op=liste');});");

		bean.print(frmTester.toString());
		bean.print("</div>");
		bean.print("</fieldset>");
	}

	private void restartFrm(KSlistClassesBean bean) {
		bean.print("<div id='_commandes'>");
		bean.print("<form id=\"_frmCommandes\" name=\"_frmCommandes\">");
		bean.print("<fieldset id=\"_fsCommandes\">");
		bean.print("<legend onclick=\"javascript:Forms.Utils.toogleText($('toogleCmd'),'_innerFsCmd');\">Redémarrage&nbsp;<div class=\"arrow-up\" id=\"toogleCmd\"></div>&nbsp;</legend>");
		bean.print("<div id=\"_innerFsCmd\">");
		KHtmlFieldControl khfc = new KHtmlFieldControl("Options de redémarrage", "", "restart", "restart", HtmlControlType.khcCheckedList, "{Connexion BDD,Validation (kox.xml),Mappings (mox.xml),Messages et ER, Css variables}", "");
		bean.print(khfc + "");
		bean.print("<input type='button' id='_btAdminRestart' value=\"Redémarrer l'application\" class='sBtn'>");
		KHtmlFieldControl ckCD = new KHtmlFieldControl("Client debug", KDebugClient.isActive() + "", "ckClientDebug", "ckClientDebug", HtmlControlType.khcCheckBox);
		bean.print("<div class='inline'>" + ckCD + "</div>");
		bean.print("<div id='_commandesResponse'></div></form>" +
				"<hr><span id='libVersion'>Librairie : " + Ko.koLibVersion + " / Plugin : " + Ko.koPluginVersion + "</div></div>");
		bean.addJs("$addEvt($('_btAdminRestart'),'click',function(){$postForm('_frmCommandes','_commandesResponse','restart.cmd');});");
		bean.addJs("$addEvt($('_ckClientDebug'),'click',function(e){var t=$gte(e);$get('_commandesResponse','startClientDebug.cmd','clientDebug='+t.checked);});");
	}
}
