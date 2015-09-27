package net.ko.http.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.ko.framework.Ko;
import net.ko.kobject.KMask;
import net.ko.utils.KStrings;

public class KListTemplateParser extends KTemplateParser {
	protected KMask mask;
	protected boolean maskModified = false;

	public KListTemplateParser(String strTemplate) {
		super(strTemplate);
		mask = new KMask();
	}

	protected void updateListColumn(String fieldName, Map<String, Object> values) {
		for (Map.Entry<String, Object> value : values.entrySet()) {
			try {
				view.invoke(value.getKey(), view.getClass(), new Object[] { fieldName, value.getValue() });
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				Ko.klogger().log(Level.WARNING, "Impossible d'appeler " + value.getKey() + " avec les paramètres " + fieldName + " et " + value.getValue(), e);
			}
		}
	}

	@Override
	protected String getOperation(Entry<String, Object> e) {
		String result = super.getOperation(e);
		String key = e.getKey();
		String value = (String) e.getValue();
		if (key.equalsIgnoreCase("mask")) {
			addMask(value);
			result = "";
		}
		return result;
	}

	@Override
	public String getValue(String jsonFieldName) {
		String result = "";
		KStrings strings = new KStrings(jsonFieldName);
		if (!strings.containsKey("0")) {
			for (Map.Entry<String, Object> e : strings.getStrings().entrySet()) {
				if (strings.containsKey("name")) {
					updateListColumn((String) strings.get("name"), strings.getStrings());
				}
			}
		} else
			result = "{" + jsonFieldName + "}";
		return result;
	}

	public void addMask(String jsonField) {
		mask.addMask(jsonField);
		maskModified = true;
	}

	@Override
	public void parse(KAbstractView view) {
		super.parse(view);
		KPageList pl = ((KPageList) view);
		if (maskModified)
			pl.setInnerMask(mask);
		pl.fieldControls.clear();
	}

	public void parseZones(KAbstractView view) {
		KMask mask = new KMask(resultString);
		KPageList pl = ((KPageList) view);
		ArrayList<String> jsonOperations = mask.getOperations("{", "}");
		for (String op : jsonOperations) {
			String updateOp = view.getDisplayInstance().getZone(op, (KPageList) view, request);
			resultString = resultString.replace("{" + op + "}", updateOp);
		}
		pl.addHTML("content", resultString);
	}

}
