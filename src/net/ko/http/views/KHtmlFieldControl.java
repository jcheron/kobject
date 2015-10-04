package net.ko.http.views;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import net.ko.dao.DaoList;
import net.ko.framework.Ko;
import net.ko.kobject.KConstraint;
import net.ko.kobject.KConstraintHasMany;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.types.HtmlControlType;
import net.ko.types.KStringsType;
import net.ko.utils.KString;
import net.ko.utils.KStrings;

/**
 * Contrôle visuel HTML permettant d'afficher ou de modifier la valeur d'un
 * champ/membre
 * 
 * @author jcheron
 * 
 */
public class KHtmlFieldControl extends KFieldControl {
	private Object listObject;
	private String options;
	private String before = "";
	private String after = "";
	private String listMask = "";
	private String listKeyField = "";
	private String valuesSep = ";";
	private String className = "field";
	private String labelClassName = "label";
	private List<String> cssClasses;
	private String _cssClass = "";
	private String template;
	private String placeHolder;

	private KobjectHttpAbstractForm owner = null;
	private boolean onlyField;

	public KHtmlFieldControl() {
		fieldType = HtmlControlType.khcNone;
		caption = "";
		name = "";
		id = "";
		listObject = null;
		options = "";
		onlyField = false;
		cssClasses = new ArrayList<>();
	}

	/**
	 * Constructeur d'un contrôle visuel HTML
	 * 
	 * @param caption
	 *            texte du label associé au contrôle
	 * @param value
	 *            valeur à afficher
	 * @param name
	 *            nom du contrôle name HTML
	 * @param id
	 *            identifiant HTML
	 * @param fieldType
	 *            type de contrôle visuel
	 * @param listObject
	 *            liste associée (KListObject ou Array)
	 * @param options
	 *            chaîne libre permettant d'insérer des éléments HTML non pris
	 *            en charge
	 * @param required
	 *            détermine si la saisie est obligatoire
	 */
	public KHtmlFieldControl(String caption, String value, String name, String id, HtmlControlType fieldType, Object listObject, String options, boolean required) {
		this.caption = caption;
		this.value = value;
		this.name = name;
		if (id == null || id.equals(""))
			this.id = name;
		else
			this.id = id;
		this.fieldType = fieldType;
		this.listObject = listObject;
		this.options = options;
		this._required = required;
		cssClasses = new ArrayList<>();
	}

	/**
	 * Constructeur d'un contrôle visuel HTML
	 * 
	 * @param caption
	 *            texte du label associé au contrôle
	 * @param value
	 *            valeur à afficher
	 * @param name
	 *            nom du contrôle name HTML
	 * @param id
	 *            identifiant HTML
	 * @param fieldType
	 *            type de contrôle visuel
	 * @param listObject
	 *            liste associée (KListObject ou Array)
	 * @param options
	 *            chaîne libre permettant d'insérer des éléments HTML non pris
	 *            en charge
	 */
	public KHtmlFieldControl(String caption, String value, String name, String id, HtmlControlType fieldType, Object listObject, String options) {
		this(caption, value, name, id, fieldType, listObject, options, false);
	}

	public KHtmlFieldControl(String caption, String value, String name, String id, HtmlControlType fieldType) {
		this(caption, value, name, id, fieldType, null, "", false);
	}

