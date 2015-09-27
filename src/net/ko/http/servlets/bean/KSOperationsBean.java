package net.ko.http.servlets.bean;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.controller.KXmlControllers;
import net.ko.http.objects.KRequest;
import net.ko.mapping.KXmlMappings;
import net.ko.utils.KProperties;

public class KSOperationsBean extends KActionBean {
	private boolean onlyURL = false;
	private KXmlMappings xmlMappings;
	private KXmlControllers xmlControllers;
	private String activeFile;
	private KProperties originalProperties;
	private KProperties copyProperties;
	ArrayList<String> urls;
	ArrayList<Integer> ids;
	private String fileType = "Css";

	public KSOperationsBean(HttpServletRequest request, HttpServletResponse response) throws IOException {
		super(request, response);
		// TODO Auto-generated constructor stub
	}

	public boolean isOnlyURL() {
		return onlyURL;
	}

	public void setOnlyURL(boolean onlyURL) {
		this.onlyURL = onlyURL;
	}

	public KXmlMappings getXmlMappings() {
		return xmlMappings;
	}

	public void setXmlMappings(KXmlMappings xmlMappings) {
		this.xmlMappings = xmlMappings;
	}

	public KXmlControllers getXmlControllers() {
		return xmlControllers;
	}

	public void setXmlControllers(KXmlControllers xmlControllers) {
		this.xmlControllers = xmlControllers;
	}

	public String getActiveFile() {
		return activeFile;
	}

	public void setActiveFile(String activeFile) {
		this.activeFile = activeFile;
	}

	public KProperties getOriginalProperties() {
		return originalProperties;
	}

	public void setOriginalProperties(KProperties originalProperties) {
		this.originalProperties = originalProperties;
	}

	public KProperties getCopyProperties() {
		return copyProperties;
	}

	public void setCopyProperties(KProperties copyProperties) {
		this.copyProperties = copyProperties;
	}

	public ArrayList<String> getUrls() {
		return urls;
	}

	public void setUrls(ArrayList<String> urls) {
		this.urls = urls;
	}

	public ArrayList<Integer> getIds() {
		return ids;
	}

	public void setIds(ArrayList<Integer> ids) {
		this.ids = ids;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String initFileType() {
		fileType = KRequest.GETPOST("fileType", request, fileType);
		return fileType;
	}

	public String initActiveFile() {
		activeFile = KRequest.GETPOST("file", request, activeFile);
		return activeFile;

	}
}
