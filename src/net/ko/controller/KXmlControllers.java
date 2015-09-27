package net.ko.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.ko.displays.Display;
import net.ko.framework.Ko;
import net.ko.kobject.KObject;
import net.ko.utils.KProperties;
import net.ko.utils.KXmlFile;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class KXmlControllers {
	private Map<String, KObjectController> kobjectControllers;
	private boolean isLoaded;
	private static KXmlControllers instance = null;
	private KXmlFile xmlFile;
	protected KProperties messages;
	protected KProperties er;

	public void setMessages(KProperties messages) {
		this.messages = messages;
		for (Map.Entry<String, KObjectController> entry : kobjectControllers.entrySet())
			entry.getValue().setMessages(messages);
	}

	public void setEr(KProperties er) {
		this.er = er;
		for (Map.Entry<String, KObjectController> entry : kobjectControllers.entrySet())
			entry.getValue().setEr(er);

	}

	public static KXmlControllers getInstance() {
		if (KXmlControllers.instance == null)
			KXmlControllers.instance = new KXmlControllers();
		return instance;
	}

	public KXmlControllers() {
		kobjectControllers = new HashMap<String, KObjectController>();
	}

	public void loadFromFile(String fileName, boolean force) {
		if (!isLoaded || force) {
			xmlFile = new KXmlFile();
			try {
				xmlFile.loadFromFile(fileName);
				initContollers();
				isLoaded = true;
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Impossible de lire le fichier de validation : " + fileName, e);
				isLoaded = false;
			}
		}
	}

	public void initContollers() {
		KTesterController testerCtrl = new KTesterController();
		testerCtrl.initFromXML(null);
		testerCtrl.setEr(er);
		testerCtrl.setMessages(messages);
		kobjectControllers.put("KTester", testerCtrl);
		NodeList nodes = xmlFile.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeName().equals("class")) {
				String className = ((Element) nodes.item(i)).getAttribute("name");
				if (className != null && className != "") {
					KObjectController koCtrl = getObjectController(className);
					if (koCtrl != null) {
						koCtrl.initFromXML(nodes.item(i));
						koCtrl.setEr(er);
						koCtrl.setMessages(messages);
						Ko.koDisplays.put(className, koCtrl.getDisplayInstance());
					}
				}
			}
		}
	}

	public KObjectController getObjectController(String className) {
		KObjectController koCtrl;
		if (kobjectControllers.containsKey(className))
			koCtrl = kobjectControllers.get(className);
		else {
			kobjectControllers.put(className, new KObjectController(className));
			koCtrl = kobjectControllers.get(className);
		}
		return koCtrl;
	}

	public boolean isValid(KObject ko) {
		boolean result = true;
		KObjectController koCtrl = getObjectController(ko.getClass().getSimpleName());
		if (koCtrl != null)
			result = koCtrl.isValid(ko);
		return result;
	}

	public Map<String, ArrayList<String>> messages(KObject ko) {
		Map<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		KObjectController koCtrl = getObjectController(ko.getClass().getSimpleName());
		if (koCtrl != null)
			result = koCtrl.messages(ko);
		return result;
	}

	public String toString() {
		String result = "";
		for (Map.Entry<String, KObjectController> e : kobjectControllers.entrySet()) {
			result += e.getKey() + " : " + e.getValue().toString() + Display.getDefault().getEndLigne();
		}
		return result;
	}

	public String display(boolean checkBoxes) {
		String result = "";
		for (Map.Entry<String, KObjectController> e : kobjectControllers.entrySet()) {
			if (!"KTester".equals(e.getKey()))
				result += e.getValue().getDisplayStructure(checkBoxes);
		}
		return result;
	}

	public List<IKoController> getAll() {
		List<IKoController> all = new ArrayList<>();
		for (Map.Entry<String, KObjectController> e : kobjectControllers.entrySet()) {
			if (!"KTester".equals(e.getKey())) {
				all.add(e.getValue());
				all.addAll(e.getValue().getMembers().values());
			}
		}
		return all;
	}

	public IKoController getElementByHashCode(String hashCode, List<IKoController> elements) {
		for (IKoController elm : elements) {
			String code = elm.getUniqueIdHash();
			if (code != null && hashCode.equals(code))
				return elm;
		}
		return null;
	}

	public void addElement(IKoController element) {
		if (element instanceof KObjectController) {
			KObjectController koCtrl = (KObjectController) element;
			kobjectControllers.put(koCtrl.getClassName(), koCtrl);
		} else if (element instanceof KObjectFieldController) {
			KObjectFieldController kofc = (KObjectFieldController) element;
			KObjectController koCtrl = getObjectController(kofc.getClassName());
			if (koCtrl == null)
				koCtrl = new KObjectController(kofc.getClassName());
			koCtrl.addElement(kofc);
		}
	}

	public Map<String, KObjectController> getKobjectControllers() {
		return kobjectControllers;
	}

	public KXmlFile getXmlFile() {
		return xmlFile;
	}
}
