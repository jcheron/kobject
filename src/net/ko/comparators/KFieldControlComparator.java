package net.ko.comparators;

import java.util.Comparator;
import java.util.HashMap;

import net.ko.http.views.KFieldControl;

public class KFieldControlComparator implements Comparator<String> {
	private HashMap<String, KFieldControl> fieldControls;
	public KFieldControlComparator(HashMap<String, KFieldControl> fieldControls) {
		super();
		this.fieldControls = fieldControls;
	}
	@Override
	public int compare(String field1, String field2) {
		return fieldControls.get(field1).getPos()-fieldControls.get(field2).getPos();
	}

}
