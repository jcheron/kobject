package net.ko.db.creation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.ko.utils.KStrings;

public class KUniqueConstraint {
	private String name;
	private List<String> fields;

	public KUniqueConstraint(String name) {
		fields = new ArrayList<>();
		this.name = name;
	}

	public KUniqueConstraint() {
		this("");
	}

	public KUniqueConstraint(String[] fieldNames) {
		this.fields = new ArrayList<>(Arrays.asList(fieldNames));
	}

	public List<String> getFields() {
		return fields;
	}

	public void addField(String fieldName) {
		fields.add(fieldName);
	}

	public String getAnnotation() {
		String result = "";
		if (fields.size() > 0) {
			result = "@UniqueConstraint(columnNames = { " + KStrings.implode(",", fields, "\"") + " })";
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		boolean result = true;
		if (o instanceof KUniqueConstraint) {
			KUniqueConstraint otherConstraint = (KUniqueConstraint) o;
			if (otherConstraint.getFields().size() == fields.size()) {
				for (String field : fields) {
					if (!otherConstraint.getFields().contains(field)) {
						result = false;
						break;
					}
				}
			} else
				result = false;
		} else
			result = false;
		return result;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
