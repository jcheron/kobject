package net.ko.check;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.http.objects.KHttpRequestWrapper;
import net.ko.http.objects.KHttpResponseStringWrapper;
import net.ko.http.servlets.KSChecks;
import net.ko.mapping.IAjaxObject;
import net.ko.mapping.IHasSelector;
import net.ko.mapping.IHasURL;
import net.ko.mapping.IMappingControl;
import net.ko.mapping.KAbstractFilterMapping;
import net.ko.mapping.KAjaxIncludes;
import net.ko.mapping.KAjaxJs;
import net.ko.mapping.KAjaxRequest;
import net.ko.mapping.KAjaxWithChilds;
import net.ko.mapping.KFilter;
import net.ko.mapping.KFilterMappings;
import net.ko.mapping.KMapping;
import net.ko.utils.KString;

public class KcheckMox extends KAbstractCheck {
	private KAjaxIncludes ajaxIncludes;
	private KFilterMappings mappings;
	private KFilterMappings filters;
	private HttpServletResponse response;
	private Map<String, String> responsesText;
	private KcheckPartList checkPartsList;
	private boolean moxFileChecked;

	public KcheckMox(HttpServletRequest request, HttpServletResponse response) {
		super(request);
		ajaxIncludes = KoHttp.kajaxIncludes();
		mappings = KoHttp.kmappings();
		filters = KoHttp.kfilters();
		this.response = response;
		responsesText = KSChecks.getResponsesText();
		checkPartsList = new KcheckPartList();
		moxFileChecked = false;
	}

	@Override
	public void checkAll() {
		if (options.contains(KCheckOptions.coMappings))
			checkMappings();
		if (options.contains(KCheckOptions.coFilters))
			checkFilters();
		if (options.contains(KCheckOptions.coAjaxIncludes))
			checkAjaxInclude();
	}

	private void checkAjaxInclude() {
		KCheckMessageWithChilds cmAjaxIncludes = messageList.addMessageWithChild(KCheckMessageType.TITLE, "Inclusions ajax");
		if (!moxFileChecked) {
			checkFile(Ko.getInstance().getMappingFile(), "Fichier contrôleur : ", cmAjaxIncludes);
			moxFileChecked = true;
		}
		for (KAbstractFilterMapping filterMapping : ajaxIncludes.getItems()) {
			if (filterMapping instanceof KAjaxRequest)
				checkOneRequest((KAjaxRequest) filterMapping, cmAjaxIncludes);
		}
		cmAjaxIncludes.setVisible(expand, true);
	}

	private void checkOneRequest(KAjaxRequest ajaxRequest, KCheckMessageWithChilds cmAjaxIncludes) {
		KCheckMessageWithChilds cmRequest = cmAjaxIncludes.addMessageWithChild(KCheckMessageType.TITLE, "request : " + ajaxRequest.getRequestURL());
		cmRequest.setId(cmAjaxIncludes.getId());
		String requestURL = ajaxRequest.getRequestURL();
		if (requestURL.contains("{#")) {
			cmRequest.addMessage(KCheckMessageType.WARNING, "URL impossible à vérifier : " + requestURL);
		} else {
			if (!KSChecks.getResponsesText().containsKey(requestURL)) {
				KAbstractFilterMapping fm = mappings.getFirstMatches(requestURL);
				if (fm != null) {
					KMapping mapping = (KMapping) fm;
					checkOneMapping(mapping, cmRequest);
				} else
					cmRequest.addMessage(KCheckMessageType.ERROR, "Aucun mapping correspondant à " + requestURL);
			}
		}
		for (IAjaxObject ajaxObject : ajaxRequest.getAjaxIncludes()) {
			checkOneAjaxJs(ajaxObject, ajaxRequest, cmRequest);
		}

	}

	private void checkOneAjaxJs(IAjaxObject ajaxObject, KAjaxRequest ajaxRequest, KCheckMessageWithChilds cmRequest) {
		if (ajaxObject instanceof KAjaxJs) {
			KAjaxJs ajaxJs = (KAjaxJs) ajaxObject;
			KCheckMessageWithChilds cmAjaxJs = cmRequest.addMessageWithChild(KCheckMessageType.TITLE, "js : " + ajaxJs.getTriggerSelector() + ".on" + ajaxJs.getTriggerEvent());
			cmAjaxJs.setId(cmRequest.getId() + "ajaxJs" + ajaxObject.getUniqueIdHash(0));
			if (checkSelector(ajaxJs.getTriggerSelector())) {
				KCheckSelector cs = new KCheckSelector(ajaxRequest.getRequestURL(), ajaxJs.getTriggerSelector(), cmAjaxJs, ajaxJs.getUniqueIdHash(0));
				checkPartsList.addItem(cs);
			} else
				cmAjaxJs.addMessage(KCheckMessageType.ERROR, "Le sélecteur " + ajaxJs.getTriggerSelector() + " contient des caractères non autorisés");

			for (IAjaxObject ajaxChildObject : ajaxJs.getChilds()) {
				checkOneAjaxInclude(ajaxChildObject, ajaxJs, ajaxRequest, cmAjaxJs, true);
			}
		}

	}

