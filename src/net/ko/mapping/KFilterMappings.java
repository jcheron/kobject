package net.ko.mapping;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KDebugConsole;

public class KFilterMappings {
	protected List<KAbstractFilterMapping> items;

	public KFilterMappings() {
		items = new ArrayList<>();
	}

	public void add(KAbstractFilterMapping filterMapping) {
		items.add(filterMapping);
	}

	public KAbstractFilterMapping remove(int index) {
		return items.remove(index);
	}

	public boolean remove(KMapping mapping) {
		return items.remove(mapping);
	}

	public KAbstractFilterMapping getFirstMatches(HttpServletRequest request) {
		KAbstractFilterMapping result = null;
		for (KAbstractFilterMapping mapping : items) {
			if (mapping.matches(request)) {
				result = mapping;
				break;
			}
		}
		return result;
	}

	public KAbstractFilterMapping getFirstMatches(String url, HttpServletRequest request) {
		KAbstractFilterMapping result = null;
		for (KAbstractFilterMapping mapping : items) {
			if (mapping.matches(url, request)) {
				result = mapping;
				break;
			}
		}
		return result;
	}

	public KAbstractFilterMapping getFirstMatches(String url) {
		KAbstractFilterMapping result = null;
		for (KAbstractFilterMapping mapping : items) {
			if (mapping.requestURLMatches(url)) {
				result = mapping;
				break;
			}
		}
		return result;
	}

	public boolean process(HttpServletRequest request, HttpServletResponse response) {
		boolean result = false;
		KAbstractFilterMapping mapping = getFirstMatches(request);
		if (mapping != null)
			result = mapping.execute(request, response);
		else {
			KDebugConsole.print("no filtering/mapping:" + request.getServletPath(), "MAPPING", "KFilterMapping.process");
			result = false;
		}
		return result;
	}

	@Override
	public String toString() {
		String result = "";
		for (KAbstractFilterMapping ajaxRequest : items) {
			result += ajaxRequest.toString();
		}
		return result;
	}

	public List<KAbstractFilterMapping> getItems() {
		return items;
	}

	public KMappingCompare exists(KAbstractFilterMapping filterMapping) {
		KMappingCompare result = KMappingCompare.mcNew;
		if (items.contains(filterMapping)) {
			result = KMappingCompare.mcIdem;
			KAbstractFilterMapping idem = items.get(items.indexOf(filterMapping));
			if (!idem.displayRecursive().equals(filterMapping.displayRecursive()))
				result = KMappingCompare.mcUpdated;
		}
		return result;
	}

	public boolean isNew(KAbstractFilterMapping filterMapping) {
		return KMappingCompare.mcNew.equals(exists(filterMapping));
	}

	public void addElement(KAbstractFilterMapping filterMapping) {
		if (isNew(filterMapping)) {
			add(filterMapping);
		} else {
			items.remove(filterMapping);
			items.add(filterMapping);
		}
	}

	public int insertElement(KAbstractFilterMapping filterMapping, int position) {
		int pos = 0;
		if (position > 0 && position < items.size())
			pos = position;
		if (isNew(filterMapping)) {
			items.add(pos, filterMapping);
		} else {
			pos = items.indexOf(filterMapping);
			items.remove(filterMapping);
			items.add(pos, filterMapping);
		}
		return pos;
	}
}
