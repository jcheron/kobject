package net.ko.http.views;

import java.util.HashMap;
import java.util.Map;

import net.ko.inheritance.KReflectObject;
import net.ko.types.HtmlControlType;
import net.ko.utils.KString;

/**
 * Repésente un contrôle visuel permettant l'affichage d'un champ/membre
 * 
 * @author jcheron
 * 
 */
public abstract class KFieldControl extends KReflectObject {
	protected boolean _allowNull = false;
	protected boolean _multiple = false;
	protected boolean _required;
	protected String caption;
	protected HtmlControlType fieldType;
	protected String id;
	protected int max = -1;
	protected int min = -1;
	protected String name;
	protected String value;
	protected int pos = 0;
	protected boolean readonly = false;
	protected Map<String, String> replacedAttributes;

	public KFieldControl() {
		super();
		replacedAttributes = new HashMap<>();
		value = "";
	}

	protected boolean serializable = true;

	public void allowNull(String value) {
		this._allowNull = KString.isBooleanTrue(value);
	}

	public void control(String cType) {
		fieldType = HtmlControlType.getType(cType);
	}

	/**
	 * Descend la position du contrôle à l'affichage
	 */
	public void down() {
		this.pos = this.pos + 2;
	}

	/**
	 * @return caption du contrôle
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @return le type du contrôle
	 */
	public HtmlControlType getFieldType() {
		return fieldType;
	}

	/**
	 * Retourne l'identifiant du contrôle
	 * 
	 * @return propriété id
	 */
	public String getId() {
		return id;
	}

	public int getMax() {
		return max;
	}

	public int getMin() {
		return min;
	}

	/**
	 * @return propriété name
	 */
	public String getName() {
		return name;
	}

	public int getPos() {
		return pos;
	}

	/**
	 * @return vrai si les valeurs nulles sont acceptées dans le champ
	 */
	public boolean isAllowNull() {
		return _allowNull;
	}

	/**
	 * @return vrai si le champ reçoit plusieurs valeurs
	 */
	public boolean isMultiple() {
		return _multiple;
	}

	/**
	 * @return vrai si la saisie est obligatoire
	 */
	public boolean isRequired() {
		return _required;
	}

	/**
	 * @return vrai si le contrôle est associé à un champ/membre sérializable
	 */
	public boolean isSerializable() {
		return serializable;
	}

	public void multiple(String value) {
		this._multiple = KString.isBooleanTrue(value);
	}

	public void required(String value) {
		this._required = KString.isBooleanTrue(value);
	}

	/**
	 * Détermine si le champ peut recevoir des valeurs nulles
	 * 
	 * @param allowNull
	 *            vrai si les valeurs nulles sont acceptées dans le champ
	 */
	public void setAllowNull(boolean allowNull) {
		this._allowNull = allowNull;
	}

	/**
	 * Définit l'étiquette du contrôle
	 * 
	 * @param caption
	 *            étiquette du contrôle
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * Définit le type du contrôle
	 * 
	 * @param fieldType
	 *            type de contrôle visuel
	 */
	public void setFieldType(HtmlControlType fieldType) {
		this.fieldType = fieldType;
	}

	/**
	 * Définit l'identifiant du contrôle
	 * 
	 * @param id
	 *            identifiant du contrôle
	 */
	public void setId(String id) {
		this.id = id;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public void setMin(int min) {
		this.min = min;
	}

	/**
	 * Détermine si le champ reçoit des valeurs multiples
	 * 
	 * @param multiple
	 *            vrai si le champ reçoit plusieurs valeurs
	 */
	public void setMultiple(boolean multiple) {
		this._multiple = multiple;
	}

	/**
	 * Retourne le nom du contrôle
	 * 
	 * @param name
	 *            nom du contrôle
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Définit la position d'un contrôle visuel à l'affichage par comparaison
	 * avec la position des autres contrôles à afficher Plus pos est petit, plus
	 * le contrôle est affiché vers le haut Plus pos est grand, plus le contrôle
	 * est affiché vers le bas
	 * 
	 * @param pos
	 *            position de l'objet
	 */
	public void setPos(int pos) {
		this.pos = pos;
	}

	/**
	 * Détermine si la saisie est obligatoire
	 * 
	 * @param required
	 */
	public void setRequired(boolean required) {
		this._required = required;
	}

	/**
	 * Précise si le contrôle sera associé à un champ sérializable dont le
	 * contenu sera à sauvegarder dans la base de données
	 * 
	 * @param serializable
	 */
	public void setSerializable(boolean serializable) {
		this.serializable = serializable;
	}

	/**
	 * Remonte la position du contrôle à l'affichage
	 */
	public void up() {
		this.pos = this.pos - 2;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isReadOnly() {
		return readonly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readonly = readOnly;
	}

	public void replaceAttribute(String attribute, String value) {
		replacedAttributes.put(attribute, value);
	}

	private String replaceAttribute(String originalString, String attribute, String value) {
		String result = originalString.replaceAll(attribute + "=(\"|\'|\\s){0,1}(.*?)(\"|\'|\\s){0,1}", attribute + "=$1" + value + "$3");
		return result;
	}

	private String replaceAttributes(String originalValue) {
		String result = originalValue;
		for (Map.Entry<String, String> e : replacedAttributes.entrySet()) {
			result = replaceAttribute(result, e.getKey(), e.getValue());
		}
		return result;
	}

	public boolean is_allowNull() {
		return _allowNull;
	}

	public void set_allowNull(boolean _allowNull) {
		this._allowNull = _allowNull;
	}

	public boolean is_multiple() {
		return _multiple;
	}

	public void set_multiple(boolean _multiple) {
		this._multiple = _multiple;
	}

	public boolean is_required() {
		return _required;
	}

	public void set_required(boolean _required) {
		this._required = _required;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
}
