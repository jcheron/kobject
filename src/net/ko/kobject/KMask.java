package net.ko.kobject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.ko.displays.Display;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.interfaces.KMaskInterface;
import net.ko.utils.KRegExpr;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.KoUtils;

public class KMask implements Iterable<String>, KMaskInterface {
	private ArrayList<String> masks;
	private String sepFirst;
	private String sepLast;
	private int counter;
	private boolean koDetails = false;
	private final String editKo = "\\[\\.\\.\\.\\]";
	private String selectedKeys = "";
	private String checkedValue = " checked ";
	private String keySep = ";";
	private KMask groupByMask = null;
	private HttpServletRequest request;

	public String getSepFirst() {
		return sepFirst;
	}

	public void setSepFirst(String sepFirst) {
		this.sepFirst = sepFirst;
	}

	public String getSepLast() {
		return sepLast;
	}

	public void setSepLast(String sepLast) {
		this.sepLast = sepLast;
	}

	public KMask() {
		counter = 0;
		sepFirst = "{";
		sepLast = "}";
		masks = new ArrayList<String>();
	}

	public KMask(String mask) {
		this();
		masks.add(mask);
	}

	public KMask(String[] masks) {
		this();
		this.masks = new KStrings(masks).toArrayList();
	}

	public void addMask(String mask) {
		masks.add(mask);
	}

	public String show(KObject ko, KObjectDisplay koDisplay) {
		return show(ko, koDisplay, null);
	}

	public String show(KObject ko, KObjectDisplay koDisplay, String cssClass) {
		String ret = "";
		if (counter < masks.size()) {
			String m = masks.get(counter);
			if (KString.isNotNull(cssClass))
				m = addCssClass(m, cssClass);
			if (!"".equals(selectedKeys) && (keySep + selectedKeys + keySep).contains(keySep + ko.getFirstKeyValue() + keySep))
				m = m.replace("{checkedValue}", checkedValue);
			else
				m = m.replace("{checkedValue}", "");
			ret = ko.getDisplay().showWithMask(ko, m, sepFirst, sepLast, koDisplay, request);
			counter++;
		}
		if (counter > masks.size() - 1)
			counter = 0;
		return ret;
	}

	private String addCssClass(String s, String cssClass) {
		return s.replace("<tr class='", "<tr class='" + cssClass + " ");
	}

	public String show(KListObject<? extends KObject> kl) {
		return show(kl, "", "");
	}

	public String show(KListObject<? extends KObject> kl, String sortedField, String sortedSens) {
		return show(kl, sortedField, sortedSens, Ko.defaultKoDisplay());
	}

	public String show(KListObject<? extends KObject> kl, String sortedField, String sortedSens, KObjectDisplay koDisplay) {
		String ret = "";
		if (sortedField != null && !"".equals(sortedField))
			kl.sortBy("{" + sortedField + "}", "ASC".equals(sortedSens));
		for (int i = 0; i < kl.count(); i++) {
			ret += show(kl.get(i), koDisplay) + "\n";
		}
		return ret;
	}

	public String show(KListObject<? extends KObject> kl, String groupByMaskStr, String sortedField, String sortedSens, KObjectDisplay koDisplay) {
		if (groupByMaskStr == null || "".equals(groupByMaskStr))
			return show(kl, sortedField, sortedSens, koDisplay);
		String ret = "";
		String cssClassGroupBy = "";
		KMask groupByMask = new KMask(groupByMaskStr);
		ArrayList<String> gbFields = groupByMask.getFields();
		if (sortedField != null && !"".equals(sortedField))
			kl.sortBy(groupByMaskStr + "{" + sortedField + "}", "ASC".equals(sortedSens), koDisplay);
		else
			kl.sortBy(groupByMaskStr, koDisplay);

		for (String gbF : gbFields) {
			this.removeField(gbF);
		}
		int nbFields = this.getFields().size();
		cssClassGroupBy = "groupBy";
		groupByMaskStr = "<tr class='" + cssClassGroupBy + "'><td colspan='" + nbFields + 1 + "'>" + groupByMaskStr + "</td></tr>";
		String gbCaption = "";
		for (int i = 0; i < kl.count(); i++) {
			String newCaption = "";
			if (koDisplay != null) {
				newCaption = Display.getDefault().showWithMask(kl.get(i), groupByMaskStr, sepFirst, sepLast, koDisplay, request);
			}
			else {
				newCaption = Display.getDefault().showWithMask(kl.get(i), groupByMaskStr, sepFirst, sepLast, request);
			}

			if (!gbCaption.equals(newCaption)) {
				ret += newCaption;
				gbCaption = newCaption;
			}
			ret += show(kl.get(i), koDisplay, "_" + cssClassGroupBy) + "\n";
		}
		return ret;
	}

