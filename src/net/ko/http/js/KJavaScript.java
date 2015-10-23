package net.ko.http.js;

import net.ko.controller.KObjectController;
import net.ko.framework.Ko;
import net.ko.http.objects.KQueryString;
import net.ko.http.objects.KRequest;
import net.ko.http.views.KPageList;
import net.ko.kobject.KObject;

/**
 * Classe d'insertion de script javascript dans les vues
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2013
 * @license 
 *          http://www.kobject.net/index.php?option=com_content&view=article&id=50
 *          &Itemid=2 LGPL License
 * @version $Id: $
 * @package net.ko.http.js
 * 
 */
public class KJavaScript {

	public static String getValidator(String idForm, String events, String JSON) {
		String result = "<script type='text/javascript'>v" + idForm + "=new Forms.Validator(" + events + "," + JSON + ",'" + idForm + "');</script>";
		return result;
	}

	public static String getKoEditBtn(KObject ko, String idLb) {
		String result = "";
		try {
			result = "<input type='button' value='...' class='sInnerBtn' onclick='javascript:lbfrmEdit" + idLb + ".show(\"" + ko.keyValuesToUrl() + "\",\"" + ko.getClass().getName() + "\",\"line-" + ko.getUniqueIdHash() + "\");'>";
		} catch (Exception e) {
		}
		return result;
	}

	public static String getKoEditBtn(KObject ko) {
		return getKoEditBtn(ko, "");
	}

	@SuppressWarnings({ "unused" })
	public static String kGetAjxForm(String name, String title, KPageList kpageList) throws InstantiationException, IllegalAccessException {
		String ret = "";
		Class<? extends KObject> koClass = kpageList.getClazz();
		String className = koClass.getSimpleName();
		String functionName = name;
		name += className;
		KObject ko = Ko.getKoInstance(koClass);
		KObjectController koc = Ko.kcontroller().getObjectController(koClass.getSimpleName());
		String ctrlString = koc.toJSON();
		String queryString = KRequest.getQueryString(kpageList.getRequest());
		String idForm = kpageList.getIdForm();
		String ajaxDivMessageSubmit = kpageList.getAjaxDivMessageSubmit();
		String ajaxDivContentRefresh = kpageList.getAjaxDivContentRefresh();
		String listContentUrl = kpageList.getListContentUrl();
		int messageDelay = kpageList.getMessageDelay();
		String selectStyle = kpageList.getSelectStyle();
		boolean modal = kpageList.isFormModal();
		String messageAndUpdateURL = kpageList.getMessageAndUpdateUrl();
		String url = kpageList.getEditFormUrl();
		String frmValidCaption = kpageList.getFrmValidCaption();
		String frmCancelCaption = kpageList.getFrmCancelCaption();

		if (!"".equals(queryString)) {
			queryString = "&" + queryString;
		}
		queryString = KQueryString.setValue(queryString, "_nb", kpageList.getRowCount() + "");

		ret = "<script type='text/javascript'>\n";
		String idLb = kpageList.getIdLb();
		ret += "lb" + functionName + idLb + "=new Forms.Framework.FormForList();";
		ret += "lb" + functionName + idLb + ".options={\"isEdit\":" + name.toLowerCase().startsWith("frmedit") + ",\"idForm\":\"" + idForm + "\"" + ",\"queryString\":\"" + queryString + "\"" + ",\"ajaxDivMessageSubmit\":\"" + ajaxDivMessageSubmit + "\"" + ",\"ajaxDivContentRefresh\":\"" + ajaxDivContentRefresh + "\"" + ",\"listContentUrl\":\"" + listContentUrl + "\"" + ",\"messageDelay\":" + messageDelay + "" + ",\"className\":\"" + className + "\"" + ",\"title\":\"" + title + "\"" + ",\"url\":\"" + url + "\"" + ",\"frmValidCaption\":\"" + frmValidCaption + "\"" + ",\"frmCancelCaption\":\"" + frmCancelCaption + "\"" + ",\"lbName\":\"" + name + "\"" + ",\"modal\":" + modal + "" + ",\"controlOn\":\"" + kpageList.getFrmControlOn() + "\"" + ",\"functionName\":\"" + functionName + "\"" + ",\"messageAndUpdateURL\":\"" + messageAndUpdateURL + "\"" + "};";
		ret += "lb" + functionName + idLb + ".selectStyle=" + selectStyle + ";";
		ret += "lb" + functionName + idLb + ".ctrlString=" + ctrlString + ";";
		ret += "lb" + functionName + idLb + ".init();";
		ret += "</script>\n";
		return ret;
	}

	public static String infoBulle(String label, String content) {
		String result = "<div class='infobulle'>" + label + "<div>" + content + "</div></div>";
		return result;
	}

}
