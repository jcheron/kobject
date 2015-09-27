package net.ko.check;

public class KCheckSelector extends KCheckPart {
	private String selector;

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public KCheckSelector(String requestURL, String selector, KCheckMessageWithChilds messageParent, String uid) {
		super(requestURL, messageParent, requestURL + "->" + selector, uid);
		this.selector = selector;
	}

	@Override
	protected String getJsCheck() {
		String result = "var elms=new $e('" + selector + "');" +
				"var target=$('" + message.getDomId() + "');var msg='';" +
				"var ajaxIncludes=($('selectors').innerHTML.indexOf('<!-- ajax-includes -->')!=-1);" +
				"var ai=(!ajaxIncludes)?' + Instruction ajax-includes absente':'';" +
				"if(elms.length()==0){" +
				"$set(target,'Aucun élément correspondant au sélecteur');" +
				"target.className='" + KCheckMessageType.WARNING.cssClass + "';" +
				"}else{" +
				"$set(target,elms.length()+' élément(s) correspondant au sélecteur <b>" + selector + "</b> : Ok');" +
				"target.className='" + KCheckMessageType.VALID.cssClass + "';" +
				"};" +
				"if(!ajaxIncludes){" +
				"var aiNo=Forms.DOM.insertAfter('aiNo" + message.getDomId() + "',target,'span');" +
				"aiNo.className='" + KCheckMessageType.WARNING.cssClass + "';" +
				"$set(aiNo,ai);" +
				"};%suite%";
		return result;
	}

	@Override
	protected String getOperation() {
		return "checkSelector";
	}
}
