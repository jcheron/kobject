package net.ko.mapping;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KDebugConsole;

public class KMappings {
	private ArrayList<KMapping> items;

	public KMappings() {
		items = new ArrayList<>();
	}

	public void add(KMapping mapping) {
		if (!items.contains(mapping))
			items.add(mapping);
	}

	public KMapping remove(int index) {
		return items.remove(index);
	}

	public boolean remove(KMapping mapping) {
		return items.remove(mapping);
	}

	public KMapping getFirstMatches(HttpServletRequest request) {
		KMapping result = null;
		for (KMapping mapping : items) {
			if (mapping.matches(request)) {
				result = mapping;
				break;
			}
		}
		return result;
	}

	public void process(HttpServletRequest request, HttpServletResponse response) {
		KMapping mapping = getFirstMatches(request);
		if (mapping != null)
			mapping.execute(request, response);
		else
			KDebugConsole.print("no map:" + request.getServletPath(), "MAPPING", "KMappings.process");
	}
}
