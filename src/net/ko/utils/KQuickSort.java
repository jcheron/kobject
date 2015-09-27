package net.ko.utils;

import net.ko.displays.KObjectDisplay;
import net.ko.kobject.KListObject;
import net.ko.kobject.KMask;
import net.ko.kobject.KObject;

public class KQuickSort {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int compare(KObject ko1, KObject ko2, String attribute) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		int ret = 0;
		if (attribute.contains("{")) {
			ret = ko1._showWithMask(attribute).compareTo(ko2._showWithMask(attribute));
		} else if (ko1.getAttribute(attribute) instanceof Comparable) {
			Comparable c;
			c = (Comparable) ko1.getAttribute(attribute);
			ret = c.compareTo(ko2.getAttribute(attribute));
		}
		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int compare(KObject ko1, KObject ko2, String attribute, KObjectDisplay koDisplay) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		int ret = 0;
		if (attribute.contains("{")) {
			KMask mask = new KMask(attribute);
			ret = mask.show(ko1, koDisplay).compareTo(mask.show(ko2, koDisplay));
		} else if (ko1.getAttribute(attribute) instanceof Comparable) {
			Comparable c;
			c = (Comparable) ko1.getAttribute(attribute);
			ret = c.compareTo(ko2.getAttribute(attribute));
		}
		return ret;
	}

	public static void triRapide(KListObject<? extends KObject> kl, String attribute, boolean asc) {
		int longueur = kl.count();
		triRapide(kl, 0, longueur - 1, attribute, asc);
	}

	public static void triRapide(KListObject<? extends KObject> kl, String attribute, boolean asc, KObjectDisplay koDisplay) {
		int longueur = kl.count();
		triRapide(kl, 0, longueur - 1, attribute, asc, koDisplay);
	}

	@SuppressWarnings("unused")
	private static int partition(KListObject<? extends KObject> kl, int deb, int fin, String attribute) {
		return partition(kl, deb, fin, attribute, true);
	}

	private static int partition(KListObject<? extends KObject> kl, int deb, int fin, String attribute, boolean asc) {
		int compt = deb;
		KObject pivot = kl.get(deb);
		for (int i = deb + 1; i <= fin; i++) {
			try {
				if ((compare(kl.get(i), pivot, attribute) < 0 && asc) || (compare(kl.get(i), pivot, attribute) > 0 && asc != true)) {
					compt++;
					kl.swap(compt, i);
				}
			} catch (Exception e) {
			}
		}
		kl.swap(deb, compt);
		return (compt);
	}

	private static int partition(KListObject<? extends KObject> kl, int deb, int fin, String attribute, boolean asc, KObjectDisplay koDisplay) {
		int compt = deb;
		KObject pivot = kl.get(deb);
		for (int i = deb + 1; i <= fin; i++) {
			try {
				if ((compare(kl.get(i), pivot, attribute, koDisplay) < 0 && asc) || (compare(kl.get(i), pivot, attribute, koDisplay) > 0 && asc != true)) {
					compt++;
					kl.swap(compt, i);
				}
			} catch (Exception e) {
			}
		}
		kl.swap(deb, compt);
		return (compt);
	}

	private static void triRapide(KListObject<? extends KObject> kl, int deb, int fin, String attribute, boolean asc) {
		if (deb < fin) {
			int positionPivot = partition(kl, deb, fin, attribute, asc);
			triRapide(kl, deb, positionPivot - 1, attribute, asc);
			triRapide(kl, positionPivot + 1, fin, attribute, asc);
		}
	}

	private static void triRapide(KListObject<? extends KObject> kl, int deb, int fin, String attribute, boolean asc, KObjectDisplay koDisplay) {
		if (deb < fin) {
			int positionPivot = partition(kl, deb, fin, attribute, asc, koDisplay);
			triRapide(kl, deb, positionPivot - 1, attribute, asc, koDisplay);
			triRapide(kl, positionPivot + 1, fin, attribute, asc, koDisplay);
		}
	}
}
