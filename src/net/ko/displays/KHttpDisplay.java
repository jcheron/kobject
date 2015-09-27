package net.ko.displays;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.ko.controller.KObjectController;
import net.ko.controller.KObjectFieldController;
import net.ko.controller.KXmlControllers;
import net.ko.framework.Ko;
import net.ko.http.js.KJavaScript;
import net.ko.http.views.KFieldControl;
import net.ko.http.views.KHtmlFieldControl;
import net.ko.kobject.KConstraint;
import net.ko.kobject.KListObject;
import net.ko.kobject.KMask;
import net.ko.kobject.KObject;
import net.ko.types.HtmlControlType;
import net.ko.types.KStringsType;
import net.ko.utils.KReflection;
import net.ko.utils.KRegExpr;
import net.ko.utils.KString;
import net.ko.utils.KStrings;

public class KHttpDisplay extends Display {

	public KHttpDisplay() {
		super();
	}

	@Override
	public KFieldControl getFc(KObject ko, String field, String id, String caption, HtmlControlType type, String options, Object listObject) {
		KHtmlFieldControl ret = new KHtmlFieldControl();
		try {
			if (ko != null) {
				Object o = ko.getAttribute(field);
				String value = "";
				if (o != null)
					value = KString.htmlSpecialChars(o.toString());
				KHtmlFieldControl hf = new KHtmlFieldControl(caption, value, field, id, type, listObject, options);
				ret = hf;
			}
		} catch (Exception e) {
		}
		return ret;
	}

