package net.ko.mapping;

import java.util.ArrayList;
import java.util.List;

import net.ko.design.KCssTransition;
import net.ko.utils.KString;

import org.w3c.dom.Element;

public abstract class KAjaxWithChilds extends KAjaxObject {
	protected List<IAjaxObject> childs;
	protected String targetFunction;
	protected String clientDebugString = "";

	public KAjaxWithChilds() {
		super();
		targetFunction = "";
		childs = new ArrayList<>();
	}

	public String getFunctions() {
		String result = "";
		String sep = "";
		int nb = 0;
		for (int i = 0; i < childs.size() - 1; i++) {
			IAjaxObject child = childs.get(i);
			result += sep + child.toString();
			sep = ",";
			nb++;
		}
		if (childs.size() > 0) {
			result += sep + childs.get(childs.size() - 1).toString();
			nb++;
			sep = ",";
		}

		if (!"".equals(targetFunction)) {
			result += sep + targetFunction;
			sep = ",";
			nb++;
		}
		if (this instanceof IHasTransition) {
			IHasTransition trans = (IHasTransition) this;
			if (KString.isNotNull(trans.getTransition())) {
				result += sep + KCssTransition.preparedTransition(trans.getTransitionId(), trans.getTransition());
				nb++;
			}
		}
		if (nb > 1)
			result = "new Array(" + result + ")";
		return result;
	}

	public List<IAjaxObject> getChilds() {
		return childs;
	}

	public String displayChilds(int niveau) {
		String result = "";
		for (IAjaxObject obj : childs) {
			result += obj.display(niveau);
		}
		return result;
	}

	public ArrayList<String> getURLs() {
		ArrayList<String> result = new ArrayList<>();
		for (IAjaxObject obj : childs) {
			if (obj instanceof IHasURL)
				result.add(((IHasURL) obj).getURL());
		}
		return result;
	}

	@Override
	public boolean hasChilds() {
		return super.hasChilds() || getURLs().size() > 0;
	}

	@Override
	public String getClientDebugString() {
		return clientDebugString;
	}

	@Override
	public String getChildsDebugClientInfo() {
		clientDebugString = "";
		int i = 0;
		for (IAjaxObject child : childs) {
			clientDebugString += child.getDebugClientInfo(i);
			i++;
		}
		return clientDebugString;
	}

	@Override
	public String getDebugClientInfo(int index) {
		String result = super.getDebugClientInfo(index);
		String cdInfo = getChildsDebugClientInfo();
		String tmp = cdInfo.replace("%visibility%", "typeof event != 'undefined'?event.target.areChildsVisible:false");
		String parent = getDebugClientId().replaceAll("\\W", "") + "-infoBulle-" + getName() + getKey();
		tmp = tmp.replace("%parent%", ",'" + parent + "'");
		tmp += "$addUEvt($('" + parent + "'),'click',function(e){event=Forms.Private.getEvent(e);if(event.ctrlKey==1){ Forms.Utils.stopPropagation(event);event.target.areChildsVisible=event.target.areChildsVisible?false:true;" + cdInfo.replace("%visibility%", "event.target.areChildsVisible").replace("%parent%", "") + "}});";
		return result + tmp;
	}

	@Override
	public void setBreakPoint(boolean breakPoint) {
		super.setBreakPoint(breakPoint);
		for (IAjaxObject child : childs) {
			child.setBreaked(breakPoint);
		}
	}

	@Override
	public void setBreaked(boolean breaked) {
		super.setBreaked(breaked);
		for (IAjaxObject child : childs) {
			child.setBreaked(breaked);
		}
	}

	public abstract String createMyOwnFunction(String childFunctions);

	public void addChild(KAjaxObject child) {
		child.setParentObject(this);
		childs.add(child);
	}

	public void removeChild(IAjaxObject child) {
		childs.remove(child);
	}

	public void setChilds(List<IAjaxObject> childs) {
		this.childs = childs;
	}

	@Override
	public String displayRecursive() {
		String result = display(0);
		for (IAjaxObject ajaxObj : childs) {
			result += ajaxObj.displayRecursive();
		}
		return result;
	}

	public List<IHasParentObject> getAllChilds() {
		List<IHasParentObject> all = new ArrayList<>();
		for (IAjaxObject ajaxObject : childs) {
			all.add((IHasParentObject) ajaxObject);
			if (ajaxObject instanceof KAjaxWithChilds)
				all.addAll(((KAjaxWithChilds) ajaxObject).getAllChilds());
		}
		return all;
	}

	@Override
	public boolean addElement(IAjaxObject element) {
		boolean result = childs.add(element);
		if (result) {
			((IHasParentObject) element).setParentObject(this);
		}
		return result;
	}

	@Override
	public boolean removeElement(IAjaxObject element) {
		return childs.remove(element);
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		for (IAjaxObject child : childs) {
			child.saveAsXML(result);
		}
		return result;
	}

	@Override
	public Object clone() {
		KAjaxWithChilds o = (KAjaxWithChilds) super.clone();
		o.childs = new ArrayList<>();
		for (IAjaxObject ajaxObject : childs) {
			o.childs.add((IAjaxObject) ((KAjaxObject) ajaxObject).clone());
		}
		return o;
	}

	public int getFirstChildIndex(Class<? extends IAjaxObject> searchClass) {
		int i = 0;
		for (IAjaxObject child : childs) {
			if (child.getClass().equals(searchClass))
				return i;
			i++;
		}
		return childs.size();
	}

}
