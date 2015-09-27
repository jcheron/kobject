package net.ko.http.servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.controller.IKoController;
import net.ko.controller.KObjectController;
import net.ko.controller.KXmlControllers;
import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KRequest;
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
@WebServlet(name = "KSOperation", urlPatterns = { "/operations.main" })
public class KSOperation extends KSAbstractMain {
	private static final long serialVersionUID = 1L;
	private boolean onlyURL = false;
	private KXmlMappings xmlMappings;
	private KXmlControllers xmlControllers;
	private String activeFile;
	private KProperties originalProperties;
	private KProperties copyProperties;
	ArrayList<String> urls;
	ArrayList<Integer> ids;
	private String fileType = "Css";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSOperation() {
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
			init(request, response);
			js = "";
			String operation = KRequest.GETPOST("_op", request, "all");
			switch (operation) {
			case "all":
				all();
				break;
			case "mox":
				displayMox();
				break;
			case "moxOp":
				moxOp();
				break;
			case "loadMoxCopy":
				loadMoxCopy();
				break;
			case "addAllMox":
				addAllMox();
				break;
			case "addSelectedMox":
				addSelectedMox();
				break;
			case "restoreMox":
				restoreMox();
				break;
			case "saveMox":
				saveMox();
				break;

			case "kox":
				displayKox();
				break;

			case "koxOp":
				koxOp();
				break;

			case "loadKoxCopy":
				loadKoxCopy();
				break;

			case "addAllKox":
				addAllKox();
				break;
			case "addSelectedKox":
				addSelectedKox();
				break;
			case "restoreKox":
				restoreKox();
				break;
			case "saveKox":
				saveKox();
				break;

			case "opProperties":
				opProperties();
				break;

			case "loadFile":
				loadFile();
				break;

			case "loadFileCopy":
				loadFileCopy();
				break;

			case "addAllProperties":
				addAllProperties();
				break;
			case "addSelectedProperties":
				addSelectedProperties();
				break;
			case "restoreProperties":
				restoreProperties();
				break;
			case "saveProperties":
				saveProperties();
				break;
			default:
				all();
				break;
			}
			printJs();
			try {
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void saveProperties() {
		try {
			originalProperties.save();
			out.print(originalProperties.getFileName() + " enregistré");
		} catch (IOException e) {
			out.print("Impossible d'enregistrer le fichier " + originalProperties.getFileName() + " " + e.getMessage());
		}
		addJs("Forms.Utils.show('btSave',false);");
	}

	private void restoreProperties() {
		reloadProperties();
	}

	private void addSelectedProperties() {
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			if (name.startsWith("ck-")) {
				name = name.substring(3);
				if (KString.isNotNull(name)) {
					originalProperties.add(name, copyProperties.getProperty(name));
				}
			}
		}
		updateProperties();

	}

	private void addAllProperties() {
		originalProperties.addAll(copyProperties);
		updateProperties();
	}

	private void updateProperties() {
		switch (fileType) {
		case "Css":
			Ko.getInstance().loadCssVars(originalProperties);
			break;
		case "Messages":
			Ko.getInstance().setKpMessages(originalProperties);
			break;
		default:
			Ko.getInstance().setKpERs(originalProperties);
			break;
		}
		out.print("<fieldset><legend>Fichier " + fileType + "</legend>");
		displayProperties(originalProperties, false);
		out.print("</fieldset>");
		addJs("Forms.Framework.opShowDifferences();");
		showMoxButtons(true);
	}

	private void reloadProperties() {
		try {
			boolean isCss = "Css".equals(fileType);
			if (isCss)
				KoHttp.krestart(application, KRestartType.rtCssVars.value);
			else
				KoHttp.krestart(application, KRestartType.rtMessages.value);
			_loadFile();
			addJs("Forms.Framework.opShowDifferences();");
			showMoxButtons(false);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void _loadFile() {
		fileType = KRequest.GETPOST("fileType", request, fileType);
		activeFile = KRequest.GETPOST("file", request, activeFile);
		out.print("<fieldset><legend>Fichier " + fileType + "</legend>");
		displayFile(application.getRealPath(activeFile), false);
		out.print("</legend>");
	}

	private void loadFile() {
		_loadFile();
		addJs("$set($('fileType'),'" + fileType + "');");
		addJs("Forms.Utils.setInnerHTML($('copy'),'');");
		addJs("Forms.Utils.setInnerHTML($('infos'),'');");
		addJs("Forms.Utils.show('btTest',false);");
		addJs("Forms.Utils.show('btTestChecked',false);");
		addJs("Forms.Utils.show('btRestore',false);");
		addJs("Forms.Utils.show('btSave',false);");
	}

	private void loadFileCopy() {
		String fileName = KRequest.GETPOST("fileName", request);
		out.print("<fieldset><legend>" + fileName + "</legend>");
		out.print("<form id='frmPropertiesCopy' name='frmPropertiesCopy'>");
		displayFile(Ko.getPath() + "/uploadFiles/" + fileName, true);
		out.print("</form>");
		out.print("</legend>");
		addJs("Forms.Framework.opShowDifferences();");
	}

	private void opProperties() {
		KStrings files = new KStrings();
		files.put(Ko.getInstance().getCssFile(), "Css");
		files.put(Ko.getInstance().getMessagesFile(), "Messages");
		files.put(Ko.getInstance().getErFile(), "Expressions Régulières");
		KHtmlFieldControl ctrlFiles = new KHtmlFieldControl("Fichiers properties", "Css", "files", "files", HtmlControlType.khcRadioList, files, "");
		ctrlFiles.setRequired(true);
		ctrlFiles.setValue(Ko.getInstance().getCssFile());
		out.print(ctrlFiles);
		out.print("<fieldset id='otherOperations'><legend>Opérations :</legend><div id='uploadProperties' class='btn'><span class='btUpload'>Importer un fichier <span id='fileType'></span></span> [<span id='importedProperties'>...</span>]</div><input style='display:none' accept='text/x-java-properties' type='file' id='importProperties' name='importProperties' " +
				"onchange=\"if(this.files[0])(new Forms.Ajax('importedProperties', 'upload.frm','_inputName=propertiesFile&_messageMask={fileName}&_valueMask={fileName}'," +
				"function(){$get('copy','operations.main','_op=loadFileCopy&_ajx=true&fileName='+$$('propertiesFile'))})).upload(this);\">");
		out.print("<div id='op1' class='op'><div id='btTest' class='btn' style='display:none;'><span class='btTestAll'>Tester avec le nouveau fichier ajouté</span></div></div>" +
				"<div id='op2' class='op'><div id='btTestChecked' class='btn' style='display:none;'><span class='btTestChecked'>Tester avec les éléments sélectionnés</span></div></div>" +
				"<div id='op3' class='op'><div id='btRestore' class='btn' style='display:none;'><span class='btRestore'>Restaurer le fichier d'origine</span></div></div>" +
				"<div id='op4' class='op'><div id='btSave' class='btn' style='display:none;'><span class='btSave'>Enregistrer les modifications</span></div></div></fieldset>");
		addJs("Forms.Utils.addEventToElement($('uploadProperties'),'click',function(){Forms.Utils.click($('importProperties'));});");
		addJs("$addEvt($('btTest'), 'click', function(){$get('original','operations.main','_op=addAllProperties&_ajx=true');});");
		addJs("$addEvt($('btTestChecked'), 'click', function(){$postForm('frmPropertiesCopy','original','operations.main','_op=addSelectedProperties&_ajx=true');});");
		addJs("$addEvt($('btRestore'),'click',function(){$get('original','operations.main','_op=restoreProperties&_ajx=true');});");
		addJs("$addEvt($('btSave'),'click',function(){$get('infos','operations.main','_op=saveProperties&_ajx=true');});");
		addJs("$get('original','operations.main','_op=loadFile&file=" + Ko.getInstance().getCssFile() + "&fileType=Css&_ajx=true');");
		addJs("$addEvt($('files'),'change',function(){$get('original','operations.main','_op=loadFile&file='+this.value+'&fileType='+this.valueText+'&_ajx=true');});");
	}

	private void displayFile(String fileName, boolean checkBoxes) {
		try {
			KProperties kp = openFile(fileName);
			if (checkBoxes)
				copyProperties = kp;
			else
				originalProperties = kp;
			displayProperties(kp, checkBoxes);
		} catch (IOException e) {
		}
	}

	private KProperties openFile(String fileName) throws IOException {
		KProperties kp = new KProperties();
		kp.loadFromFile(fileName);
		return kp;
	}

	private void displayProperties(KProperties kproperties, boolean checkBoxes) {
		String copy = "";
		String result = "";
		CssVars cssVars = null;
		boolean isCss = "Css".equals(fileType);
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
		out.print(result);
	}

	private void saveMox() {
		KMoxPersistance moxCreator = new KMoxPersistance(KXmlMappings.getInstance());
		String moxFile = Ko.getInstance().getMappingFile();
		moxCreator.saveAs(application.getRealPath("/") + "/" + moxFile);
		out.print("Fichier [" + moxFile + "] enregistré");
		addJs("Forms.Utils.show('btSave',false);");
	}

	private void restoreMox() {
		KXmlMappings.stop();
		Ko.getInstance().setMappingFile();
		_displayMox();
		addJs("Forms.Framework.opShowDifferences();");
		showMoxButtons(false);
	}

	private void addSelectedMox() {
		Enumeration<String> names = request.getParameterNames();
		List<IHasParentObject> elements = xmlMappings.getAll();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			if (name.startsWith("ck-")) {
				name = name.substring(3);
				IHasParentObject elm = xmlMappings.getElementByHashCode(name, elements);
				if (elm != null) {
					KXmlMappings.getInstance().addElement(elm);
				}
			}
		}
		_displayMox();
		addJs("Forms.Framework.opShowDifferences();");
		addJs("$set($('infos'),'Mappings sélectionnés ajoutés au fichier actuel');");
		showMoxButtons(true);
	}

	private void addAllMox() {
		for (KAbstractFilterMapping mapping : xmlMappings.getMappings().getItems()) {
			Ko.kmappings().addElement(mapping);
		}
		for (KAbstractFilterMapping ajaxInclude : xmlMappings.getAjaxIncludes().getItems()) {
			KoHttp.kajaxIncludes().addElement(ajaxInclude);
		}
		_displayMox();
		addJs("Forms.Framework.opShowDifferences();");
		addJs("$set($('infos'),'Mappings ajoutés au fichier actuel');");
		showMoxButtons(true);
	}

	private void moxOp() {
		out.print("<fieldset id='otherOperations'><legend>Opérations :</legend><div id='uploadMox' class='btn'><span class='btUpload'>Importer un fichier mox.xml</span> [<span id='importedMox'>...</span>]</div><input style='display:none' accept='text/xml' type='file' id='importMox' name='importMox' " +
				"onchange=\"if(this.files[0])(new Forms.Ajax('importedMox', 'upload.frm','_inputName=moxFile&_messageMask={fileName}&_valueMask={fileName}'," +
				"function(){$get('copy','operations.main','_op=loadMoxCopy&_ajx=true&fileName='+$$('moxFile'))})).upload(this);\">");
		out.print("<div id='op1' class='op'><div id='btTest' class='btn' style='display:none;'><span class='btTestAll'>Tester avec le nouveau mox ajouté</span></div></div>" +
				"<div id='op2' class='op'><div id='btTestChecked' class='btn' style='display:none;'><span class='btTestChecked'>Tester avec les éléments sélectionnés</span></div></div>" +
				"<div id='op3' class='op'><div id='btRestore' class='btn' style='display:none;'><span class='btRestore'>Restaurer le fichier d'origine</span></div></div>" +
				"<div id='op4' class='op'><div id='btSave' class='btn' style='display:none;'><span class='btSave'>Enregistrer les modifications</span></div></div></fieldset>");
		addJs("Forms.Utils.addEventToElement($('uploadMox'),'click',function(){Forms.Utils.click($('importMox'));});");
		addJs("$addEvt($('btTest'), 'click', function(){$get('original','operations.main','_op=addAllMox&_ajx=true');});");
		addJs("$addEvt($('btTestChecked'), 'click', function(){$postForm('frmMoxCopy','original','operations.main','_op=addSelectedMox&_ajx=true');});");
		addJs("$addEvt($('btRestore'),'click',function(){$get('original','operations.main','_op=restoreMox&_ajx=true');});");
		addJs("$addEvt($('btSave'),'click',function(){$get('infos','operations.main','_op=saveMox&_ajx=true');});");
	}

	private void showMoxButtons(boolean visible) {
		addJs("Forms.Utils.show('btRestore'," + visible + ");");
		addJs("Forms.Utils.show('btSave'," + visible + ");");
	}

	public void all() {
		urls = new ArrayList<>();
		ids = new ArrayList<>();
		if (!isAjax)
			out.print(getPageHeader("admin.kcss"));
		KoHttp.kstart(request.getServletContext());
		out.print("<div id='_commandes'><fieldset><legend>Imports</legend><div id='infos'></div><div class='boxButtons'><input type='button' class='btn' id='opKox' value='Validation & classes (kox.xml)'>" +
				"<input type='button' class='btn' id='opMox' value='Mappings, Filters & ajax-includes (mox.xml)'>" +
				"<input type='button' class='btn' id='opProperties' value='Fichiers properties'></div></fieldset></div>");
		out.print("<div id='siteMap'><table><tr><td id='original'></td><td id='copy'></td><td id='operations'></tr></table></div>");
		addJs("var operations=function(e){var target=$gte(e);" +
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
		addJs("new Forms.Selector('#_commandes input[type=button]','click','false',0,operations,{'border':'none','fontWeight':'bolder','background':'transparent','boxShadow':'none'});");
		if (!isAjax)
			out.print(getPageFooter());
	}

	public void loadMoxCopy() {
		String fileName = KRequest.GETPOST("fileName", request);
		if (KString.isNotNull(fileName)) {
			xmlMappings = new KXmlMappings(Ko.getPath() + "/uploadFiles/" + fileName);
			out.print("<form id='frmMoxCopy' name='frmMoxCopy'><fieldset id='fsMappingsCopy'><legend>Mappings/filters</legend>");
			out.print(display(xmlMappings.getMappings(), "cp-"));
			out.print("</fieldset>");
			out.print("<fieldset id='fsAjaxIncludesCopy'><legend>ajax-includes</legend>");
			out.print(display(xmlMappings.getAjaxIncludes(), "cp-"));
			out.print("</fieldset></form>");
			addJs("$('fsMappings').style.height=$('fsMappingsCopy').clientHeight+'px';$('fsMappingsCopy').style.height=$('fsMappings').style.height;");
			addJs("$('fsAjaxIncludes').style.height=$('fsAjaxIncludesCopy').clientHeight+'px';$('fsAjaxIncludesCopy').style.height=$('fsAjaxIncludes').style.height;");
			addJs("Forms.Framework.opShowDifferences();");
		}
	}

	private void _displayMox() {
		KAjaxIncludes ajaxIncludes = KoHttp.kajaxIncludes();
		KFilterMappings mappings = KoHttp.kmappings();
		out.print("<fieldset id='fsMappings'><legend>Mappings/filters</legend>");
		out.print(display(mappings, ""));
		out.print("</fieldset>");
		out.print("<fieldset id='fsAjaxIncludes'><legend>ajax-includes</legend>");
		out.print(display(ajaxIncludes, ""));
		out.print("</fieldset>");
	}

	public void displayMox() {
		_displayMox();
		addJs("Forms.Utils.setInnerHTML($('copy'),'');");
		showMoxButtons(false);
	}

	public String display(KAjaxIncludes ajaxIncludes, String copy) {
		String result = "";
		for (KAbstractFilterMapping filterMapping : ajaxIncludes.getItems()) {
			if (filterMapping instanceof KAjaxRequest) {
				KAjaxRequest ajaxReq = (KAjaxRequest) filterMapping;
				result += display(ajaxReq, 0, 0, 0, request, copy);
				urls.add(ajaxReq.getRequestURL());
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

	public String display(KAjaxRequest ajaxReq, int depth, int niveau, int margin, HttpServletRequest request, String copy) {
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
					result += display(ajaxObj, depth, niveau, margin + 1, 0, request, copy);
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

	public String display(IAjaxObject ajaxObj, int depth, int niveau, int margin, int decalage, HttpServletRequest request, String copy) {
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
				result += display(ajaxObjChild, depth, niveau, margin + 1, decalage + 1, request, copy);
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

	public int getRandomId() {
		int id = 0;
		do {
			Random r = new Random();
			double randomValue = 1 + (1000 - 1) * r.nextDouble();
			id = (int) Math.round(randomValue);
		} while (ids.indexOf(Integer.valueOf(id)) != -1);
		ids.add(Integer.valueOf(id));
		return id;
	}

	private void _displayKox(String title) {
		out.print("<fieldset><legend>" + title + "</legend>");
		out.print(Ko.kcontroller().display(false));
		out.print("</fieldset>");
	}

	private void displayKox() {
		_displayKox(Ko.getInstance().getValidationFile());
		addJs("Forms.Utils.setInnerHTML($('copy'),'');");
		showMoxButtons(false);
	}

	private void koxOp() {
		out.print("<fieldset id='otherOperations'><legend>Opérations :</legend><div id='uploadKox' class='btn'><span class='btUpload'>Importer un fichier kox.xml</span> [<span id='importedKox'>...</span>]</div><input style='display:none' accept='text/xml' type='file' id='importKox' name='importKox' " +
				"onchange=\"if(this.files[0])(new Forms.Ajax('importedKox', 'upload.frm','_inputName=koxFile&_messageMask={fileName}&_valueMask={fileName}'," +
				"function(){$get('copy','operations.main','_op=loadKoxCopy&_ajx=true&fileName='+$$('koxFile'))})).upload(this);\">");
		out.print("<div id='op1' class='op'><div id='btTest' class='btn' style='display:none;'><span class='btTestAll'>Tester avec le nouveau kox ajouté</span></div></div>" +
				"<div id='op2' class='op'><div id='btTestChecked' class='btn' style='display:none;'><span class='btTestChecked'>Tester avec les éléments sélectionnés</span></div></div>" +
				"<div id='op3' class='op'><div id='btRestore' class='btn' style='display:none;'><span class='btRestore'>Restaurer le fichier d'origine</span></div></div>" +
				"<div id='op4' class='op'><div id='btSave' class='btn' style='display:none;'><span class='btSave'>Enregistrer les modifications</span></div></div></fieldset>");
		addJs("Forms.Utils.addEventToElement($('uploadKox'),'click',function(){Forms.Utils.click($('importKox'));});");
		addJs("$addEvt($('btTest'), 'click', function(){$get('original','operations.main','_op=addAllKox&_ajx=true');});");
		addJs("$addEvt($('btTestChecked'), 'click', function(){$postForm('frmKoxCopy','original','operations.main','_op=addSelectedKox&_ajx=true');});");
		addJs("$addEvt($('btRestore'),'click',function(){$get('original','operations.main','_op=restoreKox&_ajx=true');});");
		addJs("$addEvt($('btSave'),'click',function(){$get('infos','operations.main','_op=saveKox&_ajx=true');});");
	}

	public void loadKoxCopy() {
		String fileName = KRequest.GETPOST("fileName", request);
		if (KString.isNotNull(fileName)) {
			xmlControllers = new KXmlControllers();
			xmlControllers.loadFromFile(Ko.getPath() + "/uploadFiles/" + fileName, true);
			out.print("<form id='frmKoxCopy' name='frmKoxCopy'>");
			out.print("<fieldset><legend>" + fileName + "</legend>");
			out.print(xmlControllers.display(true));
			out.print("</form>");
			// addJs("$('fsMappings').style.height=$('fsMappingsCopy').clientHeight+'px';$('fsMappingsCopy').style.height=$('fsMappings').style.height;");
			// addJs("$('fsAjaxIncludes').style.height=$('fsAjaxIncludesCopy').clientHeight+'px';$('fsAjaxIncludesCopy').style.height=$('fsAjaxIncludes').style.height;");
			addJs("Forms.Framework.opShowDifferences();");
		}
	}

	private void addAllKox() {
		KXmlControllers controllers = Ko.kcontroller();
		for (Map.Entry<String, KObjectController> e : xmlControllers.getKobjectControllers().entrySet()) {
			controllers.addElement(e.getValue());
		}
		_displayKox(Ko.getInstance().getValidationFile());
		addJs("Forms.Framework.opShowDifferences();");
		addJs("$set($('infos'),'Controllers ajoutés au fichier actuel');");
		showMoxButtons(true);
	}

	private void addSelectedKox() {
		KXmlControllers controllers = Ko.kcontroller();
		Enumeration<String> names = request.getParameterNames();
		List<IKoController> elements = xmlControllers.getAll();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			if (name.startsWith("ck-")) {
				name = name.substring(3);
				IKoController elm = xmlControllers.getElementByHashCode(name, elements);
				if (elm != null) {
					controllers.addElement(elm);
				}
			}
		}
		_displayKox(Ko.getInstance().getValidationFile());
		addJs("Forms.Framework.opShowDifferences();");
		addJs("$set($('infos'),'Controllers sélectionnés ajoutés au fichier actuel');");
		showMoxButtons(true);
	}

	private void restoreKox() {
		Ko.getInstance().setValidationFile();
		_displayKox(Ko.getInstance().getValidationFile());
		addJs("Forms.Framework.opShowDifferences();");
		showMoxButtons(false);
	}

	private void saveKox() {
		KKoxPersistance koxCreator = new KKoxPersistance(Ko.kcontroller());
		String koxFile = Ko.getInstance().getValidationFile();
		koxCreator.saveAs(application.getRealPath("/") + "/" + koxFile);
		out.print("Fichier [" + koxFile + "] enregistré");
		addJs("Forms.Utils.show('btSave',false);");
	}
}
