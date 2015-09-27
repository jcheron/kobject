package net.ko.mapping;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface IAjaxObject {
	public String createFunction();

	public String createFunction(HttpServletRequest request);

	public boolean isValid();

	public void initFromXml(Node item);

	public String execute(HttpServletRequest request);

	public String executeByName();

	public String display(int margin);

	public String displayRecursive();

	public String getUniqueIdHash(int niveau);

	public String getDisplayCaption();

	public String getDisplayStructure();

	public String getDisplayDebugClientInfo();

	public String getDebugClientInfo(int index);

	public String getChildsDebugClientInfo();

	public String getDebugClientId();

	public String getName();

	public boolean hasChilds();

	public String replaceJsExpressions(String input);

	public String getClientDebugString();

	public String getClientDebugTitle();

	public void setBreaked(boolean breaked);

	public void setBreakPoint(boolean breakPoint);

	public boolean isBreakPoint();

	public Element saveAsXML(Element parentElement);

}
