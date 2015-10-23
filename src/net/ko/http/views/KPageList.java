package net.ko.http.views;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import net.ko.dao.DatabaseDAO;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.js.KJavaScript;
import net.ko.http.objects.KQueryString;
import net.ko.http.objects.KRequest;
import net.ko.interfaces.IKobjectDisplay;
import net.ko.interfaces.KMaskInterface;
import net.ko.kobject.KListObject;
import net.ko.kobject.KMask;
import net.ko.kobject.KObject;
import net.ko.ksql.KSqlQuery;
import net.ko.utils.KRegExpr;
import net.ko.utils.KString;
import net.ko.utils.KoUtils;

/**
 * Liste tabulaire d'objets (KListObject) gérée avec pagination intégrant des
 * formulaires de mise à jour, insertion et suppression de données
 * 
 * @see net.ko.http.views.KPagination
 * @see net.ko.http.views.KNavBarre
 * @see net.ko.http.views.KHttpForm
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2013
 * @license 
 *          http://www.kobject.net/index.php?option=com_content&view=article&id=50
 *          &Itemid=2 LGPL License
 * @version $Id: $
 * @package net.ko.http.views
 * 
 */
public class KPageList extends KAbstractView implements KMaskInterface {
	private String addBtnCaption = "Ajouter";
	private String afterfield;
	private String ajaxDivContentRefresh = "_ajxContent";
	private String ajaxDivMessageSubmit = "_ajx";
	private String beforeField;
	private boolean checked = false;
	private String checkedMask = "<td><input {checkedValue} type='{checkedType}' id='{id}{getUniqueIdHash}' name='{name}' value='{getFirstKeyValue}' class='checkable'><label for='{id}{getUniqueIdHash}'>&nbsp;</label></td>";
	private String checkedType = "checkbox";
	private String className;
	private Class<KObject> clazz;
	private int countFields;
	private boolean createAjaxDivs = false;
	private String editBtnCaption = "Modifier";
	private String editFormUrl = "";
	private String footer;
	private KHttpForm form;
	private String frmCancelCaption = "Annuler";
	private String frmControlOn = "['onchange','onkeyup']";
	private String frmTitleAdd = "Ajout";
	private String frmTitleEdit = "Modification";
	private String frmValidCaption = "Valider";
	private boolean hasAddBtn = true;
	private boolean hasEditBtn = false;
	private boolean hasFiltre;
	private boolean hasNavBarre;
	private boolean hasPageCounter;
	private boolean autoDisplayNavBarre = false;
	private String header;
	private String id = "id";
	private String idForm = "frm";
	private String idLb = "";
	private String imgFolder;
	private KMask innerMask = null;
	private boolean isAjax = true;
	private boolean isFormModal = true;
	private boolean isShowCaption;
	private boolean isLoaded = false;
	private boolean koDetails = false;
	private String listContentUrl = "";
	private KMask mask = null;
	private String messageAndUpdateUrl = "";
	private int messageDelay = -1;
	private int mode = -1;
	private String name = "name";
	private int pageNavCount;
	private KPagination pagination;
	private String requestUrl = "";
	private int rowCount = 10;
	private String selectStyle = "{'color':'white','backgroundColor':'#9D3819','filter': 'alpha(opacity=60)','opacity':'0.6'}";
	private String sql;
	private String value = "";
	protected String emptyMessage = "<div id='emptyMessage'>Aucune information disponible</div>";
	protected String filtreCaption = "";
	protected String pageContent = "";
	protected String updateMessageMask = null;
	protected String where;
	protected Object groupByMask = null;
	protected String selector;
	protected String tableMask = "list-{idListe}";
	protected Map<String, String> extZones;

	/**
	 * Instancie une nouvelle liste affichant les objets de type clazz issus de
	 * la base de données<br/>
	 * L'instruction sql utilisée est dans ce cas celle définie dans la classe
	 * clazz
	 * 
	 * @param clazz
	 *            classe dérivée de KObject
	 * @param request
	 *            objet requête Http
	 */
	@SuppressWarnings("rawtypes")
	public KPageList(Class clazz, HttpServletRequest request) {
		this(clazz, request, "", "");
	}

	/**
	 * @param clazz
	 *            classe dérivée de KObject
	 * @param request
	 *            objet requête Http
	 * @param sql
	 *            Instruction sql permettant d'extraire les enregistrements de
	 *            la base de données
	 */
	@SuppressWarnings({ "rawtypes" })
	public KPageList(Class clazz, HttpServletRequest request, String sql) {
		this(clazz, request, sql, "");
	}

	/**
	 * @param clazz
	 *            classe dérivée de KObject
	 * @param request
	 *            objet requête Http
	 * @param sql
	 *            Instruction sql permettant d'extraire les enregistrements de
	 *            la base de données
	 * @param idForm
	 *            id HTML du formulaire de modification/ajout d'un objet
	 */
	@SuppressWarnings({ "rawtypes" })
	public KPageList(Class clazz, HttpServletRequest request, String sql, String idForm) {
		this(clazz, request, sql, idForm, KRequest.GETPOST("_mode", request, 1));
	}