	public String __show(KListObject<? extends KObject> kl, String[] groupByMaskStrs, String sortedField, String sortedSens, KObjectDisplay koDisplay) {
		if (groupByMaskStrs == null || "".equals(groupByMaskStrs))
			return show(kl, sortedField, sortedSens, koDisplay);
		String ret = "";
		int nbFields = this.getFields().size();
		int nbGroupBy = groupByMaskStrs.length;
		if (sortedField != null && !"".equals(sortedField)) {
			groupByMaskStrs = (String[]) KoUtils.extend(groupByMaskStrs, 1);
			groupByMaskStrs[groupByMaskStrs.length - 1] = sortedField;
		}
		kl.sort(groupByMaskStrs, "ASC".equals(sortedSens), koDisplay);
		for (int i = 0; i < nbGroupBy; i++) {
			KMask groupByMask = new KMask(groupByMaskStrs[i]);
			ArrayList<String> gbFields = groupByMask.getFields();

			for (String gbF : gbFields) {
				this.removeField(gbF);
			}
		}

		String[] newCaption = new String[nbGroupBy];
		String[] gbCaption = new String[nbGroupBy];
		for (int j = 0; j < nbGroupBy; j++) {
			gbCaption[j] = "";
			groupByMaskStrs[j] = "<tr class='groupBy" + (j + 1) + "' id='groupBy" + (j + 1) + "-{groupRef}'><td colspan='" + (nbFields + 1) + "'>" + groupByMaskStrs[j] + "</td></tr>";
		}
		int nbElement = 0;
		int groupRef = 1;
		for (int i = 0; i < kl.count(); i++) {
			String m = "";
			String cssClass = "";
			for (int j = 0; j < nbGroupBy; j++) {
				cssClass = "";
				m = groupByMaskStrs[j];
				m = m.replaceAll("\\{groupRef\\}", groupRef + "");
				for (int k = 1; k <= j; k++)
					cssClass += "_groupBy" + j + "-" + groupRef + " ";
				if (koDisplay != null) {
					newCaption[j] = Display.getDefault().showWithMask(kl.get(i), m, sepFirst, sepLast, koDisplay, request);
				}
				else
					newCaption[j] = Display.getDefault().showWithMask(kl.get(i), m, sepFirst, sepLast, request);
				if (!gbCaption[j].equals(newCaption[j])) {
					if (nbElement > 0) {
						if (j == 0)
							groupRef++;
						for (int z = 0; z < nbGroupBy - j; z++)
							ret += "</table></td></tr>";
					}
					nbElement = 0;
					ret += newCaption[j] + "<tr class='" + cssClass + "'><td colspan='" + (nbFields + 1) + "'><table style='width: 100%'>";
					gbCaption[j] = newCaption[j];
					for (int z = j + 1; z < nbGroupBy; z++)
						gbCaption[z] = "";
				} else
					nbElement++;
			}
			cssClass += "_groupBy" + nbGroupBy + "-" + groupRef + " ";
			ret += show(kl.get(i), koDisplay, cssClass) + "\n";
			nbElement++;
		}
		if (nbElement > 0) {
			for (int z = 0; z < nbGroupBy; z++)
				ret += "</table></td></tr>";
		}
		return ret;
	}

