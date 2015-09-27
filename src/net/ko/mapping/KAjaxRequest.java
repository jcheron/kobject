package net.ko.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.debug.KDebugClient;
import net.ko.framework.Ko;
import net.ko.utils.KFileUtils;
import net.ko.utils.KHash;
import net.ko.utils.KString;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KAjaxRequest extends KAbstractFilterMapping implements Cloneable {
	private List<IAjaxObject> ajaxIncludes;

	public KAjaxRequest() {
		this("");
	}

	public KAjaxRequest(String requestURL) {
		super(requestURL);
		ajaxIncludes = new ArrayList<>();
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		String requestURL = ((Element) item).getAttribute("requestURL");
		if (requestURL != null)
			this.requestURL = requestURL;
		boolean isInRootFolder = KFileUtils.isInRootFolder(requestURL);
		NodeList paramsList = item.getChildNodes();
		for (int i = 0; i < paramsList.getLength(); i++) {
			if (paramsList.item(i).getNodeName().equalsIgnoreCase("js")) {
				Element e = (Element) paramsList.item(i);
				KAjaxJs ajaxJs = new KAjaxJs();
				ajaxJs.setInRootFolder(isInRootFolder);
				ajaxJs.initFromXml(e);
				if (ajaxJs.isValid()) {
					ajaxJs.setKey(ajaxJs.hashCode() + "-" + i);
					ajaxIncludes.add(ajaxJs);
					ajaxJs.setParentObject(this);
				}
			}
		}
	}

	@Override
	public boolean matches(HttpServletRequest request) {
		return requestURLMatches(request);
	}

	@Override
	public boolean execute(HttpServletRequest request, HttpServletResponse response) {
		boolean result = false;
		boolean clientDebug = KDebugClient.isActive();
		synchronized (out) {
			out = "";
			if (ajaxIncludes.size() > 0) {
				result = true;
				out = "<script type='text/javascript'>\n";
				for (IAjaxObject ajaxInclude : ajaxIncludes) {
					out += ajaxInclude.execute(request) + "\n";
				}
				if (clientDebug) {
					String clientDebugString = "";
					for (IAjaxObject ajaxInclude : ajaxIncludes) {
						if (ajaxInclude instanceof KAjaxIncludeForm)
							Ko.klogger().log(Level.SEVERE, "INCLUDEFORM", ajaxInclude);
						if (KDebugClient.hasOption(ajaxInclude.getName()))
							clientDebugString += ajaxInclude.getDebugClientInfo(-1);
					}
					out += KString.removeCRLF(clientDebugString);
				}
				out += "</script>\n";
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return out;
	}

	public List<IAjaxObject> getAjaxIncludes() {
		return ajaxIncludes;
	}

	@Override
	public String display(int niveau) {
		String result = "<div style='margin-left:" + (20 * niveau) + "px;' class='" + getClass().getSimpleName() + "'>" + requestURL + "</div>";
		return result;
	}

	public String getUniqueIdHash(int niveau) {
		return KHash.getMD5(niveau + requestURL + "ajaxRequest");
	}

	@Override
	public boolean executeByName(String name) {
		boolean result = false;
		boolean clientDebug = KDebugClient.isActive();

		synchronized (out) {
			out = "";
			if (ajaxIncludes.size() > 0) {
				result = true;
				out = "<script type='text/javascript'>\n";
				for (IAjaxObject ajaxInclude : ajaxIncludes) {
					out += ajaxInclude.executeByName() + "\n";
				}
				if (clientDebug) {
					String clientDebugString = "";
					for (IAjaxObject ajaxInclude : ajaxIncludes) {
						if (ajaxInclude instanceof KAjaxIncludeForm)
							Ko.klogger().log(Level.SEVERE, "INCLUDEFORM", ajaxInclude);
						if (KDebugClient.hasOption(ajaxInclude.getName()))
							clientDebugString += ajaxInclude.getDebugClientInfo(-1);
					}
					out += KString.removeCRLF(clientDebugString);
				}
				out += "</script>\n";
			}
		}
		return result;
	}

	@Override
	public String displayRecursive() {
		String result = display(0);
		for (IAjaxObject ajaxObj : ajaxIncludes)
			result += ajaxObj.display(0);
		return result;
	}

	public List<IHasParentObject> getAll() {
		List<IHasParentObject> result = new ArrayList<>();
		for (IAjaxObject ajaxObject : ajaxIncludes) {
			result.add((IHasParentObject) ajaxObject);
			if (ajaxObject instanceof KAjaxWithChilds)
				result.addAll(((KAjaxWithChilds) ajaxObject).getAllChilds());
		}
		return result;
	}

	public void addFromOther(IAjaxObject ajaxObject) {
		List<IHasParentObject> all = getAll();
		IHasParentObject parent = (IHasParentObject) ((IHasParentObject) ajaxObject).getParentObject();
		if (all.indexOf(ajaxObject) != -1) {
			parent.removeElement(ajaxObject);
		}
		parent.addElement(ajaxObject);
	}

	@Override
	public boolean addElement(IAjaxObject element) {
		boolean result = ajaxIncludes.add(element);
		if (result) {
			((IHasParentObject) element).setParentObject(this);
		}
		return result;
	}

	@Override
	public boolean removeElement(IAjaxObject element) {
		return ajaxIncludes.remove(element);
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Document document = parentElement.getOwnerDocument();
		Element node = document.createElement(getName());
		parentElement.appendChild(node);
		XmlUtils.setAttribute(node, "requestURL", requestURL);
		for (IAjaxObject child : ajaxIncludes) {
			child.saveAsXML(node);
		}
		return node;
	}

	@Override
	public String getName() {
		return "request";
	}

	@Override
	public Object clone() {
		KAjaxRequest o = (KAjaxRequest) super.clone();
		o.ajaxIncludes = new ArrayList<>();
		for (IAjaxObject ajaxObject : ajaxIncludes) {
			o.ajaxIncludes.add((IAjaxObject) ((KAjaxObject) ajaxObject).clone());
		}
		return o;
	}

}