	private void checkOneAjaxInclude(IAjaxObject ajaxChildObject, KAjaxJs ajaxJs, KAjaxRequest ajaxRequest, KCheckMessageWithChilds cmAjaxJs, boolean directJsChild) {
		KCheckMessageWithChilds cmAjaxChildObject = cmAjaxJs.addMessageWithChild(KCheckMessageType.TITLE, ajaxChildObject.getName() + " : " + ajaxChildObject.getDisplayCaption());
		cmAjaxChildObject.setId(cmAjaxJs.getId() + ajaxChildObject.getUniqueIdHash(0));
		if (("body".equals(ajaxJs.getTriggerSelector()) && "load".equals(ajaxJs.getTriggerEvent())) || directJsChild) {
			if (ajaxChildObject instanceof IHasSelector) {
				String selector = ((IHasSelector) ajaxChildObject).getDOMSelector();
				if (KString.isNotNull(selector) && !selector.contains("{")) {
					if (checkSelector(selector)) {
						KCheckSelector cs = new KCheckSelector(ajaxRequest.getRequestURL(), selector, cmAjaxChildObject, ajaxChildObject.getUniqueIdHash(0));
						checkPartsList.addItem(cs);
					} else
						cmAjaxChildObject.addMessage(KCheckMessageType.ERROR, "Le sélecteur " + selector + " contient des caractères non autorisés");
				}
			}
		} else
			cmAjaxChildObject.addMessage(KCheckMessageType.UNKNOWN, "Sélecteur Impossible à vérifier");

		if (ajaxChildObject instanceof IHasURL) {
			String requestURL = ((IHasURL) ajaxChildObject).getURL();
			if (requestURL != null && !requestURL.contains("{")) {
				if (!KSChecks.getResponsesText().containsKey(requestURL)) {
					KAbstractFilterMapping fm = mappings.getFirstMatches(requestURL);
					if (fm != null) {
						KMapping mapping = (KMapping) fm;
						checkOneMapping(mapping, cmAjaxChildObject);
					} else
						cmAjaxChildObject.addMessage(KCheckMessageType.ERROR, "Aucun mapping correspondant à " + requestURL);
				}
			} else
				cmAjaxChildObject.addMessage(KCheckMessageType.UNKNOWN, "URL Impossible à vérifier " + requestURL);
		}
		if (ajaxChildObject instanceof KAjaxWithChilds) {
			KAjaxWithChilds ajaxWithChilds = (KAjaxWithChilds) ajaxChildObject;
			for (IAjaxObject child : ajaxWithChilds.getChilds()) {
				checkOneAjaxInclude(child, ajaxJs, ajaxRequest, cmAjaxChildObject, false);
			}
		}
	}

	protected void checkMappings() {
		KCheckMessageWithChilds cmMappings = messageList.addMessageWithChild(KCheckMessageType.TITLE, "Mappings");
		if (!moxFileChecked) {
			checkFile(Ko.getInstance().getMappingFile(), "Fichier contrôleur : ", cmMappings);
			moxFileChecked = true;
		}
		for (KAbstractFilterMapping filterMapping : mappings.getItems()) {
			if (filterMapping instanceof KMapping) {
				checkOneMapping((KMapping) filterMapping, cmMappings);
			}
		}
		cmMappings.setVisible(expand, true);
	}

	protected void checkFilters() {
		KCheckMessageWithChilds cmFilters = messageList.addMessageWithChild(KCheckMessageType.TITLE, "Filters");
		if (!moxFileChecked) {
			checkFile(Ko.getInstance().getMappingFile(), "Fichier contrôleur : ", cmFilters);
			moxFileChecked = true;
		}
		for (KAbstractFilterMapping filterMapping : filters.getItems()) {
			if (filterMapping instanceof KFilter) {
				checkOneFilter((KFilter) filterMapping, cmFilters);
			}
		}
		cmFilters.setVisible(expand, true);
	}

