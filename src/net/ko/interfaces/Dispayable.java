package net.ko.interfaces;

import net.ko.http.views.KFieldControl;
import net.ko.kobject.KObject;
import net.ko.types.HtmlControlType;

/**
 * Interface définissant les méthodes principales destinées à permettre l'affichage d'un objet de type KObject
 * @author jcheron
 *
 */
public interface Dispayable {
	/**
	 * crée un contrôle visuel de type "type" pour l'objet "ko" 
	 * @param ko objet instance d'une classe dérivée de KObject
	 * @param field membre de l'instance ko
	 * @param id identifiant HTML du contrôle
	 * @param caption étiquette du contrôle
	 * @param type type de contrôle
	 * @param options options HTML supplémentaires
	 * @param listObject liste (KListObject ou Array)
	 * @return contrôle visuel
	 */
	public KFieldControl getFc(KObject ko,String field,String id,String caption,HtmlControlType type,String options,Object listObject);	
	/**
	 * Retourne un champ <b>input</b> de formulaire de type $type permettant de mettre à jour la proprieté field<br>
	 * caption correspond au libelle du champ<br>
	 * type peut prendre une valeur parmi : khcText, khcHidden, khcCmb, khcList, khcRadio, khcCheckBox, khcDate, khcFile, khcImage, khcTextarea, khcPassWord
	 * @param ko  objet instance d'une classe dérivée de KObject
	 * @param field membre de l'instance ko
	 * @param id identifiant HTML du contrôle
	 * @param caption étiquette du contrôle
	 * @param type type du contrôle
	 * @param options options HTML supplémentaires
	 * @return contrôle visuel de type input text
	 */
	public KFieldControl getFcInput(KObject ko,String field,String id,String caption,HtmlControlType type,String options);

	/**
	 * @param ko objet instance d'une classe dérivée de KObject
	 * @param field membre de l'instance ko
	 * @param caption étiquette du contrôle
	 * @param options options HTML supplémentaires
	 * @return contrôle visuel de type label
	 */
	public KFieldControl getFcLabel(KObject ko,String field,String caption,String options);

	/**
	 * Retourne un champ <b>Select</b> de formulaire de type $type (khcList par défaut) permettant de mettre à jour la proprieté $field<br>
	 * caption correspond au libelle du champ<br>
	 * listObject est une collection (Klistobject), un tableau ou une chaîne représentant les élements de la liste<br>
	 * type peut prendre une valeur parmi : khcText, khcHidden, khcCmb, khcList, khcRadio, khcCheckBox, khcDate, khcFile, khcImage, khcTextarea, khcPassWord
	 * @param ko  objet instance d'une classe dérivée de KObject
	 * @param field membre de l'instance ko
	 * @param id identifiant HTML du contrôle
	 * @param listObject liste (KListObject ou Array)
	 * @param caption étiquette du contrôle
	 * @param type type du contrôle
	 * @param options options HTML supplémentaires
	 * @return contrôle visuel de type Liste
	 */
	public KFieldControl getFcList(KObject ko,String field,String id,Object listObject,String caption,HtmlControlType type,String options);
	/**
	 * Retourne un champ <b>input</b> de formulaire de type $type (khcRadio par défaut) permettant de mettre à jour la proprieté $field<br>
	 * caption correspond au libelle du champ<br>
	 * type peut prendre une valeur parmi : khcText, khcHidden, khcCmb, khcList, khcRadio, khcCheckBox, khcDate, khcFile, khcImage, khcTextarea, khcPassWord
	 * listObject est une collection (Klistobject), un tableau ou une chaîne représentant les options du groupe radio<br>
	 * @param ko  objet instance d'une classe dérivée de KObject
	 * @param field membre de l'instance ko
	 * @param id identifiant HTML du contrôle
	 * @param listObject liste (KListObject ou Array)
	 * @param caption étiquette du contrôle
	 * @param options options HTML supplémentaires
	 * @return contrôle visuel de type bouton radio
	 */
	public KFieldControl getFcRadio(KObject ko,String field,String id,String listObject,String caption,String options);
	/**
	 * Retourne un champ <b>input</b> de formulaire de type $type permettant de mettre à jour la proprieté $field<br>
	 * caption correspond au libelle du champ<br>
	 * type peut prendre une valeur parmi : khcText, khcHidden, khcCmb, khcList, khcRadio, khcCheckBox, khcDate, khcFile, khcImage, khcTextarea, khcPassWord
	 * listObject est une collection (Klistobject), un tableau ou une chaîne représentant les cases à cocher du groupe<br>
	 * @param ko  objet instance d'une classe dérivée de KObject
	 * @param field membre de l'instance ko
	 * @param id identifiant HTML du contrôle
	 * @param listObject liste (KListObject ou Array)
	 * @param caption étiquette du contrôle
	 * @param options options HTML supplémentaires
	 * @return contrôle visuel de type case à cocher
	 */
	public KFieldControl getFcCheckBox(KObject ko,String field,String id,Object listObject,String caption,String options);
	/**
	 * Retourne le contrôle visuel le mieux adapté à la modification du membre field de l'objet ko
	 * par analyse du type de données, et des paramètres spécifiés dans le fichier xml contrôleur
	 * @param ko objet instance d'une classe dérivée de KObject
	 * @param field membre de l'instance ko
	 * @return le contrôle visuel adapté
	 */
	public KFieldControl getBestControl(KObject ko,String field);
	public KFieldControl getDefaultControl(KObject ko,String field);
	public HtmlControlType getControlType(KObject ko,String field);

}
