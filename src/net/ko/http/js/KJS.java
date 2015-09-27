package net.ko.http.js;


public class KJS {
	public static String insertAfter(String elementId, String targetId) {
		return insertAfter(elementId, targetId, "div");
	}

	public static String insertAfter(String elementId, String targetId, String tagName) {
		return "Forms.DOM.insertAfter('" + elementId + "',$('" + targetId + "'),'" + tagName + "');";
	}

	public static String insertBefore(String elementId, String targetId) {
		return insertBefore(elementId, targetId, "div");
	}

	public static String insertBefore(String elementId, String targetId, String tagName) {
		return "Forms.DOM.insertBefore('" + elementId + "',$('" + targetId + "'),'" + tagName + "');";
	}

	public static String set(String elementId, String value) {
		return "$set('" + elementId + "','" + value + "');";
	}

	public static String setClassName(String elementId, String className) {
		return "Forms.Utils.setClass('" + className + "','" + elementId + "');";
	}

	public static String addClassName(String elementId, String className) {
		return "Forms.Utils.addClass('" + className + "','" + elementId + "');";
	}

	public static String removeClassName(String elementId, String className) {
		return "Forms.Utils.rmClass('" + className + "','" + elementId + "');";
	}

	public static String removeElement(String elementId) {
		return "Forms.DOM.remove('" + elementId + "');";
	}
}
