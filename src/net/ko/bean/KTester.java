package net.ko.bean;

import net.ko.framework.Ko;
import net.ko.kobject.KObject;
import net.ko.utils.KStrings;

public class KTester extends KObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _cls;
	private int _depth = 2;
	private boolean _useCache = false;
	private String _field;
	private String _Commandes;
	private int countFields;

	public KTester() {
		super();
	}

	public String get_cls() {
		return _cls;
	}

	public void set_cls(String cls) {
		this._cls = cls;
	}

	public int get_depth() {
		return _depth;
	}

	public void set_depth(int depth) {
		this._depth = depth;
		Ko.ConstraintsDepth = depth;
	}

	public boolean is_useCache() {
		return _useCache;
	}

	public void set_useCache(boolean useCache) {
		this._useCache = useCache;
		Ko.useCache = useCache;
	}

	@SuppressWarnings("rawtypes")
	public String getListFields() {
		String result = "{";
		try {
			Class clazz = Class.forName(_cls);
			KObject ko = (KObject) clazz.newInstance();
			KStrings strs = new KStrings(ko.getFieldNames());
			result += strs.implode(",");
			countFields = strs.getStrings().size();
		} catch (Exception e) {
		}

		return result + "}";
	}

	public String get_field() {
		return _field;
	}

	public void set_fields(String field) {
		this._field = field;
	}

	public String get_commandes() {
		return _Commandes;
	}

	public void set_commandes(String commandes) {
		_Commandes = commandes;
	}

	public void set_field(String field) {
		this._field = field;
	}

	public String getListFieldsValues() {
		String result = "";
		for (int i = 0; i < countFields; i++) {
			if ("".equals(result))
				result = i + "";
			else
				result += ";" + i;

		}
		return result;
	}
}
