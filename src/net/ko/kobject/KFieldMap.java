/**
 * Classe KFieldMap
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: $
 * @package ko.kobject
 */
package net.ko.kobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classe gérant le mapping entre les champs de la table de base de données et
 * les membres de la classe
 * 
 * @author jcheron
 * 
 */
public class KFieldMap implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1903828451239918340L;
	private Map<String, String> items;

	public KFieldMap() {
		items = new HashMap<String, String>();
	}

	public Map<String, String> getItems() {
		return items;
	}

	public void setItems(HashMap<String, String> items) {
		this.items = items;
	}

	public String getFieldName(String member) {
		String ret = member;
		for (String k : items.keySet()) {
			if (items.get(k).equals(member)) {
				ret = k;
			}
		}
		return ret;
	}

	public String getMemberName(String field) {
		return items.get(field.toLowerCase());
	}

	public boolean existsMember(String member) {
		return items.containsValue(member);
	}

	public boolean existsField(String field) {
		return items.containsKey(field.toLowerCase());
	}

	public void addMap(String member, String field) {
		items.put(field.toLowerCase(), member);
	}

	public Map<String, Object> getFieldRow(Map<String, Object> row) {
		Map<String, Object> ret = new LinkedHashMap<String, Object>();
		for (String k : row.keySet()) {
			String fn = getFieldName(k);
			if (!fn.startsWith("_"))// && row.get(k)!=null
				ret.put(fn, row.get(k));
		}
		return ret;
	}

	public ArrayList<String> getFieldNames(Map<String, Object> row) {
		ArrayList<String> ret = new ArrayList<String>();
		for (String k : row.keySet()) {
			String fn = getFieldName(k);
			if (!fn.startsWith("_"))
				ret.add(fn);
		}
		return ret;
	}
}
