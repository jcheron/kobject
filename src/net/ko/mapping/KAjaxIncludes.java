package net.ko.mapping;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KDebugConsole;
import net.ko.http.objects.KRequest;

public class KAjaxIncludes extends KFilterMappings {

	public KAjaxIncludes() {
		super();
	}

	@Override
	public boolean process(HttpServletRequest request, HttpServletResponse response) {
		boolean result = false;
		for (KAbstractFilterMapping ajaxRequest : items) {
			synchronized (ajaxRequest) {
				if (ajaxRequest.matches(request)) {
					if (ajaxRequest.execute(request, response)) {
						result = true;
						KDebugConsole.print(KRequest.getRequestURI(request), "AJAX", "KAjaxIncludes.process");
					}
				} else
					ajaxRequest.setOut("");
			}
		}
		return result;
	}

	public boolean processByName(String name) {
		boolean result = false;
		for (KAbstractFilterMapping ajaxRequest : items) {
			synchronized (ajaxRequest) {
				if (ajaxRequest.getRequestURL().equals(name)) {
					if (ajaxRequest.executeByName(name)) {
						result = true;
						KDebugConsole.print(name, "AJAX", "KAjaxIncludes.process");
					}
				} else
					ajaxRequest.setOut("");
			}
		}
		return result;
	}

	public List<IHasParentObject> getAll() {
		List<IHasParentObject> all = new ArrayList<>();
		for (KAbstractFilterMapping fm : items) {
			all.add(fm);
			KAjaxRequest ajaxRequest = (KAjaxRequest) fm;
			all.addAll(ajaxRequest.getAll());
		}
		return all;
	}

	public void addElement(KAjaxRequest ajaxRequest) {
		if (items.indexOf(ajaxRequest) != -1)
			items.remove(ajaxRequest);
		items.add(ajaxRequest);
	}

	public void addElement(IAjaxObject element) {
		List<IHasParentObject> all = getAll();
		IHasParentObject parent = (IHasParentObject) ((IHasParentObject) element).getParentObject();
		if (parent != null) {
			if (all.indexOf(parent) == -1) {
				if (parent instanceof KAbstractFilterMapping)
					addElement((KAbstractFilterMapping) parent);
				else
					addElement((IAjaxObject) parent);
				return;
			}
			parent = all.get(all.indexOf(parent));
			if (all.indexOf(element) != -1) {
				parent.removeElement(element);
			}
			parent.addElement(element);
		}
	}

}
