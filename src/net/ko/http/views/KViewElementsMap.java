package net.ko.http.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import net.ko.bean.KViewElementsMapOperation;
import net.ko.comparators.KFieldControlComparator;
import net.ko.kobject.KObject;

/**
 * Liste d'éléments à afficher dans une vue (Dérivant de KAbstractView)
 * 
 * @see KPageList
 * @see KHttpForm
 * @author jc
 * 
 */
public class KViewElementsMap extends LinkedHashMap<String, KFieldControl> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<KViewElementsMapOperation> elementsToUpdate;
	private boolean loaded = false;
	private int max = -1;

	public KViewElementsMap() {
		super();
		elementsToUpdate = new ArrayList<KViewElementsMapOperation>();
		loaded = false;
	}

	private KFieldControl addIn(String keyType, KFieldControl field) {
		String key = keyType + size() + 1;
		put(key, field);
		field.setId(key);
		return field;
	}

	public KFieldControl addHtml(String html) {
		return addIn("html", new KHtmlElement(html));
	}

	public KFieldControl addHtml(String key, String html) {
		return put(key, new KHtmlElement(html));
	}

	public KFieldControl addFieldSet(String legend) {
		return addIn("fieldset", new KHtmlFieldSet(legend));
	}

	public KFieldControl addButton(String caption, String onclick) {
		return addIn("button", new KHtmlButton(caption, onclick));
	}

	public void swap(String fieldName1, String fieldName2) {
		if (loaded) {
			int tmpPos = 0;
			KFieldControl f1 = get(fieldName1);
			KFieldControl f2 = get(fieldName2);
			if (f1 != null && f2 != null) {
				tmpPos = f1.getPos();
				f1.setPos(f2.getPos());
				f2.setPos(tmpPos);
			}
		} else
			elementsToUpdate.add(new KViewElementsMapOperation().toSwap(fieldName1, fieldName2));
	}

	@Override
	public KFieldControl put(String key, KFieldControl value) {
		KFieldControl result = value;
		if (loaded) {
			super.put(key, value);
			if (value != null)
				if (value.getPos() == 0)
					value.setPos(max + 10);
			if (value.getPos() > max)
				max = value.getPos();

		} else
			elementsToUpdate.add(new KViewElementsMapOperation().toAdd((String) key, value));
		return result;
	}

	public List<String> getSortedKeySet() {
		List<String> cles = new ArrayList<String>(this.keySet());
		Collections.sort(cles, new KFieldControlComparator(this));
		return cles;
	}

	@Override
	public String toString() {
		loaded = true;
		for (KViewElementsMapOperation op : elementsToUpdate)
			op.execute(this);
		List<String> cles = new ArrayList<String>(this.keySet());
		Collections.sort(cles, new KFieldControlComparator(this));
		String result = "";
		for (String field : cles)
			result += this.get(field).toString();
		return result;
	}

	public String getElementsAsString(Class<KFieldControl> clazz) {
		List<String> cles = new ArrayList<String>(this.keySet());
		Collections.sort(cles, new KFieldControlComparator(this));
		String result = "";
		for (String field : cles) {
			if (this.get(field).getClass().equals(clazz))
				result += this.get(field).toString();
		}
		return result;
	}

	private void removes(String[] keys) {
		for (String k : keys)
			remove(k);
	}

	@Override
	public KFieldControl remove(Object key) {
		if (key != null && key.getClass().isArray()) {
			removes((String[]) key);
			return null;
		}
		else {
			if (loaded && containsKey(key))
				return super.remove(key);
			else {
				if (!loaded)
					elementsToUpdate.add(new KViewElementsMapOperation().toDelete((String) key));
				return null;
			}
		}
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public String parseTemplate(KHttpFormTpl form, KObject ko, String strMask, String sepFirst, String sepLast) {
		KFormTemplateParser tp = new KFormTemplateParser(ko, strMask);
		tp.setSepFirst(sepFirst);
		tp.setSepLast(sepLast);
		tp.parse(form);
		return tp.toString();
	}

	public String parseTemplate(KHttpShowTpl show, KObject ko, String strMask, String sepFirst, String sepLast) {
		KFormTemplateParser tp = new KFormTemplateParser(ko, strMask);
		tp.setSepFirst(sepFirst);
		tp.setSepLast(sepLast);
		tp.parse(show);
		return tp.toString();
	}

	public String parseTemplate(KHttpFormTpl form, KObject ko, String strMask, String sepFirst, String sepLast, boolean exec) {
		KFormTemplateParser tp = new KFormTemplateParser(ko, strMask);
		tp.setSepFirst(sepFirst);
		tp.setSepLast(sepLast);
		if (exec)
			tp.parseForExec(form);
		else
			tp.parse(form);
		return tp.toString();
	}

	@Override
	public void clear() {
		elementsToUpdate.clear();
		super.clear();
	}

}
