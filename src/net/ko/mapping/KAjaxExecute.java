package net.ko.mapping;

import org.w3c.dom.Node;

public class KAjaxExecute extends KAjaxObject {
	protected KAjaxInclude ajaxInclude;
	protected String call;
	protected String scope;
	protected String targetId;
	protected String targetParams;
	protected String condition;
	protected int timein;

	@Override
	public String createFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid() {
		return (call != null) && !"".equals(call);
	}

	@Override
	public void initFromXml(Node item) {
		super.initFromXml(item);

	}

	@Override
	public String getDisplayCaption() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDebugClientId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
