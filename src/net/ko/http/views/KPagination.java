package net.ko.http.views;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import net.ko.dao.DatabaseDAO;
import net.ko.dao.IGenericDao;
import net.ko.db.KDataBase;
import net.ko.db.KDataTypeConverter;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.http.controls.KCtrlInputHidden;
import net.ko.http.objects.KQueryString;
import net.ko.http.objects.KRequest;
import net.ko.kobject.KListObject;
import net.ko.kobject.KMask;
import net.ko.kobject.KObject;
import net.ko.ksql.KSqlQuery;

/**
 * Permet d'afficher une liste d'objet (KListObject) paginée
 * 
 * @author jcheron
 * 
 */
public class KPagination {
	private HttpServletRequest request;
	private String jsToogle;
	private boolean noLimit;
	private int rowCount;
	private int pageNavCount;
	private int activePage;
	private KListObject<KObject> list;
	private String sql;
	private Class itemClass;
	private boolean isShowCaptions;
	private Map<String, String> captions;
	public Object mask;
	public int nbPages;
	public int totalRowCount;
	public String sortedField = "1";
	public String sortedSens;
	public String sortedFieldType = "";
	private KNavBarre navBarre;
	private String value = "";
	private String requestUrl = "";
	private boolean ajax = false;
	private String ajaxDivContentRefresh = "ajxContent";
	private boolean empty;
	private Object groupBy = "";
	private String idListe = "";
	private boolean koDetails = false;
	private KObjectDisplay displayInstance;
	private DatabaseDAO<KObject> dao;
	private boolean noDb = false;
	private boolean checked = false;

	/**
	 * Instancie une liste d'objets paginée affichant 10 enregistrements/objets
	 * 
	 * @param request
	 *            requête Http
	 * @param itemClass
	 *            class d'objets à afficher
	 */
	public KPagination(HttpServletRequest request, Class itemClass) {
		this(request, itemClass, "", 5, 10);
	}

	/**
	 * Instancie une liste d'objets paginée
	 * 
	 * @param request
	 *            requête Http
	 * @param itemClass
	 *            class d'objets à afficher
	 * @param sql
	 *            instruction sql de sélection des enregistrements/objets à
	 *            afficher
	 * @param pageNavCount
	 *            nombre de pages à afficher dans la barre de navigation
	 * @param rowCount
	 *            nombre d'enregistrement à afficher par page
	 */
	@SuppressWarnings("unchecked")
	public KPagination(HttpServletRequest request, Class itemClass, String sql, int pageNavCount, int rowCount) {
		this.mask = new KMask();
		this.captions = new LinkedHashMap<String, String>();
		this.request = request;
		this.itemClass = itemClass;
		// this.setSql(sql);
		this.sql = sql;
		this.pageNavCount = pageNavCount - 1;
		this.rowCount = rowCount;
		this.list = new KListObject<KObject>(itemClass);
		this.sortedField = "1";
		this.sortedSens = "ASC";
		this.sortedFieldType = "db";
		this.sortedField = KRequest.GETPOST("_sf", request, "1");
		this.sortedSens = KRequest.GETPOST("_ss", request, "ASC");
		this.sortedFieldType = KRequest.GETPOST("_ft", request, "db");
		this.noLimit = false;
	}

	/**
	 * @return vrai si les en-têtes des colonnes sont visibles
	 */
	public boolean getIsShowCaptions() {
		return isShowCaptions;
	}

	/**
	 * @param noLimit
	 *            vrai si la liste affiche de manière complète tous les
	 *            enregistrements/objets (sans pagination)
	 */
	public void setNoLimit(boolean noLimit) {
		this.noLimit = noLimit;
	}

	/**
	 * @return la liste d'objets
	 */
	public KListObject<KObject> getKListObject() {
		return list;
	}

	public boolean getNoLimit() {
		return noLimit;
	}

	public void setIsShowCaptions(boolean showCaptions) {
		this.isShowCaptions = showCaptions;
	}

	private String sortedSensInverse(String sens) {
		String ret = "ASC";
		if (sens.equalsIgnoreCase("asc"))
			ret = "DESC";
		return ret;
	}

	private String getButton(String field, String pDefault) {
		String queryString = KRequest.getQueryString(request);
		KQueryString qs = new KQueryString(queryString);
		qs.setValue("_sf", field);
		qs.setValue("_ss", sortedSensInverse(this.sortedSens));
		if (KObject.isSerializable(list.getClazz(), field))
			qs.setValue("_ft", "db");
		else
			qs.setValue("_ft", "object");
		queryString = getRequestUrl() + "?" + qs;
		if (ajax)
			return "<a onclick=\"Forms.Framework.gotoPage('" + this.ajaxDivContentRefresh + "','" + this.getRequestUrl() + "','" + qs + "&_toToogle='+Forms.Framework.getToToogleValue(),'" + idListe + "'," + activePage + ");\">" + pDefault + "</a>";
		else
			return "<a href='" + queryString + "'>" + pDefault + "</a>";
	}

