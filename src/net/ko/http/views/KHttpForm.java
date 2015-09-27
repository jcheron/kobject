package net.ko.http.views;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.controller.KObjectController;
import net.ko.controller.KObjectFieldController;
import net.ko.dao.DaoList;
import net.ko.displays.Display;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.js.KJavaScript;
import net.ko.http.objects.KRequest;
import net.ko.inheritance.KReflectObject;
import net.ko.kobject.KConstraint;
import net.ko.kobject.KConstraintHasAndBelongsToMany;
import net.ko.kobject.KConstraintHasMany;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.kobject.KRecordStatus;
import net.ko.types.HtmlControlType;
import net.ko.utils.KString;
import net.ko.utils.KStrings;

/**
 * Formulaire HTML complet, avec contrôles visuels permettant de modifier/
 * ajouter un KObject
 * 
 * @author jcheron
 * 
 */
public class KHttpForm extends KobjectHttpAbstractForm {
	protected String action;
	protected String addedValidatorString = "";
	protected boolean autoClose = true;
	protected boolean autoOpen = true;
	protected boolean clientControl;
	protected String controlOn = "['onchange','onkeyup']";
	protected String fieldSetLegend = "";
	protected int fieldSets;
	protected ArrayList<KHtmlFieldControl> fieldsToSubmit;
	protected String id = "";
	protected String keyValue = "";
	protected String method;
	protected KRecordStatus updateAction;
	protected String updateMessageMask = "{toString} {updateMessage}";
	protected String validatorString;
	protected String redirectUrl = "";
	protected boolean partialRequest = false;

	/**
	 * @param ko
	 *            objet KObject à ajouter/modifier/supprimer
	 * @param request
	 *            requête Http
	 */
	public KHttpForm(KObject ko, HttpServletRequest request) {
		this(ko, request, true, "frm" + ko.getClass().getSimpleName());
	}

	/**
	 * Crée une instance de formulaire permettant d'ajouter/modifier une
	 * instance d'objet dérivant de KObject
	 * 
	 * @param ko
	 *            objet KObject à ajouter/modifier
	 * @param request
	 *            requête Http
	 * @param clientControl
	 *            vrai pour une activation du contrôleur côté client
	 * @param id
	 *            id HTML du formulaire
	 */
	public KHttpForm(KObject ko, HttpServletRequest request, boolean clientControl, String id) {
		super(ko, request);
		this.clientControl = clientControl;
		this.controlOn = "['onkeyup','onchange']";
		this.action = "";
		this.method = "POST";
		this.fieldSets = 0;
		this.id = id;
		fieldsToSubmit = new ArrayList<>();
	}

	@Override
	public void init() {
	}

	private KFieldControl fieldSet(String legend, String options) {
		return fieldSet(legend, "fs" + KString.cleanHTMLAttribute(legend), options);
	}

	private KFieldControl fieldSet(String legend, String id, String options) {
		return new KHtmlFieldControl(legend, "", "", id, HtmlControlType.khcFieldSet, null, options);
	}

	private KFieldControl getDefaultFieldSet() {
		String fsId = "fs" + KString.capitalizeFirstLetter(id);
		if (fieldSetLegend == null || "".equals(fieldSetLegend))
			return this.fieldSet(this.ko.getClass().getSimpleName(), fsId, "");
		else
			return this.fieldSet(fieldSetLegend, fsId, "");
	}

	protected void initFieldControls() {
		setLoaded(true);
		KObjectDisplay display = getDisplayInstance();
		fieldSetLegend = display.getFormCaption(ko.getClass(), kobjectController);
		ko.refresh();
		for (Map.Entry<String, KObjectFieldController> e : kobjectController.getMembers().entrySet()) {
			String field = e.getKey();
			KFieldControl bestCtrl = display.getControl(ko, field, kobjectController, request);
			((KHtmlFieldControl) bestCtrl).setOwner(this);
			fieldControls.put(field, bestCtrl);
			KConstraint cMany = ko.getConstraints().getConstraint(field, KConstraintHasMany.class);
			if (bestCtrl.getFieldType().equals(HtmlControlType.khcListForm) || bestCtrl.getFieldType().equals(HtmlControlType.khcListFormMany) || cMany != null)
				fieldsToSubmit.add((KHtmlFieldControl) bestCtrl);
		}
		if (KString.isNotNull(kobjectController.getValidateOn()))
			this.controlOn = kobjectController.getValidateOn();
	}