	/**
	 * @param clazz
	 *            classe dérivée de KObject
	 * @param request
	 *            objet requête Http
	 * @param sql
	 *            Instruction sql permettant d'extraire les enregistrements de
	 *            la base de données
	 * @param idForm
	 *            id HTML du formulaire de modification/ajout d'un objet
	 * @param mode
	 *            mode d'ouverture 1 pour liste, 2 pour formulaire, 3 pour
	 *            validation de formulaire
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public KPageList(Class clazz, HttpServletRequest request, String sql, String idForm, int mode) {
		super(request);
		selector = "";
		setSelectStyle(selectStyle);
		this.clazz = clazz;
		this.where = "";
		this.pagination = new KPagination(request, clazz);
		className = this.clazz.getSimpleName();
		this.mode = mode;
		this.idForm = idForm;
		this.idLb = "";
		extZones = new HashMap<>();
		switch (mode) {
		case 1:// List
			this.sql = sql;
			this.hasNavBarre = false;
			this.hasPageCounter = false;
			this.rowCount = 10;
			this.pageNavCount = 5;
			this.header = "<table id='" + tableMask + "' class='KPageList'>\n";
			this.footer = "</table>\n";
			this.beforeField = "<td><div class='td'>";
			this.afterfield = "</div></td>";
			mask = null;
			innerMask = null;
			try {
				initKoClass();
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Impossible d'initialiser l'objet ko dans KPageList ", e);
			}
			getMask();
			break;
		case 2:// Load form
			createForm();
			try {
				formLoad();
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Impossible de charger le formulaire", e);
			}
			break;
		case 3:// Submit form
			createForm();
		default:
			break;
		}
	}

	@Override
	public void init() {
	}

	private KFieldControl addAddBtn() {
		return this.addHTML("_addBtn", "{_addBtn}");
	}

	private void createForm() {
		try {
			form = new KHttpForm(((KObject) clazz.newInstance()), request, true, getIdForm());
			form.setControlOn(frmControlOn);
			form.setResponse(response, false);
			form.setInsertMode(KRequest.GETPOST("_insertMode", request, "0").equals("1"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private String createInput() {
		String result = "<input type='hidden' id='h-" + id + "' name='h-" + name + "'>";
		result += "<script type='text/javascript'>Forms.Utils.addEvent('" + getTableId() + "','tr','click',function(){$('h-" + id + "').value=$$('id-'+this.id);});</script>";
		return result;
	}

	private String endLine(int nb) {
		String ret = "<td>" + this.getEditBtn() + "</td></tr><tr id='idTR{getUniqueIdHash}' style='display:none'><td></td>";
		ArrayList<String> fields = innerMask.getFields();
		for (String f : fields) {
			f = KString.cleanHTMLAttribute(f);
			ret += "<td id='idTD{getUniqueIdHash}-" + f + "' class='tdKlist'><div class='Klist' id='idDiv{getUniqueIdHash}-" + f + "' style='display:none'></div></td>";
		}
		return ret + "<td></td>";
	}

	private void formLoad() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException, InstantiationException, ClassNotFoundException {
		if (form != null) {
			form.getDefaultComplete("", null);
			// if(isAjax)
			// form.removeFieldControl("buttons");
		}
	}

	private String formSubmit() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException, InstantiationException, ClassNotFoundException {
		String result = "";
		if (form != null) {
			if (updateMessageMask != null)
				form.setUpdateMessageMask(updateMessageMask);
			form.loadAndSubmit();
			result = "<div id='message-" + ajaxDivMessageSubmit + "' class='alert alert-info'>&nbsp;" + form.getUpdateMessage() + "</div>";

		}
		return result;
	}

	private String generateMask(ArrayList<String> fields) {
		String result = "";
		String details = "";
		if (koDetails)
			details = "[...]";
		for (String f : fields) {
			result += beforeField + "{" + f + details + "}" + afterfield;
		}
		return result;
	}

	private String getAddBtn() {
		String result = "";
		if (this.isAjax) {
			result = "<div id='" + getId() + "-btnAdd' class='sBtn' onclick='lbfrmAdd" + idLb + ".show" + "(\"0\",\"" + this.clazz.getName() + "\")'><span>" + addBtnCaption + "</span></div>";
		}
		else {
			result = "<a class='sBtn' href='" + getEditFormUrl() + "'>" + addBtnCaption + "</a>";
		}
		return result;
	}

	private String getCheckedString(String id, String name) {
		String result = "";
		if (checked && !"".equals(checkedMask))
			result = checkedMask.replace("{id}", id).replace("{name}", name).replace("{checkedType}", checkedType);
		return result;
	}

	private String getEditBtn() {
		String result = "";
		if (hasEditBtn) {
			if (this.isAjax) {
				result = "<div class='sBtn default' onclick='lbfrmEdit" + idLb + ".show" + "(\"{keyValuesToUrl}\",\"" + this.clazz.getName() + "\",\"line-{getUniqueIdHash}\")'><span>" + editBtnCaption + "</span></div>";
			} else {
				result = "<a class='sBtn' href='" + getEditFormUrl() + "?{keyValuesToUrl}'>" + editBtnCaption + "</a>";
			}
		}
		return result;
	}

	private KMask getMask() {
		String ck = getCheckedString(id, name);
		countFields = KRegExpr.getCount("<td>", "</td>", this.innerMask.get(0));
		String btn = endLine(countFields);
		String inputId = "<input type='hidden' id='id-line-{getUniqueIdHash}' value='{getFirstKeyValue}'>";
		mask = new KMask();
		if (isShowCaption && !"checkbox".equals(checkedType))
			mask.addMask("<tr class='' id='line-{getUniqueIdHash}'><td>" + inputId + "</td><td></td>" + this.innerMask.get(0) + "<td></td></tr>");
		else
			mask.addMask("<tr class='' id='line-{getUniqueIdHash}'><td>" + inputId + "</td>" + ck + this.innerMask.get(0) + "<td></td></tr>");
		int i = 1;
		if (innerMask.count() > 1) {
			for (String s : innerMask) {
				mask.addMask("<tr class='selection" + i + " selectable' id='line-{getUniqueIdHash}'><td>" + inputId + "</td>" + ck + s + btn + "</tr>");
				i++;
			}
		} else
			mask.addMask("<tr class='odd selectable' id='line-{getUniqueIdHash}'>" + "<td>" + inputId + "</td>" + ck + innerMask.get(0) + btn + "</tr>");
		if (!isShowCaption && mask.count() > 2)
			mask.remove(0);
		return this.mask;
	}

	private String getQueryString() {
		KQueryString qs = new KQueryString("");
		qs.addValueFromRequest("_ss", request);
		qs.addValueFromRequest("_sf", request);
		qs.addValueFromRequest("_ft", request);
		qs.addValueFromRequest("_page", request);
		return qs.toString();
	}

	private void initKoClass() throws InstantiationException, IllegalAccessException {
		KObject ko = Ko.getKoInstance(clazz);
		ArrayList<String> fields = ko.getFieldNames();
		innerMask = new KMask();
		String aMask = generateMask(fields);
		innerMask.addMask(aMask);
		innerMask.addMask(aMask);
		countFields = fields.size();
	}

	private String insertJs(String result, String script) {
		if (result.indexOf("</body>") != -1)
			result = result.replace("</body>", script + "</body>");
		else
			result += script;
		return result;
	}

	/**
	 * Ajoute un champ à afficher dans une colonne de la liste
	 * 
	 * @param fieldName
	 *            champ à ajouter
	 */
	public void addField(String fieldName) {
		addField(beforeField, fieldName, afterfield);
	}

