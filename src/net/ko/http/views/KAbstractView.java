package net.ko.http.views;

import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.controller.KObjectController;
import net.ko.dao.DatabaseDAO;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.http.objects.KRequest;
import net.ko.inheritance.KReflectObject;
import net.ko.kobject.KObject;
import net.ko.utils.KoUtils;

/**
 * Vue HTML abstraite (formulaire ou liste) contient l'objet request, ainsi
 * qu'une map des éléments HTML dont elle est composée
 * 
 * @author jcheron
 * 
 */
public abstract class KAbstractView<T extends KObject> extends KReflectObject {
	protected boolean loaded;
	/**
	 * instance de HttpServletRequest
	 */
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected boolean _ajx = false;
	protected boolean _refresh = false;
	protected String pageHeader = "";
	protected String pageFooter = "";
	protected String autoPageHeader = "";
	protected String autoPageFooter = "";
	protected String koDisplay = "";
	protected KObjectDisplay displayInstance = null;
	protected boolean hasPageHeader = true;
	protected boolean ajaxIncludes = false;
	protected String daoClassName;
	protected DatabaseDAO<T> dao;
	protected KObjectController kobjectController;

	/**
	 * Map des éléments HTML
	 */
	protected KViewElementsMap fieldControls;

	public KAbstractView(HttpServletRequest request) {
		fieldControls = new KViewElementsMap();
		this.request = request;
		_ajx = KRequest.keyExists("_ajx", request);
		_refresh = KRequest.keyExists("_refresh", request);
		loaded = false;
	}

	public abstract void init();

	/**
	 * Retourne le contrôle visuel HTML associé à un champ/membre
	 * 
	 * @param fieldName
	 *            nom du champ
	 * @return contrôle visuel HTML associé
	 */
	public KFieldControl getFieldControl(String fieldName) {
		return fieldControls.get(fieldName);
	}

	/**
	 * Supprime le contrôle visuel HTML associé à un champ/membre
	 * 
	 * @param fieldName
	 *            nom du champ
	 */
	public void removeFieldControl(String fieldName) {
		fieldControls.remove(fieldName);
	}

	/**
	 * Supprime les contrôles visuels HTML associés à un champ/membre
	 * 
	 * @param fieldNames
	 *            Tableau des noms de champs
	 */
	public void removeFieldControl(String[] fieldNames) {
		fieldControls.remove(fieldNames);
	}

	/**
	 * Inverse la position d'affichage de deux champs
	 * 
	 * @param fieldName1
	 *            nom du champ1
	 * @param fieldName2
	 *            nom du champ2
	 */
	public void swapFcPos(String fieldName1, String fieldName2) {
		fieldControls.swap(fieldName1, fieldName2);
	}

	/**
	 * Ajoute une zone html dans la liste des éléments à afficher
	 * 
	 * @param html
	 *            contenu html ajouté
	 * @return l'élément créé
	 */
	public KFieldControl addHTML(String html) {
		return fieldControls.addHtml(html + "\n");
	}

	/**
	 * Ajoute un élément visuel HTML dans la liste fieldControls
	 * 
	 * @param key
	 *            identifiant de l'objet à créer
	 * @param html
	 *            contenu html ajouté
	 * @return contrôle visuel HTML créé
	 */
	public KFieldControl addHTML(String key, String html) {
		return fieldControls.addHtml(key, html + "\n");
	}

	/**
	 * @param key
	 *            identifiant de l'objet à créer
	 * @param html
	 *            contenu html ajouté
	 * @param fieldName
	 *            nom de l'élément après lequel l'élément ajouté sera inséré
	 * @return
	 */
	public KFieldControl insertAfter(String key, String html, String fieldName) {
		KFieldControl fc = fieldControls.addHtml(key, html);
		KFieldControl afc = fieldControls.get(fieldName);
		if (afc != null)
			fc.setPos(afc.getPos() + 1);
		return fc;
	}

	/**
	 * @param key
	 *            identifiant de l'objet à créer
	 * @param html
	 *            contenu html ajouté
	 * @param fieldName
	 *            nom de l'élément avant lequel l'élément ajouté sera inséré
	 * @return
	 */
	public KFieldControl insertBefore(String key, String html, String fieldName) {
		KFieldControl fc = fieldControls.addHtml(key, html);
		KFieldControl afc = fieldControls.get(fieldName);
		if (afc != null)
			fc.setPos(afc.getPos() - 1);
		return fc;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
		fieldControls.setLoaded(loaded);
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response, boolean autoPages) {
		this.response = response;
		if (autoPages) {
			autoPageHeader = getAutoPageHeader();
			autoPageFooter = getAutoPageFooter();
		}
	}

	public String getAutoPageHeader() {
		String result = "";
		if (!_ajx) {
			if (!"".equals(Ko.kHeaderURL()))
				result = KRequest.includeResponse(Ko.kHeaderURL(), request, response);
		}
		return result;
	}

	public String getPageHeader() {
		String result = autoPageHeader;
		if (!"".equals(pageHeader) || !hasPageHeader)
			result = pageHeader;
		return result;
	}

	public String getPageFooter() {
		String result = autoPageFooter;
		if (!"".equals(pageFooter) || !hasPageHeader)
			result = pageFooter;
		return result;
	}

	public void setPageHeader(String pageHeader) {
		this.pageHeader = pageHeader;
	}

	public String getAutoPageFooter() {
		String result = "";
		if (!_ajx) {
			if (!"".equals(Ko.kFooterURL()))
				result = KRequest.includeResponse(Ko.kFooterURL(), request, response);
		}
		return result;
	}

	public void setPageFooter(String pageFooter) {
		this.pageFooter = pageFooter;
	}

	public KViewElementsMap getFieldControls() {
		return fieldControls;
	}

	public String getKoDisplay() {
		return koDisplay;
	}

	public void setKoDisplay(String koDisplay) {
		if (koDisplay != this.koDisplay) {
			this.koDisplay = koDisplay;
			displayInstance = null;
			getDisplayInstance().getKobjectController(kobjectController, request);
		}
	}

	public KObjectDisplay getDisplayInstance() {
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

	protected abstract KObjectDisplay getKobjectDisplay();

	public boolean isAjaxIncludes() {
		return ajaxIncludes;
	}

	public void setAjaxIncludes(boolean ajaxIncludes) {
		this.ajaxIncludes = ajaxIncludes;
	}

	public String getClientIp() {
		return KRequest.getClientIpAddr(request);
	}

	public abstract DatabaseDAO<T> getDao();

	public void setDao(DatabaseDAO<T> dao) {
		this.dao = dao;
	}

	public KObjectController getKobjectController() {
		return kobjectController;
	}

	public void setKobjectController(KObjectController kobjectController) {
		this.kobjectController = kobjectController;
	}

	protected void initKobjectController(KObject ko) {
		try {
			kobjectController = Ko.getInstance().getKobjectController(ko.getClass().getSimpleName()).clone();
		} catch (CloneNotSupportedException e) {
			Ko.klogger().log(Level.WARNING, "Impossible de cloner le contrôleur de " + ko, e);
			kobjectController = Ko.getInstance().getKobjectController(ko.getClass().getSimpleName());
		}
	}
}