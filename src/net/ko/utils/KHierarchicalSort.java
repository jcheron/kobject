package net.ko.utils;

import java.util.ArrayList;
import java.util.List;

import net.ko.comparators.HierarchicalComparator;

/**
 * @author jc
 * 
 */
public class KHierarchicalSort {
	/**
	 * Trie une liste en fonction du comparateur hiérarchique passé en paramètre
	 * 
	 * @param list
	 *            Liste à ordonner
	 * @param comparator
	 *            Comparateur hiérarchique
	 */
	public static <T extends Object> void sort(List<T> list, HierarchicalComparator<T> comparator) {
		List<T> sortedList = new ArrayList<T>();
		while (list.size() > 0) {
			sortedList = innerSort(list, list.get(0), sortedList, comparator);
		}
		list.addAll(0, sortedList);
	}

	private static <T extends Object> List<T> innerSort(List<T> list, T object, List<T> sortedList, HierarchicalComparator<T> comparator) {
		int i = 0;
		while (i < list.size()) {
			if (!list.get(i).equals(object)) {
				if (comparator.isParent(list.get(i), object)) {
					innerSort(list, list.get(i), sortedList, comparator);
				}
			}
			i++;
		}
		list.remove(object);
		sortedList.add(object);
		return sortedList;
	}
}