	@Override
	public void addField(String beforeField, String fieldName, String afterField) {
		if (innerMask != null)
			innerMask.addField(beforeField, fieldName, afterField);
	}

	/**
	 * Ajoute la zone HTML "filtre" si le membre hasFiltre est vrai
	 * 
	 * @return control visuel filtre
	 */
	public KFieldControl addFiltre() {
		return this.addHTML("_filtre", "{_filtre}");
	}

	/**
	 * Ajoute la zone barre de navigation entre les pages "navBarre" si le
	 * membre hasNavBarre vaut vrai
	 */
	public void addNavBarre() {
		addNavBarre(5, 10, "");
	}

	/**
	 * Ajoute la zone barre de navigation entre les pages "navBarre" si le
	 * membre hasNavBarre vaut vrai
	 * 
	 * @param pageNavCount
	 *            nombre de pages à afficher dans la barre
	 * @param rowCount
	 * @param imgFolder
	 *            dossier contenant les images de la barre de navigation
	 * @return la zone HTML créée
	 */
	public KFieldControl addNavBarre(int pageNavCount, int rowCount, String imgFolder) {
		this.hasNavBarre = true;
		this.pageNavCount = pageNavCount;
		this.rowCount = rowCount;
		return this.addHTML("_navBarre", "\n{_navBarre}\n");
	}

	/**
	 * @return contrôle visuel HTML contenant la zone "pageCounter"
	 */
	public KFieldControl addPageCounter() {
		this.hasPageCounter = true;
		return this.addHTML("_pageCounter", "\n{_pageCounter}\n");
	}

	public String fieldControlsToString() {
		return fieldControls.toString();
	}

	/**
	 * Retourne le contrôle visuel nommé fieldName du formulaire de mise à jour
	 * ou d'insertion de l'objet
	 * 
	 * @param fieldName
	 *            nom du contrôle
	 * @return le contrôle visuel si il existe, null dans le cas contraire
	 */
	public KFieldControl formGetFieldControl(String fieldName) {
		KFieldControl result = null;
		if (form != null)
			result = form.getFieldControl(fieldName);
		return result;
	}

	/**
	 * Supprime le contrôle visuel nommé fieldName du formulaire de mise à jour
	 * ou d'insertion de l'objet
	 * 
	 * @param fieldName
	 */
	public void formRemoveFieldControl(String fieldName) {
		if (form != null) {
			form.removeFieldControl(fieldName);
		}
	}

	/**
	 * Echange la position de deux contrôles visuels du formulaire de mise à
	 * jour ou d'insertion de l'objet
	 * 
	 * @param fieldName1
	 *            nom du premier contrôle
	 * @param fieldName2
	 *            nom du second contrôle
	 */
	public void formSwapFcPos(String fieldName1, String fieldName2) {
		if (form != null) {
			form.swapFcPos(fieldName1, fieldName2);
		}
	}

	/**
	 * Retourne le numéro de la page active,passé dans l'URL avec le paramètre
	 * "page"
	 * 
	 * @return numéro de la page active
	 */
	public int getActivePage() {
		int page = KRequest.GETPOST("_page", request, 1);
		int totalRowCount = KRequest.GETPOST("_totalRowCount", request, -1);
		if (totalRowCount != -1) {
			if (page > Math.ceil(1.0 * totalRowCount / rowCount))
				page -= 1;
		}
		if (page < 1)
			page = 1;
		return page;
	}

	public String getAddBtnCaption() {
		return addBtnCaption;
	}

	public String getAfterfield() {
		return afterfield;
	}

	public String getAjaxDivContentRefresh() {
		return ajaxDivContentRefresh;
	}

	/**
	 * Retourne la zone HTML (identifiée par son ID) qui affichera le résultat
	 * de la mise à jour de l'objet après validation d'un formulaire d'insertion
	 * ou de modification
	 * 
	 * @return id de la zone d'affichage
	 */
	public String getAjaxDivMessageSubmit() {
		return ajaxDivMessageSubmit;
	}

	public String getBeforeField() {
		return beforeField;
	}

	public String getCheckedMask() {
		return checkedMask;
	}

	public Class<KObject> getClazz() {
		return clazz;
	}

