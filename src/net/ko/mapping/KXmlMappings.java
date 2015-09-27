package net.ko.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.ko.framework.Ko;
import net.ko.utils.KXmlFile;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class KXmlMappings {
	private KXmlFile xmlFile;
	private static KXmlMappings instance;
	private KFilterMappings mappings;
	private KFilterMappings filters;
	private KAjaxIncludes ajaxIncludes;
	private boolean isLoaded;

	private KXmlMappings() {
		this(true);
	}

	public KXmlMappings(boolean addAction) {
		mappings = new KFilterMappings();
		filters = new KFilterMappings();
		if (addAction)
			addActionsFilter();
		ajaxIncludes = new KAjaxIncludes();
		isLoaded = false;
	}

	public KXmlMappings(String fileName) {
		this();
		loadFromFile(fileName, true);
	}

	public static void stop() {
		KXmlMappings.instance = null;
	}

	public static KXmlMappings getInstance() {
		if (KXmlMappings.instance == null)
			KXmlMappings.instance = new KXmlMappings();
		return instance;
	}

	public void loadFromFile(String fileName, boolean force) {
		if (!isLoaded || force) {
			xmlFile = new KXmlFile();
			try {
				xmlFile.loadFromFile(fileName);
				initMappings();
				isLoaded = true;
			} catch (Exception e) {
				Ko.klogger().log(Level.SEVERE, "Impossible de terminer la lecture du fichier de configuration " + fileName, e);
				isLoaded = false;
			}
		}
	}

	private void initMappings() {
		NodeList nodes = xmlFile.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("mappings")) {
				NodeList cNodes = nodes.item(i).getChildNodes();
				for (int j = 0; j < cNodes.getLength(); j++) {
					if (cNodes.item(j).getNodeName().equals("mapping")) {
						KMapping mapping = new KMapping();
						mapping.initFromXml(cNodes.item(j));
						mappings.add(mapping);
						mapping.setParentObject(mappings);
					} else if (cNodes.item(j).getNodeName().equals("virtualMapping")) {
						KMapping mapping = new KVirtualMapping();
						mapping.initFromXml(cNodes.item(j));
						mappings.add(mapping);
						mapping.setParentObject(mappings);
					}
				}
			} else if (nodes.item(i).getNodeName().equals("ajax-includes")) {
				NodeList cNodes = nodes.item(i).getChildNodes();
				for (int j = 0; j < cNodes.getLength(); j++) {
					if (cNodes.item(j).getNodeName().equals("request")) {
						KAjaxRequest ajaxRequest = new KAjaxRequest();
						ajaxRequest.initFromXml(cNodes.item(j));
						ajaxIncludes.add(ajaxRequest);
						ajaxRequest.setParentObject(ajaxIncludes);
					}
				}
			}
			else if (nodes.item(i).getNodeName().equals("filters")) {
				NodeList cNodes = nodes.item(i).getChildNodes();
				for (int j = 0; j < cNodes.getLength(); j++) {
					if (cNodes.item(j).getNodeName().equals("filter")) {
						KFilter filter = new KFilter();
						filter.initFromXml(cNodes.item(j));
						filters.add(filter);
						filter.setParentObject(filters);
					}
				}
			}
		}
	}

	private void addActionsFilter() {
		String actName = Ko.kactionsName();
		KFilter actionFilter = new KFilterForAction(actName);
		filters.add(actionFilter);
	}

	public KFilterMappings getMappings() {
		return mappings;
	}

	public KFilterMappings getFilters() {
		return filters;
	}

	public KAjaxIncludes getAjaxIncludes() {
		return ajaxIncludes;
	}

	public void addElement(IHasParentObject element) {
		if (element instanceof KFilter)
			filters.addElement((KFilter) element);
		else if (element instanceof KMapping)
			mappings.addElement((KMapping) element);
		else if (element instanceof KAjaxRequest)
			ajaxIncludes.addElement((KAjaxRequest) element);
		else
			ajaxIncludes.addElement((IAjaxObject) element);
	}

	public List<IHasParentObject> getAll() {
		List<IHasParentObject> all = new ArrayList<>();
		all.addAll(mappings.getItems());
		all.addAll(filters.getItems());
		all.addAll(ajaxIncludes.getAll());
		return all;
	}

	public IHasParentObject getElementByHashCode(String hashCode, List<IHasParentObject> elements) {
		for (IHasParentObject elm : elements) {
			String code = null;
			if (elm instanceof IAjaxObject)
				code = ((IAjaxObject) elm).getUniqueIdHash(0);
			else if (elm instanceof KAbstractFilterMapping)
				code = ((KAbstractFilterMapping) elm).getUniqueIdHash(0);
			if (code != null && hashCode.equals(code))
				return elm;
		}
		return null;
	}

	public IHasParentObject getElementByHashCode(String hashCode) {
		return getElementByHashCode(hashCode, getAll());
	}

	public KXmlFile getXmlFile() {
		return xmlFile;
	}

	public Element getMappingsNode() {
		return XmlUtils.getFirstNodeByTagName(xmlFile.getDocument(), "mappings");
	}

	public Element getFiltersNode() {
		return XmlUtils.getFirstNodeByTagName(xmlFile.getDocument(), "filters");
	}

	public Element getAjaxIncludesNode() {
		return XmlUtils.getFirstNodeByTagName(xmlFile.getDocument(), "ajax-includes");
	}

}
