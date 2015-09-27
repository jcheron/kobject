package net.ko.mapping;

import org.w3c.dom.Node;

public class KAjaxDeleteMulti extends KAjaxDeleteOne {

	public KAjaxDeleteMulti() {
		super();
	}

	public KAjaxDeleteMulti(String virtualURL) {
		super(virtualURL);
	}

	@Override
	protected String getDefaultJsValues() {
		return "{js:new $e('#list-" + kobjectShortClassName + " input.checkable:checked').getValuesAsString()}";
	}

	@Override
	public String getName() {
		return "deleteMulti";
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);
		parameters.put("_multiple", true);
	}
}