	/**
	 * Charge de façon complète la liste si le paramètre de l'URL mode vaut 1<br/>
	 * Appelle dans l'ordre : addFiltre, addNavBarre, load, addPageCounter,
	 * getAddBtn
	 * 
	 * @see net.ko.http.views.KPageList.#addFiltre()
	 * @see net.ko.http.views.KPageList.#addNavBarre()
	 * @see net.ko.http.views.KPageList.#load()
	 * @see net.ko.http.views.KPageList.#addPageCounter()
	 * @see net.ko.http.views.KPageList.#getAddBtn()
	 */
	public void getDefaultComplete() {
		if (mode == 1) {
			if (hasFiltre)
				this.addFiltre();
			this.addHTML("_listContent", "{_ajx}{_listContent}");
			if (hasNavBarre)
				this.addNavBarre();
			this.load();
			if (!fieldControls.containsKey("_page"))
				this.addHTML("_page", "{_page}");
			if (hasPageCounter)
				this.addPageCounter();
			if (hasAddBtn)
				this.addAddBtn();
			this.addHTML("listContentEnd", "{/_listContent}");
		}
	}

	public String getEditBtnCaption() {
		return editBtnCaption;
	}

	/**
	 * @return retourne l'url correspondant au formulaire de
	 *         modification/insertion d'un objet
	 */
	public String getEditFormUrl() {
		editFormUrl = KRequest.getRequestURI(editFormUrl, request);
		return editFormUrl;
	}

	/**
	 * Retourne l'étiquette (en-tête) d'une colonne de la liste
	 * 
	 * @param fieldName
	 *            nom du champ/membre/méthode
	 * @return l'étiquette d'une colonne de la liste
	 */
	public String getFieldCaption(String fieldName) {
		return pagination.getFieldCaption(fieldName);
	}

	public String getFiltre() {
		String onkeyup = "onkeyup=\"Forms.Utils.execOnKeyUp(event,13,function(){Forms.Framework.filtre('" + this.ajaxDivContentRefresh + "','" + this.listContentUrl + "?_refresh&" + KQueryString.setValue(KRequest.getQueryString(request), "_page", "1") + "','_filtre='+$('_filtre').value,'" + getTableId() + "')});\"";
		String ret = "";
		ret += "<fieldset>\n";
		ret += "<legend>" + getFiltreCaption() + "</legend>\n";
		ret += "<input type='search' class='form-control filter' placeholder='Rechercher...' id='_filtre' name='_filtre' value='" + KRequest.POST("_filtre", request, "") + "' " + onkeyup + ">\n";
		ret += "</fieldset>\n";
		return ret;
	}

	private String getTableId() {
		return tableMask.replace("{idListe}", id);
	}

	public String getFiltreCaption() {
		String result = filtreCaption;
		if ("".equals(result)) {
			result = "Filtrer sur " + getDisplayInstance().getCaption(clazz);
		}
		return result;
	}

	public String getFooter() {
		return footer;
	}

	/**
	 * Retourne le formulaire associé à la liste ou null s'il n'existe pas
	 * 
	 * @return formulaire de modification/insertion
	 */
	public KHttpForm getForm() {
		return form;
	}

	public String getFrmCancelCaption() {
		return frmCancelCaption;
	}

	public String getFrmControlOn() {
		return frmControlOn;
	}

	public String getFrmTitleAdd() {
		return frmTitleAdd;
	}

	public String getFrmTitleEdit() {
		return frmTitleEdit;
	}

	public String getFrmValidCaption() {
		return frmValidCaption;
	}

	public String getHeader() {
		return header;
	}

	public String getId() {
		return id;
	}

	/**
	 * @return retourne l'url de la page mettant à jour un objet après
	 *         validation du formulaire
	 */
	public String getIdForm() {
		if ("".equals(idForm) || idForm == null)
			idForm = "frm" + className;
		return idForm;
	}

	public String getImgFolder() {
		return imgFolder;
	}

	public KMask getInnerMask() {
		return innerMask;
	}

	/**
	 * Retourne la liste complète des objets à afficher
	 * 
	 * @return la liste des objets affichés
	 */
	public KListObject<? extends KObject> getKListObject() {
		return this.pagination.getKListObject();
	}

	/**
	 * @return retourne l'url de la page affichant la liste des objets
	 */
	public String getListContentUrl() {
		listContentUrl = KRequest.getRequestURI(listContentUrl, request);
		return listContentUrl;
	}

	/**
	 * @return retourne l'url de la page mettant à jour un objet après
	 *         validation du formulaire
	 */
	public String getMessageAndUpdateUrl() {
		if ("".equals(messageAndUpdateUrl) || messageAndUpdateUrl == null)
			messageAndUpdateUrl = getListContentUrl();
		return messageAndUpdateUrl;
	}

	public int getMessageDelay() {
		return messageDelay;
	}

	public int getMode() {
		return mode;
	}

	public String getName() {
		return name;
	}

	/**
	 * Retourne le contenu HTML de la zone pageCounter affichant le nombre de
	 * pages
	 * 
	 * @return contenu HTML
	 */
	public String getPageCounter() {
		String qs = KRequest.getQueryString(request);
		qs = KQueryString.removeKey(qs, "_nb");
		int totalRowCount = this.pagination.totalRowCount;
		String onkeyup = "onkeyup=\"Forms.Utils.execOnKeyUp(event,13,function(){Forms.Framework.gotoPage('" + this.ajaxDivContentRefresh + "','" + this.listContentUrl + "?_totalRowCount=" + totalRowCount + "&_refresh&" + qs + "','_nb='+$('_nb').value,'" + getTableId() + "',1,true)});\"";
		String ret = "<input type='hidden' name='_cls' value='" + clazz.getName() + "' id='_cls'>" +
				"<input type='hidden' name='_ss' value='" + this.pagination.sortedSens + "'>" +
				"<input type='hidden' name='_sf' value='" + this.pagination.sortedField + "'>" +
				"<input type='hidden' name='_page' value='" + this.getActivePage() + "'>" +
				"<div class='pageCounter'><label style='vertical-align: middle' for='nb'>Afficher&nbsp;</label>" +
				"<input class='rowCount' type='number' min='1' max='" + totalRowCount + "' value='" + this.rowCount + "' id='_nb' name='_nb' " + onkeyup + ">" +
				"<label class='totalRowCount' style='vertical-align: middle' for='_nb'>&nbsp;enregistrement(s) sur " + totalRowCount + "</label>" + "<div class='nbPages'>" + KString.pluriel(this.pagination.nbPages, "page") + " au total</div></div>";
		return ret;
	}