	public String cmdAsc(String field) {
		return getButton(field, "&nbsp;<div class='arrow-up'></div>&nbsp;");
	}

	public String cmdDesc(String field) {
		return getButton(field, "&nbsp;<div class='arrow-down'></div>&nbsp;");
	}

	private int getCount() {
		if (!noDb) {
			String query = KSqlQuery.getCountQuery(this.sql);
			KDataBase kdb = null;
			try {
				kdb = dao.getDatabase();
				if (kdb != null)
					this.totalRowCount = kdb.rowCount(query);
				else
					this.totalRowCount = -1;
			} catch (Exception e) {
				Ko.klogger().log(Level.SEVERE, "Exécution de la requête impossible : " + query, e);
			} finally {
				if (kdb != null) {
					try {
						kdb.close();
					} catch (Exception e) {
						Ko.klogger().log(Level.WARNING, "Fermeture de la connexion impossible :" + kdb, e);
					}
				}
			}
		}
		return this.totalRowCount;
	}

	private int getNbPages(int totalRowCount) {
		int res = Math.round(totalRowCount / this.rowCount);
		if (totalRowCount % this.rowCount != 0)
			res += 1;
		this.nbPages = res;
		return res;
	}

	public String getFieldCaption(String fieldName) {
		String result = fieldName;
		if (captions.containsKey(fieldName))
			result = captions.get(fieldName);
		return result;
	}

	public void setFieldCaption(String fieldName, String caption) {
		captions.put(fieldName, caption);
	}

	public void setSql(String sql) {
		if (sql == null || "".equals(sql)) {
			KObject ko = null;
			try {
				ko = Ko.getKoInstance(itemClass);
				this.sql = ko.getSql(dao.quote());
			} catch (Exception e) {
				Ko.klogger().log(Level.SEVERE, "Impossible d'instancier une instance de la classe " + itemClass + " pour construire l'instruction sql", e);
			}
		} else {
			this.sql = sql;
		}
	}

	public void gotoo(int aPage) {
		noDb = false;
		IGenericDao<KObject> dao = Ko.getDao(itemClass);
		this.activePage = aPage;
		list.setSql(sql, dao.quote());
		list = dao.gotoo(list, (this.activePage - 1) * this.rowCount, this.rowCount, sortedField, "asc".equalsIgnoreCase(sortedSens));
		this.sql = list.getSql();
		empty = (list.count() == 0);
	}

	public void gotooWhenLoaded(int aPage) {
		noDb = true;
		this.activePage = aPage;
		this.totalRowCount = list.count();
		int limit = (this.activePage - 1) * this.rowCount;
		if (limit != -1) {
			list = list.subList(limit, this.rowCount);
		}
		list.sortBy(sortedField, "asc".equalsIgnoreCase(sortedSens));
		empty = (list.count() == 0);
	}

	public String getInputCountAsString() {
		KCtrlInputHidden input = new KCtrlInputHidden("", this.list.count() + "", "_" + itemClass.getSimpleName() + "Count");
		return input.toString();
	}

	public String toString() {
		String ret = "";
		// KCtrlInputHidden input = new KCtrlInputHidden("", this.list.count() +
		// "", "_" + itemClass.getSimpleName() + "Count");
		// ret = input.toString();
		KMask mask = null;
		if (this.mask.getClass().isArray()) {
			mask = new KMask((String[]) this.mask);
		} else if (this.mask instanceof KMask)
			mask = (KMask) this.mask;
		else
			mask = new KMask((String) this.mask);
		if (this.isShowCaptions) {
			ret += this.showCaptions(mask.get(0));
			mask.remove(0);
		}
		if (!"".equals(value))
			mask.setSelectedKeys(value);
		mask.setKoDetails(koDetails);
		mask.setRequest(request);

		if (groupBy != null) {
			if (groupBy.getClass().isArray())
				return ret + mask.show(this.list, (String[]) groupBy, this.sortedField, this.sortedSens, displayInstance);
			return ret + mask.show(this.list, groupBy + "", this.sortedField, this.sortedSens, displayInstance);
		} else
			return ret + mask.show(this.list, this.sortedField, this.sortedSens, displayInstance);
	}

