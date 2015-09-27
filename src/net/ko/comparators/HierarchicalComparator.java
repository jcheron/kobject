package net.ko.comparators;

/**
 * Interface de comparaison d'objets hiérarchisés
 * 
 * @author jc
 * @param <T>
 */
public interface HierarchicalComparator<T> {
	/**
	 * Détermine si obj1 est le père de obj2
	 * 
	 * @param obj1
	 * @param obj2
	 * @return Vrai si obj1 est le père de obj2
	 */
	public boolean isParent(T obj1, T obj2);
}
