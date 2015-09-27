package net.ko.http.js;

import java.util.Map;

import net.ko.utils.KProperties;

public class KJsVars {
	public static String getERs(KProperties kp) {
		String result = "_ER=new Array();\n";
		for (Map.Entry<Object, Object> e : kp.getProperties().entrySet()) {
			String value = e.getValue() + "";
			if (value.contains("(?i)")) {
				value = value.replace("(?i)", "");
				result += "_ER[\"" + e.getKey() + "\"]=/" + value.replace("/", "\\/") + "/i;\n";
			}
			else
				result += "_ER[\"" + e.getKey() + "\"]=/" + value.replace("/", "\\/") + "/;\n";
		}
		return result;
	}

	public static String getMessages(KProperties kp) {
		String result = "_message=new Array();\n";
		for (Map.Entry<Object, Object> e : kp.getProperties().entrySet()) {
			result += "_message[\"" + e.getKey() + "\"]=\"" + e.getValue() + "\";\n";
		}
		return result;
	}

	public static String getMessagesAndERs(KProperties kpMessages, KProperties kpErs) {
		return getMessages(kpMessages) + getERs(kpErs);
	}
}
