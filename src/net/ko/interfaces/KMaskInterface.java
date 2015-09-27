package net.ko.interfaces;

/**
 * Interface donnant la possibilté d'agir les champs/membres/méthodes utilisées et leur position
 * @author jcheron
 *
 */
public interface KMaskInterface {
	/**
	 * Supprime un champ/membre/méthode de la liste des champs
	 * @param fieldName nom du champ/membre/méthode
	 * @return vrai si le champ a été supprimé
	 */
	public boolean removeField(String fieldName);
	/**
	 * Echange la position de deux champs
	 * @param fieldName1 nom du premier champ
	 * @param fieldName2 nom du second champ
	 */
	public void swapFields(String fieldName1,String fieldName2);
	/**
	 * Remplace le contenu d'un champ par celui d'un autre
	 * @param oldFieldName ancien champ/membre/méthode
	 * @param newFieldName nouveau champ/membre/méthode
	 */
	public void replaceField(String oldFieldName,String newFieldName);
	/**
	 * Remplace le contenu d'un champ par celui d'un autre et modifie le caption
	 * @param oldFieldName ancien champ/membre/méthode
	 * @param newFieldName nouveau champ/membre/méthode
	 * @param newCaption nouvelle étiquette
	 */
	public void replaceField(String oldFieldName, String newFieldName,String newCaption);
	/**
	 * Supprime les champs/membres/méthodes dont le nom est spécifié
	 * @param fieldNames noms des champs/membres/méthodes à supprimer
	 */
	public void removeFields(String[] fieldNames);
	/**
	 * Ajoute le champ/membre/méthode dont le nom est spécifié
	 * @param fieldName noms des champs/membres/méthodes à ajouter
	 * @param beforeField chaîne à afficher devant le champ
	 * @param afterField chaîne à afficher après le champ
	 */
	public void addField(String beforeField,String fieldName,String afterField);
}
