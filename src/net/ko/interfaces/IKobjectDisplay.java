package net.ko.interfaces;

import javax.servlet.http.HttpServletRequest;

import net.ko.controller.KObjectController;
import net.ko.http.views.KFieldControl;
import net.ko.http.views.KPageList;
import net.ko.http.views.KobjectHttpAbstractView;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;

/**
 * Interface d'affichage d'un KOBject
 * 
 * @author jc
 * 
 */
public interface IKobjectDisplay {
	/**
	 * Retourne une chaîne représentant l'instance ko
	 * 
	 * @param ko
	 * @return chaîne d'affichage
	 */
	public String show(KObject ko);

	/**
	 * Retourne un contrôle visuel en lecture seule associé au membre memberName
	 * pour l'objet ko
	 * 
	 * @param ko
	 *            instance de KObject
	 * @param memberName
	 *            nom du membre de l'objet
	 * @param koc
	 *            Objet de validation correspondant aux définitions du fichier
	 *            kox.xml
	 * @param request
	 *            requête Http
	 * @return contrôle visuel
	 */
	public KFieldControl getReadOnlyControl(KObject ko, String memberName, KObjectController koc, HttpServletRequest request);

	/**
	 * Retourne une chaîne représentant le membre memberName de l'objet ko dans
	 * une liste
	 * 
	 * @param ko
	 * @param memberName
	 * @param request
	 *            Requête Http
	 * @return chaîne d'affichage
	 */
	public String showInList(KObject ko, String memberName, HttpServletRequest request);

	/**
	 * Retourne une chaîne représentant l'objet ko dans une liste
	 * 
	 * @param ko
	 * @param memberName
	 * @param request
	 *            Requête Http
	 * @return chaîne d'affichage
	 */
	// public String showInList(KObject ko, HttpServletRequest request);

	/**
	 * Retourne une chaîne représentant l'objet ko affiché avec un masque
	 * 
	 * @param ko
	 * @param mask
	 * @return chaîne d'affichage
	 */
	public String showWithMask(KObject ko, String mask, HttpServletRequest request);

	/**
	 * Retourne le caption de la classe
	 * 
	 * @param clazz
	 * @return chaîne d'affichage
	 */
	public String getCaption(Class<? extends KObject> clazz);

	/**
	 * Retourne la chaîne d'affichage du membre memberName de l'objet ko
	 * 
	 * @param ko
	 * @param memberName
	 * @return chaîne d'affichage
	 */
	public String getCaption(KObject ko, String memberName);

	/**
	 * Retourne l'étiquette associée à un type d'objet dérivé de KObject pour un
	 * formulaire
	 * 
	 * @param clazz
	 * @param koc
	 * @return
	 */
	public String getFormCaption(Class<? extends KObject> clazz, KObjectController koc);

	/**
	 * Retourne un contrôle visuel utilisé dans les formulaires associé au
	 * membre memberName pour l'objet ko
	 * 
	 * @param ko
	 *            instance de KObject
	 * @param memberName
	 *            nom du membre de l'objet
	 * @param koc
	 *            Objet de validation correspondant aux définitions du fichier
	 *            kox.xml
	 * @param request
	 *            requête Http
	 * @return contrôle visuel
	 */
	public KFieldControl getControl(KObject ko, String memberName, KObjectController koc, HttpServletRequest request);

	/**
	 * Permet d'introduire une zone personnalisée dans une liste sous la forme
	 * {zoneName}
	 * 
	 * @param zoneName
	 *            nom de la zone personalisée
	 * @param list
	 *            liste en cours
	 * @param request
	 *            requête HTTP
	 * @return le contenu de la zone
	 */
	public String getZone(String zoneName, KPageList list, HttpServletRequest request);

	/**
	 * Retourne la valeur du membre memberName de l'objet ko pour une inclusion
	 * ajax refreshFormValues
	 * 
	 * @param ko
	 * @param memberName
	 * @param koc
	 * @param request
	 * @return valeur du membre memberName modifiée
	 */
	public String getRefreshValue(KObject ko, String memberName, KObjectController koc, HttpServletRequest request);

	/**
	 * Retourne un tableau des membres supplémentaires à ajouter dans la réponse
	 * JSON d'une inclusion ajax refreshFormValues
	 * 
	 * @return tableau des membres
	 */
	public String[] getRefreshFields();

	/**
	 * Se produit avant le chargement de l'objet ko dans la vue view
	 * 
	 * @param KObject
	 *            instance de KObject chargée
	 * @param view
	 *            Vue permettant le chargement
	 * @param request
	 *            requête Http
	 */
	public void beforeLoading(KObject object, KobjectHttpAbstractView view, HttpServletRequest request);

	/**
	 * Se produit après le chargement de l'objet ko dans la vue view
	 * 
	 * @param ko
	 *            instance de KObject chargée
	 * @param view
	 *            Vue permettant le chargement
	 * @param request
	 *            requête Http
	 */
	public void afterLoading(KObject ko, KobjectHttpAbstractView view, HttpServletRequest request);

	/**
	 * Se produit avant le chargement de la liste
	 * 
	 * @param clazz
	 *            classe des objets de la liste
	 * @param list
	 *            liste chargée
	 * @param request
	 *            requête Http
	 */
	public void beforeLoading(Class<? extends KObject> clazz, KPageList list, HttpServletRequest request);

	/**
	 * Se produit après le chargment de la liste
	 * 
	 * @param kl
	 *            instance de KListObject chargée
	 * @param list
	 *            liste chargée
	 * @param request
	 *            requête Http
	 */
	public void afterLoading(KListObject<? extends KObject> kl, KPageList list, HttpServletRequest request);

	public KObjectController getKobjectController(KObjectController koc, HttpServletRequest request);
}
