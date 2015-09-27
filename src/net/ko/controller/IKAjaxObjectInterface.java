package net.ko.controller;

import javax.servlet.http.HttpServletRequest;

import net.ko.mapping.KAjaxObject;

public interface IKAjaxObjectInterface {
	public void beforeInclude(KAjaxObject ajaxObject, HttpServletRequest request);

	public void afterInclude(StringBuilder responseText, HttpServletRequest request);
}