	/**
	 * Ajoute l'élément HTML d'identifiant "_buttons" affichant le bouton submit
	 * du formulaire
	 * 
	 * @return l'élément ajouté
	 */
	public KFieldControl addDefaultButtons() {
		if (!_ajx && !_refresh)
			return fieldControls.put("_buttons", getDefaultButtons());
		return null;
	}

	/**
	 * @return le fieldset HTML ayant pour legend le nom de la classe de l'objet
	 */
	public KFieldControl addDefaultFieldSet() {
		KFieldControl result = fieldControls.put("_fieldset", getDefaultFieldSet());
		fieldSets++;
		return result;
	}

	/**
	 * Ajoute l'élément HTML d'identifiant "deleteCk" affichant la case à cocher
	 * permettant de supprimer l'objet KObject
	 * 
	 * @return l'élément ajouté
	 */
	public KFieldControl addDeleteCk() {
		if (this.ko.getRecordStatus().equals(KRecordStatus.rsLoaded))
			return fieldControls.put("_deleteCk", deleteCk());
		else
			return null;
	}

	/**
	 * Ajoute la liste des contrôles visuels dans le formulaire<br/>
	 * à partir de l'analyse des membres de l'objet par refexion
	 */
	public void addFieldControls() {
		initFieldControls();
		addHTML(fieldControlsToString());
	}

	public KFieldControl addFieldSet(String legend) {
		return addFieldSet(legend, "");
	}

	/**
	 * Ajoute un fieldset HTML
	 * 
	 * @param legend
	 *            legend HTML du fieldset
	 * @param options
	 *            options HTML supplémentaires
	 * @return le fieldset créé
	 */
	public KFieldControl addFieldSet(String legend, String options) {
		KFieldControl result = fieldSet(legend, options);
		fieldControls.put("_fieldset" + this.fieldSets, result);
		this.fieldSets++;
		return result;
	}

	public void addValidator(String field, String validatorString) {
		String strValid = "\"" + field + "\":\"" + validatorString + "\"";
		if ("".equals(addedValidatorString))
			addedValidatorString = strValid;
		else
			addedValidatorString += "," + strValid;
	}

	public void addValidator(String validatorString) {
		if ("".equals(addedValidatorString))
			addedValidatorString = validatorString;
		else
			addedValidatorString += "," + validatorString;
	}

	public String checkBox(String member, String id, String caption, String options) {
		return this.lists(HtmlControlType.khcCheckBox, member, id, null, caption, options);
	}

	public String checkBoxGroup(String member, String id, Object listObject, String caption, String options) {
		return this.lists(HtmlControlType.khcCheckBox, member, id, listObject, caption, options);
	}

	/**
	 * @return la fermeture du formulaire HTML et insère éventuellement le
	 *         script de validation côté client
	 */
	public String close() {
		String ret = "";
		for (int i = 0; i < this.fieldSets; i++)
			ret += "</fieldset>\n";
		String js = "";
		if (this.clientControl) {
			js = KJavaScript.getValidator(this.id, getControlOn(), this.getValidatorString());
		}

		ret += "</form>\n" + "<input type='hidden' id='koKeyValue' name='koKeyValue' value='" + this.ko.getFirstKeyValue() + "'>" + js;
		return ret;
	}

	public KFieldControl closeDefaultForm() {
		KHtmlFieldControl cfs = new KHtmlFieldControl();
		cfs.setFieldType(HtmlControlType.khcCustom);
		cfs.setValue(close());
		KFieldControl result = fieldControls.put("/_form", cfs);
		return result;
	}

	public KFieldControl closeDefaultFieldSet() {
		KHtmlFieldControl cfs = new KHtmlFieldControl();
		cfs.setFieldType(HtmlControlType.khcCustom);
		cfs.setValue("</fieldset>");
		KFieldControl result = fieldControls.put("/_fieldset", cfs);
		fieldSets--;
		return result;
	}

	public String comboBox(String member, String id, Object listObject, String caption, String options) {
		return this.lists(HtmlControlType.khcCmb, member, id, listObject, caption, options);
	}

	/**
	 * @return un contrôle HTML contenant la case à cocher permettant de
	 *         supprimer l'objet KObject
	 */
	public KFieldControl deleteCk() {
		KFieldControl ret = new KHtmlFieldControl("cochez pour supprimer puis validez", this.ko.getFirstKey(), "_delete", "_delete", HtmlControlType.khcCheckBox, null, "");
		return ret;
	}

	public void execBeforeSubmit() {

	}

	public String fieldControlsToString() {
		return fieldControls.getElementsAsString(KFieldControl.class);
	}

	/**
	 * @return
	 */
	public String getAction() {
		return action;
	}

