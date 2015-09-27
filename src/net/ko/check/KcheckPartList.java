package net.ko.check;

import java.util.ArrayList;
import java.util.List;

public class KcheckPartList {
	private List<KCheckPart> items;

	public KcheckPartList() {
		items = new ArrayList<>();
	}

	public void addItem(KCheckSelector selector) {
		items.add(selector);
	}

	public List<KCheckPart> getItems() {
		return items;
	}

	public String getJsRequests() {
		String result = "";
		if (items.size() > 0)
			result = getJsRequest(0);
		return result;
	}

	private String getJsRequest(int index) {
		String result = items.get(index).getJsRequest();
		if (index + 1 < items.size())
			result = result.replace("%suite%", getJsRequest(index + 1));
		else
			result = result.replace("%suite%", "");
		return result;
	}
}
