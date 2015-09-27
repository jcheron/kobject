package net.ko.framework;

import java.util.HashMap;
import java.util.Map;

import net.ko.kobject.KObject;

public class KoInstances {
	private Map<Class<? extends KObject>, KObject> instances;

	public KoInstances() {
		instances = new HashMap<Class<? extends KObject>, KObject>();
	}

	@SuppressWarnings("unchecked")
	public <T extends KObject> T getInstance(Class<T> clazz) {
		Object inst = instances.get(clazz);
		T ko = null;
		if (inst != null)
			ko = (T) inst;
		if (ko == null)
			ko = createKoInstance(clazz);
		return ko;
	}

	private <T extends KObject> T createKoInstance(Class<T> clazz) {
		T ko = null;
		try {
			ko = clazz.newInstance();
			instances.put(clazz, ko);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ko;
	}
}