	public String getControlOn() {
		String co = controlOn;
		co = co.replace("[", "").replace("]", "").replace("'", "");
		KStrings evts = new KStrings(co);
		return "[" + evts.implode(",", "'") + "]";
	}

	/**
	 * @return le bouton submit pour valider le formulaire
	 */
	public KFieldControl getDefaultButtons() {
		KFieldControl ret = new KHtmlFieldControl("", "Valider", id + "-button", id + "-button", HtmlControlType.khcSubmit, null, "class='btn'");
		return ret;
	}

	/**
	 * Charge de façon complète le formulaire, ou effectue sa soumission, en
	 * fonction de l'état de l'objet request<br/>
	 * Appelle dans l'ordre : loadAndSubmit, addDeleteCk, addDefaultButtons
	 * 
	 * @see net.ko.http.views.KHttpForm.#loadAndSubmit()
	 * @see net.ko.http.views.KHttpForm.#addDeleteCk()
	 * @see net.ko.http.views.KHttpForm.#addDefaultButtons()
	 * @param redirect
	 *            page de redirection facultative
	 * @param response
	 *            response Http utilisée pour l'éventuelle redirection
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public void getDefaultComplete(String redirect, HttpServletResponse response) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException, InstantiationException, ClassNotFoundException {
		this.loadAndSubmit(redirect, response);
		addDeleteCk();
		addDefaultButtons();
		openDefaultForm().setPos(-50);
		addDefaultFieldSet().setPos(-30);
		closeDefaultFieldSet();
		closeDefaultForm();
	}

	/**
	 * @return legend HTML du formulaire
	 */
	public String getFieldSetLegend() {
		return fieldSetLegend;
	}

	public String getFormContent() {
		return fieldControls.toString();
	}

	public String getId() {
		return id;
	}

	/**
	 * @return
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @return le message relatif à la mise à jour de l'objet dans la base de
	 *         données
	 */
	public String getUpdateMessage() {
		String ret = "";
		switch (this.updateAction) {
		case rsDelete:
			ret = "[supprimé]";
			break;
		case rsNew:
			ret = "[inséré]";
			break;
		case rsUpdate:
			ret = "[modifié]";
			break;
		case rsImpossible:
			ret = "[opération impossible ou annulée]";
			break;
		default:
			ret = "[aucune action]";
			break;
		}
		updateMessageMask = updateMessageMask.replace("{updateMessage}", ret);
		ret = this.ko._showWithMask(updateMessageMask);
		return ret;
	}

	public String getUpdateMessageMask() {
		return updateMessageMask;
	}

	/**
	 * Retourne la chaîne JSON de validation côté client du formulaire
	 * 
	 * @return chaîne JSON
	 */
	public String getValidatorString() {
		if (validatorString == null || "".equals(validatorString)) {
			validatorString = kobjectController.toJSON();
		}
		if (!"".equals(addedValidatorString)) {
			if ("".equals(validatorString))
				validatorString = "{" + addedValidatorString + "}";
			else
				validatorString = "{" + KString.cleanJSONString(validatorString) + "," + addedValidatorString + "}";
		}
		return validatorString;
	}

	public String input(HtmlControlType type, String member, String id, String caption, String options) {
		String ret = this.ko.getFcInput(member, id, caption, type, options).toString();
		return ret;
	}

	public String inputHidden(String member, String id) {
		return this.input(HtmlControlType.khcHidden, member, id, "", "");
	}

	public String inputPassword(String member, String id) {
		return this.input(HtmlControlType.khcPassWord, member, id, "", "");
	}

	public String inputText(String member, String id, String caption, String options) {
		return this.input(HtmlControlType.khcText, member, id, caption, options);
	}

	/**
	 * @return
	 */
	public boolean isAutoClose() {
		return autoClose;
	}

	public String label(String member, String caption, String options) {
		return this.ko.getFcLabel(member, caption, options).toString();
	}

	public String listBox(String member, String id, Object listObject, String caption, String options) {
		return this.lists(HtmlControlType.khcList, member, id, listObject, caption, options);
	}

	public String lists(HtmlControlType type, String member, String id, Object listObject, String caption, String options) {
		String ret = this.ko.getFcList(member, id, listObject, caption, type, options).toString();
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ko.http.views.KobjectHttpAbstractView#load()
	 */
	@Override
	public boolean load() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		return load(false, false);
	}