	@Override
	public HtmlControlType getControlType(KObject ko, String field) {
		HtmlControlType result = HtmlControlType.khcNone;
		KXmlControllers controllers = Ko.kcontroller();
		KObjectController koc = null;
		if (controllers != null) {
			koc = controllers.getObjectController(ko.getClass().getSimpleName());
			if (koc != null) {
				KObjectFieldController fc = koc.getFieldController(field);
				if (fc != null) {
					// KConstraint c=ko.getConstraints().getConstraint(field);
					result = fc.getHtmlControlType();
					if (result == null || HtmlControlType.khcNone.equals(result) || HtmlControlType.khcText.equals(result))
						result = HtmlControlType.getCtForList(result);
				}
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public KFieldControl getBestControl(KObject ko, String field) {
		KFieldControl result = null;
		KObjectController koc = null;
		koc = getObjectController(ko.getClass().getSimpleName());
		if (koc != null) {
			KObjectFieldController fc = koc.getFieldController(field);
			if (fc != null) {
				KConstraint c = ko.getConstraints().getConstraint(field);
				HtmlControlType ct = fc.getHtmlControlType();
				if (ct == null || HtmlControlType.khcNone.equals(ct) || HtmlControlType.khcText.equals(ct))
					ct = HtmlControlType.getCtForList(ct);
				if (c != null) {
					result = createList(ko, field, (Class<KObject>) c.getClazz(), ct, c.isMultiple() || fc.isMultiple(), "");
				}
				if (fc.getList() != null) {
					if (fc.getList().matches("^\\{.*\\}$")) {
						String strList = fc.getList();
						strList = strList.replace("{", "");
						strList = strList.replace("}", "");
						KStrings ks = new KStrings(strList, KStringsType.kstJSON);
						result = ko.getFcList(field, field, ks, field + " :", ct);
						result.setMultiple(HtmlControlType.khcCheckBox.equals(ct));
					}
					if (fc.getList().matches("^\\[.*\\]$")) {
						String strList = fc.getList();
						String strListMask = "";
						strList = strList.replace("[", "");
						strList = strList.replace("]", "");
						if (strList.contains(":")) {
							String tmpList = KString.getBefore(strList, ":");
							strListMask = KString.getAfter(strList, ":");
							strList = tmpList;
						}
						try {
							Class clazz = Class.forName(strList);
							result = createList(ko, field, (Class<KObject>) clazz, ct, false, strListMask);
						} catch (Exception e) {
							Ko.klogger().log(Level.WARNING, "Impossible d'instancier : " + strList, e);
						}
					}
				}
				String caption = fc.getCaption();
				if (result != null && caption != null && !"".equals(caption))
					result.setCaption(caption);
				if (result != null) {
					result.setRequired(fc.isRequired());
					result.setPos(fc.getPos() * 10);
					if (fc.isMultiple())
						result.setMultiple(true);
					result.setMin(fc.getMin());
					result.setMax(fc.getMax());
					((KHtmlFieldControl) result).setOptions(fc.getOptions());
				}
			}
		}
		return result;
	}

	@Override
	public KFieldControl getDefaultControl(KObject ko, String field) {
		KFieldControl result = null;
		KObjectFieldController kofc = ko.getKObjectFieldController(field);
		if (kofc != null) {
			HtmlControlType htmlCT = HtmlControlType.khcText;
			htmlCT = kofc.getHtmlControlType();
			String options = " " + kofc.getOptions() + " ";
			String caption = "";
			boolean multiple = false;
			int pos = 0;
			if (!"".equals(kofc.getCaption()))
				caption = kofc.getCaption();
			pos = kofc.getPos();
			if (kofc.isMultiple())
				multiple = true;
			result = Display.getDefault().getFc(ko, kofc.getName(), kofc.getName(), caption, htmlCT, options, null);
			result.setMultiple(multiple);
			if (pos != 0)
				result.setPos(pos * 10);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private KFieldControl createList(KObject ko, String field, Class<KObject> clazz, HtmlControlType ct, boolean multiple, String listMask) {
		KFieldControl result = null;
		KListObject<KObject> list = null;
		String strList = null;
		try {
			if (!ct.isAjax()) {
				Object value = ko.getAttribute(field);
				if (ko.isLoaded() && value != null && ct.equals(HtmlControlType.khcReadOnlyList)) {
					list = Ko.getDao(clazz).readAll(value);
				} else
					list = Ko.getDao(clazz).readAll();
			} else {
				Object member = ko.getAttribute(field);
				if (member != null) {
					if (member instanceof KObject) {
						list = new KListObject<>(clazz);
						list.add((KObject) member, true);
					}
					else if (member instanceof KListObject) {
						list = new KListObject<>(clazz);
						list.addAll((KListObject<KObject>) member);
					}
					else {
						KListObject<KObject> tmpList = Ko.getDao(clazz).readAll();
						if (ct.equals(HtmlControlType.khcAjaxList)) {
							strList = "{" + tmpList.explode("{" + field + "}", ",") + "}";
						} else
							list = tmpList;
						// Ko.getDao(clazz).readAll((member +
						// "").split(Ko.krequestValueSep()));
					}
				}
			}
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Erreur sur la création de la liste pour le champ : " + field, e);
		}
		if (list != null)
			result = ko.getFcList(field, field, list, field + " :", ct);
		else
			result = ko.getFcList(field, field, strList, field + " :", ct);
		if (listMask != null && !"".equals(listMask))
			((KHtmlFieldControl) result).setListMask("{" + listMask + "}");
		result.setMultiple(multiple);
		if (multiple) {
			KListObject<KObject> kl;
			try {
				kl = (KListObject<KObject>) ko.getAttribute(field);
				if (kl != null)
					((KHtmlFieldControl) result).setValue(kl.getKeyValuesForHasAndBelongsToMany(";"));
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Erreur sur la création de la liste pour le champ : " + field, e);
			}
		}
		return result;
	}

	@Override
	public String getEndLigne() {
		return "<br>";
	}

	public ArrayList<String> getFields(String mask, String sepFirst, String sepLast) {
		String editKo = "\\[\\.\\.\\.\\]";
		return KRegExpr.getGroups("\\" + sepFirst + "(.+?)(" + editKo + ")?\\" + sepLast, mask, 1);
	}

	@Override
	public String showWithMask(KObject ko, String mask, String sepFirst, String sepLast, HttpServletRequest request) {
		KObjectDisplay koDisplay = Ko.koDisplays.get(ko.getClass().getSimpleName());
		if (koDisplay == null)
			koDisplay = Ko.defaultKoDisplay();
		return showWithMask(ko, mask, sepFirst, sepLast, koDisplay, request);
	}

	@SuppressWarnings("unchecked")
	public String showWithMask(KObject ko, String mask, String sepFirst, String sepLast, KObjectDisplay koDisplay, HttpServletRequest request) {
		// mask=mask.replace("\"", "\\\"");
		ArrayList<String> members = getFields(mask, sepFirst, sepLast);

		String editKo = "\\[\\.\\.\\.\\]";
		for (String mN : members) {
			String attributeName = mN;
			String mnReg = Pattern.quote(mN);
			String editKoValue = "";
			String[] captions = mN.split(":");
			String caption = mN;
			if (captions.length > 1) {
				caption = captions[0];
				mN = captions[1];
			}
			String[] memberName = mN.split("\\.");
			Object value = KReflection.getMemberValue(memberName[0], ko);
			Object value2 = null;
			String kl = "";
			if (memberName.length > 1) {
				if (value instanceof KListObject) {
					caption = koDisplay.getCaption(ko, memberName[0]);
					kl = getKlistObject(memberName[1], (KListObject<KObject>) value, caption, ko, attributeName, request);
					mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + editKo + "\\" + sepLast, kl);
					mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + "\\" + sepLast, kl);
					mask = mask.replace(sepFirst + "_" + mN + "[...]" + sepLast, mN);
					mask = mask.replace(sepFirst + "_" + mN + sepLast, mN);
				} else if (value instanceof Object) {
					value2 = KReflection.getMemberValue(memberName[1], value);
					if (value2 instanceof KListObject) {
						caption = koDisplay.getCaption(ko, memberName[1]);
						kl = getKlistObject((KListObject<KObject>) value, caption, ko, attributeName, request);
						mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + editKo + "\\" + sepLast, kl);
						mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + "\\" + sepLast, kl);
						mask = mask.replace(sepFirst + "_" + mN + "[...]" + sepLast, mN);
						mask = mask.replace(sepFirst + "_" + mN + sepLast, mN);
					} else {
						if (value2 instanceof KObject) {
							editKoValue = KJavaScript.getKoEditBtn((KObject) value2);
							value2 = koDisplay.showInList(ko, memberName[1], request);
						} else {
							// value2=(value2+"").replace("[...]",
							// KJavaScript.getKoEditBtn(ko));
							value2 = (koDisplay.showInList(ko, memberName[1], request) + "").replace("[...]", KJavaScript.getKoEditBtn(ko));
						}
						mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + editKo + "\\" + sepLast, value2 + editKoValue);
						mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + "\\" + sepLast, value2 + "");
					}
				} else {
					// value=(value+"").replace("[...]",
					// KJavaScript.getKoEditBtn(ko));
					value = (koDisplay.showInList(ko, memberName[0], request)).replace("[...]", KJavaScript.getKoEditBtn(ko));
					mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + editKo + "\\" + sepLast, value + editKoValue);
					mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + "\\" + sepLast, value + "");
				}
			} else {
				if (value instanceof KListObject) {
					caption = koDisplay.getCaption(ko, memberName[0]);
					// TOTO Vérifier...passer koDisplay ?
					kl = getKlistObject((KListObject<KObject>) value, caption, ko, attributeName, request);
					mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + editKo + "\\" + sepLast, kl);
					mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + "\\" + sepLast, kl);
					mask = mask.replace(sepFirst + "_" + mN + "[...]" + sepLast, mN);
					mask = mask.replace(sepFirst + "_" + mN + sepLast, mN);
				} else {
					if (value instanceof KObject) {
						editKoValue = KJavaScript.getKoEditBtn((KObject) value);
						value = koDisplay.showInList(ko, memberName[0], request);
					} else {
						value = (koDisplay.showInList(ko, memberName[0], request)).replace("[...]", KJavaScript.getKoEditBtn(ko));
					}
					mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + editKo + "\\" + sepLast, Matcher.quoteReplacement(value + "") + editKoValue);
					mask = mask.replaceAll("(?i)" + "\\" + sepFirst + mnReg + "\\" + sepLast, Matcher.quoteReplacement(value + ""));
				}
			}
		}
		mask = mask.replace("\\\"", "\"");
		return mask;
	}

	private String getKlistObject(String memberName, KListObject<KObject> list, String label, KObject ko, String attributeName, HttpServletRequest request) {
		int nb = list.count();
		String listContent = "";
		KObjectDisplay koDisplay = Ko.koDisplays.get(list.getClazz().getSimpleName());
		String cAttribute = KString.cleanHTMLAttribute(attributeName);
		if (memberName == null || "".equals(memberName)) {
			// memberName="toString";
			listContent = koDisplay.showInList(ko, attributeName, request);
		} else {
			if (memberName.matches(".*?\\[.+?\\].*?"))
				listContent = getListContent(list, memberName, koDisplay, "[", "]", request);
			else
				listContent = getListContent(list, "{" + memberName + "}", koDisplay, "{", "}", request);
		}

		String kl = "";
		if (nb > 0) {
			String id = ko.getUniqueIdHash();
			String js = "javascript:Forms.Framework.toogle(\"" + id + "-" + cAttribute + "\",this);";
			kl = "<a class='toogleable' href='#' onclick='" + js + "'>" + label + " (" + nb + ")&nbsp;<div id='imgToogle" + id + "-" + cAttribute + "' class='arrow-down'></div></a>";
			kl += "<div id='idL" + id + "-" + cAttribute + "' style='display:none'>" + listContent + "</div>";
		} else {
			kl = label + "(0)";
		}
		return kl;
	}

	private String getListContent(KListObject<KObject> list, String mask, KObjectDisplay koDisplay, String sepfirst, String seplast, HttpServletRequest request) {
		String result = "";
		KMask kmask = new KMask(mask);
		kmask.setRequest(request);
		kmask.setSepFirst(sepfirst);
		kmask.setSepLast(seplast);
		result = kmask.show(list, null, null, koDisplay);
		return result;
	}

	private String getKlistObject(KListObject<KObject> list, String name, KObject ko, String attributeName, HttpServletRequest request) {
		return getKlistObject("", list, name, ko, attributeName, request);
	}

	private KObjectController getObjectController(String className) {
		KXmlControllers controllers = Ko.kcontroller();
		KObjectController koc = null;
		if (controllers != null) {
			koc = controllers.getObjectController(className);
		}
		return koc;
	}

	@Override
	public String showWithMask(KObject ko, String mask, String sepFirst, String sepLast, KObjectDisplay koDisplay) {
		return showWithMask(ko, mask, sepFirst, sepLast, koDisplay, null);
	}
}