	public int getPageNavCount() {
		return pageNavCount;
	}

	/**
	 * @return retourne l'url de la page mettant à jour un objet après
	 *         validation du formulaire
	 */
	public String getRequestUrl() {
		requestUrl = KRequest.getRequestURI(requestUrl, request);
		return requestUrl;
	}

	public int getRowCount() {
		return rowCount;
	}

	public String getSelectStyle() {
		return selectStyle;
	}

	public String getSql() {
		return sql;
	}

	public String getUpdateMessageMask() {
		return updateMessageMask;
	}

	public String getValue() {
		return value;
	}

	public boolean isAjax() {
		return isAjax;
	}

	public boolean isChecked() {
		return checked;
	}

	public boolean isCreateAjaxDivs() {
		return createAjaxDivs;
	}

	/**
	 * Retourne vrai si la form est modale<br/>
	 * Une forme modale est affichée au dessus de la page et empêche
	 * l'utilisateur de cliquer en dessous
	 * 
	 * @return vrai si la form est modale
	 */
	public boolean isFormModal() {
		return isFormModal;
	}

	/**
	 * @return vrai si la liste possède un bouton Ajouter
	 */
	public boolean isHasAddBtn() {
		return hasAddBtn;
	}

	public boolean isHasEditBtn() {
		return hasEditBtn;
	}

	public boolean isHasFiltre() {
		return hasFiltre;
	}

	public boolean isHasNavBarre() {
		return hasNavBarre;
	}

	public boolean isHasPageCounter() {
		return hasPageCounter;
	}

	/**
	 * @return vrai si les détails sont activés
	 */
	public boolean isKoDetails() {
		return koDetails;
	}

	public boolean isShowCaption() {
		return isShowCaption;
	}

	public String kajaxIncludes() {
		return KoHttp.kajaxIncludes(request);
	}

	public void keepFieldsByIndex(String before, String after, String indexes) {
		if (innerMask != null)
			innerMask.keep(before, after, indexes);
	}

	/**
	 * @param clazz
	 *            classe à comparer avec la classe affichée dans la liste
	 * @return vrai si clazz est égal à la classe affichée dans la liste
	 */
	public boolean koClassEquals(Class<KObject> clazz) {
		return this.clazz.equals(clazz);
	}

