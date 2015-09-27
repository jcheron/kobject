package net.ko.http.views;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.controller.KObjectController;
import net.ko.controller.KObjectFieldController;
import net.ko.dao.DaoList;
import net.ko.dao.DatabaseDAO;
import net.ko.dao.IGenericDao;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.http.objects.KRequest;
import net.ko.kobject.KConstraint;
import net.ko.kobject.KConstraintHasAndBelongsToMany;
import net.ko.kobject.KConstraintHasMany;
import net.ko.kobject.KConstraintHasManyBelongsTo;
import net.ko.kobject.KListObject;
import net.ko.kobject.KMask;
import net.ko.kobject.KObject;
import net.ko.kobject.KRecordStatus;
import net.ko.ksql.KSqlQuery;
import net.ko.types.HtmlControlType;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;

public class KHttpListForm extends KAbstractView {
	private Class<? extends KObject> clazz;
	private KMask innerMask = null;
	private KMask mask = null;
	private int countFields;
	private String beforeField;
	private String afterfield;
	private String header;
	private String footer;
	private String id = "id";
	private String name = "name";
	private boolean checked = true;
	private String fieldSetLegend;
	private String method = "POST";
	private String pageContent;
	private String checkedMask = "<td class='td1'><div class='field'><input onclick='Forms.Framework.checkRowToDelete(this);' type='checkbox' id='delete{id}-{num}' name='delete-o{id}' value='{getUniqueIdHash}'><label for='delete{id}-{num}'>&nbsp;</label></div></td>";
	private boolean allowAdd = true;
	private KListObject<? extends KObject> listObject;
	private KViewElementsMap koFieldsControl = new KViewElementsMap();
	private ArrayList<String> captions = new ArrayList<>();
	private KStrings where;
	private KObject ko;
	private boolean listLoaded;
	private boolean hasFieldSet;
	private boolean innerForm;
	private boolean submited = false;
	private boolean allValues = false;
	private boolean readonly = false;

	private KConstraintHasMany cHasMany;

	public KHttpListForm(KListObject<? extends KObject> listObject, HttpServletRequest request, String member) {
		this(listObject, request, member, null);
	}

	public KHttpListForm(Class<? extends KObject> clazz, HttpServletRequest request, String member) {
		this(new KListObject<>(clazz), request, member, null);
	}

	public KHttpListForm(KListObject<? extends KObject> kl, HttpServletRequest request, String member, KConstraintHasMany cHasMany) {
		super(request);
		this.id = member;
		this.name = member;
		this.listObject = kl;
		if (kl != null)
			this.clazz = kl.getClazz();
		this.cHasMany = cHasMany;
		allValues = (cHasMany != null && (cHasMany instanceof KConstraintHasManyBelongsTo));
		this.beforeField = "<td><div class='td'>";
		this.afterfield = "</div></td>";
		mask = null;
		innerMask = null;
		pageContent = "";
		fieldControls.addHtml("_fieldset", "{_fieldset}");
		fieldControls.addHtml("_pageContent", "{_pageContent}");
		fieldControls.addHtml("/_fieldset", "{/_fieldset}");
		where = new KStrings();

	}

	@Override
	public void init() {
		try {
			initKoClass();
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible d'initialiser l'objet KHttpListForm ", e);
		}
		getMask();
		listLoaded = false;
		hasFieldSet = false;
		innerForm = true;
	}

	public void addWhere(String field, String value) {
		where.put(field, value);
	}

	private String getSql(String quote) {
		String result = "";
		String strWhere = where.implode_param(" and ", "'", "=", KGlueMode.KEY_AND_VALUE, quote, true);
		result = KSqlQuery.addWhere(listObject.getSql(), strWhere);
		return result;
	}

