package net.ko.mapping;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Ajax include permettant d'afficher l'exécution d'instructions java dans un
 * élément HTML
 * 
 * @author jc
 * 
 */
public class KAjaxSet extends KAjaxMessage {
	private String javaExpression;
	private HttpServletRequest request;

	public KAjaxSet() {
		super();
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		targetId = e.getAttribute("targetId");
		javaExpression = e.getTextContent();
	}

	@Override
	public String createFunction() {
		setMessage();
		String result = "";
		if (isChild)
			result = "$set($('" + targetId + "')," + message + ");";
		else
			result = "$set($('" + targetId + "')," + message + ");";

		if (condition != null && !"".equals(condition))
			result = "if(" + condition + "){" + result + "}";
		return result;
	}

	private void setMessage() {
		ExpressionParser tp = new ExpressionParser(javaExpression);
		tp.setCaller(this);
		tp.parse(request, null);
		message = tp.toString();
	}

	@Override
	public String execute(HttpServletRequest request) {
		this.request = request;
		setMessage();
		return super.execute(request);
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getName() {
		return "set";
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		if (javaExpression != null)
			result.setTextContent(javaExpression);
		return result;
	}

}
