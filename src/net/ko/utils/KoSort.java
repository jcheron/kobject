package net.ko.utils;

import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;

public class KoSort {
	private static int compare(KObject ko1, KObject ko2) {
		return ko1.compareTo(ko2);
	}

	public static void sort(KListObject<? extends KObject> kl, boolean asc) {
		int longueur = kl.count();
		triRapide(kl, 0, longueur - 1, asc);
	}

	@SuppressWarnings("unused")
	private static int partition(KListObject<? extends KObject> kl, int deb, int fin) {
		return partition(kl, deb, fin, true);
	}

	private static int partition(KListObject<? extends KObject> kl, int deb, int fin, boolean asc) {
		int compt = deb;
		KObject pivot = kl.get(deb);
		for (int i = deb + 1; i <= fin; i++) {
			if ((compare(kl.get(i), pivot) < 0 && asc) || (compare(kl.get(i), pivot) > 0 && asc != true)) {
				compt++;
				kl.swap(compt, i);
			}
		}
		kl.swap(deb, compt);
		return (compt);
	}

	private static void triRapide(KListObject<? extends KObject> kl, int deb, int fin, boolean asc) {
		if (deb < fin) {
			int positionPivot = partition(kl, deb, fin, asc);
			triRapide(kl, deb, positionPivot - 1, asc);
			triRapide(kl, positionPivot + 1, fin, asc);
		}
	}
}