	@Override
	public boolean load(boolean noDb, boolean beforeSubmit) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		boolean result = super.load(noDb || insertMode, beforeSubmit);
		initFieldControls();
		if (ko != null)
			keyValue = ko.getFirstKeyValue();
		return result;
	}

	/**
	 * @return vrai si le formulaire a été soumis
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public boolean loadAndSubmit() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException, InstantiationException, ClassNotFoundException {
		return loadAndSubmit("", null);
	}

	/**
	 * Charge l'objet depuis la base de données (éventuellement), lui affecte
	 * les paramètres (POST) de la requête<br/>
	 * puis le met à jour dans la base de données (insertion, modification ou
	 * suppression)
	 * 
	 * @param redirect
	 *            url de redirection facultative
	 * @param response
	 *            response Http pour effectuer l'éventuelle redirection
	 * @return vrai si le formulaire a été soumis
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public boolean loadAndSubmit(String redirect, HttpServletResponse response) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException, InstantiationException, ClassNotFoundException {
		this.load(false, true);
		return this.submit(redirect, response);
	}

	/**
	 * Crée la balise form du formulaire
	 * 
	 * @param action
	 *            action du formulaire HTML de type URL
	 * @param method
	 *            methode d'envoi du formulaire : POST ou GET
	 * @return chaîne HTML contenant la définition du formulaire
	 */
	public String open(String action, String method) {
		String js = "";
		String queryString = "";
		if (request.getQueryString() != null)
			queryString = "?" + KRequest.getQueryString(request);
		if ("".equals(action)) {
			action = KRequest.getRequestURI(request) + queryString;
		}
		if ("".equals(method))
			method = "POST";
		this.action = action;
		this.method = method;
		KDebugConsole.print("Ouverture du formulaire : " + id + ", action : " + action + "->" + method, "PAGE", "KHttpForm.open");
		if (this.clientControl)
			js = "onsubmit='javascript:return v" + this.id + ".validate();'";
		return "<form id='" + this.id + "' name='" + this.id + "' method='" + method + "' " + js + " action='" + action + "'>";
	}

	public KFieldControl openDefaultForm() {
		KHtmlFieldControl cfs = new KHtmlFieldControl();
		cfs.setFieldType(HtmlControlType.khcCustom);
		cfs.setValue(open(this.action, this.method));
		KFieldControl result = fieldControls.put("_form", cfs);
		return result;
	}

	public String radio(String member, String id, String caption, String options) {
		return this.lists(HtmlControlType.khcRadio, member, id, null, caption, options);
	}

	public String radioGroup(String member, String id, Object listObject, String caption, String options) {
		return this.lists(HtmlControlType.khcRadio, member, id, listObject, caption, options);
	}

	/**
	 * @param action
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @param autoClose
	 */
	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	public void setControlOn(String controlOn) {
		this.controlOn = controlOn;
	}

	/**
	 * Définit la légende du fieldset du formulaire
	 * 
	 * @param fieldSetLegend
	 *            legend HTML du formulaire
	 */
	public void setFieldSetLegend(String fieldSetLegend) {
		this.fieldSetLegend = fieldSetLegend;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param method
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Affecte les paramètres postés dans la requête aux membres de l'objet
	 */
	@SuppressWarnings("rawtypes")
	public void setRequestParameters() {
		KObjectController koc = ko.getController();
		for (Map.Entry<String, KFieldControl> field : fieldControls.entrySet()) {
			String key = field.getKey();
			if ("_keyValues".equals(key)) {
				String strKeys = KRequest.GETPOST("_keyValues", request, "");
				if (KString.isNotNull(strKeys)) {
					List<Object> keyValues = new ArrayList<Object>(Arrays.asList(strKeys.split(",")));
					try {
						ko.setKeyValues(keyValues);
					} catch (SecurityException | NoSuchFieldException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (!key.startsWith("_")) {
				KFieldControl fc = field.getValue();
				if (fc.isSerializable()) {
					if (fc instanceof KHtmlFieldControl) {
						KObjectFieldController kofc = null;
						if (koc != null)
							kofc = koc.getFieldController(fc.getId());
						String value;
						if (kofc != null)
							value = kofc.getValue(request);
						else
							value = ((KHtmlFieldControl) fc).getValue(request);
						if (KRequest.keyExists(key, request) || ((!fc.isRequired() || fc.isAllowNull()) && !partialRequest))
							try {
								if (!fc.isMultiple())
									ko.setAttribute(key, value, false);
								else {
									KConstraint kc = ko.getConstraints().getConstraint(key, KConstraintHasAndBelongsToMany.class);
									if (kc != null) {
										((KListObject) ko.getAttribute(kc.getMember())).markForHasAndBelongsToMany(value, ";");
									} else {
										ko.setAttribute(key, value, false);
									}
								}
							} catch (Exception e) {
								Ko.klogger().log(Level.WARNING, "Impossible d'affecter " + value + " au membre " + key + " pour l'objet " + ko, e);
							}
					}
				}
			}
		}
	}

	public void setUpdateMessageMask(String updateMessageMask) {
		this.updateMessageMask = updateMessageMask;
	}

	/**
	 * @param validatorString
	 *            chaîne de validation JSON du formulaire utilisée côté client
	 */
	public void setValidatorString(String validatorString) {
		this.validatorString = validatorString;
	}

	/**
	 * Effectue la soumission du formulaire<br/>
	 * affecte aux membres de l'objet les paramètres de la requête<br/>
	 * effectue la mise à jour dans la base de données
	 * 
	 * @param redirect
	 *            page de redirection, si vide, aucune redirection n'est
	 *            effectuée
	 * @param response
	 *            response Http, paramètre utilisé si redirect n'est pas une
	 *            chaîne vide
	 * @return vrai si le formulaire est effectivement soumis
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public boolean submit(String redirect, HttpServletResponse response) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (request.getMethod().equalsIgnoreCase(method) && request.getParameterMap().size() > 0) {
			execBeforeSubmit();
			request.setAttribute("ko", ko);
			if (KString.isBooleanTrue(KRequest.GETPOST("_delete", request)))
				this.ko.toDelete();
			else {
				setRequestParameters();
				if (ko.getRecordStatus() != KRecordStatus.rsLoaded)
					ko.toAdd();
				else
					ko.toUpdate();
			}
			this.updateAction = this.ko.getRecordStatus();

			DaoList daoList = Ko.getDaoList(ko.getClass());
			daoList.updateToSupport(this.ko);
			daoList.close();

			if (request.getParameter("_delete") == null || !KString.isBooleanTrue(request.getParameter("_delete"))) {
				for (KHtmlFieldControl fc : fieldsToSubmit) {
					fc.submit();
				}
			}

			if (redirect != null && !"".equals(redirect) && response != null) {
				try {
					KRequest.forwardWithQueryString(redirect, request, response);
				} catch (ServletException e) {
					Ko.klogger().log(Level.WARNING, "Impossible de rediriger vers " + redirect, e);
				}
			}
			return true;
		} else
			return false;
	}

	public String textArea(String member, String id, String caption, String options) {
		return this.input(HtmlControlType.khcTextarea, member, id, caption, options);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * Retourne la chaîne HTML contenant le formulaire complet
	 */
	public String toString() {
		String formContent = getFormContent();
		String ret = "";
		// if (autoOpen)
		// ret = this.open(this.action, this.method);
		ret += formContent;
		// if (autoClose)
		// ret += this.close();
		if (ajaxIncludes)
			ret += KoHttp.kajaxIncludes(request);
		return getPageHeader() + ret + getPageFooter();
	}

	public void updateFieldControl(String field) {
		int pos = 0;
		if (kobjectController != null) {
			ko.refresh();
			String caption = field + " :";
			String options = "";
			boolean multiple = false;
			KFieldControl bestCtrl = Display.getDefault().getBestControl(ko, field);
			if (bestCtrl == null) {
				HtmlControlType htmlCT = HtmlControlType.khcText;
				if (kobjectController != null) {
					KObjectFieldController kofc = kobjectController.getMembers().get(field);
					htmlCT = kofc.getHtmlControlType();
					options = " " + kofc.getOptions() + " ";
					if (!"".equals(kofc.getCaption()))
						caption = kofc.getCaption();
					pos = kofc.getPos();
					if (kofc.isMultiple())
						multiple = true;
				}
				bestCtrl = Display.getDefault().getFc(ko, field, field, caption, htmlCT, options, null);
				bestCtrl.setMultiple(multiple);
				if (pos != 0)
					bestCtrl.setPos(pos * 10);
			}
			((KHtmlFieldControl) bestCtrl).setOwner(this);
			fieldControls.put(field, bestCtrl);
		}
	}

	public void setKoFieldValue(String fieldName, Object value) {
		try {
			if (ko != null)
				KReflectObject.__setAttribute(ko, fieldName, value);
		} catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
			Ko.klogger().log(Level.WARNING, "Impossible d'affecter la valeur " + value + " au membre " + fieldName, e);
		}
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public boolean isAutoOpen() {
		return autoOpen;
	}

	public void setAutoOpen(boolean autoOpen) {
		this.autoOpen = autoOpen;
	}

	public boolean isPartialRequest() {
		return partialRequest;
	}

	public void setPartialRequest(boolean partialRequest) {
		this.partialRequest = partialRequest;
	}
}