	public boolean loadAndSubmit(String redirect, HttpServletResponse response) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		boolean result = false;
		this.load();
		if (!submited)
			result = this.submit(redirect, response);
		return result;
	}

	public void load() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		if (!listLoaded) {
			listObject = new KListObject<>(clazz);
			IGenericDao<KObject> dao = getDao();
			dao.setSelect((KListObject<KObject>) listObject, getSql(dao.quote()));
			// dao.readAll();
		}
	}

	public KListObject<? extends KObject> getFkList() {
		KListObject<? extends KObject> fkList = null;
		if (cHasMany == null || !(cHasMany instanceof KConstraintHasManyBelongsTo)) {
			allValues = false;
		} else {
			KConstraintHasManyBelongsTo chmbto = (KConstraintHasManyBelongsTo) cHasMany;
			String bToField = chmbto.getBelongsToField();
			fkList = new KListObject<>(chmbto.getClazz());
			String keys = listObject.getFieldValues(chmbto.getBelongsToField(), ",");
			try {
				Class<? extends KObject> clazzList = (Class<? extends KObject>) chmbto.getBelongsToClass();
				KListObject<? extends KObject> values = Ko.getDao(clazzList).readAll((Object[]) keys.split(","), true, 0);
				for (KObject v : values) {
					KObject obj = clazz.newInstance();
					for (String member : where)
						obj.setAttribute(member, where.get(member), false);
					obj.setAttribute(bToField, v.getFirstKeyValue(), false);
					fkList.add(obj);
				}
			} catch (InstantiationException | IllegalAccessException | SecurityException | IllegalArgumentException | NoSuchFieldException | InvocationTargetException e) {
				allValues = false;
			}
		}
		return fkList;
	}

	public void createContent() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		pageContent = "";
		ko = clazz.newInstance();
		String perm = "1";
		if (allValues)
			perm = "2";
		if (allowAdd && !readonly) {
			for (String member : where)
				ko.setAttribute(member, where.get(member), false);

			pageContent = createOneContent(ko, 0, false, "0");
		}
		int i = 1;
		for (KObject o : listObject) {
			String koValue = createOneContent(o, i, true, perm);
			i++;
			pageContent += koValue;
		}
		if (allowAdd && !readonly) {
			pageContent += createOneContent(ko, i, true, "0");
			listObject.add(ko, true);
		}
		if (allValues) {
			KListObject<? extends KObject> fkList = getFkList();
			if (fkList != null) {
				for (KObject fkKo : fkList) {
					pageContent += createOneContent(fkKo, i, true, perm);
					listObject.add(fkKo, true);
				}
			}
		}
		if ("".equals(pageContent))
			pageContent = "Aucune information";

	}

	private String createOneContent(KObject ko, int index, boolean visible, String updatedValue) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		String koValue = mask.get(0);
		String display = "";
		if (!visible) {
			display = " style='display: none;'";
		}
		for (Map.Entry<String, KFieldControl> e : koFieldsControl.entrySet()) {
			String id = e.getKey();
			KHtmlFieldControl f = (KHtmlFieldControl) e.getValue();
			f.setValue(ko.getAttribute(id) + "");
			f.setId(id + "-" + index);
			if (!f.getFieldType().equals(HtmlControlType.khcCheckBox))
				f.setCaption("");
			else {
				f.setCaption("&nbsp;");
			}
			koValue = koValue.replaceAll("(?i)" + "\\{" + id + "\\}", f + "");
		}
		koValue = koValue.replaceAll("\\{num\\}", index + "");
		koValue = koValue.replaceAll("\\{display\\}", display);
		koValue = koValue.replaceAll("\\{updated-value\\}", updatedValue);
		// KMask mask=new KMask(koValue);
		// koValue= mask.show(ko, getDisplayInstance());
		koValue = ko._showWithMask(koValue);
		return koValue;
	}

	public int getCountObjectsInSubmit() {
		int result = 0;
		String f = where.getFirstKey();
		if (f != null) {
			String[] values = request.getParameterValues("updated" + id);
			if (values != null)
				result = values.length;
		}
		return result;
	}

	public boolean submit(String redirect, HttpServletResponse response) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		boolean result = false;
		if (request.getMethod().equalsIgnoreCase(method) && request.getParameterMap().size() > 0) {
			String toDelete = "";
			int decalNb = 2;
			int decalIndex = 1;
			if (allValues || !allowAdd) {
				decalNb = 0;
				decalIndex = 0;
			}
			int nb = getCountObjectsInSubmit() - listObject.count() - decalNb;
			// if(nb>0){
			for (int i = 0; i < nb; i++) {
				listObject.add(clazz.newInstance(), true);
			}
			String[] updatedValues = request.getParameterValues("updated" + id);
			if (updatedValues != null) {
				if (request.getParameterValues("delete-o" + id) != null)
					toDelete = KStrings.implode(";", request.getParameterValues("delete-o" + id));
				for (int i = 0; i < listObject.count(); i++) {
					KObject ko = listObject.get(i);
					if ((";" + toDelete + ";").contains(";" + ko.getUniqueIdHash() + ";"))
						ko.toDelete();
					else {
						if (!"0".equals(updatedValues[i + decalIndex])) {
							setRequestParameters(ko, i + decalIndex);
							if (ko.getRecordStatus() != KRecordStatus.rsLoaded) {
								if (cHasMany != null) {
									try {
										if (cHasMany instanceof KConstraintHasManyBelongsTo)
											((KConstraintHasManyBelongsTo) cHasMany).setKeyValue(ko);
										ko.toAdd();
									} catch (Exception e) {

									}
								}
							} else if (!"1".equals(updatedValues[i + decalIndex]))
								ko.toUpdate();
						}
					}
				}
				DaoList daoList = Ko.getDaoList(clazz);
				daoList.update(listObject);
				daoList.close();
				result = true;
				submited = true;
				// }
			}
		}
		return result;
	}

	public void getDefaultComplete(String redirect, HttpServletResponse response) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		loadAndSubmit(redirect, response);
	}

	public void setRequestParameters(KObject ko, int position) {
		KObjectController koc = ko.getController();
		for (Map.Entry<String, KFieldControl> field : koFieldsControl.entrySet()) {
			String key = field.getKey();
			KFieldControl fc = field.getValue();
			if (fc.isSerializable()) {
				if (fc instanceof KHtmlFieldControl) {
					KObjectFieldController kofc = null;
					if (koc != null)
						kofc = koc.getFieldController(fc.getName());
					String value;
					if (kofc != null)
						value = kofc.getValue(request, position);
					else
						value = ((KHtmlFieldControl) fc).getValue(request, position);
					if (KRequest.keyExists(key, request) || !fc.isRequired() || fc.isAllowNull())
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
							Ko.klogger().log(Level.WARNING, "Impossible d'affecter les paramètres de la requête (" + value + ") à " + key, e);
						}
				}
			}
		}
	}

	private void initKoClass() throws InstantiationException, IllegalAccessException {
		ko = (KObject) clazz.newInstance();
		initFieldControls(ko);
	}

	private void initFieldControls(KObject ko) {
		// String options="";
		String koMask = "";
		ko.refresh();
		koFieldsControl.setLoaded(true);
		initKobjectController(ko);
		KObjectDisplay display = getDisplayInstance();
		fieldSetLegend = display.getFormCaption(ko.getClass(), kobjectController);
		for (Map.Entry<String, KObjectFieldController> e : kobjectController.getMembers().entrySet()) {
			String field = e.getKey();
			KFieldControl bestCtrl;
			if (!readonly)
				bestCtrl = display.getControl(ko, field, kobjectController, request);
			else
				bestCtrl = display.getReadOnlyControl(ko, field, kobjectController, request);
			((KHtmlFieldControl) bestCtrl).addCssClass("changeable");
			/*
			 * int pos=0;
			 * 
			 * String caption=field; int min=-1; int max=-1; boolean
			 * multiple=false; //KFieldControl
			 * bestCtrl=Display.getDefault().getBestControl(ko,field);
			 * if(bestCtrl==null){ HtmlControlType
			 * htmlCT=HtmlControlType.khcText; if(koc!=null){
			 * KObjectFieldController kofc=e.getValue();
			 * htmlCT=kofc.getHtmlControlType();
			 * options=" "+kofc.getOptions()+" ";
			 * if(!"".equals(kofc.getCaption())) caption=kofc.getCaption();
			 * pos=kofc.getPos(); if(kofc.isMultiple()) multiple=true;
			 * min=kofc.getMin(); max=kofc.getMax(); }
			 * bestCtrl=Display.getDefault().getFc(ko,field, field, "",
			 * htmlCT,options,null); bestCtrl.setMax(max); bestCtrl.setMin(min);
			 * bestCtrl.setMultiple(multiple); bestCtrl.setReadOnly(readonly);
			 * if(pos!=0) bestCtrl.setPos(pos*10); }
			 */
			koFieldsControl.put(field, (KHtmlFieldControl) bestCtrl);
			// koMask+=beforeField+"{"+field+"}"+afterfield;
			// captions.add(caption);
		}
		List<String> keys = koFieldsControl.getSortedKeySet();
		for (String k : keys) {
			if (koFieldsControl.get(k).getFieldType().equals(HtmlControlType.khcHidden))
				koMask += "{" + k + "}";
			else
				koMask += beforeField + "{" + k + "}" + afterfield;
			captions.add(koFieldsControl.get(k).getCaption());
		}
		innerMask = new KMask();
		innerMask.addMask(koMask);
		innerMask.addMask(koMask);
	}

	private String getCaptions() {
		String result = "";
		if (!pageContent.equals("Aucune information")) {
			String ck = "<abbr class='delete' title='Supprimer'></abbr>";
			if (allValues || !allowAdd || !checked || readonly)
				ck = "";
			result = "<tr><td class='td1'>" + ck + "</td>" + innerMask.get(0) + "</tr>";
			for (Map.Entry<String, KObjectFieldController> e : kobjectController.getMembers().entrySet()) {
				String caption = "";
				if (!HtmlControlType.khcHidden.equals(e.getValue().getHtmlControlType())) {
					caption = e.getValue().getCaption();
					if (caption == null || "".equals(caption))
						caption = e.getKey();
				}
				result = result.replaceAll("\\{" + e.getKey() + "\\}", "<label>" + caption + "</label>");
			}
		}
		return result;
	}

	private KMask getMask() {
		String ck = getCheckedString(id, name) + "<input type='hidden' name='updated" + id + "' id='updated" + id + "-{num}' value='{updated-value}'>";
		mask = new KMask();
		mask.addMask("<tr id='line-{num}' {display}>" + ck + this.innerMask.get(0) + "</tr>");
		return this.mask;
	}

	private String getCheckedString(String id, String name) {
		String result = "<td></td>";
		if (!allValues && checked && !"".equals(checkedMask) && !readonly)
			result = checkedMask.replace("{id}", id).replace("{name}", name);
		return result;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
		fieldControls.setLoaded(loaded);
	}

	public String fieldControlsToString() {
		return fieldControls.toString();
	}

	private String getFieldset() {
		String result = "";
		if (hasFieldSet)
			result = "<fieldset><legend>" + getFieldSetLegend() + "</legend>";
		return result;
	}

	public String toString() {
		String result = "";
		setLoaded(true);
		try {
			// if(!submited)
			// loadAndSubmit("", response);
			if (!submited || innerForm) {
				createContent();
				result = fieldControlsToString();
				String frmHeader = "";
				String frmFooter = "";
				if (!innerForm) {
					frmHeader = "<form name='frm-" + name + "' id='frm-" + id + "' method='" + method + "'>";
					frmFooter = "</form>";
				}
				this.header = frmHeader + "<div id='listform'><table id='table-" + id + "' class='KHttpListForm'>\n";
				this.footer = "</table></div>" + frmFooter + "\n";
				pageContent = header + getCaptions() + pageContent + footer;
				result = result.replace("{_fieldset}", getFieldset());
				result = result.replace("{_pageContent}", pageContent);
				result = result.replace("{/_fieldset}", (hasFieldSet) ? "</fieldset>" : "");
				if (!allValues && allowAdd && !readonly) {
					String js = "<script>(new $e('#table-" + id + " .changeable')).addEvent('change',function(e){var t=$gte(e);var numLigne=Forms.Utils.getIdNum(t);Forms.Framework.updateRow(t,'table-" + id + "','" + id + "',numLigne);})</script>";
					result += js;
				}
			}
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible d'afficher l'objet KHttpListForm ", e);
		}
		return result;
	}

	public boolean isAllowAdd() {
		return allowAdd;
	}

	public void setAllowAdd(boolean allowAdd) {
		this.allowAdd = allowAdd;
	}

	public String getFieldSetLegend() {
		String result = fieldSetLegend;
		if (result == null || "".equals(result)) {
			if (kobjectController != null)
				result = kobjectController.getCaption();
			if (result == null || "".equals(result))
				result = clazz.getSimpleName();
		}
		return result;
	}

	public void setFieldSetLegend(String fieldSetLegend) {
		this.fieldSetLegend = fieldSetLegend;
	}

	public boolean isHasFieldSet() {
		return hasFieldSet;
	}

	public void setHasFieldSet(boolean hasFieldSet) {
		this.hasFieldSet = hasFieldSet;
	}

	public boolean isInnerForm() {
		return innerForm;
	}

	public void setInnerForm(boolean innerForm) {
		this.innerForm = innerForm;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isAllValues() {
		return allValues;
	}

	public void setAllValues(boolean allValues) {
		this.allValues = allValues;
		if (allValues)
			allowAdd = false;
	}

	@Override
	protected KObjectDisplay getKobjectDisplay() {
		KObjectDisplay koDisplay = Ko.koDisplays.get(this.clazz.getSimpleName());
		if (koDisplay == null)
			koDisplay = Ko.defaultKoDisplay();
		return koDisplay;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public KConstraintHasMany getcHasMany() {
		return cHasMany;
	}

	public void setcHasMany(KConstraintHasMany cHasMany) {
		this.cHasMany = cHasMany;
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
}
