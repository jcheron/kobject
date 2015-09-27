package net.ko.ksql;

import java.util.LinkedHashMap;
import java.util.Map;

public class KSqlParameter {
	private String name;
	private Object value;

	public KSqlParameter(String name, Object value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public static KSqlParameter p(String name, Object value) {
		return new KSqlParameter(name, value);
	}

	public static Map<String, Object> parametersMap(KSqlParameter... parameters) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (KSqlParameter p : parameters) {
			result.put(p.getName(), p.getValue());
		}
		return result;
	}
}