	public String showCaptions(String aMask) {
		String ss = "ASC";
		String result = "";
		if (this.sortedSens.toLowerCase().equals("asc"))
			ss = "DESC";
		result = aMask;
		if (this.list.count() > 0) {
			KObject obj = null;
			try {
				obj = Ko.getKoInstance(this.itemClass);
				ArrayList<String> lfields = new KMask(aMask).getOperations("{", "}");
				int i = 1;
				String caption = "";
				for (String field : lfields) {
					boolean captionUpdated = false;
					String objectField = field;
					caption = getFieldCaption(field);
					String[] captions = field.split(":");
					if (captions.length > 1) {
						captionUpdated = true;
						caption = captions[0];
						objectField = captions[1];
					}
					Class<?> cls = null;
					try {
						cls = obj.getFieldType(objectField);
					} catch (NoSuchFieldException e) {
						KDebugConsole.print("Le membre " + objectField + " n'existe pas pour l'objet " + obj, "TEMPLATE", "KPagination.showCaptions");
					}
					if (!"getFirstKeyValue".equals(field) && !"getUniqueIdHash".equals(field) && !"checkedValue".equals(field)) {
						KQueryString qs = new KQueryString(KRequest.getQueryString(request));
						if (cls != null && KDataTypeConverter.isSerializable(cls)) {
							qs.setValue("_ft", "db");
						} else {
							qs.setValue("_ft", "object");
						}
						String add = "";

						qs.setValue("_sf", objectField);
						qs.setValue("_ss", ss);
						qs.setValue("_nb", rowCount + "");
						String k = "";
						if (!captionUpdated)
							caption = displayInstance.getCaption(obj, caption);
						if (ajax)
							k = "<a onclick=\"Forms.Framework.gotoPage('" + this.ajaxDivContentRefresh + "','" + this.getRequestUrl() + "','_refresh&" + qs + "&_toToogle='+Forms.Framework.getToToogleValue(),'" + idListe + "'," + activePage + ",true);\">" + caption + "</a>";
						else
							k = "<a href='" + getRequestUrl() + "?" + qs + "'>" + caption + "</a>";
						if (objectField.equals(this.sortedField) || String.valueOf(i).equals(this.sortedField)) {
							if (this.sortedSens.equalsIgnoreCase("asc"))
								add = this.cmdDesc(field);
							else
								add = this.cmdAsc(field);
						}
						result = result.replace("{" + field + "[...]}", add + k);
						result = result.replace("{" + field + "}", add + k);
					} else {
						result = result.replace("{" + field + "[...]}", caption);
						result = result.replace("{" + field + "}", caption);
					}
					i++;
				}
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Erreur sur KPagination.showCaptions", e);
			}

			result = finallyReplaceCaptions(result);
			result = result.replace("{", "");
			result = result.replace("}", "");
			if (checked) {
				result += "<script>$addEvt($('" + itemClass.getSimpleName() + "getUniqueIdHash'),'click',function(e){var target=$gte(e);new $e('#list-" + itemClass.getSimpleName() + " input[type=checkbox].checkable').each(function(k,v){v.checked=target.checked;});});</script>";
			}
		}
		return result;
	}

	private String getFolder() {
		String result = "";
		if (this.navBarre != null)
			result = this.navBarre.folder;
		return result;
	}

	public String showNavBarre(String imgFolder) {
		String result = "";
		int nbp = this.getNbPages(this.getCount());
		if (nbp > 0) {
			this.navBarre = new KNavBarre(request, imgFolder);
			this.navBarre.setIdListe(idListe);
			this.navBarre.setRequestUrl(getRequestUrl());
			this.navBarre.setAjax(ajax);
			this.navBarre.setRowCount(rowCount);
			this.navBarre.setAjaxDivContentRefresh(ajaxDivContentRefresh);
			this.navBarre.lastPage = nbp;
			this.navBarre.pageNavCount = this.pageNavCount;
			this.navBarre.gotoPage(this.activePage);
			try {
				result = this.navBarre.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getPageNavCount() {
		return pageNavCount;
	}

	public void setPageNavCount(int pageNavCount) {
		this.pageNavCount = pageNavCount;
	}

	private String finallyReplaceCaptions(String mask) {
		String result = mask;
		for (Map.Entry<String, String> e : captions.entrySet())
			result = result.replace("{" + e.getKey() + "}", e.getValue());
		return result;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public Object getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(Object groupBy) {
		this.groupBy = groupBy;
	}

	public boolean isKoDetails() {
		return koDetails;
	}

	public void setKoDetails(boolean koDetails) {
		this.koDetails = koDetails;
	}

	public void setDisplayInstance(KObjectDisplay displayInstance) {
		this.displayInstance = displayInstance;
	}

	public DatabaseDAO<KObject> getDao() {
		return dao;
	}

	public void setDao(DatabaseDAO<KObject> dao) {
		this.dao = dao;
	}

	public String getIdListe() {
		return idListe;
	}

	public void setIdListe(String idListe) {
		this.idListe = idListe;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}
}
