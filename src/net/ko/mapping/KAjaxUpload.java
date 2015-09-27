package net.ko.mapping;

import net.ko.utils.KString;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KAjaxUpload extends KAjaxWithChilds implements IHasSelector,
		IHasURL {
	protected KMapping mapping;
	protected String targetId;
	protected String inputFileId;
	protected String inputName;
	protected String condition;
	protected String messageMask;
	protected String valueMask;
	protected String virtualURL;
	protected String uploadDir;
	protected String accept;

	public KAjaxUpload() {
		super();
	}

	@Override
	public String createMyOwnFunction(String childFunctions) {
		String result = "if($('" + inputFileId + "').files[0])(new Forms.Ajax('" + targetId + "', '" + virtualURL + "','_inputName=" + inputName + "&_messageMask=" + messageMask + "&_valueMask=" + valueMask + "&_uploadDir=" + uploadDir + "&_accept=" + accept + "'";
		if (!"".equals(childFunctions))
			result += "," + childFunctions;
		result = result + ")).upload($('" + inputFileId + "'));";
		if (condition != null && !"".equals(condition))
			result = "if(" + condition + "){" + result + "}";
		return result;
	}

	@Override
	public String createFunction() {
		String result = createMyOwnFunction(getFunctions());
		result = addDeboggerExpression(result, getName() + ":" + getDebugClientId() + "->" + virtualURL, createMyOwnFunction(""));
		return result;
	}

	@Override
	public String toString() {
		String result = createFunction();
		if (isChild)
			result = getPreludeFunction() + result + "}";
		return result;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "accept", accept);
		XmlUtils.setAttribute(result, "inputFileId", inputFileId);
		XmlUtils.setAttribute(result, "inputName", inputName);
		XmlUtils.setAttribute(result, "messageMask", messageMask);
		XmlUtils.setAttribute(result, "targetId", targetId);
		XmlUtils.setAttribute(result, "uploadDir", uploadDir);
		XmlUtils.setAttribute(result, "valueMask", valueMask);
		XmlUtils.setAttribute(result, "targetFunction", targetFunction);
		XmlUtils.setAttribute(result, "condition", condition);
		XmlUtils.setAttribute(result, "virtualURL", virtualURL);
		return result;
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		Element e = (Element) item;
		this.inputFileId = e.getAttribute("inputFileId");
		if (KString.isNotNull(inputFileId)) {
			if (this.inputFileId.startsWith("file_"))
				this.inputFileId = this.inputFileId.substring(5);
			targetId = XmlUtils.getAttribute(e, "targetId", "");
			inputName = XmlUtils.getAttribute(e, "inputName", inputFileId);
			this.inputFileId = "file_" + this.inputFileId;
			condition = e.getAttribute("condition");
			messageMask = KString.urlEncode(XmlUtils.getAttribute(e, "messageMask", "{fileName}"));
			valueMask = KString.urlEncode(XmlUtils.getAttribute(e, "valueMask", "{fileName}"));
			uploadDir = e.getAttribute("uploadDir");
			virtualURL = XmlUtils.getAttribute(e, "virtualURL", "upload.frm");
			accept = XmlUtils.getAttribute(e, "accept", "");
			childs = KAjaxJs.getChilds(this, item, true, isInRootFolder);
		}
	}

	@Override
	public String getDisplayStructure() {
		String result = "<div class='upload'>" + inputFileId + "->inputName:" + inputName + "</div>";
		return result;
	}

	@Override
	public boolean isValid() {
		return KString.isNotNull(inputFileId);
	}

	@Override
	public String getName() {
		return "upload";
	}

	@Override
	public String getDebugClientId() {
		return inputFileId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ko.mapping.IHasSelector#getDOMSelector()
	 */
	@Override
	public String getDOMSelector() {
		if (KString.isNotNull(targetId))
			return "#" + targetId + ", #" + inputFileId;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ko.mapping.KAjaxObject#getDisplayCaption()
	 */
	@Override
	public String getDisplayCaption() {
		return targetId;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String getTargetId() throws Exception {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getInputFileId() {
		return inputFileId;
	}

	public void setInputFileId(String inputFileId) {
		this.inputFileId = inputFileId;
	}

	public String getInputName() {
		return inputName;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getMessageMask() {
		return messageMask;
	}

	public void setMessageMask(String messageMask) {
		this.messageMask = messageMask;
	}

	public String getValueMask() {
		return valueMask;
	}

	public void setValueMask(String valueMask) {
		this.valueMask = valueMask;
	}

	public String getVirtualURL() {
		return virtualURL;
	}

	public void setVirtualURL(String virtualURL) {
		this.virtualURL = virtualURL;
	}

	public String getUploadDir() {
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}

	public String getAccept() {
		return accept;
	}

	public void setAccept(String accept) {
		this.accept = accept;
	}

	@Override
	public String getURL() {
		return virtualURL;
	}

}
