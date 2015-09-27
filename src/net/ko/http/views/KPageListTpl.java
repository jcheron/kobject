package net.ko.http.views;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("rawtypes")
public class KPageListTpl extends KPageList {
	private String strTemplate;
	private String sepFirst = "{";
	private String sepLast = "}";

	public KPageListTpl(Class clazz, HttpServletRequest request, HttpServletResponse response, String strTemplate) {
		super(clazz, request);
		this.response = response;
		this.strTemplate = strTemplate;
	}

	public KPageListTpl(Class clazz, HttpServletRequest request, HttpServletResponse response, String sql,
			String idForm, int mode) {
		super(clazz, request, sql, idForm, mode);
		this.response = response;
	}

	public KPageListTpl(Class clazz, HttpServletRequest request, HttpServletResponse response, String sql,
			String idForm, String strTemplate) {
		super(clazz, request, sql, idForm);
		this.response = response;
		this.strTemplate = strTemplate;
	}

	public KPageListTpl(Class clazz, HttpServletRequest request, HttpServletResponse response, String sql, String strTemplate) {
		super(clazz, request, sql);
		this.response = response;
		this.strTemplate = strTemplate;
	}

	public String getStrTemplate() {
		return strTemplate;
	}

	public void setStrTemplate(String strTemplate) {
		this.strTemplate = strTemplate;
	}

	@Override
	public void load() {
		KListTemplateParser tp = new KListTemplateParser(strTemplate);
		tp.setSepFirst(sepFirst);
		tp.setSepLast(sepLast);
		tp.parse(this);
		super.load();
		tp.parseZones(this);
	}

	public void loadForExec() {
		KListTemplateParser tp = new KListTemplateParser(strTemplate);
		tp.setSepFirst(sepFirst);
		tp.setSepLast(sepLast);
		tp.parseForExec(this);
	}

	public String getSepFirst() {
		return sepFirst;
	}

	public void setSepFirst(String sepFirst) {
		this.sepFirst = sepFirst;
	}

	public String getSepLast() {
		return sepLast;
	}

	public void setSepLast(String sepLast) {
		this.sepLast = sepLast;
	}

}