	protected void checkOneMapping(KMapping mapping, KCheckMessageWithChilds cmMappings) {
		KCheckMessageWithChilds cmMapping = cmMappings.addMessageWithChild(KCheckMessageType.INFO, mapping.getRequestURL() + "(" + mapping.getMethod() + ")");
		cmMapping.setId(cmMappings.getId() + mapping.getUniqueIdHash(0));
		if (!mapping.getRequestURL().endsWith(".do"))
			cmMapping.addMessage(KCheckMessageType.ERROR, "Le mapping doit être en *.do");
		addInfo(cmMapping, "Paramètres de request : ", mapping.getParameters().toString());
		addInfo(cmMapping, "Methode : ", mapping.getMethod());
		addInfo(cmMapping, "Paramètres de response : ", mapping.getQueryString());
		cmMapping.addInfoMessage("ResponseURL : " + mapping.getResponseURL());
		addInfo(cmMapping, "DefaultTargetId : ", mapping.getDefaultTargetId());
		String responseURL = mapping.getResponseURL();
		if (responseURL != null && !(responseURL.contains("$") || responseURL.contains("#"))) {
			String realPath = request.getServletContext().getRealPath(responseURL);
			File f = new File(realPath);
			if (!f.exists())
				cmMapping.addMessage(KCheckMessageType.WARNING, "L'URL de réponse n'existe pas sur le serveur");

			try {
				String responseText = getResponseText(mapping.getRequestURL(), responseURL, mapping.getRequestParameters());
				if (KString.isNull(responseText))
					cmMapping.addMessage(KCheckMessageType.WARNING, "La réponse est vide");
			} catch (ServletException | IOException e) {
				cmMapping.addMessage(KCheckMessageType.ERROR, e.getMessage());
			}
		}
		String classControl = mapping.getClassControl();
		if (KString.isNotNull(classControl)) {
			try {
				Class<?> clazzControl = Class.forName(classControl);
				if (IMappingControl.class.isAssignableFrom(clazzControl)) {
					cmMapping.addInfoMessage("classControl : " + classControl);
				} else
					cmMapping.addMessage(KCheckMessageType.ERROR, "classControl : " + classControl + " doit implémenter IMappingControl");
			} catch (ClassNotFoundException e) {
				cmMapping.addMessage(KCheckMessageType.ERROR, "classControl : " + e.getMessage());
			}
		}
		if (mapping.isMainControl()) {
			String mainControlClassName = Ko.getInstance().getMainControlClassName();
			if (KString.isNotNull(mainControlClassName)) {
				IMappingControl mainControlInstance = Ko.getInstance().getMainControlInstance();
				if (mainControlInstance != null)
					cmMapping.addInfoMessage("mainControl : " + mainControlClassName);
				else
					cmMapping.addMessage(KCheckMessageType.ERROR, "mainControl : " + mainControlClassName + " non instanciée");
			} else
				cmMapping.addMessage(KCheckMessageType.ERROR, "mainControl : le fichier de configuration config.ko ne définit pas controlClass");
		}
	}

	protected void checkOneFilter(KFilter filter, KCheckMessageWithChilds cmFilters) {
		String requestURL = filter.getRequestURL();
		String responseURL = filter.getResponseURL();
		KCheckMessageWithChilds cmFilter = cmFilters.addMessageWithChild(KCheckMessageType.INFO, "filter : " + requestURL);
		cmFilter.setId(filter.getUniqueIdHash(0));
		if (!requestURL.endsWith(".do"))
			cmFilter.addMessage(KCheckMessageType.ERROR, "RequestURL doit être en *.do");
		if (!responseURL.endsWith(".do"))
			cmFilter.addMessage(KCheckMessageType.ERROR, "ResponseURL doit être en *.do");
		addInfo(cmFilter, "QueryString : ", filter.getQueryString());
		if (responseURL.contains("$"))
			cmFilter.addMessage(KCheckMessageType.UNKNOWN, "Filter impossible à vérifier");
		else {
			KAbstractFilterMapping f = filters.getFirstMatches(responseURL);
			if (f instanceof KFilter) {
				cmFilter.addMessage(KCheckMessageType.WARNING, "Le filtre correspond à un autre filtre : " + f.getRequestURL());
			}
			KAbstractFilterMapping fm = mappings.getFirstMatches(responseURL);
			if (fm instanceof KMapping) {
				KMapping mapping = (KMapping) fm;
				checkOneMapping(mapping, cmFilter);
			} else
				cmFilter.addMessage(KCheckMessageType.WARNING, "Aucun mapping correspondant à l'url de réponse " + responseURL);
		}
	}

	protected String getResponseText(String requestURL, String url, String parameters) throws ServletException, IOException {
		String result = null;
		if (responsesText.containsKey(requestURL))
			result = responsesText.get(requestURL);
		else {
			result = includeResponse(url, request, response, parameters, "GET");
			if (result != null)
				responsesText.put(requestURL, result);
		}
		return result;
	}

	public static String includeResponse(String includePage, HttpServletRequest request, HttpServletResponse response, String params, String method) throws ServletException, IOException {
		String result = "";
		try {
			KHttpResponseStringWrapper resp = new KHttpResponseStringWrapper(response);
			if (params != null) {
				KHttpRequestWrapper newRequest = KHttpRequestWrapper.setParameter(request, params, method);
				request.getRequestDispatcher(includePage).include(newRequest, resp);
			} else
				request.getRequestDispatcher(includePage).include(request, resp);
			result = resp.getOutput();
		} catch (Exception e) {
			result = new KCheckMessage(KCheckMessageType.WARNING, e.getMessage()).toString();
		}
		return result;
	}

	public KcheckPartList getCheckSelectors() {
		return checkPartsList;
	}

	@Override
	public boolean isActive() {
		return options.contains(KCheckOptions.coAjaxIncludes) || options.contains(KCheckOptions.coFilters) || options.contains(KCheckOptions.coMappings);
	}

	protected boolean checkSelector(String selector) {
		String[] notGood = new String[] { "\\", "/" };
		if (selector == null)
			return false;
		for (String s : notGood) {
			if (selector.contains(s))
				return false;
		}
		return true;

	}
}