	public String show(KListObject<? extends KObject> kl, String[] groupByMaskStrs, String sortedField, String sortedSens, KObjectDisplay koDisplay) {
		if (groupByMaskStrs == null || "".equals(groupByMaskStrs))
			return show(kl, sortedField, sortedSens, koDisplay);
		String ret = "";
		int nbFields = this.getFields().size();
		int nbGroupBy = groupByMaskStrs.length;
		if (sortedField != null && !"".equals(sortedField)) {
			groupByMaskStrs = (String[]) KoUtils.extend(groupByMaskStrs, 1);
			groupByMaskStrs[groupByMaskStrs.length - 1] = sortedField;
		}
		kl.sort(groupByMaskStrs, "ASC".equals(sortedSens), koDisplay);
		for (int i = 0; i < nbGroupBy; i++) {
			KMask groupByMask = new KMask(groupByMaskStrs[i]);
			ArrayList<String> gbFields = groupByMask.getFields();

			for (String gbF : gbFields) {
				this.removeField(gbF);
			}
		}

		String[] newCaption = new String[nbGroupBy];
		String[] gbCaption = new String[nbGroupBy];
		String[] activeGroupRef = new String[nbGroupBy];
		int[] niveaux = new int[nbGroupBy];

		for (int j = 0; j < nbGroupBy; j++) {
			gbCaption[j] = "";
			groupByMaskStrs[j] = "<tr class='groupBy" + (j + 1) + " [cssClass]' id='[groupRef]'><td colspan='" + (nbFields + 1) + "'>" + groupByMaskStrs[j] + "</td></tr>";
		}
		int nbElement = 0;

		for (int i = 0; i < kl.count(); i++) {
			String m = "";
			String cssClass = "";
			for (int j = 0; j < nbGroupBy; j++) {
				if (koDisplay != null) {
					newCaption[j] = Display.getDefault().showWithMask(kl.get(i), groupByMaskStrs[j], sepFirst, sepLast, koDisplay, request);
				}
				else
					newCaption[j] = Display.getDefault().showWithMask(kl.get(i), groupByMaskStrs[j], sepFirst, sepLast, request);

				if (!gbCaption[j].equals(newCaption[j])) {
					// if (nbElement > 0) {
					niveaux[j]++;
					// for (int z = 0; z < nbGroupBy - j; z++)
					// ret += "</table></td></tr>";
					// }

					nbElement = 0;

					activeGroupRef[j] = "groupBy" + (j + 1) + "-" + niveaux[j];
					cssClass = getCss(activeGroupRef, j);

					gbCaption[j] = newCaption[j];

					newCaption[j] = newCaption[j].replaceAll("\\[groupRef\\]", activeGroupRef[j]);
					newCaption[j] = newCaption[j].replaceAll("\\[cssClass\\]", cssClass);

					ret += newCaption[j];// + "<tr class='" + cssClass +
											// "'><td colspan='" + (nbFields +
											// 1) +
											// "'><table style='width: 100%'>";
					for (int z = j + 1; z < nbGroupBy; z++)
						gbCaption[z] = "";
				} else
					nbElement++;
			}
			cssClass = getCss(activeGroupRef, nbGroupBy);
			ret += show(kl.get(i), koDisplay, cssClass) + "\n";
			nbElement++;
		}
		if (nbElement > 0) {
			for (int z = 0; z < nbGroupBy; z++)
				ret += "</table></td></tr>";
		}
		return ret;
	}

	private String getCss(String[] activeGroupRef, int nb) {
		String s = "";
		for (int i = 0; i < nb - 1; i++) {
			s += "_" + activeGroupRef[i] + " ";
		}
		if (nb - 1 < activeGroupRef.length && nb > 0)
			s += "_" + activeGroupRef[nb - 1];
		return s;
	}

