package net.ko.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ExpressionParser {
	private List<String> openedChars;
	private Map<String, String> openedCharRef;
	private String expression;

	public ExpressionParser(String expression) {
		openedChars = new ArrayList<>();
		openedCharRef = new HashMap<String, String>();
		openedCharRef.put("\"", "\"");
		openedCharRef.put("'", "'");
		openedCharRef.put("(", ")");
		this.expression = expression;
	}

	protected void addChar(String c) {
		if (openedCharRef.containsValue(c))
			openedChars.remove(getKeyByValue(c));
		else if (openedCharRef.containsKey(c))
			openedChars.add(c);

	}

	protected String getKeyByValue(String value) {
		for (Entry<String, String> entry : openedCharRef.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	protected boolean isOpened() {
		return openedChars.size() > 0;
	}

	public String[] parse() {
		StringBuilder newExpression = new StringBuilder();
		for (int i = 0; i < expression.length(); i++) {
			if (expression.charAt(i) == '.' && isOpened())
				newExpression.append('§');
			else {
				addChar(expression.charAt(i) + "");
				newExpression.append(expression.charAt(i));
			}
		}
		String[] result = newExpression.toString().split("\\.");
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i].replace("§", ".");
		}
		return result;

	}
}
