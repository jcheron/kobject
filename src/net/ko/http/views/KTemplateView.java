package net.ko.http.views;

import javax.servlet.http.HttpServletRequest;

import net.ko.dao.DatabaseDAO;
import net.ko.displays.KObjectDisplay;
import net.ko.kobject.KObject;

public class KTemplateView extends KAbstractView {
	private KTplTemplateParser tplParser;

	public KTemplateView(HttpServletRequest request, String strTemplate) {
		super(request);
		tplParser = new KTplTemplateParser(strTemplate);
	}

	@Override
	public String toString() {
		tplParser.parse(this);
		return tplParser.toString();
	}

	@Override
	protected KObjectDisplay getKobjectDisplay() {
		return null;
	}

	@Override
	public void init() {
	}

	@Override
	public DatabaseDAO<KObject> getDao() {
		return null;
	}

}