	public String show(KListObject<? extends KObject> kl, String groupByMaskStr, String sortedField, String sortedSens) {
		return show(kl, groupByMaskStr, sortedField, sortedSens, null);
	}

	public String get(int index) {
		return masks.get(index);
	}

	@Override
	public Iterator<String> iterator() {
		return masks.iterator();
	}

	public int count() {
		return masks.size();
	}

	public void remove(int index) {
		if (index > -1 && index < masks.size())
			masks.remove(index);
	}

	public void remove(Integer[] fieldsIndex) {
		ArrayList<String> fields = getFields();
		if (fieldsIndex != null)
			if (fieldsIndex.length > 0) {
				for (int i : fieldsIndex) {
					if (i < fields.size())
						removeField(fields.get(i));
				}
			}
	}

	public void keep(Integer[] fieldsIndex) {
		if (fieldsIndex != null)
			if (fieldsIndex.length > 0)
				keep(KStrings.implode(Ko.krequestValueSep(), fieldsIndex));
	}

	public void keep(String indexes) {
		ArrayList<String> fields = getFields();
		ArrayList<String> toRemove = new ArrayList<>();
		String sep = Ko.krequestValueSep();
		for (int i = 0; i < fields.size(); i++) {
			if (!(sep + indexes + sep).contains(sep + i + sep))
				toRemove.add(fields.get(i));
		}
		for (int i = 0; i < toRemove.size(); i++)
			removeField(toRemove.get(i));
	}

	public void keep(String before, String after, String indexes) {
		ArrayList<String> fields = getFields();
		ArrayList<String> toRemove = new ArrayList<>();
		String sep = Ko.krequestValueSep();
		for (int i = 0; i < fields.size(); i++) {
			if (!(sep + indexes + sep).contains(sep + i + sep))
				toRemove.add(fields.get(i));
		}
		for (int i = 0; i < toRemove.size(); i++)
			removeField(before, toRemove.get(i), after);
	}

	public boolean removeField(String before, String fieldName, String after) {
		boolean updated = false;
		before = Pattern.quote(before);
		after = Pattern.quote(after);
		ArrayList<String> tmpMask = new ArrayList<String>();
		for (String mask : masks) {
			String m = mask.replaceAll(before + "\\" + sepFirst + fieldName + getEditKo() + "\\" + sepLast + after, "");
			if (!mask.equals(m)) {
				updated = true;
				mask = m;
			}
			tmpMask.add(mask);
		}
		masks = tmpMask;
		return updated;
	}

	@Override
	public boolean removeField(String fieldName) {
		boolean updated = false;
		ArrayList<String> tmpMask = new ArrayList<String>();
		for (String mask : masks) {
			String m = mask.replaceAll("\\" + sepFirst + fieldName + getEditKo() + "\\" + sepLast, "");
			if (!mask.equals(m)) {
				updated = true;
				mask = m;
			}
			tmpMask.add(mask);
		}
		masks = tmpMask;
		return updated;
	}

	@Override
	public void swapFields(String fieldName1, String fieldName2) {
		ArrayList<String> tmpMask = new ArrayList<String>();
		for (String mask : masks) {
			String tmpVar = "_swapField1_";
			mask = mask.replaceAll("\\" + sepFirst + fieldName1 + getEditKo() + "\\" + sepLast, "\\" + sepFirst + tmpVar + getEditKo() + "\\" + sepLast);
			mask = mask.replaceAll("\\" + sepFirst + fieldName2 + getEditKo() + "\\" + sepLast, "\\" + sepFirst + fieldName1 + getEditKo() + "\\" + sepLast);
			mask = mask.replaceAll("\\" + sepFirst + tmpVar + getEditKo() + "\\" + sepLast, "\\" + sepFirst + fieldName2 + getEditKo() + "\\" + sepLast);
			tmpMask.add(mask);
		}
		masks = tmpMask;
	}

