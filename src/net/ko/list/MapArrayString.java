package net.ko.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapArrayString<K extends Object> implements Iterable<K> {
	private Map<K, List<Object>> map;
	private boolean unique;

	public MapArrayString() {
		unique = true;
		map = new HashMap<>();
	}

	public void put(K key, Object value) {
		if (map.containsKey(key)) {
			if (unique && map.get(key).contains(value))
				return;
			map.get(key).add(value);
		} else {
			List<Object> list = new ArrayList<>();
			list.add(value);
			map.put(key, list);
		}
	}

	public Map<K, List<Object>> getMap() {
		return map;
	}

	public List<Object> get(K key) {
		return map.get(key);
	}

	@Override
	public Iterator<K> iterator() {
		return map.keySet().iterator();
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

}