	/**
	 * Charge la page à afficher en tenant compte du numéro de page passé dans
	 * l'URL,du filtre, du nombre d'enregistements à afficher
	 */
	public void load() {
		IKobjectDisplay display = getDisplayInstance();
		if (!(display.getClass().equals(KObjectDisplay.class))) {
			display.beforeLoading(clazz, this, request);
		}

		if (KRequest.keyExists("_nb", request))
			this.rowCount = KRequest.GETPOST("_nb", request, 20);
		if (this.rowCount < 1)
			this.rowCount = 1;
		int p = getActivePage();
		String filtre = KRequest.POST("_filtre", request);
		if (filtre != null) {
			try {
				this.sql = KObject.makeSQLFilter_(filtre, clazz, getDao().quote());
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Impossible d'initialiser l'instruction SQL de filtre dans KPageList ", e);
			}
		} else
			setSql(sql);
		String _where = KRequest.POST("_where", request);
		if (_where != null) {
			try {
				where = URLDecoder.decode(_where, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				where = "";
			}
		}
		this.sql = KSqlQuery.addWhere(this.sql, where);
		this.pagination.setIdListe(getTableId());
		this.pagination.setDao(getDao());
		this.pagination.setDisplayInstance(getDisplayInstance());
		this.pagination.setRequestUrl(getListContentUrl());
		this.pagination.setAjaxDivContentRefresh(ajaxDivContentRefresh);
		this.pagination.setAjax(this.isAjax);
		this.pagination.setSql(this.sql);
		this.pagination.setPageNavCount(pageNavCount);
		this.pagination.setRowCount(rowCount);
		this.pagination.setIsShowCaptions(this.isShowCaption);
		this.pagination.setNoLimit(!hasNavBarre);
		this.pagination.setChecked(checked);
		this.pagination.mask = getMask();
		this.pagination.setValue(value);
		if (!isLoaded) {
			this.pagination.gotoo(p);
			isLoaded = true;
		} else
			this.pagination.gotooWhenLoaded(p);
		String pHeader = this.header;
		if (!"".equals(pHeader))
			pHeader = pHeader.replaceFirst("\\{idListe\\}", id);
		this.pagination.setGroupBy(this.groupByMask);
		this.pagination.setKoDetails(koDetails);
		if (!(display.getClass().equals(KObjectDisplay.class))) {
			display.afterLoading(pagination.getKListObject(), this, request);
		}
		this.pageContent = this.pagination.getInputCountAsString() + pHeader + this.pagination + this.footer;
	}

	@Override
	public boolean removeField(String fieldName) {
		boolean result = false;
		if (innerMask != null) {
			result = innerMask.removeField(fieldName);
		}
		return result;
	}

	@Override
	public void removeFields(String[] fieldNames) {
		for (String f : fieldNames)
			removeField(f);
	}

	public void removeFieldsByIndex(String indexes) {
		if (innerMask != null)
			innerMask.remove(KString.toArrayOfInt(indexes, Ko.krequestValueSep()));
	}

	@Override
	public void replaceField(String oldFieldName, String newFieldName) {
		if (innerMask != null)
			innerMask.replaceField(oldFieldName, newFieldName);
	}

	@Override
	public void replaceField(String oldFieldName, String newFieldName, String newCaption) {
		replaceField(oldFieldName, newFieldName);
		setFieldCaption(newFieldName, newCaption);
	}

	public void setAddBtnCaption(String addBtnCaption) {
		this.addBtnCaption = addBtnCaption;
	}

	/**
	 * Détermine les éléments HTML à insérer après chaque membre de chaque objet
	 * à afficher dans la liste
	 * 
	 * @param afterfield
	 *            élément HTML
	 */
	public void setAfterfield(String afterfield) {
		if (this.afterfield != afterfield) {
			this.afterfield = afterfield;
			try {
				initKoClass();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * @param isAjax
	 */
	public void setAjax(boolean isAjax) {
		this.isAjax = isAjax;
	}

	/**
	 * Détermine la zone HTML (identifiée par son ID) qui affichera avec ajax la
	 * liste mise à jour après validation du formulaire<br/>
	 * la liste complète doit appartenir à cette zone
	 * 
	 * @param ajaxDivContentRefresh
	 */
	public void setAjaxDivContentRefresh(String ajaxDivContentRefresh) {
		this.ajaxDivContentRefresh = ajaxDivContentRefresh;
	}

	/**
	 * Définit la zone HTML (identifiée par son ID) qui affichera le résultat de
	 * la mise à jour de l'objet après validation d'un formulaire d'insertion ou
	 * de modification
	 * 
	 * @param ajaxDivMessageSubmit
	 */
	public void setAjaxDivMessageSubmit(String ajaxDivMessageSubmit) {
		this.ajaxDivMessageSubmit = ajaxDivMessageSubmit;
	}

	/**
	 * Détermine les éléments HTML à insérer avant chaque membre de chaque objet
	 * à afficher dans la liste
	 * 
	 * @param beforeField
	 *            éléments HTML
	 */
	public void setBeforeField(String beforeField) {
		if (this.beforeField != beforeField) {
			this.beforeField = beforeField;
			try {
				initKoClass();
			} catch (Exception e) {
			}
		}
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void setCheckedMask(String checkedMask) {
		this.checkedMask = checkedMask;
	}

	public void setClazz(Class<KObject> clazz) {
		this.clazz = clazz;
	}

	public void setCreateAjaxDivs(boolean createAjaxDivs) {
		this.createAjaxDivs = createAjaxDivs;
	}

	public void setEditable() {
		setEditable(!hasEditBtn);
	}

	/**
	 * Détermine si chaque objet/enregistrement doit comporter un bouton
	 * "Modifier" et si la page contiendra un bouton "Ajouter"
	 * 
	 * @param isEditable
	 *            vrai si chaque objet/enregistrement affiche le bouton
	 *            "Modifier" et si la page contient un bouton "Ajouter"
	 */
	public void setEditable(boolean isEditable) {
		hasAddBtn = isEditable;
		hasEditBtn = isEditable;
		setKoDetails(isEditable);
	}

	public void setEditBtnCaption(String editBtnCaption) {
		this.editBtnCaption = editBtnCaption;
	}

	/**
	 * Modifie les paramètres du formulaire de modification/ajout d'objet
	 * 
	 * @param idForm
	 *            id HTML du formulaire
	 * @param editFormUrl
	 *            URL du formulaire
	 */
	public void setEditFormParams(String idForm, String editFormUrl) {
		this.idForm = idForm;
		this.editFormUrl = editFormUrl;
	}

	/**
	 * @param editFormUrl
	 *            URL de la page affichant le formulaire de Modification/Ajout
	 *            d'un objet
	 */
	public void setEditFormUrl(String editFormUrl) {
		this.editFormUrl = editFormUrl;
	}

	/**
	 * Modifie l'étiquette (en-tête) d'une colonne de la liste
	 * 
	 * @param fieldName
	 *            nom du champ/membre/méthode
	 * @param caption
	 */
	public void setFieldCaption(String fieldName, String caption) {
		pagination.setFieldCaption(fieldName, caption);
	}

	public void setFiltreCaption(String filtreCaption) {
		this.filtreCaption = filtreCaption;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	/**
	 * Détermine si la fenêtre doit être modale
	 * 
	 * @param isFormModal
	 *            vrai si la form doit être modale
	 */
	public void setFormModal(boolean isFormModal) {
		this.isFormModal = isFormModal;
	}

	public void setFrmCancelCaption(String frmCancelCaption) {
		this.frmCancelCaption = frmCancelCaption;
	}

	public void setFrmControlOn(String frmControlOn) {
		this.frmControlOn = frmControlOn;
	}

	public void setFrmTitleAdd(String frmTitleAdd) {
		this.frmTitleAdd = frmTitleAdd;
	}

	public void setFrmTitleEdit(String frmTitleEdit) {
		this.frmTitleEdit = frmTitleEdit;
	}

	public void setFrmValidCaption(String frmValidCaption) {
		this.frmValidCaption = frmValidCaption;
	}

	/**
	 * Détermine si la page comporte un bouton "Ajouter"
	 * 
	 * @param hasAddBtn
	 *            vrai si la page affiche un bouton "Ajouter"
	 */
	public void setHasAddBtn(boolean hasAddBtn) {
		this.hasAddBtn = hasAddBtn;
	}

	/**
	 * @param hasEditBtn
	 */
	public void setHasEditBtn(boolean hasEditBtn) {
		this.hasEditBtn = hasEditBtn;
	}

	public void setHasFiltre(boolean hasFiltre) {
		this.hasFiltre = hasFiltre;
	}

	/**
	 * @param hasNavBarre
	 */
	public void setHasNavBarre(boolean hasNavBarre) {
		this.hasNavBarre = hasNavBarre;
	}

	/**
	 * @param hasPageCounter
	 */
	public void setHasPageCounter(boolean hasPageCounter) {
		this.hasPageCounter = hasPageCounter;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param idForm
	 *            id HTML du formulaire permettant la modification/ajout d'un
	 *            objet
	 */
	public void setIdForm(String idForm) {
		this.idForm = idForm;
	}

	/**
	 * Spécifie le chemin vers le dossier contenant les images de la navBarre
	 * 
	 * @see net.ko.http.views.KNavBarre
	 * @param imgFolder
	 *            dossier
	 */
	public void setImgFolder(String imgFolder) {
		this.imgFolder = imgFolder;
	}

	public void setInnerMask(KMask innerMask) {
		this.innerMask = innerMask;
	}

	/**
	 * Détermine si les en-têtes de colonnes seront affichées, permettant de
	 * trier les enregistrement suivant la valeur du champ
	 * 
	 * @param isShowCaption
	 *            vrai si les en-têtes des colonnes doivent être affichées
	 */
	public void setIsShowCaption(boolean isShowCaption) {
		this.isShowCaption = isShowCaption;
	}

	/**
	 * Détermine si le bouton Détails est accessible sur chaque objet de la
	 * liste
	 * 
	 * @param koDetails
	 *            vrai si les détails sont activés
	 */
	public void setKoDetails(boolean koDetails) {
		if (innerMask != null) {
			this.koDetails = koDetails;
			innerMask.setKoDetails(koDetails);
		}
	}

	/**
	 * Modifie les paramètres de la zone responsable du rafraichissement de la
	 * liste d'objets
	 * 
	 * @param ajaxDivContentRefresh
	 *            id HTML de la zone affichant la liste
	 * @param listContentUrl
	 *            URL de la liste
	 */
	public void setListContentRefreshParams(String ajaxDivContentRefresh, String listContentUrl) {
		this.ajaxDivContentRefresh = ajaxDivContentRefresh;
		this.listContentUrl = listContentUrl;
	}

	/**
	 * @param listContentUrl
	 *            URL de la page affichant la liste des objets
	 */
	public void setListContentUrl(String listContentUrl) {
		this.listContentUrl = listContentUrl;
	}

	/**
	 * Modifie les paramètres de la zone effectuant la mise à jour de l'objet
	 * 
	 * @param ajaxDivMessageSubmit
	 *            id HTML de la zone affichant le message de mise à jour
	 * @param messageAndUpdateUrl
	 *            URL de la page de mise à jour et d'affichage de message
	 */
	public void setMessageAndUpdateParams(String ajaxDivMessageSubmit, String messageAndUpdateUrl) {
		this.ajaxDivMessageSubmit = ajaxDivMessageSubmit;
		this.messageAndUpdateUrl = messageAndUpdateUrl;
	}

	/**
	 * @param messageAndUpdateUrl
	 *            URL de la page mettant à jour un objet après validation du
	 *            formulaire
	 */
	public void setMessageAndUpdateUrl(String messageAndUpdateUrl) {
		this.messageAndUpdateUrl = messageAndUpdateUrl;
	}

	/**
	 * @param messageDelay
	 */
	public void setMessageDelay(int messageDelay) {
		this.messageDelay = messageDelay;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPageNavCount(int pageNavCount) {
		this.pageNavCount = pageNavCount;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
		if (this.pagination != null)
			this.pagination.setRequestUrl(requestUrl);
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public void setSelectStyle(String selectStyle) {
		this.selectStyle = selectStyle.replace("'", "\"");
	}

	public void setShowCaption(boolean isShowCaption) {
		this.isShowCaption = isShowCaption;
	}

	public void setSql(String sql) {
		if (sql == null || "".equals(sql)) {
			KObject ko = null;
			try {
				ko = Ko.getKoInstance(clazz);
				this.sql = ko.getSql(getDao().quote());
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Impossible d'instancier l'objet ko dans KPageList de la classe : " + clazz, e);
			}
		} else {
			this.sql = sql;
		}
	}

	public void addWhere(String where) {
		setSql(this.sql);
		this.sql = KSqlQuery.addWhere(this.sql, where);
	}

	public void setUpdateMessageMask(String updateMessageMask) {
		this.updateMessageMask = updateMessageMask;
	}

	public void setValue() {
		KListObject<? extends KObject> kl = getKListObject();
		if (kl != null)
			value = kl.getKeyValuesForHasAndBelongsToMany(";");
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void swapFields(String fieldName1, String fieldName2) {
		if (innerMask != null)
			innerMask.swapFields(fieldName1, fieldName2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * Retourne la chaîne HTML affichant en fonction du mode passé
	 * automatiquement dans l'URL<br/>
	 * <ul>
	 * <li>si mode vaut 1 : la liste des objets</li>
	 * <li>si mode vaut 2 : le chargmement et l'affichage du formulaire de
	 * modification/insertion</li>
	 * <li>si mode vaut 3 : la soumission du formulaire par la méthode post</li>
	 * </ul>
	 */
	public String toString() {
		String result = "";
		switch (mode) {
		case 1:// List
			setLoaded(true);
			result += fieldControlsToString();
			String navBarre = "";
			String pageCounter = "";
			String addBtn = "";
			String filtreContent = "";
			String _ajxDiv = "";
			String _ajxContentDiv = "";
			String _ajxContentDivEnd = "";
			if (_refresh) {
				result = KString.getPart("{_listContent}", "{/_listContent}", result);
			}
			if (createAjaxDivs && !_refresh) {
				_ajxDiv = "<div id='" + ajaxDivMessageSubmit + "'></div>";
				_ajxContentDiv = "<div id='" + ajaxDivContentRefresh + "'>";
				_ajxContentDivEnd = "</div>";
			}
			boolean autoNav = autoDisplayNavBarre && (pagination.totalRowCount > rowCount);

			if ((!autoDisplayNavBarre && this.hasNavBarre) || autoNav)
				navBarre = this.pagination.showNavBarre(this.imgFolder);
			if (this.hasPageCounter)
				pageCounter = this.getPageCounter();
			if (hasAddBtn)
				addBtn = getAddBtn();
			if (hasFiltre)
				filtreContent = getFiltre();
			if (pagination.isEmpty())
				pageContent = emptyMessage;
			result = result.replace("{_ajx}", _ajxDiv);
			result = result.replace("{_listContent}", _ajxContentDiv);
			result = result.replace("{/_listContent}", _ajxContentDivEnd);
			result = result.replace("{_navBarre}", navBarre);
			result = result.replace("{_pageCounter}", pageCounter);
			result = result.replace("{_page}", pageContent);
			result = result.replace("{_filtre}", filtreContent);
			result = result.replace("{_addBtn}", addBtn);

			for (Map.Entry<String, String> extZone : extZones.entrySet()) {
				result = result.replace("{" + extZone.getKey() + "}", extZone.getValue() + "");
			}

			String ajax = "<script>_qs='" + getQueryString() + "';</script>";
			if (KRequest.keyExists("_toToogle", request)) {
				String toToogle = KRequest.GETPOST("_toToogle", request);
				result += "<script>Forms.Framework.toogleAll('" + toToogle + "');</script>";
			}
			if (this.isAjax) {
				try {
					ajax += KJavaScript.kGetAjxForm("frmEdit", frmTitleEdit, this);
					ajax += KJavaScript.kGetAjxForm("frmAdd", frmTitleAdd, this);
					if (KString.isNotNull(selector)) {
						String selectItem = "";
						if (KRequest.GETPOST("selectedItem", request) != null) {
							String selectItemId = KRequest.GETPOST("selectedItem", request);
							try {
								KObject selectedObject = pagination.getKListObject().selectById(selectItemId);
								selectItem = ".select($('line-" + selectedObject.getUniqueIdHash() + "'))";
							} catch (Exception e) {
								selectItem = "";
							}
						}
						ajax += "<script>(new $selector('#" + ajaxDivContentRefresh + " tr.selectable','keyup',false,0,undefined,undefined," + selector + "))" + selectItem + ";</script>";
					}
				} catch (Exception e) {
				}
			}

			if (ajaxIncludes)
				result += KoHttp.kajaxIncludes(request);

			if (_ajx) {
				result = insertJs(result, ajax);
			} else {
				result = insertJs(getPageHeader() + result + getPageFooter(), ajax);
			}
			result = insertJs(result, createInput());
			break;
		case 2:// Form load
			form.setId(idForm);
			result = form.toString();
			break;
		case 3:// Form submit
			try {
				result = formSubmit();
			} catch (Exception e) {
				Ko.klogger().log(Level.SEVERE, "Impossible de soumettre le formulaire " + this.idForm, e);
			}
			break;
		}

		return result;
	}

	public Object getGroupByMask() {
		return groupByMask;
	}

	public void setGroupByMask(Object groupByMask) {
		this.groupByMask = groupByMask;
	}

	public void addGroupByMask(String aGroupByMask) {
		if (groupByMask == null)
			groupByMask = aGroupByMask;
		else {
			if (groupByMask.getClass().isArray()) {
				groupByMask = KoUtils.extend(groupByMask, 1);
				String[] strs = (String[]) groupByMask;
				strs[strs.length - 1] = aGroupByMask;
				groupByMask = strs;
			} else {
				String[] strs = new String[2];
				strs[0] = groupByMask + "";
				strs[1] = aGroupByMask;
				groupByMask = strs;
			}
		}
	}

	protected KObjectDisplay getKobjectDisplay() {
		KObjectDisplay koDisplay = Ko.koDisplays.get(this.clazz.getSimpleName());
		if (koDisplay == null)
			koDisplay = Ko.defaultKoDisplay();
		return koDisplay;
	}

	@Override
	public DatabaseDAO<KObject> getDao() {
		if (dao == null) {
			if (daoClassName != null & !"".equals(daoClassName)) {
				try {
					Class<DatabaseDAO<KObject>> classDao = (Class<DatabaseDAO<KObject>>) Class.forName(daoClassName);
				} catch (ClassNotFoundException e) {
					if (clazz != null) {
						dao = (DatabaseDAO<KObject>) Ko.getDao(clazz);
					}
				}
			} else {
				if (clazz != null) {
					dao = (DatabaseDAO<KObject>) Ko.getDao(clazz);
				}
			}
		}
		return dao;
	}

	public void addSelector() {
		this.addSelector("113");
	}

	public void addSelector(String selector) {
		if (KString.isNotNull(selector))
			this.selector = selector;
	}

	public void setZoneValue(String key, String value) {
		extZones.put(key, value);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getCountFields() {
		return countFields;
	}

	public void setCountFields(int countFields) {
		this.countFields = countFields;
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	public KPagination getPagination() {
		return pagination;
	}

	public void setPagination(KPagination pagination) {
		this.pagination = pagination;
	}

	public String getEmptyMessage() {
		return emptyMessage;
	}

	public void setEmptyMessage(String emptyMessage) {
		this.emptyMessage = emptyMessage;
	}

	public String getPageContent() {
		return pageContent;
	}

	public void setPageContent(String pageContent) {
		this.pageContent = pageContent;
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(String where) {
		this.where = where;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getTableMask() {
		return tableMask;
	}

	public void setTableMask(String tableMask) {
		this.tableMask = tableMask;
	}

	public Map<String, String> getExtZones() {
		return extZones;
	}

	public void setExtZones(Map<String, String> extZones) {
		this.extZones = extZones;
	}

	public void setForm(KHttpForm form) {
		this.form = form;
	}

	public void setMask(KMask mask) {
		this.mask = mask;
	}

	public String getIdLb() {
		return idLb;
	}

	public void setIdLb(String idLb) {
		this.idLb = idLb;
	}
}