	@Override
	public void replaceField(String oldFieldName, String newFieldName) {
		ArrayList<String> tmpMask = new ArrayList<String>();
		for (String mask : masks) {
			mask = mask.replaceAll("\\" + sepFirst + oldFieldName + getEditKo() + "\\" + sepLast, "\\" + sepFirst + newFieldName + "\\" + sepLast);
			tmpMask.add(mask);
		}
		masks = tmpMask;
	}

	private String getEditKo() {
		String result = "";
		if (koDetails)
			result = editKo;
		return result;
	}

	public void setKoDetails(boolean koDetails) {
		this.koDetails = koDetails;
		ArrayList<String> tmpMask = new ArrayList<String>();
		for (String mask : masks) {
			mask = mask.replaceAll(editKo + "\\" + sepLast, "\\" + sepLast);
			if (koDetails)
				mask = mask.replaceAll("\\" + sepLast, editKo + "\\" + sepLast);
			tmpMask.add(mask);
		}
		masks = tmpMask;
	}

	@Override
	public void replaceField(String oldFieldName, String newFieldName,
			String newCaption) {
		replaceField(oldFieldName, newFieldName);

	}

	@Override
	public void removeFields(String[] fieldNames) {
		for (String f : fieldNames)
			removeField(f);
	}

	@Override
	public void addField(String beforeField, String fieldName, String afterField) {
		ArrayList<String> tmpMask = new ArrayList<String>();
		for (String mask : masks) {
			mask += beforeField + sepFirst + fieldName + sepLast + afterField;
			tmpMask.add(mask);
		}
		masks = tmpMask;
	}

	public String getSelectedKeys() {
		return selectedKeys;
	}

	public void setSelectedKeys(String selectedKeys) {
		this.selectedKeys = selectedKeys;
	}

	public String getCheckedValue() {
		return checkedValue;
	}

	public void setCheckedValue(String checkedValue) {
		this.checkedValue = checkedValue;
	}

	public ArrayList<String> getFields() {
		ArrayList<String> result = new ArrayList<String>();
		if (masks != null && masks.size() > 0) {
			String m = Pattern.quote(masks.get(0));
			String editKo = "\\[\\.\\.\\.\\]";
			result = KRegExpr.getGroups("\\" + sepFirst + "{1}((?:\\s|.)+?)(" + editKo + ")?\\" + sepLast + "{1}", m, 1);
		}
		return result;
	}

	public ArrayList<String> getOperations(String sep1, String sep2) {
		ArrayList<String> result = new ArrayList<String>();
		sep1 = Pattern.quote(sep1);
		sep2 = Pattern.quote(sep2);
		if (masks != null && masks.size() > 0) {
			String m = Pattern.quote(masks.get(0));
			String editKo = "\\[\\.\\.\\.\\]";
			result = KRegExpr.getGroups(sep1 + "{1}((?:\\s|.)+?)(" + editKo + ")?" + sep2 + "{1}", m, 1);
		}
		return result;
	}

	public String getOperation(String op, String sep1, String sep2) {
		String result = null;
		if (masks != null && masks.size() > 0) {
			result = KRegExpr.getPart(sep1 + op + ":", sep2, masks.get(0));
			if (result.equals(masks.get(0)))
				result = null;
		}
		return result;
	}

	public String getOperation(String op, String sep1, String sep2, boolean withReplacement) {
		String result = getOperation(op, sep1, sep2);
		if (result != null && !"".equals(result)) {
			ArrayList<String> tmpMasks = new ArrayList<>();
			for (String m : masks) {
				m = m.replaceAll("(?i)" + "\\" + sep1 + op + ":" + Pattern.quote(result) + "\\" + sep2, "");
				tmpMasks.add(m);
			}
			masks = tmpMasks;
		}
		return result;
	}

	public static KMask repeat(String mask, int count) {
		KMask result = new KMask();
		for (int i = 0; i < count; i++)
			result.addMask(mask);
		return result;
	}

	public KMask getGroupByMask() {
		return groupByMask;
	}

	public void setGroupByMask(KMask groupByMask) {
		this.groupByMask = groupByMask;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
}
