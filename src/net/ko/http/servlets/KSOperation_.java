package net.ko.http.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.controller.IKoController;
import net.ko.controller.KObjectController;
import net.ko.controller.KXmlControllers;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
import net.ko.http.servlets.bean.KSOperationsBean;
import net.ko.http.views.CssVars;
import net.ko.http.views.KHtmlFieldControl;
import net.ko.mapping.IAjaxObject;
import net.ko.mapping.IHasParentObject;
import net.ko.mapping.IHasURL;
import net.ko.mapping.KAbstractFilterMapping;
import net.ko.mapping.KAjaxIncludeDialog;
import net.ko.mapping.KAjaxIncludes;
import net.ko.mapping.KAjaxJs;
import net.ko.mapping.KAjaxRequest;
import net.ko.mapping.KAjaxWithChilds;
import net.ko.mapping.KFilterMappings;
import net.ko.mapping.KXmlMappings;
import net.ko.types.HtmlControlType;
import net.ko.types.KRestartType;
import net.ko.utils.KProperties;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.xml.KKoxPersistance;
import net.ko.xml.KMoxPersistance;

/**
 * Servlet implementation class KSOperation
 */
public class KSOperation_ extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSOperation_() {
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
		synchronized (this) {
			KSOperationsBean bean = new KSOperationsBean(request, response);
			String operation = KRequest.GETPOST("_op", request, "all");
			switch (operation) {
			case "all":
				all(bean);
				break;
			case "mox":
				displayMox(bean);
				break;
			case "moxOp":
				moxOp(bean);
				break;
			case "loadMoxCopy":
				loadMoxCopy(bean);
				break;
			case "addAllMox":
				addAllMox(bean);
				break;
			case "addSelectedMox":
				addSelectedMox(bean);
				break;
			case "restoreMox":
				restoreMox(bean);
				break;
			case "saveMox":
				saveMox(bean);
				break;

			case "kox":
				displayKox(bean);
				break;

			case "koxOp":
				koxOp(bean);
				break;

			case "loadKoxCopy":
				loadKoxCopy(bean);
				break;

			case "addAllKox":
				addAllKox(bean);
				break;
			case "addSelectedKox":
				addSelectedKox(bean);
				break;
			case "restoreKox":
				restoreKox(bean);
				break;
			case "saveKox":
				saveKox(bean);
				break;

			case "opProperties":
				opProperties(bean);
				break;

			case "loadFile":
				loadFile(bean);
				break;

			case "loadFileCopy":
				loadFileCopy(bean);
				break;

			case "addAllProperties":
				addAllProperties(bean);
				break;
			case "addSelectedProperties":
				addSelectedProperties(bean);
				break;
			case "restoreProperties":
				restoreProperties(bean);
				break;
			case "saveProperties":
				saveProperties(bean);
				break;
			default:
				all(bean);
				break;
			}
			bean.printJs();
			try {
				bean.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void saveProperties(KSOperationsBean bean) {
		KProperties originalProperties = null;
		try {
			originalProperties = bean.getOriginalProperties();
			originalProperties.save();
			bean.print(originalProperties.getFileName() + " enregistré");
		} catch (IOException e) {
			bean.print("Impossible d'enregistrer le fichier " + originalProperties.getFileName() + " " + e.getMessage());
		}
		bean.addJs("Forms.Utils.show('btSave',false);");
	}

	private void restoreProperties(KSOperationsBean bean) {
		reloadProperties(bean);
	}

	private void addSelectedProperties(KSOperationsBean bean) {
		Enumeration<String> names = bean.getRequest().getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			if (name.startsWith("ck-")) {
				name = name.substring(3);
				if (KString.isNotNull(name)) {
					bean.getOriginalProperties().add(name, bean.getCopyProperties().getProperty(name));
				}
			}
		}
		updateProperties(bean);

	}

	private void addAllProperties(KSOperationsBean bean) {
		bean.getOriginalProperties().addAll(bean.getCopyProperties());
		updateProperties(bean);
	}

	private void updateProperties(KSOperationsBean bean) {
		switch (bean.getFileType()) {
		case "Css":
			Ko.getInstance().loadCssVars(bean.getOriginalProperties());
			break;
		case "Messages":
			Ko.getInstance().setKpMessages(bean.getOriginalProperties());
			break;
		default:
			Ko.getInstance().setKpERs(bean.getOriginalProperties());
			break;
		}
		bean.print("<fieldset><legend>Fichier " + bean.getFileType() + "</legend>");
		displayProperties(bean, bean.getOriginalProperties(), false);
		bean.print("</fieldset>");
		bean.addJs("Forms.Framework.opShowDifferences();");
		showMoxButtons(bean, true);
	}

	private void reloadProperties(KSOperationsBean bean) {
		try {
			boolean isCss = "Css".equals(bean.getFileType());
			if (isCss)
				KoHttp.krestart(bean.getApplication(), KRestartType.rtCssVars.value);
			else
				KoHttp.krestart(bean.getApplication(), KRestartType.rtMessages.value);
			_loadFile(bean);
			bean.addJs("Forms.Framework.opShowDifferences();");
			showMoxButtons(bean, false);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void _loadFile(KSOperationsBean bean) {
		String fileType = bean.initFileType();
		String activeFile = bean.initActiveFile();
		bean.print("<fieldset><legend>Fichier " + fileType + "</legend>");
		displayFile(bean, bean.getApplication().getRealPath(activeFile), false);
		bean.print("</legend>");
	}

	private void loadFile(KSOperationsBean bean) {
		_loadFile(bean);
		bean.addJs("$set($('fileType'),'" + bean.getFileType() + "');");
		bean.addJs("Forms.Utils.setInnerHTML($('copy'),'');");
		bean.addJs("Forms.Utils.setInnerHTML($('infos'),'');");
		bean.addJs("Forms.Utils.show('btTest',false);");
		bean.addJs("Forms.Utils.show('btTestChecked',false);");
		bean.addJs("Forms.Utils.show('btRestore',false);");
		bean.addJs("Forms.Utils.show('btSave',false);");
	}

	private void loadFileCopy(KSOperationsBean bean) {
		String fileName = KRequest.GETPOST("fileName", bean.getRequest());
		bean.print("<fieldset><legend>" + fileName + "</legend>");
		bean.print("<form id='frmPropertiesCopy' name='frmPropertiesCopy'>");
		displayFile(bean, Ko.getPath() + "/uploadFiles/" + fileName, true);
		bean.print("</form>");
		bean.print("</legend>");
		bean.addJs("Forms.Framework.opShowDifferences();");
	}

	private void opProperties(KSOperationsBean bean) {
		KStrings files = new KStrings();
		files.put(Ko.getInstance().getCssFile(), "Css");
		files.put(Ko.getInstance().getMessagesFile(), "Messages");
		files.put(Ko.getInstance().getErFile(), "Expressions Régulières");
		KHtmlFieldControl ctrlFiles = new KHtmlFieldControl("Fichiers properties", "Css", "files", "files", HtmlControlType.khcRadioList, files, "");
		ctrlFiles.setRequired(true);
		ctrlFiles.setValue(Ko.getInstance().getCssFile());
		bean.print(ctrlFiles + "");
		bean.print("<fieldset id='otherOperations'><legend>Opérations :</legend><div id='uploadProperties' class='btn'><span class='btUpload'>Importer un fichier <span id='fileType'></span></span> [<span id='importedProperties'>...</span>]</div><input style='display:none' accept='text/x-java-properties' type='file' id='importProperties' name='importProperties' " +
				"onchange=\"if(this.files[0])(new Forms.Ajax('importedProperties', 'upload.frm','_inputName=propertiesFile&_messageMask={fileName}&_valueMask={fileName}'," +
				"function(){$get('copy','operations.main','_op=loadFileCopy&_ajx=true&fileName='+$$('propertiesFile'))})).upload(this);\">");
		bean.print("<div id='op1' class='op'><div id='btTest' class='btn' style='display:none;'><span class='btTestAll'>Tester avec le nouveau fichier ajouté</span></div></div>" +
				"<div id='op2' class='op'><div id='btTestChecked' class='btn' style='display:none;'><span class='btTestChecked'>Tester avec les éléments sélectionnés</span></div></div>" +
				"<div id='op3' class='op'><div id='btRestore' class='btn' style='display:none;'><span class='btRestore'>Restaurer le fichier d'origine</span></div></div>" +
				"<div id='op4' class='op'><div id='btSave' class='btn' style='display:none;'><span class='btSave'>Enregistrer les modifications</span></div></div></fieldset>");
		bean.addJs("Forms.Utils.addEventToElement($('uploadProperties'),'click',function(){Forms.Utils.click($('importProperties'));});");
		bean.addJs("$addEvt($('btTest'), 'click', function(){$get('original','operations.main','_op=addAllProperties&_ajx=true');});");
		bean.addJs("$addEvt($('btTestChecked'), 'click', function(){$postForm('frmPropertiesCopy','original','operations.main','_op=addSelectedProperties&_ajx=true');});");
		bean.addJs("$addEvt($('btRestore'),'click',function(){$get('original','operations.main','_op=restoreProperties&_ajx=true');});");
		bean.addJs("$addEvt($('btSave'),'click',function(){$get('infos','operations.main','_op=saveProperties&_ajx=true');});");
		bean.addJs("$get('original','operations.main','_op=loadFile&file=" + Ko.getInstance().getCssFile() + "&fileType=Css&_ajx=true');");
		bean.addJs("$addEvt($('files'),'change',function(){$get('original','operations.main','_op=loadFile&file='+this.value+'&fileType='+this.valueText+'&_ajx=true');});");
	}

	private void displayFile(KSOperationsBean bean, String fileName, boolean checkBoxes) {
		try {
			KProperties kp = openFile(fileName);
			if (checkBoxes)
				bean.setCopyProperties(kp);
			else
				bean.setOriginalProperties(kp);
			displayProperties(bean, kp, checkBoxes);
		} catch (IOException e) {
		}
	}

	private KProperties openFile(String fileName) throws IOException {
		KProperties kp = new KProperties();
		kp.loadFromFile(fileName);
		return kp;
	}

	private void displayProperties(KSOperationsBean bean, KProperties kproperties, boolean checkBoxes) {
		String copy = "";
		String result = "";
		CssVars cssVars = null;
		boolean isCss = "Css".equals(bean.getFileType());
		if (isCss)
			cssVars = new CssVars(kproperties.getProperties());
		if (checkBoxes)
			copy = "cp-";
		Enumeration<Object> keys = kproperties.getProperties().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			String spanCss = "<span class='noColor'>&nbsp;</span>";
			if (isCss) {
				String color = cssVars.getRealValue(key + "");
				if (color != null && color.startsWith("#"))
					spanCss = "<span class='color' style='background-color: " + color + "'>&nbsp;</span>";
			}
			String id = key + "";
			result += "<div class='row'>";
			if (checkBoxes)
				result += "<input type='checkbox' id='ck-" + id + "' name='ck-" + id + "'>";
			result += "<div class='element' id='" + copy + id + "_'><span class='key'>" + id + "</span>" + spanCss + "<span class='value'>" + kproperties.getProperty(id) + "</span></div>";
			result += "</div>";
		}
		bean.print(result);
	}

	private void saveMox(KSOperationsBean bean) {
		KMoxPersistance moxCreator = new KMoxPersistance(KXmlMappings.getInstance());
		String moxFile = Ko.getInstance().getMappingFile();
		moxCreator.saveAs(bean.getApplication().getRealPath("/") + "/" + moxFile);
		bean.print("Fichier [" + moxFile + "] enregistré");
		bean.addJs("Forms.Utils.show('btSave',false);");
	}

	private void restoreMox(KSOperationsBean bean) {
		KXmlMappings.stop();
		Ko.getInstance().setMappingFile();
		_displayMox(bean);
		bean.addJs("Forms.Framework.opShowDifferences();");
		showMoxButtons(bean, false);
	}

	private void addSelectedMox(KSOperationsBean bean) {
		Enumeration<String> names = bean.getRequest().getParameterNames();
		List<IHasParentObject> elements = bean.getXmlMappings().getAll();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			if (name.startsWith("ck-")) {
				name = name.substring(3);
				IHasParentObject elm = bean.getXmlMappings().getElementByHashCode(name, elements);
				if (elm != null) {
					KXmlMappings.getInstance().addElement(elm);
				}
			}
		}
		_displayMox(bean);
		bean.addJs("Forms.Framework.opShowDifferences();");
		bean.addJs("$set($('infos'),'Mappings sélectionnés ajoutés au fichier actuel');");
		showMoxButtons(bean, true);
	}

	private void addAllMox(KSOperationsBean bean) {
		for (KAbstractFilterMapping mapping : bean.getXmlMappings().getMappings().getItems()) {
			Ko.kmappings().addElement(mapping);
		}
		for (KAbstractFilterMapping ajaxInclude : bean.getXmlMappings().getAjaxIncludes().getItems()) {
			KoHttp.kajaxIncludes().addElement(ajaxInclude);
		}
		_displayMox(bean);
		bean.addJs("Forms.Framework.opShowDifferences();");
		bean.addJs("$set($('infos'),'Mappings ajoutés au fichier actuel');");
		showMoxButtons(bean, true);
	}

	private void moxOp(KSOperationsBean bean) {
		bean.print("<fieldset id='otherOperations'><legend>Opérations :</legend><div id='uploadMox' class='btn'><span class='btUpload'>Importer un fichier mox.xml</span> [<span id='importedMox'>...</span>]</div><input style='display:none' accept='text/xml' type='file' id='importMox' name='importMox' " +
				"onchange=\"if(this.files[0])(new Forms.Ajax('importedMox', 'upload.frm','_inputName=moxFile&_messageMask={fileName}&_valueMask={fileName}'," +
				"function(){$get('copy','operations.main','_op=loadMoxCopy&_ajx=true&fileName='+$$('moxFile'))})).upload(this);\">");
		bean.print("<div id='op1' class='op'><div id='btTest' class='btn' style='display:none;'><span class='btTestAll'>Tester avec le nouveau mox ajouté</span></div></div>" +
				"<div id='op2' class='op'><div id='btTestChecked' class='btn' style='display:none;'><span class='btTestChecked'>Tester avec les éléments sélectionnés</span></div></div>" +
				"<div id='op3' class='op'><div id='btRestore' class='btn' style='display:none;'><span class='btRestore'>Restaurer le fichier d'origine</span></div></div>" +
				"<div id='op4' class='op'><div id='btSave' class='btn' style='display:none;'><span class='btSave'>Enregistrer les modifications</span></div></div></fieldset>");
		bean.addJs("Forms.Utils.addEventToElement($('uploadMox'),'click',function(){Forms.Utils.click($('importMox'));});");
		bean.addJs("$addEvt($('btTest'), 'click', function(){$get('original','operations.main','_op=addAllMox&_ajx=true');});");
		bean.addJs("$addEvt($('btTestChecked'), 'click', function(){$postForm('frmMoxCopy','original','operations.main','_op=addSelectedMox&_ajx=true');});");
		bean.addJs("$addEvt($('btRestore'),'click',function(){$get('original','operations.main','_op=restoreMox&_ajx=true');});");
		bean.addJs("$addEvt($('btSave'),'click',function(){$get('infos','operations.main','_op=saveMox&_ajx=true');});");
	}

	private void showMoxButtons(KSOperationsBean bean, boolean visible) {
		bean.addJs("Forms.Utils.show('btRestore'," + visible + ");");
		bean.addJs("Forms.Utils.show('btSave'," + visible + ");");
	}

	public void all(KSOperationsBean bean) {
		bean.setUrls(new ArrayList<String>());
		bean.setIds(new ArrayList<Integer>());
		if (!bean.isAjax())
			bean.printPageHeader("admin.kcss");
		KoHttp.kstart(bean.getApplication());
		bean.print("<div id='_commandes'><fieldset><legend>Imports</legend><div id='infos'></div><div class='boxButtons'><input type='button' class='btn' id='opKox' value='Validation & classes (kox.xml)'>" +
				"<input type='button' class='btn' id='opMox' value='Mappings, Filters & ajax-includes (mox.xml)'>" +
				"<input type='button' class='btn' id='opProperties' value='Fichiers properties'></div></fieldset></div>");
		bean.print("<div id='siteMap'><table><tr><td id='original'></td><td id='copy'></td><td id='operations'></tr></table></div>");
		bean.addJs("var operations=function(e){var target=$gte(e);" +
				"if(target.id=='opMox'){" +
				"$get('original','operations.main','_op=mox&_ajx=true');" +
				"$get('operations','operations.main','_op=moxOp&_ajx=true');" +
				"}else if(target.id=='opKox'){" +
				"$get('original','operations.main','_op=kox&_ajx=true');" +
				"$get('operations','operations.main','_op=koxOp&_ajx=true');" +
				"}else{" +
				"$get('operations','operations.main','_op=opProperties&_ajx=true');" +
				"}" +
				"};");
		bean.addJs("new Forms.Selector('#_commandes input[type=button]','click','false',0,operations,{'border':'none','fontWeight':'bolder','background':'transparent','boxShadow':'none'});");
		if (!bean.isAjax())
			bean.printPageFooter();
	}

	public void loadMoxCopy(KSOperationsBean bean) {
		String fileName = KRequest.GETPOST("fileName", bean.getRequest());
		if (KString.isNotNull(fileName)) {
			bean.setXmlMappings(new KXmlMappings(Ko.getPath() + "/uploadFiles/" + fileName));
			bean.print("<form id='frmMoxCopy' name='frmMoxCopy'><fieldset id='fsMappingsCopy'><legend>Mappings/filters</legend>");
			bean.print(display(bean.getXmlMappings().getMappings(), "cp-"));
			bean.print("</fieldset>");
			bean.print("<fieldset id='fsAjaxIncludesCopy'><legend>ajax-includes</legend>");
			bean.print(display(bean.getXmlMappings().getAjaxIncludes(), "cp-"));
			bean.print("</fieldset></form>");
			bean.addJs("$('fsMappings').style.height=$('fsMappingsCopy').clientHeight+'px';$('fsMappingsCopy').style.height=$('fsMappings').style.height;");
			bean.addJs("$('fsAjaxIncludes').style.height=$('fsAjaxIncludesCopy').clientHeight+'px';$('fsAjaxIncludesCopy').style.height=$('fsAjaxIncludes').style.height;");
			bean.addJs("Forms.Framework.opShowDifferences();");
		}
	}

	private void _displayMox(KSOperationsBean bean) {
		KAjaxIncludes ajaxIncludes = KoHttp.kajaxIncludes();
		KFilterMappings mappings = KoHttp.kmappings();
		bean.print("<fieldset id='fsMappings'><legend>Mappings/filters</legend>");
		bean.print(display(mappings, ""));
		bean.print("</fieldset>");
		bean.print("<fieldset id='fsAjaxIncludes'><legend>ajax-includes</legend>");
		bean.print(display(ajaxIncludes, ""));
		bean.print("</fieldset>");
	}

	public void displayMox(KSOperationsBean bean) {
		_displayMox(bean);
		bean.addJs("Forms.Utils.setInnerHTML($('copy'),'');");
		showMoxButtons(bean, false);
	}

	public String display(KSOperationsBean bean, KAjaxIncludes ajaxIncludes, String copy) {
		String result = "";
		for (KAbstractFilterMapping filterMapping : ajaxIncludes.getItems()) {
			if (filterMapping instanceof KAjaxRequest) {
				KAjaxRequest ajaxReq = (KAjaxRequest) filterMapping;
				result += display(bean.isOnlyURL(), ajaxReq, 0, 0, 0, bean.getRequest(), copy);
				bean.getUrls().add(ajaxReq.getRequestURL());
			}
		}
		return result;
	}

	public String display(KFilterMappings filterMappings, String copy) {
		String result = "";
		boolean isCopy = !"".equals(copy);
		for (KAbstractFilterMapping filterMapping : filterMappings.getItems()) {
			String id = filterMapping.getUniqueIdHash(0);
			result += "<div id='" + copy + id + "'>";
			if (isCopy)
				result += "<input type='checkbox' id='ck-" + id + "' name='ck-" + id + "'>";
			result += filterMapping.display(0);
			result += "</div>";
		}
		return result;
	}

	public String display(boolean onlyURL, KAjaxRequest ajaxReq, int depth, int niveau, int margin, HttpServletRequest request, String copy) {
		String result = "";
		boolean isCopy = !"".equals(copy);
		String id = ajaxReq.getUniqueIdHash(0);
		result = "<div id='" + copy + id + "'>";
		if (niveau == 0) {
			if (isCopy)
				result += "<input type='checkbox' id='ck-" + id + "' name='ck-" + id + "'>";
			result += getDisplayCaption(ajaxReq, "ss-" + copy + id, margin);
			result += "<div id='ss-" + copy + id + "'>";
		}
		for (IAjaxObject ajaxReqChild : ajaxReq.getAjaxIncludes()) {
			id = ajaxReqChild.getUniqueIdHash(0);
			if (!onlyURL || (ajaxReqChild instanceof IHasURL)) {
				result += "<div id='" + copy + id + "'>";
				if (isCopy)
					result += "<input type='checkbox' id='ck-" + id + "' name='ck-" + id + "'>";
				result += getDisplayCaption(ajaxReqChild, "ss-" + copy + id, margin + 1);
				result += "</div>";
			}
			if (ajaxReqChild instanceof KAjaxJs) {
				result += "<div id='ss-" + copy + id + "'>";
				for (IAjaxObject ajaxObj : ((KAjaxJs) ajaxReqChild).getChilds()) {
					result += display(onlyURL, ajaxObj, depth, niveau, margin + 1, 0, request, copy);
				}
				result += "</div>";
			}

		}
		if (niveau == 0)
			result += "</div>";
		result += "</div>";
		return result;
	}

	private String getDisplayCaption(IAjaxObject ajaxObj, String id, int margin) {
		String disp = ajaxObj.display(margin);
		String caption = ajaxObj.getDisplayCaption();
		if (!"".equals(caption) && ajaxObj.hasChilds())
			disp = disp.replace(caption, "<a onclick='Forms.Utils.toogleText($(\"imgToogle" + id + "\"),\"" + id + "\",false);'>" + caption + "<div id='imgToogle" + id + "' class='arrow-up'></div></a>");
		return disp;
	}

	private String getDisplayCaption(KAjaxRequest ajaxRequest, String id, int margin) {
		String disp = ajaxRequest.display(margin);
		String caption = ajaxRequest.getRequestURL();
		if (!"".equals(caption))
			disp = disp.replace(caption, "<a onclick='Forms.Utils.toogleText($(\"imgToogle" + id + "\"),\"" + id + "\",false);'>" + caption + "<div id='imgToogle" + id + "' class='arrow-up'></div></a>");
		return disp;
	}

	public String display(boolean onlyURL, IAjaxObject ajaxObj, int depth, int niveau, int margin, int decalage, HttpServletRequest request, String copy) {
		boolean isCopy = !"".equals(copy);
		String id = ajaxObj.getUniqueIdHash(0);
		String result = "<div id='" + copy + id + "'>";
		List<IAjaxObject> childs = getChilds(ajaxObj);
		if (!onlyURL || (ajaxObj instanceof IHasURL)) {
			if (isCopy)
				result += "<input type='checkbox' id='ck-" + id + "' name='ck-" + id + "'>";
			result += getDisplayCaption(ajaxObj, "ss-" + copy + id, margin + 1);
			if (childs.size() > 0)
				result += "<div id='ss-" + copy + id + "'>";
		}

		if (ajaxObj instanceof KAjaxWithChilds) {
			for (IAjaxObject ajaxObjChild : childs) {
				result += display(onlyURL, ajaxObjChild, depth, niveau, margin + 1, decalage + 1, request, copy);
			}
		}
		if (!onlyURL || (ajaxObj instanceof IHasURL))
			if (childs.size() > 0)
				result += "</div>";
		return result + "</div>";
	}

	private List<IAjaxObject> getChilds(IAjaxObject ajaxObject) {
		List<IAjaxObject> result = new ArrayList<>();
		if (ajaxObject instanceof KAjaxWithChilds) {
			if (ajaxObject instanceof KAjaxIncludeDialog) {
				result = ((KAjaxIncludeDialog) ajaxObject).getDialogChilds();
			} else {
				result = ((KAjaxWithChilds) ajaxObject).getChilds();
			}
		}
		return result;
	}

	public int getRandomId(KSOperationsBean bean) {
		int id = 0;
		do {
			Random r = new Random();
			double randomValue = 1 + (1000 - 1) * r.nextDouble();
			id = (int) Math.round(randomValue);
		} while (bean.getIds().indexOf(Integer.valueOf(id)) != -1);
		bean.getIds().add(Integer.valueOf(id));
		return id;
	}

	private void _displayKox(KSOperationsBean bean, String title) {
		bean.print("<fieldset><legend>" + title + "</legend>");
		bean.print(Ko.kcontroller().display(false));
		bean.print("</fieldset>");
	}

	private void displayKox(KSOperationsBean bean) {
		_displayKox(bean, Ko.getInstance().getValidationFile());
		bean.addJs("Forms.Utils.setInnerHTML($('copy'),'');");
		showMoxButtons(bean, false);
	}

	private void koxOp(KSOperationsBean bean) {
		bean.print("<fieldset id='otherOperations'><legend>Opérations :</legend><div id='uploadKox' class='btn'><span class='btUpload'>Importer un fichier kox.xml</span> [<span id='importedKox'>...</span>]</div><input style='display:none' accept='text/xml' type='file' id='importKox' name='importKox' " +
				"onchange=\"if(this.files[0])(new Forms.Ajax('importedKox', 'upload.frm','_inputName=koxFile&_messageMask={fileName}&_valueMask={fileName}'," +
				"function(){$get('copy','operations.main','_op=loadKoxCopy&_ajx=true&fileName='+$$('koxFile'))})).upload(this);\">");
		bean.print("<div id='op1' class='op'><div id='btTest' class='btn' style='display:none;'><span class='btTestAll'>Tester avec le nouveau kox ajouté</span></div></div>" +
				"<div id='op2' class='op'><div id='btTestChecked' class='btn' style='display:none;'><span class='btTestChecked'>Tester avec les éléments sélectionnés</span></div></div>" +
				"<div id='op3' class='op'><div id='btRestore' class='btn' style='display:none;'><span class='btRestore'>Restaurer le fichier d'origine</span></div></div>" +
				"<div id='op4' class='op'><div id='btSave' class='btn' style='display:none;'><span class='btSave'>Enregistrer les modifications</span></div></div></fieldset>");
		bean.addJs("Forms.Utils.addEventToElement($('uploadKox'),'click',function(){Forms.Utils.click($('importKox'));});");
		bean.addJs("$addEvt($('btTest'), 'click', function(){$get('original','operations.main','_op=addAllKox&_ajx=true');});");
		bean.addJs("$addEvt($('btTestChecked'), 'click', function(){$postForm('frmKoxCopy','original','operations.main','_op=addSelectedKox&_ajx=true');});");
		bean.addJs("$addEvt($('btRestore'),'click',function(){$get('original','operations.main','_op=restoreKox&_ajx=true');});");
		bean.addJs("$addEvt($('btSave'),'click',function(){$get('infos','operations.main','_op=saveKox&_ajx=true');});");
	}

	public void loadKoxCopy(KSOperationsBean bean) {
		String fileName = KRequest.GETPOST("fileName", bean.getRequest());
		if (KString.isNotNull(fileName)) {
			bean.setXmlControllers(new KXmlControllers());
			bean.getXmlControllers().loadFromFile(Ko.getPath() + "/uploadFiles/" + fileName, true);
			bean.print("<form id='frmKoxCopy' name='frmKoxCopy'>");
			bean.print("<fieldset><legend>" + fileName + "</legend>");
			bean.print(bean.getXmlControllers().display(true));
			bean.print("</form>");
			// addJs("$('fsMappings').style.height=$('fsMappingsCopy').clientHeight+'px';$('fsMappingsCopy').style.height=$('fsMappings').style.height;");
			// addJs("$('fsAjaxIncludes').style.height=$('fsAjaxIncludesCopy').clientHeight+'px';$('fsAjaxIncludesCopy').style.height=$('fsAjaxIncludes').style.height;");
			bean.addJs("Forms.Framework.opShowDifferences();");
		}
	}

	private void addAllKox(KSOperationsBean bean) {
		KXmlControllers controllers = Ko.kcontroller();
		for (Map.Entry<String, KObjectController> e : bean.getXmlControllers().getKobjectControllers().entrySet()) {
			controllers.addElement(e.getValue());
		}
		_displayKox(bean, Ko.getInstance().getValidationFile());
		bean.addJs("Forms.Framework.opShowDifferences();");
		bean.addJs("$set($('infos'),'Controllers ajoutés au fichier actuel');");
		showMoxButtons(bean, true);
	}

	private void addSelectedKox(KSOperationsBean bean) {
		KXmlControllers controllers = Ko.kcontroller();
		Enumeration<String> names = bean.getRequest().getParameterNames();
		List<IKoController> elements = bean.getXmlControllers().getAll();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			if (name.startsWith("ck-")) {
				name = name.substring(3);
				IKoController elm = bean.getXmlControllers().getElementByHashCode(name, elements);
				if (elm != null) {
					controllers.addElement(elm);
				}
			}
		}
		_displayKox(bean, Ko.getInstance().getValidationFile());
		bean.addJs("Forms.Framework.opShowDifferences();");
		bean.addJs("$set($('infos'),'Controllers sélectionnés ajoutés au fichier actuel');");
		showMoxButtons(bean, true);
	}

	private void restoreKox(KSOperationsBean bean) {
		Ko.getInstance().setValidationFile();
		_displayKox(bean, Ko.getInstance().getValidationFile());
		bean.addJs("Forms.Framework.opShowDifferences();");
		showMoxButtons(bean, false);
	}

	private void saveKox(KSOperationsBean bean) {
		KKoxPersistance koxCreator = new KKoxPersistance(Ko.kcontroller());
		String koxFile = Ko.getInstance().getValidationFile();
		koxCreator.saveAs(bean.getApplication().getRealPath("/") + "/" + koxFile);
		bean.print("Fichier [" + koxFile + "] enregistré");
		bean.addJs("Forms.Utils.show('btSave',false);");
	}
}