	private String getDefaultHtmlCaption() {
		String result = "";
		if (caption != null && !"".equals(caption))
			if (!onlyField || "*".equals(fieldType.getCardinality()))
				result = "<div class='" + labelClassName + "'><label for='" + this.id + "'>" + this.caption + "</label></div>\n";
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * analyse le contenu du contrôle et retourne la chaîne associée
	 */
	@SuppressWarnings("unchecked")
	public String toString() {
		String cssClass = "";
		if (KString.isNotNull(_cssClass))
			cssClass = " class='" + _cssClass + "' ";
		else
			cssClass = stylesToString();
		String beforeAll = "";
		String afterAll = "";
		String beforeField = "";
		String beforeFieldWithOptions = "";
		String afterField = "";
		if (!onlyField) {
			beforeAll = "<div class='form-row'>";
			afterAll = "</div>\n";
			beforeFieldWithOptions = "<div id='field-" + this.name + "' class='" + className + "' " + this.options + cssClass + " >";
			beforeField = "<div id='field-" + this.name + "' class='" + className + "'>";
			afterField = "</div>\n";
		}

		String ret = beforeAll;
		String type = this.fieldType.toString();
		String innerHtml = mapListToString();
		String ajaxDiv = "";
		String ajaxLoader = "";
		if (fieldType.isAjax()) {
			if (listObject instanceof KListObject) {
				ajaxDiv = "<input id='ckList-ajax-clsList-" + this.name + "' type='hidden' value='" + ((KListObject<KObject>) listObject).getClassName() + "'>";
				ajaxDiv += "<input id='ckList-ajax-listMask-" + this.name + "' type='hidden' value='" + this.listMask + "'>";

			}
			if (owner != null)
				ajaxDiv += "<input id='ckList-ajax-cls-" + this.name + "' type='hidden' value='" + owner.getKobject().getClass().getName() + "'>";
			ajaxDiv += "<div id='ckList-ajax-" + this.name + "' class='ckListAjax'></div>\n";
			ajaxLoader = "<div id='ckList-ajax-loader-" + this.name + "' class='ckListAjaxLoader' style='display:none'></div>\n";
		}
		if (this._required)
			this.options += " required ";
		String strMin = "";
		String strMax = "";
		if (this.readonly)
			this.options += " readonly ";

		switch (fieldType) {
		case khcRange:
		case khcNumber:
		case khcMonth:
		case khcWeek:
		case khcDateTimeLocal:
			if (min != -1)
				strMin = " min='" + min + "' ";
			if (max != -1)
				strMax = " max='" + max + "' ";
		case khcText:
		case khcPassWord:
		case khcEmail:
		case khcUrl:
		case khcTel:
		case khcColor:
		case khcSearch:
			String strPlaceHolder = "";
			if (KString.isNotNull(placeHolder)) {
				strPlaceHolder = "placeholder='" + placeHolder + "'";
			}
			ret += getDefaultHtmlCaption();
			ret += beforeField + "<input type='" + type + "' id='" + this.id + "' name='" + this.name + "' value='" + this.value + "' " + strPlaceHolder + " class='form-control' " + strMax + strMin + this.options + "/>" + afterField;
			ret += afterAll;
			break;
		case khcFile:
			ret += "<div id='btn_" + id + "' " + options + "><span class='btUpload'>" + caption + "</span> [<span id='fileName_" + id + "'>...</span>]</div><input style='display:none' type='file' id='file_" + id + "' name='file_" + id + "'><div class='resUpload' id='res_" + id + "'></div>";
			ret += "<input type='hidden' name='" + id + "' id='" + id + "' value='" + value + "'>";
			ret += "<script>Forms.Utils.addEventToElement($('btn_" + id + "'),'click',function(){Forms.Utils.click($('file_" + id + "'));});" +
					"Forms.Utils.addEventToElement($('file_" + id + "'),'change',function(){if($('file_" + id + "').files[0])$set($('fileName_" + id + "'),$('file_" + id + "').files[0].name+'...',true);});</script>";
			break;
		case khcReadOnlyText:
			ret += getDefaultHtmlCaption();
			className = getClassName("readonlytext");
			ret += "<div id='field-" + this.name + "' class='" + className + "' " + this.options + cssClass + " >" + "<div id='value-" + this.id + "' title='" + KString.htmlSpecialChars(this.value) + "'>" + this.value + "</div><input type='hidden' id='" + this.id + "' name='" + this.name + "' value='" + this.value + "'/>" + afterField;
			ret += afterAll;
			break;
		case khcReadOnlyList:
			className = getClassName("readonlylist");
			ret += getDefaultHtmlCaption();
			if (!onlyField)
				ret += "<div id='field-" + this.name + "' class='" + className + "' " + this.options + cssClass + ">" + innerHtml + "" + afterField;
			else
				ret += innerHtml;
			ret += afterAll;
			break;
		case khcDateTime:
		case khcDate:
		case khcTime:
		case khcDateCmb:
		case khcDateTimeCmb:
		case khcTimeCmb:
			boolean isCmb = fieldType.getLabel().contains("cmb");
			ret += getDefaultHtmlCaption();
			String inputType = "hidden";
			if (isCmb)
				inputType = "text";
			ret += beforeField + "<input type='" + inputType + "' id='" + this.id + "' name='" + this.name + "' value='" + this.value + "' class='form-control' " + this.options + "/>" + afterField;
			ret += "<script type='text/javascript'>new Forms.DatePicker('" + this.id + "','dtp" + this.id + "','" + type + "'," + (!isCmb) + ");</script>\n";
			ret += afterAll;
			break;
		case khcCheckBox:
			if (!innerHtml.equals("")) {
				ret += beforeField + "<fieldset class='checkboxList'><legend class='" + labelClassName + "'>" + caption + "</legend>" + innerHtml + "</fieldset>" + afterField;
				ret += "<input type='hidden' id='" + this.id + "Value' name='" + this.name + "Value' value='" + this.value + "'/>";
			} else {
				String checked = "";
				if (KString.isBooleanTrue(this.value))
					checked = "checked";
				ret += beforeField + "<input type='hidden' id='" + this.id + "' name='" + this.name + "' value='" + KString.getBooleanIntValue(this.value) + "'><input onclick='javascript:document.getElementById(\"" + this.id + "\").value=(this.checked?\"true\":\"false\");' type='" + type + "' " + checked + " id='_" + this.id + "' name='_" + this.name + "' " + this.options + "/><label for='_" + this.id + "' class='" + labelClassName + "'>" + this.caption + "</label>" + afterField;
			}
			ret += afterAll;

			break;
		case khcRadio:
			ret += "<div class='" + labelClassName + "'></div>";
			if (!innerHtml.equals("")) {
				ret += beforeField + "<fieldset><legend class='" + labelClassName + "'>" + caption + "</legend>" + innerHtml + "</fieldset>" + afterField;
				ret += "<input type='hidden' id='" + this.id + "Value' name='" + this.name + "Value' value='" + this.value + "'/>";
			} else
				ret += beforeField + "<input type='" + type + "' id='" + this.id + "' name='" + this.name + "' value='" + this.value + "' " + this.options + "/><label for='" + this.name + "'>" + this.caption + "</label>" + afterField;
			ret += afterAll;
			break;
		case khcHidden:
			ret = "<input type='" + type + "' id='" + this.id + "' name='" + this.name + "' value='" + this.value + "'/>";
			break;
		case khcCmb:
			String defaultOption = "";
			if (KString.isNotNull(placeHolder)) {
				defaultOption = "<option value=''>" + placeHolder + "</option>";
			}
			ret += getDefaultHtmlCaption() + beforeField + "<" + type + " id='" + this.id + "' name='" + this.name + "' " + this.options + " class='form-control' " + ">" + defaultOption + innerHtml + "</select>" + afterField;
			ret += afterAll;
			break;
		case khcList:
			String strMultiple = "";
			if (_multiple)
				strMultiple = " multiple ";
			ret += getDefaultHtmlCaption() + beforeField + "<" + type + " id='" + this.id + "' name='" + this.name + "' " + this.options + cssClass + " " + strMultiple + ">" + innerHtml + "</select>" + afterField;
			ret += afterAll;
			break;
		case khcPageList:
		case khcListForm:
		case khcListFormMany:
			ret += beforeField + "<fieldset><legend>" + caption + "</legend>" + innerHtml + "</fieldset>" + afterField;
			ret += afterAll;
			break;
		case khcCheckedList:
		case khcRadioList:
		case khcCheckedDataList:
		case khcRadioDataList:
		case khcCheckedAjaxList:
		case khcRadioAjaxList:
		case khcAjaxList:
			String searchText = "&nbsp;<span id='ckListInfo-" + this.name + "' class='ckListInfo'></span>";
			if (HtmlControlType.khcAjaxList.equals(fieldType))
				searchText = "&nbsp;<input id='" + this.id + "' name='" + this.name + "' class='ckListSearchText' value='" + this.value + "' type='text'>";
			else if (HtmlControlType.khcRadioDataList.equals(fieldType) || HtmlControlType.khcCheckedDataList.equals(fieldType) || fieldType.isAjax())
				searchText = "&nbsp;<input id='ckListSearchText-" + this.name + "' class='ckListSearchText' placeholder='Rechercher...' value='' type='text'>";

			String listCaption = "<div id='ckListCaption-" + this.name + "' onselectstart='javascript:return false;' class='ckListCaption'><span>" + this.caption + "</span>" + searchText + "</div>";
			String jsClass = "Radio";
			if (HtmlControlType.khcCheckedList.equals(fieldType) || HtmlControlType.khcCheckedDataList.equals(fieldType) || HtmlControlType.khcCheckedAjaxList.equals(fieldType)) {
				jsClass = "Checked";
				listCaption = "<div onselectstart='javascript:return false;' class='ckListCaption'><span class='ckListCaption-Checkall' id='ckListCaption-checkall-" + this.name + "'>&nbsp;</span>" + "<span id='ckListCaption-" + this.name + "'>" + this.caption + "</span>" + searchText + "</div>";
			}
			if (HtmlControlType.khcAjaxList.equals(fieldType))
				jsClass = "Ajax";

			String jsRequired = "";
			if ("".equals(caption))
				listCaption = "";
			if (_required)
				jsRequired = ",false";
			ret += beforeField + "<div class='ckListOuter'>" + listCaption + "\n" + ajaxLoader + "<div id='ckList-inner-" + this.name + "' class='ckListInner'><div id='ckList-" + this.name + "'>" + innerHtml + "</div>" + ajaxDiv + "</div>" + afterField;

			if (!HtmlControlType.khcAjaxList.equals(fieldType))
				ret += "<input type='text' style='display:none;' id='" + this.name + "' name='" + this.name + "' value='" + this.value + "'/>";
			ret += "<script type='text/javascript'>var ckList" + this.name + "=new Forms." + jsClass + "List('" + this.name + "'" + jsRequired + ");</script>\n";
			ret += afterField + "</div>";
			break;

		case khcDataList:
			String idDataList = "dataList-" + this.id;
			String dataList = "<datalist id='" + idDataList + "'>" + innerHtml + "</datalist>\n";
			ret += getDefaultHtmlCaption() + beforeField + "<input type='text' id='" + this.id + "' name='" + this.name + "' value='" + this.value + "' " + this.options + "list='" + idDataList + "'/>" + dataList + "" + afterField;
			ret += afterAll;
			break;
		case khcTextarea:
			ret += getDefaultHtmlCaption() + beforeField + "<" + type + " id='" + this.id + "' name='" + this.name + "' " + this.options + " class='form-control'>" + this.value + "</" + type + ">" + afterField;
			ret += afterAll;
			break;
		case khcLabel:
			ret += getDefaultHtmlCaption() + "<label title='" + KString.htmlSpecialChars(this.value) + "' id='" + this.id + "' " + this.options + cssClass + ">" + this.value + "</label>" + afterField;
			ret += "<input type='hidden' id='" + this.id + "' name='" + this.name + "' value='" + this.value + "'/>";
			ret += afterAll;
			break;
		case khcButton:
			ret += beforeField + "<div class='btn' id='" + this.id + "' " + this.options + "><span " + cssClass + ">" + this.value + "</span></div>" + afterField;
			ret += afterAll;
			break;
		case khcSubmit:
			ret += beforeField + "<input type='" + type + "' id='" + this.id + "' name='" + this.name + "' value='" + this.value + "' " + this.options + cssClass + "/>" + afterField;
			ret += afterAll;
			break;
		case khcFieldSet:
			ret = "<fieldset id='" + this.id + "' " + this.options + cssClass + "><legend>" + this.caption + "</legend>";
			break;
		case khcCustom:
			ret = this.value;
			if (KString.isNotNull(template)) {
				ret = template.replace("{value}", value);
				ret = ret.replace("{id}", id);
			}
			break;
		default:
			break;
		}

		return before + ret + after;
	}

	@SuppressWarnings("rawtypes")
	private Map<String, String> listToMap() {
		Map<String, String> result = new LinkedHashMap<String, String>();
		if (this.listObject instanceof KListObject) {
			KListObject kl = ((KListObject) this.listObject);
			String lListMask = getListMask();
			String lListKeyField = getListKeyField();
			for (Object o : kl) {
				try {
					KObject ko = (KObject) o;
					String idItem = "";
					if (lListKeyField != null && !"".equals(lListKeyField)) {
						try {
							idItem = ko._showWithMask(lListKeyField);
						} catch (Exception e) {
							idItem = ko.getFirstKeyValue() + "";
						}
					} else
						idItem = ko.getFirstKeyValue() + "";
					String valueItem = "";
					if (lListMask == null || "".equals(lListMask))
						valueItem = ko.toString();
					else
						valueItem = ko._showWithMask(lListMask);
					result.put(idItem, valueItem);
				} catch (Exception e) {
				}
			}
		} else if (this.listObject instanceof KStrings) {
			Map<String, Object> ks = ((KStrings) this.listObject).getStrings();
			for (Map.Entry<String, Object> e : ks.entrySet()) {
				if (HtmlControlType.khcAjaxList.equals(this.fieldType))
					result.put(e.getValue() + "", e.getValue() + "");
				else
					result.put(e.getKey(), e.getValue() + "");
			}
		} else {
			if (this.listObject != null) {
				String sl = this.listObject.toString();
				sl = sl.replace("{", "").replace("}", "");
				this.listObject = new KStrings(sl, KStringsType.kstJSON);
				result = listToMap();
			}
		}
		return result;
	}

	public void submit() {
		if (HtmlControlType.khcPageList.equals(this.fieldType)) {
			if (owner != null) {
				KPageList kpage = owner.hasAndBelongsToManyList(name, 1);
				if (kpage != null) {
					kpage.setEditable(false);
					kpage.setChecked(!readonly);
					kpage.getDefaultComplete();
				}
			}
		} else if (HtmlControlType.khcListForm.equals(this.fieldType) || HtmlControlType.khcListFormMany.equals(this.fieldType)) {
			if (owner != null) {
				KHttpListForm listForm = null;
				listForm = owner.addHttpListForm(name, HtmlControlType.khcListFormMany.equals(this.fieldType), readonly);
				if (listForm != null) {
					try {
						listForm.getDefaultComplete("", null);
					} catch (Exception e) {
						Ko.klogger().log(Level.WARNING, "Impossible de charger ListFormMany pour le champ : " + name, e);
					}
				}
			}
		} else {
			if (this.listObject instanceof KListObject) {
				if (owner != null) {
					KObject koUpdated = owner.getKobject();
					Object koUpdatedKey = koUpdated.getFirstKeyValue();
					KConstraint c = koUpdated.getConstraints().getConstraint(name, KConstraintHasMany.class);
					if (c != null) {
						String fkField = c.getDestFieldKey();
						String values = getValue(owner.getRequest());
						KListObject<? extends KObject> kl = (KListObject<? extends KObject>) this.listObject;
						String valueSep = Ko.krequestValueSep();
						for (KObject ko : kl) {
							try {
								Object oldValue = ko.getAttribute(fkField);
								Object newValue = null;
								if ((valueSep + values + valueSep).contains(valueSep + ko.getFirstKeyValue() + valueSep)) {
									newValue = koUpdatedKey;
								}
								if (newValue == null) {
									if ((koUpdatedKey + "").equals(oldValue + "")) {
										ko.setAttribute(fkField, newValue, false);
										ko.toUpdate();
									}
								} else if (!(newValue + "").equals(oldValue + "")) {
									ko.setAttribute(fkField, newValue, false);
									ko.toUpdate();
								}
							} catch (Exception e) {
								Ko.klogger().log(Level.WARNING, "Impossible de soumettre la requête pour le contrôle " + this.name, e);
							}
						}
						DaoList daoList = Ko.getDaoList(kl.getClazz());
						daoList.update(kl);
						daoList.close();
					}
				}
			}
		}
	}

	public String mapListToString() {
		String result = "";
		if (HtmlControlType.khcPageList.equals(this.fieldType)) {
			if (owner != null) {
				KPageList kpage = owner.hasAndBelongsToManyList(name, 1);
				if (kpage != null) {
					kpage.setEditable(false);
					kpage.getDefaultComplete();
					result += kpage.toString();
				}
			}
		} else if (HtmlControlType.khcListForm.equals(this.fieldType)) {
			if (owner != null) {
				KHttpListForm listForm = owner.addHttpListForm(name, readonly);
				if (listForm != null) {
					try {
						result += listForm.toString();
					} catch (Exception e) {
						Ko.klogger().log(Level.WARNING, "Impossible de charger ListForm pour le champ : " + name, e);
					}
				}
			}
		} else if (HtmlControlType.khcListFormMany.equals(this.fieldType)) {
			if (owner != null) {
				KHttpListForm listForm = owner.addHttpListForm(name, true, readonly);
				if (listForm != null) {
					try {
						result += listForm.toString();
					} catch (Exception e) {
						Ko.klogger().log(Level.WARNING, "Impossible de charger ListFormMany pour le champ : " + name, e);
					}
				}
			}
		} else {
			Map<String, String> list = listToMap();
			String type = this.fieldType.toString();
			int i = 0;
			for (Map.Entry<String, String> item : list.entrySet()) {
				String idItem = item.getKey();
				String valueItem = item.getValue();
				String selected = "";
				String style = "";
				switch (this.fieldType) {
				case khcDataList:
					result += "<option>" + valueItem + "</option>\n";
					break;
				case khcReadOnlyList:
					if ((valuesSep + this.value + valuesSep).contains(valuesSep + idItem + valuesSep)) {
						result += "<div title='" + KString.htmlSpecialChars(valueItem) + "'>" + valueItem + "<input id='" + this.id + "' name='" + this.name + "' type='hidden' value='" + idItem + "'></div>\n";
					}
					break;
				case khcCmb:
				case khcList:
					if ((valuesSep + this.value + valuesSep).contains(valuesSep + idItem + valuesSep))
						selected = " selected ";
					result += "<option" + selected + " value='" + idItem + "' ondblclick='if(this.selected)this.parentNode.selectedIndex=-1;'>" + valueItem + "</option>\n";
					break;
				case khcCheckedList:
					selected = "ckItem";
					if ((valuesSep + this.value + valuesSep).contains(valuesSep + idItem + valuesSep))
						selected = "ckItem-selected";
					result += "<div class='item " + selected + "' id='ckItem-" + this.name + "-" + idItem + "' " + this.options + ">" + valueItem + "</div>\n";
					break;
				case khcRadioList:
					selected = "rItem";
					if ((this.value).equals(idItem))
						selected = "rItem-selected";
					result += "<div class='item " + selected + "' id='rItem-" + this.name + "-" + idItem + "' " + this.options + ">" + valueItem + "</div>\n";
					break;
				case khcCheckedDataList:
				case khcCheckedAjaxList:
					selected = "ckItem";
					if ((valuesSep + this.value + valuesSep).contains(valuesSep + idItem + valuesSep))
						selected = "ckItem-selected";
					else
						style = "style='display:none'";
					result += "<div class='item " + selected + "' id='ckItem-" + this.name + "-" + idItem + "' " + this.options + " " + style + ">" + valueItem + "</div>\n";
					break;
				case khcRadioDataList:
				case khcRadioAjaxList:
					selected = "rItem";
					if ((this.value).equals(idItem))
						selected = "rItem-selected";
					else
						style = "style='display:none'";
					result += "<div class='item " + selected + "' id='rItem-" + this.name + "-" + idItem + "' " + this.options + " " + style + ">" + valueItem + "</div>\n";
					break;
				case khcAjaxList:
					selected = "ajaxItem";
					if ((this.value).equals(valueItem))
						selected = "ajaxItem-selected";
					else
						style = "style='display:none'";
					result += "<div class='item " + selected + "' id='ajaxItem-" + this.name + "-" + idItem + "' " + this.options + " " + style + ">" + valueItem + "</div>\n";
					break;
				case khcCheckBox:
				case khcRadio:
					_multiple = true;
					if ((valuesSep + this.value + valuesSep).contains(valuesSep + idItem + valuesSep))
						selected = " checked ";
					result += "<div class='" + className + "'><input " + selected + " type='" + type
							+ "' onclick=\"$('" + this.name + "Value').value=$getValues('" + this.name + "',true);\" id='"
							+ this.id + i + "' name='" + this.name + "' value='" + idItem + "' " + this.options
							+ "><label for='" + this.name + i + "'>" + valueItem + "</label></div>\n";
					break;
				case khcCustom:
					result = value;
					if (KString.isNotNull(template)) {
						result = template.replace("{value}", value);
						result = template.replace("{id}", value);
					}
					break;
				default:
					break;
				}

				i++;
			}
		}
		return result;
	}

	/**
	 * Retourne la valeur du contrôle contenue dans les paramètres de la requête<br/>
	 * traitement spécial des checkboxes qui retournent null si elle ne sont pas
	 * cochées en java
	 * 
	 * @param request
	 *            requête Http
	 * @return valeur du contrôle
	 */
	public String getValue(HttpServletRequest request) {
		String result = null;
		if (!_multiple && HtmlControlType.khcCheckBox.equals(fieldType)) {
			result = "false";
			if (KString.isBooleanTrue(request.getParameter(name)))
				result = "true";
		} else if (_multiple)
			return KStrings.implode(valuesSep, request.getParameterValues(name));
		else
			result = request.getParameter(name);
		return result;
	}

	public String getValue(HttpServletRequest request, int position) {
		String[] values = request.getParameterValues(name);
		String result = null;
		if (values == null) {
			if (!_multiple && HtmlControlType.khcCheckBox.equals(fieldType))
				result = "false";
		} else {
			if (values.length > position) {
				if (!_multiple && HtmlControlType.khcCheckBox.equals(fieldType)) {
					if (KString.isBooleanTrue(values[position]))
						result = "true";
				} else
					result = values[position];
			}
		}
		return result;
	}

	public Object getListObject() {
		return listObject;
	}

	public void setListObject(Object listObject) {
		this.listObject = listObject;
	}

	public String getOptions() {
		return options;
	}

	/**
	 * @param options
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	public String getBefore() {
		return before;
	}

	/**
	 * Chaîne à insérer avant le contrôle visuel
	 * 
	 * @param before
	 *            chaîne HTML
	 */
	public void setBefore(String before) {
		this.before = before;
	}

	public String getAfter() {
		return after;
	}

	/**
	 * Chaîne à insérer arès le contrôle visuel
	 * 
	 * @param after
	 *            chaîne HTML
	 */
	public void setAfter(String after) {
		this.after = after;
	}

	/**
	 * 
	 * @param controlType
	 */
	public void setControlType(String controlType) {
		HtmlControlType ct = HtmlControlType.getType(controlType);
		if (ct != null)
			fieldType = HtmlControlType.getType(controlType);
	}

	/**
	 * @return listMask
	 */
	public String getListMask() {
		return listMask;
	}

	/**
	 * Modifie le masque d'affichage de la valeur de la liste le masque est
	 * utilisé avec la méthode showWithMask
	 * 
	 * @param listMask
	 */
	public void setListMask(String listMask) {
		this.listMask = listMask;
	}

	public static KHtmlFieldControl createList(String idNameCaption, Object listObject) {
		return createList(idNameCaption, "", idNameCaption, idNameCaption, listObject, "");
	}

	public static KHtmlFieldControl createList(String idNameCaption, String value, Object listObject) {
		return createList(idNameCaption, value, idNameCaption, idNameCaption, listObject, "");
	}

	public static KHtmlFieldControl createList(String idName, String caption, String value, Object listObject) {
		return createList(caption, value, idName, idName, listObject, "");
	}

	/**
	 * Retourne une liste HTML alimentée par l'objet listObject passé en
	 * paramètre
	 * 
	 * @param caption
	 *            étiquette de la liste
	 * @param value
	 *            valeur par défaut
	 * @param name
	 *            nom HTML
	 * @param id
	 *            identifiant HTML
	 * @param listObject
	 *            liste de type KListObject ou KStrings
	 * @param options
	 *            options HTML supplémentaires
	 * @return
	 */
	public static KHtmlFieldControl createList(String caption, String value, String name, String id, Object listObject, String options) {
		return new KHtmlFieldControl(caption, value, name, id, HtmlControlType.khcList, listObject, options, true);
	}

	public KobjectHttpAbstractForm getOwner() {
		return owner;
	}

	public void setOwner(KobjectHttpAbstractForm owner) {
		this.owner = owner;
	}

	public String getListKeyField() {
		String result = listKeyField;
		if (listKeyField == null || "".equals(listKeyField)) {
			if (HtmlControlType.khcAjaxList.equals(fieldType))
				result = getListMask();
		}
		return result;
	}

	public void setListKeyField(String listKeyField) {
		this.listKeyField = listKeyField;
	}

	public String getClassName(String dClassName) {
		String result = className;
		if ("field".equals(className))
			result = dClassName;
		return result;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getLabelClassName() {
		return labelClassName;
	}

	public void setLabelClassName(String labelClassName) {
		this.labelClassName = labelClassName;
	}

	public boolean isOnlyField() {
		return onlyField;
	}

	public void setOnlyField(boolean onlyField) {
		this.onlyField = onlyField;
	}

	public String getValidatorString() {
		String result = "\"" + id + "\":";
		ArrayList<String> validator = new ArrayList<>();
		if (_required)
			validator.add("@");
		if (max != -1)
			validator.add(max + "");
		if (min != -1)
			validator.add("-" + min);
		validator.add(fieldType.getLabel());
		String strValidator = "\"" + KStrings.implode(".", validator, "") + "\"";
		return result + strValidator;
	}

	public String stylesToString() {
		String result = "";
		if (cssClasses.size() > 0) {
			String part = KString.getFirstPart("class='", "'", options);
			if (!part.equals(options)) {
				cssClasses.add(part);
				options = options.replaceFirst("class\\=\\'(.*?)\\'", "");
			}
			result = KStrings.implode(" ", cssClasses, "");
			result = " class='" + result + "' ";
		}
		return result;
	}

	public void addCssClass(String cssClass) {
		if (!cssClasses.contains(cssClass))
			cssClasses.add(cssClass);
	}

	public void removeCssClass(String cssClass) {
		cssClasses.remove(cssClass);
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
		if (KString.isNotNull(template)) {
			fieldType = HtmlControlType.khcCustom;
		}
	}

	public String getPlaceHolder() {
		return placeHolder;
	}

	public void setPlaceHolder(String placeHolder) {
		this.placeHolder = placeHolder;
	}
}
