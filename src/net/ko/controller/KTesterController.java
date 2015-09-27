package net.ko.controller;

import org.w3c.dom.Node;

public class KTesterController extends KObjectController {

	private static final long serialVersionUID = 1L;

	public KTesterController() {
		super("KTester");
	}

	@Override
	public void initFromXML(Node classXMLElement) {
		KObjectFieldController kofc = addFieldCtrl("_cls");
		kofc.setType("string");
		kofc.setControl("cmb");
		kofc.setPos(1);
		kofc = addFieldCtrl("_depth");
		kofc.setControl("range");
		kofc.setMin(0);
		kofc.setMax(6);
		kofc = addFieldCtrl("_useCache");
		kofc.setType("boolean");
		kofc = addFieldCtrl("_field");
		kofc.setControl("checkedlist");
		kofc.setPos(2);
		kofc.setMultiple(true);
	}

	private KObjectFieldController addFieldCtrl(String memberName) {
		KObjectFieldController kofc = new KObjectFieldController(memberName);
		members.put(memberName, kofc);
		kofc.setClassName(koClass);
		kofc.setKobjectController(this);
		return kofc;
	}

}
