package net.ko.bean;

import java.util.ArrayList;
import java.util.List;

import net.ko.kobject.KObject;

public class KoSorted extends KObject {
	private static final long serialVersionUID = 1L;
	private List<String> values;

	public KoSorted() {
		super();
		values = new ArrayList<>();
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}
