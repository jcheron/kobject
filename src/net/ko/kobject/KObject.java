/**
 * Classe KObject
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2012
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  LGPL License
 * @version $Id: KObject.java,v 1.10 2011/01/21 01:22:52 jcheron Exp $
 * @package ko.kobject
 */
package net.ko.kobject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ko.cache.KCache;
import net.ko.controller.KObjectController;
import net.ko.controller.KObjectFieldController;
import net.ko.converters.DateFormatter;
import net.ko.db.KDataBase;
import net.ko.db.KDataTypeConverter;
import net.ko.displays.Display;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.http.views.KFieldControl;
import net.ko.inheritance.KReflectObject;
import net.ko.ksql.KSqlQuery;
import net.ko.persistence.orm.KMetaField;
import net.ko.persistence.orm.KMetaObject;
import net.ko.types.HtmlControlType;
import net.ko.utils.Converter;
import net.ko.utils.KHash;
import net.ko.utils.KProperties;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;

/**
 * Classe abstraite fournissant des services de sérialisation/désérialisation
 * par rapport à une base de données
 * 
 * Membres :
 * <ul>
 * <li>id est l'identifiant de l'objet, il doit être de type primitif.</li>
 * <li>recordStatus définit le statut de l'objet : rsNone =>non modifié, rsNew :
 * ajouté, rsUpdate: modifié, rsDelete: supprimé<br>
 * recordStatus est utilisé par la méthode updateToDB pour effectuer la mise à
 * jour de l'objet dans la base de données</li>
 * </ul>
 * 
 */
@SuppressWarnings("rawtypes")
public class KObject implements Comparable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5972667339120593280L;
	private boolean loadConstraints;
	protected KListConstraint constraints;
	protected Object id;
	protected KRecordStatus recordStatus;
	protected Map<String, Object> row;
	protected String sql;
	protected transient KObjectController controller;
	protected Map<String, Object> keyValues;
	protected transient KMetaObject<? extends KObject> metaObject;

	/**
	 * Constructeur, à appeler impérativement dans toutes les classes dérivées
	 */
	public KObject() {
		super();
		row = new LinkedHashMap<String, Object>();
		metaObject = null;
		recordStatus = KRecordStatus.rsNone;
		constraints = new KListConstraint();
		loadConstraints = true;
		id = -1;
	}

	private static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}

	/**
	 * Retourne le type d'un champ/membre de l'objet
	 * 
	 * @param fieldName
	 *            nom du champ
	 * @return le type du champ
	 * @throws NoSuchFieldException
	 */
	public Class<?> getFieldType(String fieldName) throws NoSuchFieldException {
		Class<?> cls = null;
		Field f = getField(fieldName);
		if (f != null)
			cls = f.getType();
		return cls;
	}

	/**
	 * Détermine si le membre fieldName de la classe koClass est persistant
	 * (sérialisable dans la BDD)
	 * 
	 * @param koClass
	 *            Classe dérivée de KObject
	 * @param fieldName
	 *            nom du champ/membre
	 * @return vrai si le champ est sérialisable
	 */
	public static boolean isSerializable(Class<? extends KObject> koClass, String fieldName) {
		KObject obj;
		boolean result = false;
		try {
			obj = Ko.getKoInstance(koClass);
			Class<?> cls = null;
			cls = obj.getFieldType(fieldName);
			result = cls != null && KDataTypeConverter.isSerializable(cls);
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/**
	 * @param fields
	 *            Liste des membres de la classe type
	 * @param type
	 *            Classe dérivée de KObject
	 * @return Liste des membres de la classe type
	 */
	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		for (Field field : type.getDeclaredFields()) {
			fields.add(field);
		}

		if (type.getSuperclass() != null && !type.getSuperclass().equals(KObject.class)) {
			fields = getAllFields(fields, type.getSuperclass());
		}

		return fields;
	}

	/**
	 * Charge depuis la base de données un objet présent dans une contrainte
	 * 
	 * @param clazz
	 *            Classe dérivée de KObject
	 * @param db
	 *            instance de KDataBase
	 * @param where
	 *            condition SQL
	 * @return instance de KObject chargée
	 */
	public static KObject getForConstraint(Class<? extends KObject> clazz, KDataBase db, String where) {
		String criteria = where.replace("and", ",");
		KObject ko;
		try {
			ko = clazz.newInstance();
			ko.setAttributes(criteria, ",");
			ko = (KObject) KCache.getObjectValue(ko);
			// ko=(KObject) KObject.getCache().getObjectValue(ko);
		} catch (Exception e) {
			ko = null;
		}
		return ko;
	}

	/**
	 * Retourne le nom du membre définit en tant que clé primaire dans la classe
	 * clazz
	 * 
	 * @param clazz
	 *            classe dérivée de KObject
	 * @return nom du membre
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static String getFirstKey(Class<? extends KObject> clazz) throws InstantiationException, IllegalAccessException {
		KObject ko = Ko.getKoInstance(clazz);
		if (ko != null)
			return ko.getFirstKey();
		else
			return "id";
	}

	/**
	 * Retourne le nom du champ associé à un membre
	 * 
	 * @param memberName
	 * @return nom du champ
	 */
	public String getFieldName(String memberName) {
		return getMetaObject().getFieldName(memberName);
	}

	private String getMemberName(String fieldName) {
		return getMetaObject().getMemberName(fieldName);
	}

	/**
	 * Retourne la liste des membres de l'objet
	 * 
	 * @return Liste des membres
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public ArrayList<String> getFieldNames() throws IllegalArgumentException, IllegalAccessException {
		return getFieldNames(getClass());
	}

	private ArrayList<String> getFieldNames(Class clazz) throws IllegalArgumentException, IllegalAccessException {
		ArrayList<String> result = new ArrayList<String>();
		for (Field field : clazz.getDeclaredFields()) {
			result.add(field.getName());
		}

		if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(KObject.class)) {
			result.addAll(getFieldNames(clazz.getSuperclass()));
		}

		return result;
	}

	/**
	 * Retourne le membre java correspondant au nom passé en paramétre
	 * 
	 * @param fieldName
	 *            nom du membre
	 * @return membre java de l'objet
	 * @throws NoSuchFieldException
	 */
	public Field getField(String fieldName) throws NoSuchFieldException {
		return getField(getClass(), fieldName);
	}

	protected Object __getAttribute(String attribute) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field f = getField(this.getClass(), attribute);
		if (f != null)
			return __getAttribute(f, false);
		else
			return null;
	}

	protected Object __getAttribute(String attribute, boolean noLazy) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field f = getField(this.getClass(), attribute);
		if (f != null)
			return __getAttribute(f, noLazy);
		else
			return null;
	}

	protected Object __getAttribute(Field field) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		return __getAttribute(field, false);
	}

	protected Object __getAttribute(Field field, boolean noLazy) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		if (!field.getName().equals("id")) {
			field.setAccessible(true);
			if (field.getType().equals(java.sql.Date.class)) {
				if (field.get(this) != null)
					return DateFormatter.toKoDate((java.sql.Date) field.get(this));
				else
					return "";
			}
			Object result = field.get(this);
			if (result == null && !noLazy) {
				KConstraint co = constraints.getConstraint(field.getName());
				if (co != null) {
					if (co.isLazy()) {
						KDataBase db = Ko.kdefaultDatabase(co.getClazz());
						co.load(db);
						result = field.get(this);
						KDebugConsole.print("lazyLoading:" + co, "KOBJECT", getClass().getSimpleName() + ".__getAttribute");
					}
				}
			}
			return result;
		} else
			return id;
	}

	/**
	 * @param attribute
	 * @param mixValue
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	protected void __setAttribute(String attribute, Object mixValue, boolean fromDb) throws SecurityException, NoSuchFieldException, IllegalAccessException {
		if (!attribute.equals("id")) {
			Field f = getField(this.getClass(), attribute);
			f.setAccessible(true);
			try {
				f.set(this, mixValue);
			} catch (IllegalArgumentException iae) {
				if (f.getType().equals(java.sql.Date.class)) {
					if (mixValue instanceof Number)
						f.set(this, new java.sql.Date(Long.valueOf(mixValue + "")));
					else if ((mixValue + "").contains(" ")) {
						if (!fromDb)
							f.set(this, DateFormatter.getKoDateTime(mixValue + ""));
						else
							f.set(this, DateFormatter.getSqlDateTime(mixValue + ""));
					}
					else {
						if (!fromDb)
							f.set(this, DateFormatter.getKoDate(mixValue + ""));
						else
							f.set(this, DateFormatter.getSqlDate(mixValue + ""));
					}
				} else if (f.getType().equals(java.sql.Time.class)) {
					if (mixValue instanceof Number)
						f.set(this, new java.sql.Time(Long.valueOf(mixValue + "")));
					else
						f.set(this, DateFormatter.getKoTime(mixValue + ""));
				} else if (f.getType().equals(java.sql.Timestamp.class)) {
					f.set(this, Timestamp.valueOf(mixValue + ""));
				} else
					f.set(this, Converter.convert(mixValue + "", f.getType()));
			}
		} else
			setId(mixValue);
	}

	protected void _setAttributeWithSetter(String attribute, Object mixValue) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
		if (!attribute.equals("id")) {
			Field f = getField(this.getClass(), attribute);
			f.setAccessible(true);
			try {
				_setter(attribute, mixValue);
			} catch (IllegalArgumentException iae) {
				_setter(attribute, Converter.convert(mixValue + "", f.getType()));
			}
		} else
			setId(mixValue);
	}

	protected void _setter(String attribute, Object mixValue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		KReflectObject.kinvokeForSetter("set" + KString.capitalizeFirstLetter(attribute), this, new Object[] { mixValue });
	}

	protected void __setRow() throws IllegalArgumentException, IllegalAccessException {
		__setRow(false);
	}

	protected void __setRow(boolean all) throws IllegalArgumentException, IllegalAccessException {
		row.put("id", id);
		KMetaObject<? extends KObject> metaObject = getMetaObject();
		for (KMetaField metaField : metaObject.getFields().values()) {
			if (KDataTypeConverter.toDbType(metaField.getField().getType()) != null) {
				metaField.getField().setAccessible(true);
				Object value = metaField.getField().get(this);
				if (metaField.isNullable() || value != null || all)
					row.put(metaField.getFieldName(), value);
			}
		}
	}

	protected Object getDbFieldValue(String fieldName, boolean all) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Object result = null;
		Field field = this.getField(fieldName);
		field.setAccessible(true);
		result = field.get(this);
		// getMetaObject().getField(fieldName).isNullable()
		// if (!Modifier.isTransient(field.getModifiers())) {
		// String type = KTableCreator.javaTypeToDbType(field.getType());
		// if (fieldName.equals("id"))
		// type = "id";
		// if (type != null) {
		// KObjectFieldController kofc =
		// getKObjectFieldController(field.getName());
		// boolean allowAdd = false;
		// if (kofc != null) {
		// allowAdd = (kofc.isAllowNull() || (field.get(this) != null || all));
		// } else {
		// allowAdd = (field.get(this) != null || all);
		// }
		// if (allowAdd) {
		// result = field.get(this);
		// }
		// }
		// }
		return result;
	}

	protected void __setRow(boolean all, KDataBase db) throws IllegalArgumentException, IllegalAccessException {
		row.put("id", id);
		KMetaObject<? extends KObject> metaObject = getMetaObject();
		for (KMetaField metaField : metaObject.getFields().values()) {
			if (db.javaTypeToDbType(metaField) != null) {
				metaField.getField().setAccessible(true);
				Object value = metaField.getField().get(this);
				if (metaField.isNullable() || value != null || all)
					row.put(metaField.getFieldName(), value);
			}
		}/*
		 * List<Field> lfields = getAllFields(new LinkedList<Field>(),
		 * this.getClass()); for (Field field : lfields) {
		 * field.setAccessible(true); if
		 * (!Modifier.isTransient(field.getModifiers())) { String type =
		 * KTableCreator.javaTypeToDbType(field.getType()); if (type != null) {
		 * KObjectFieldController kofc =
		 * getKObjectFieldController(field.getName()); boolean allowAdd = false;
		 * if (kofc != null) { allowAdd = (kofc.isAllowNull() ||
		 * (field.get(this) != null || all)); } else { allowAdd =
		 * (field.get(this) != null || all); } if (allowAdd) { { try {
		 * row.put(field.getName(), field.get(this)); } catch (SecurityException
		 * e) { } } } } } }
		 */
	}

	protected Class<?> getClassInstance(String member) {
		Class<?> result = null;
		try {
			Field f = KReflectObject.getField(getClass(), member);
			if (f != null) {
				f.setAccessible(true);
				result = f.getType();
			}
		} catch (NoSuchFieldException | IllegalArgumentException e) {
			result = null;
		}
		return result;
	}

	public boolean hasConstraintWith(Class<? extends KObject> clazz, Class<? extends KConstraint> constraintClazz) {
		return getConstraints().constraintExists(clazz, constraintClazz);
	}

	protected KConstraintBelongsTo belongsTo(Class<? extends KObject> clazz) {
		String member = clazz.getSimpleName().toLowerCase().substring(1);
		return belongsTo(member, clazz);
	}

	protected KConstraintBelongsTo belongsTo(String member) {
		KConstraintBelongsTo c = null;

		String destFieldKey;
		try {
			Class<? extends KObject> clazz;
			KObject ko = (KObject) __getAttribute(member);
			if (ko != null) {
				clazz = ko.getClass();
				destFieldKey = ko.getFirstKey();
			} else {
				clazz = (Class<? extends KObject>) getClassInstance(member);
				destFieldKey = "id";
			}
			if (clazz != null) {
				String fieldKey = "id" + clazz.getSimpleName().substring(1);
				String destTable = KString.unCapitalizeFirstLetter(clazz.getSimpleName().substring(1));
				c = belongsTo(member, clazz, fieldKey, destTable, destFieldKey);
			}
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Erreur de construction de belongsTo pour le membre " + member + " de l'instance de " + this.getClass() + " depuis la base de données", e);
		}
		return c;
	}

	protected KConstraintBelongsTo belongsTo(String member, Class<? extends KObject> clazz) {
		KConstraintBelongsTo c = null;
		Class<? extends KObject> clazzCheck = (Class<? extends KObject>) getClassInstance(member);
		if (clazzCheck != null && clazz.equals(clazzCheck)) {
			String destFieldKey;
			try {
				KObject ko = (KObject) __getAttribute(member);
				if (ko != null) {
					destFieldKey = ko.getFirstKey();
				} else {
					destFieldKey = "id";
				}
				if (clazz != null) {
					String fieldKey = "id" + clazz.getSimpleName().substring(1);
					String destTable = KString.unCapitalizeFirstLetter(clazz.getSimpleName().substring(1));
					c = belongsTo(member, clazz, fieldKey, destTable, destFieldKey);
				}
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Erreur de construction de belongsTo pour le membre " + member + " de l'instance de " + this.getClass() + " depuis la base de données", e);
			}
		}
		return c;
	}

	/*
	 * protected KConstraint belongsTo(String member, Class<? extends KObject>
	 * clazz, String where) { KConstraintBelongsTo c = new
	 * KConstraintBelongsTo(member, clazz, where); c.setOwner(this);
	 * constraints.add(c); return c; }
	 * 
	 * protected KConstraint belongsTo(String member, String where) { KObject
	 * ko; KConstraint c = null; try { ko = (KObject) __getAttribute(member); c
	 * = belongsTo(member, ko.getClass(), where); } catch (Exception e) {
	 * Ko.klogger().log(Level.WARNING, "Impossible d'acceder au membre " +
	 * member + " de l'instance de " + this.getClass() +
	 * " pour définir la contrainte belongsTo", e); } return c; }
	 */
	protected KConstraintBelongsTo belongsTo(String member, Class<? extends KObject> clazz, String fieldKey, String destTable, String destFieldKey) {
		KConstraintBelongsTo c = new KConstraintBelongsTo(member, clazz, fieldKey, destTable, destFieldKey);
		c.setOwner(this);
		constraints.add(c);
		return c;
	}

	protected KConstraint hasAndBelongsToMany(Class<? extends KObject> joinClass, Class<? extends KObject> clazz) {
		String member = KString.unCapitalizeFirstLetter(clazz.getSimpleName().substring(1)) + "s";
		return hasAndBelongsToMany(joinClass, member, clazz);
	}

	protected KConstraint hasAndBelongsToMany(Class<? extends KObject> joinClass, String member) {
		Object list;
		try {
			list = getAttribute(member);
			if (list instanceof KListObject) {
				Class<? extends KObject> clazz = ((KListObject) list).getClazz();
				return hasAndBelongsToMany(joinClass, member, clazz);
			}
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	protected KConstraint hasAndBelongsToMany(Class<? extends KObject> joinClass, String member, Class<? extends KObject> clazz) {
		KObject destInstance;
		try {
			destInstance = Ko.getKoInstance(clazz);
			KObject joinInstance = Ko.getKoInstance(joinClass);
			String fieldKey = this.getFirstKey();
			String destTable = KSqlQuery.getTableName(destInstance.getTableName());
			String destFieldKey = destInstance.getFirstKey();
			List<String> joinFieldsKey = joinInstance.getKeyFields();
			if (joinFieldsKey.size() == 2) {
				String joinFieldKey = joinFieldsKey.get(0);
				String joinDestFieldKey = joinFieldsKey.get(1);
				if (joinFieldKey.toLowerCase().contains(destTable.toLowerCase())) {
					joinFieldKey = joinFieldsKey.get(1);
					joinDestFieldKey = joinFieldsKey.get(0);
				}

				return hasAndBelongsToMany(member, clazz, joinClass, fieldKey, destTable, destFieldKey, joinFieldKey, joinDestFieldKey);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	protected KConstraint hasAndBelongsToMany(String member, Class<? extends KObject> targetClass, Class<? extends KObject> joinClass, String primaryKey, String targetTable, String targetPrimaryKey, String joinForeignKey, String joinTargetForeignKey) {

		KConstraintHasAndBelongsToMany c = new KConstraintHasAndBelongsToMany(member, targetClass, joinClass, primaryKey, targetTable, targetPrimaryKey, joinForeignKey, joinTargetForeignKey);
		c.setOwner(this);
		constraints.add(c);
		return c;
	}

	protected KConstraint hasMany(Class<? extends KObject> clazz) {
		String member = clazz.getSimpleName().toLowerCase().substring(1) + "s";
		return hasMany(member, clazz);
	}

	protected KConstraint hasMany(String member) {
		KConstraint c = null;

		String fieldKey;
		String destFieldKey;
		String destTable;
		try {
			Class<? extends KObject> clazz;
			KObject ko = (KObject) __getAttribute(member);
			if (ko != null) {
				clazz = ko.getClass();
			} else {
				clazz = (Class<? extends KObject>) getClassInstance(member);
			}
			if (clazz != null) {
				fieldKey = getFirstKey();
				destFieldKey = "id" + getClass().getSimpleName().substring(1);
				destTable = KString.unCapitalizeFirstLetter(clazz.getSimpleName().substring(1));
				c = hasMany(member, clazz, fieldKey, destTable, destFieldKey);
			}
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Erreur de construction de hasMany pour le membre " + member + " de l'instance de " + this.getClass() + " depuis la base de données", e);
		}
		return c;
	}

	protected KConstraint hasMany(String member, Class<? extends KObject> clazz) {
		KConstraint c = null;
		String fieldKey;
		String destFieldKey;
		String destTable;
		try {
			fieldKey = getFirstKey();
			destFieldKey = "id" + getClass().getSimpleName().substring(1);
			destTable = KString.unCapitalizeFirstLetter(clazz.getSimpleName().substring(1));
			c = hasMany(member, clazz, fieldKey, destTable, destFieldKey);
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Erreur de construction de belongsTo pour le membre " + member + " de l'instance de " + this.getClass() + " depuis la base de données", e);
		}

		return c;
	}

	protected KConstraint hasMany(String member, Class<? extends KObject> clazz, String where) {
		KConstraintHasMany c = new KConstraintHasMany(member, clazz, where);
		c.setOwner(this);
		constraints.add(c);
		return c;
	}

	protected KConstraint hasMany(String member, Class<? extends KObject> clazz, String primaryKey, String targetTable, String targetPrimaryKey) {
		KConstraintHasMany c = new KConstraintHasMany(member, clazz, primaryKey, targetTable, targetPrimaryKey);
		c.setOwner(this);
		constraints.add(c);
		return c;
	}

	protected KConstraint hasManyBelongsTo(Class<? extends KObject> clazz, Class<? extends KObject> belongsToClass) {
		String member = clazz.getSimpleName().toLowerCase().substring(1) + "s";
		String fieldKey = getFirstKey();
		String destFieldKey = "id" + getClass().getSimpleName().substring(1);
		String destTable = KString.unCapitalizeFirstLetter(clazz.getSimpleName().substring(1));
		return hasManyBelongsTo(member, clazz, fieldKey, destTable, destFieldKey, belongsToClass, null);
	}

	protected KConstraint hasManyBelongsTo(Class<? extends KObject> clazz, Class<? extends KObject> belongsToClass, String belongsToField) {
		String member = clazz.getSimpleName().toLowerCase().substring(1) + "s";
		String fieldKey = getFirstKey();
		String destFieldKey = "id" + getClass().getSimpleName().substring(1);
		String destTable = KString.unCapitalizeFirstLetter(clazz.getSimpleName().substring(1));
		return hasManyBelongsTo(member, clazz, fieldKey, destTable, destFieldKey, belongsToClass, belongsToField);
	}

	protected KConstraint hasManyBelongsTo(String member, Class<? extends KObject> clazz, String primaryKey, String targetTable, String targetPrimaryKey, Class<? extends KObject> belongsToClass, String belongsToField) {
		KConstraintHasManyBelongsTo c = new KConstraintHasManyBelongsTo(member, clazz, primaryKey, targetTable, targetPrimaryKey, belongsToClass, belongsToField);
		c.setOwner(this);
		constraints.add(c);
		return c;
	}

	/**
	 * Alimente le(s) membre(s) de l'objet associé(s) à une contrainte hasMany à
	 * partir des objets de la liste passée en paramètre
	 * 
	 * @param listObject
	 *            liste d'objets
	 */
	public void setHasMany(KListObject<KObject> listObject) {
		for (KConstraint c : constraints) {
			if (c instanceof KConstraintHasMany)
				if (c.getClazz().equals(listObject.getClazz())) {
					try {
						setAttribute(c.getMember(), listObject.select(c.getWhere("")), false);
					} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
					}
				}
		}
	}

	/**
	 * Ajoute l'instance courante à la liste passée en paramètre<br>
	 * L'ajout ne sera persistant qu'après mise à jour de la liste dans la base
	 * 
	 * @param kl
	 *            Liste d'objets
	 */
	public void addIn(KListObject<? extends KObject> kl) {
		kl.add(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		return 0;
	}

	/**
	 * Effectue une copie membre à membre de l'objet passé en paramètre vers
	 * l'instance courante
	 * 
	 * @param ko
	 *            objet à copier
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public void copyFrom(KObject ko) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		List<Field> lfields = getAllFields(new LinkedList<Field>(), this.getClass());
		for (Field field : lfields) {
			field.setAccessible(true);
			field.set(this, ko.getAttribute(field.getName()));
		}
		// this.setAttributes(ko.getAttributes());
	}

	/**
	 * Supprime l'instance courante de la liste passée en paramètre<br>
	 * La suppression ne sera effective qu'après mise à jour de la liste dans la
	 * base
	 * 
	 * @param kl
	 *            liste d'objets
	 * @return vrai si la suppression est possible
	 */
	public boolean deleteFrom(KListObject<? extends KObject> kl) {
		return kl.remove(this);
	}

	/**
	 * Retourne le résultat de la comparaison membre à membre de l'objet passé
	 * en paramètre avec l'objet en cours
	 * 
	 * @param ko
	 *            instance de KObject
	 * @return vrai si les 2 objets sont égaux
	 */
	public boolean equalsWith(KObject ko) {
		List<Field> lfields = getAllFields(new LinkedList<Field>(), this.getClass());
		boolean ret = false;
		for (Field field : lfields) {
			try {
				if (field.get(this).toString().equals(ko.getAttribute(field.getName()).toString())) {
					ret = true;
					break;
				}
			} catch (Exception e) {
			}
		}
		return ret;
	}

	/**
	 * Retourne la variable d'instance dont le nom est attribute
	 * 
	 * @param attribute
	 * @return Object
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Object getAttribute(String attribute) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		return this.__getAttribute(attribute);
	}

	/**
	 * Retourne la variable d'instance dont le nom est attribute<br>
	 * Si noLazy vaut false (chargement paresseux), le membre est chargé depuis
	 * la base de données s'il est associé à une contrainte
	 * 
	 * @param attribute
	 * @param noLazy
	 *            si vrai, le chargement paresseux n'est pas pris en charge
	 * @return Object
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Object getAttribute(String attribute, boolean noLazy) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		return this.__getAttribute(attribute, noLazy);
	}

	/**
	 * Retourne une map constituée des membres de l'objet en cours
	 * 
	 * @return Map des membres de l'objet
	 */
	public Map<String, Object> getAttributes() {
		return row;
	}

	/**
	 * Retourne le membre id de l'objet encours
	 * 
	 * @return id
	 */
	public Object getId() {
		return id;
	}

	/**
	 * Retourne la liste des clés primaires de l'objet
	 * 
	 * @return la liste des clés primaires
	 */
	public List<String> getKeyFields() {
		return getMetaObject().getKeyFields();
	}

	/**
	 * Retourne la condition SQL définie à partir des valeurs des clés primaires
	 * de l'objet
	 * 
	 * @return une condition SQL
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public String keyValuesToSql() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		return KStrings.implode_param(getKeyValues(), " AND ", "'", "=", KGlueMode.KEY_AND_VALUE, true);
	}

	/**
	 * Retourne la chaîne de requête (queryString) à passer dans l'URL
	 * constituée des valeurs des clés primaires de l'objet
	 * 
	 * @return une chaîne de requête (queryString)
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public String keyValuesToUrl() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		return KStrings.implode_param(getKeyValues(), "&", "", "=", KGlueMode.KEY_AND_VALUE, false);
	}

	/**
	 * Retourne une map constituée des couple clé/valeur des clés primaires de
	 * l'objet
	 * 
	 * @return map des couples clé/valeur des clés primaires
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public Map<String, Object> getKeyValues() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		List<String> keys = getKeyFields();
		Map<String, Object> ret = new LinkedHashMap<String, Object>();
		for (String key : keys) {
			ret.put(getFieldName(key), __getAttribute(key));
		}
		return ret;
	}

	/**
	 * Retourne une map constituée des couples clé/valeur des clés primaires de
	 * l'objet
	 * 
	 * @param all
	 * @param beforeUpdate
	 * @return map des couples clé/valeur des clés primaires
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public Map<String, Object> getKeyValues(boolean all, boolean beforeUpdate) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		Map<String, Object> ret = keyValues;
		if (keyValues == null || !beforeUpdate) {
			List<String> keys = getKeyFields();
			ret = new LinkedHashMap<String, Object>();
			for (String key : keys) {
				ret.put(getFieldName(key), getDbFieldValue(key, all));
			}
			if (beforeUpdate)
				keyValues = ret;
		}
		return ret;
	}

	/**
	 * Retourne la valeur de la première clé primaire de l'objet
	 * 
	 * @return valeur de la première clé primaire
	 */
	public String getFirstKeyValue() {
		String result = "";
		String key = getFirstKey();
		if (!key.equals(""))
			try {
				result = __getAttribute(key).toString();
			} catch (Exception e) {
			}
		return result;
	}

	/**
	 * Affecte la valeur passée en paramètre au membre défini en tant que
	 * première clé primaire
	 * 
	 * @param value
	 *            nouvelle valeur de la clé
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public void setFirstKeyValue(Object value) throws SecurityException, NoSuchFieldException, IllegalAccessException {
		String key = getFirstKey();
		if (!"".equals(key))
			__setAttribute(key, value, false);
	}

	/**
	 * Affecte les valeurs passées en paramètre aux membres faisant partie de la
	 * clé primaire
	 * 
	 * @param values
	 *            List de valeurs
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public void setKeyValues(List<Object> values) throws SecurityException, NoSuchFieldException, IllegalAccessException {
		if (values != null) {
			List<String> keys = getKeyFields();
			for (int i = 0; i < keys.size(); i++) {
				if (i < values.size())
					__setAttribute(keys.get(i), values.get(i), false);
				else
					break;
			}
		}
	}

	/**
	 * Retourne le nom du premier membre faisant partie de la clé primaire
	 * 
	 * @return nom du membre clé primaire
	 */
	public String getFirstKey() {
		String result = "";
		List<String> keys = getKeyFields();
		if (keys != null && keys.size() > 0)
			result = keys.get(0);
		return result;
	}

	/**
	 * Retourne l'instruction SQL complète permettant de sélectionner l'objet
	 * 
	 * @return instruction SQL
	 */
	public String getQuery() {
		return getQuery("", "");
	}

	/**
	 * Retourne l'instruction SQL associée à l'objet complétée par la condition
	 * <b>where</b> passée en paramètre
	 * 
	 * @param where
	 *            condition SQL
	 * @return instruction SQL
	 */
	public String getQuery(String where, String quoteChar) {
		String sql = getSql(quoteChar);
		return KSqlQuery.addWhere(sql, where);
	}

	/**
	 * Retourne le statut d'enregistrement de l'objet en cours
	 * 
	 * @return recordStatus
	 */
	public KRecordStatus getRecordStatus() {
		return recordStatus;
	}

	/**
	 * Retourne le nom de la table de base de données associée à la classe de
	 * l'objet
	 * 
	 * @return nom de table
	 */
	public String getSimpleTableName() {
		return KSqlQuery.getSimpleTableName(getTableName());
	}

	/**
	 * Retourne le membre SQL de l'objet
	 * 
	 * @return instruction SQL
	 */
	public String getSql(String quoteChar) {
		String ret = sql;
		if (quoteChar == null)
			quoteChar = "";
		if (ret == null)
			ret = KSqlQuery.makeSelect(quoteChar + getTableName() + quoteChar);
		return ret;
	}

	public String getSql() {
		return sql;
	}

	public String getTableName() {
		String ret = "";
		String tableName = getMetaObject().getTableName();
		if (tableName != null)
			ret = KSqlQuery.getSimpleTableName(tableName);
		if (ret.equals(""))
			if (sql != null)
				ret = KSqlQuery.getSimpleTableName(sql);
		return ret;
	}

	/**
	 * Retourne un identifiant unique généré à partir des valeurs des membres de
	 * l'objet
	 * 
	 * @return
	 */
	public String getUniqueId() {
		String ret = "";
		try {
			__setRow();
			ret = getClass() + "#" + makeWhere();
			ret = ret.toLowerCase();
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Erreur sur __setRow dans getUniqueId", e);
		}
		return ret;
	}

	/**
	 * Retourne le hash MD5 correspondant à l'uniqueId de l'objet
	 * 
	 * @return hash MD5
	 */
	public String getUniqueIdHash() {
		return KHash.getMD5(getUniqueId());
	}

	/**
	 * Retourne un masque d'affichage de l'objet constitué de l'ensemble de ses
	 * membres
	 * 
	 * @param before
	 *            chaîne à insérer avant chaque membre
	 * @param after
	 *            chaîne à insérer après chaque membre
	 * @return
	 */
	public String generateMask(String before, String after) {
		String result = "";
		try {
			for (String f : getFieldNames()) {
				result += before + f + after;
			}
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * Retourne le nombre de membres affichables de l'objet
	 * 
	 * @return nombre de membres
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public int countShowableFields() throws IllegalArgumentException, IllegalAccessException {
		return getFieldNames().size();
	}

	/**
	 * Détermine si les contraintes définies sur l'objet ont été chargées
	 * 
	 * @return vrai si les contraintes sont chargées
	 */
	public boolean isLoadConstraints() {
		return loadConstraints;
	}

	/**
	 * Charge l'objet en cours depuis le cache
	 * 
	 * @return instance chargée
	 */
	public KObject loadFromCache() {
		boolean cache = Ko.useCache;
		KObject result = null;
		if (cache) {
			try {
				__setRow();
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Erreur sur __setRow dans loadFromCache", e);
			}
			Object ko = KCache.getObjectValue(this);
			if (ko != null) {
				try {
					this.copyFrom((KObject) ko);
					this.recordStatus = KRecordStatus.rsLoaded;
					result = this;
				} catch (Exception e) {
				}
			}
		}
		return result;
	}

	/**
	 * retourne une chaîne de sélection de l'objet en cours basée sur la valeur
	 * de ses membres appartenant à la clé primaire
	 * 
	 * @return chaîne de sélection
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public String makeCriteria() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		return makeCriteria(getKeyValues());
	}

	/**
	 * retourne une chaîne de sélection de l'objet en cours basée sur la map des
	 * couples nomMembre/valeurMembre passée en paramètre
	 * 
	 * @param map
	 *            couples nomMembre/valeurMembre
	 * @return chaîne de sélection
	 */
	public String makeCriteria(Map<String, Object> map) {
		String criteria = "";
		criteria = new KStrings(map).implode_param(";", "", KGlueMode.KEY_AND_VALUE, false);
		return criteria;
	}

	/**
	 * retourne une condition SQL basée sur la valeur des membres de l'objet
	 * appartenant à la clé primaire
	 * 
	 * @return condition SQL
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public String makeWhere() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		return makeWhere(getKeyValues(false, true));
	}

	/**
	 * retourne une condition SQL basée sur la map des couples
	 * nomMembre/valeurMembre passée en paramètre
	 * 
	 * @param map
	 *            couples nomMembre/valeurMembre
	 * @return condition SQL
	 */
	public String makeWhere(Map<String, Object> map) {
		String where = "";
		String tableName = getTableName();
		Map<String, Object> whereMap = new LinkedHashMap<>();
		for (Map.Entry<String, Object> e : map.entrySet())
			whereMap.put(tableName + "." + e.getKey(), e.getValue());
		where = new KStrings(whereMap).implode_param(" and ", "'", KGlueMode.KEY_AND_VALUE, false);
		return where;
	}

	/**
	 * Détermine si l'objet correspond au critère de sélection passé en
	 * paramètre
	 * 
	 * @param criteria
	 *            critère de sélection de la forme
	 *            nomMembre1=valeurMembre1;nomMembre2=valeurMembre2...
	 * @return vrai si l'objet correspond au critère
	 */
	public boolean matchWith(String criteria) {
		boolean ret = true;
		KProperties criteres = new KProperties(criteria);
		for (Entry<Object, Object> entry : criteres.getEntry()) {
			String member = "";
			try {
				member = entry.getKey().toString();
				if (!(__getAttribute(member) + "").matches(entry.getValue() + "")) {
					ret = false;
					break;
				}
			} catch (Exception e) {
				KDebugConsole.print("Le membre " + member + " est introuvable sur l'instance de la classe " + this.getClass(), "KOBJECT", "KObject.matchWith");
			}
		}
		return ret;
	}

	/**
	 * Retourne l'instruction SQL permettant de filtrer les objets dont les
	 * membres contiennent la valeur du <b>filtre</b> passé en paramètre
	 * 
	 * @param filter
	 *            valeur du filtre à appliquer
	 * @return instruction SQL
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public String makeSQLFilter(String filter, String quoteChar) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		Map<String, Object> values = new HashMap<String, Object>();
		for (String field : getFieldNames()) {
			if (KDataTypeConverter.isSerializable(getFieldType(field)))
				values.put(field, "'%" + filter + "%'");
		}
		String where = KStrings.implode_param(values, " or ", "", " like ", KGlueMode.KEY_AND_VALUE, quoteChar, false);
		return this.getQuery("where " + where, quoteChar);
	}

	/**
	 * Retourne l'instruction SQL permettant de filtrer les objets de la classe
	 * <b>clazz</b> dont les membres contiennent la valeur du <b>filtre</b>
	 * passé en paramètre
	 * 
	 * @param filter
	 *            valeur du filtre à appliquer
	 * @param clazz
	 *            classe dérivant de KObject sur laquelle le filtre sera
	 *            appliqué
	 * @return instruction SQL
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 */
	public static String makeSQLFilter_(String filter, Class clazz, String quoteChar) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
		KObject ko = (KObject) clazz.newInstance();
		return ko.makeSQLFilter(filter, quoteChar);
	}

	/**
	 * Retourne l'instruction SQL associée à la classe passée en paramètre à
	 * laquelle vient s'ajouter la condition where
	 * 
	 * @param clazz
	 *            classe dérivant de KObject
	 * @param where
	 *            condition SQL
	 * @return instruction SQL
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static String getQuery(Class clazz, String where, String quoteChar) throws InstantiationException, IllegalAccessException {
		KObject ko = (KObject) clazz.newInstance();
		return ko.getQuery(where, quoteChar);
	}

	/**
	 * retourne une condition SQL basée sur la map des couples
	 * nomMembre/valeurMembre passée en paramètre
	 * 
	 * @param map
	 *            couples nomMembre/valeurMembre
	 * @return condition SQL
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	public String makeWhere(String quoteChar) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		String where = "";
		String tableName = getTableName();
		Map<String, Object> whereMap = new HashMap<>();
		for (Map.Entry<String, Object> e : getKeyValues(false, true).entrySet())
			whereMap.put(quoteChar + tableName + quoteChar + "." + quoteChar + e.getKey() + quoteChar, e.getValue());
		where = new KStrings(whereMap).implode_param(" and ", "'", "=", KGlueMode.KEY_AND_VALUE, false);
		return where;
	}

	/**
	 * Détermine si l'objet en cours posséde un membre dont la valeur contient
	 * <b>value</b>
	 * 
	 * @param value
	 *            valeur à comparer
	 * @return vrai si l'objet en cours correspond
	 */
	public boolean matchWithValue(String value) {
		boolean ret = false;
		List<Field> lfields = getAllFields(new LinkedList<Field>(), this.getClass());

		for (Field field : lfields) {
			field.setAccessible(true);
			try {
				if (field.get(this) == null | !field.get(this).toString().matches(value)) {
					ret = true;
					break;
				}
			} catch (Exception e) {
				KDebugConsole.print("Le membre " + field + " n'est pas accessible pour l'instance de " + getClass(), "KOBJECT", "KObject.matchWithValue");
			}
		}
		return ret;
	}

	/**
	 * Affecte à chacun des membres <b>nomMembre</b> de l'objet en cours la
	 * valeur <b>valeurMembre</b> à partir de la map passée en paramètre
	 * contenant des couples nomMembre/valeurMembre
	 * 
	 * @param map
	 *            couples nomMembre/valeurMembre
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public void setAttributes(Map<String, Object> map, boolean fromDb) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		List<String> copyList = new ArrayList<>(map.keySet());
		for (String attr : copyList) {
			Object value = map.get(attr);
			String mn = getMemberName(attr);
			if (!attr.equals(mn)) {
				map.put(mn, value);
				map.remove(attr);
			}
			__setAttribute(mn, value, fromDb);
		}/*
		 * for (Iterator iterator = map.entrySet().iterator();
		 * iterator.hasNext();) { Map.Entry pairs = (Map.Entry) iterator.next();
		 * try { String memberName = getMemberName((String) pairs.getKey());
		 * __setAttribute(getMemberName((String) pairs.getKey()),
		 * pairs.getValue()); } catch (Exception e) {
		 * 
		 * } }
		 */
	}

	/**
	 * Affecte à chacun des membres <b>nomMembre</b> de l'objet en cours la
	 * valeur <b>valeurMembre</b> à partir du paramètre <b>criteria</b>
	 * 
	 * @param criteria
	 *            couples nomMembre=valeurMembre séparés par separator
	 * @param separator
	 *            séparateur utilisé pour partitionner les couples
	 *            nomMembre=valeurMembre définis dans <b>criteria</b>
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public void setAttributes(String criteria, String separator) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		KStrings ks = new KStrings(criteria, separator);
		setAttributes(ks.getStrings(), false);
	}

	/**
	 * Affecte la valeur value au membre member de l'objet en cours<br>
	 * Le passage par les setters est déterminé par la valeur de la variable de
	 * configuration Ko.useSetters définie dans le fichier <b>config.ko</b>
	 * 
	 * @param member
	 *            membre de l'objet
	 * @param value
	 *            valeur à affecter
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void setAttribute(String member, Object value, boolean fromDb) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
		if (Ko.isKoUseSetters()) {
			_setAttributeWithSetter(member, value);
		} else
			__setAttribute(member, value, fromDb);
	}

	public void setId(Object id) {
		this.id = id;
	}

	public void setId(String id) {
		try {
			this.id = Integer.valueOf(id);
		} catch (Exception e) {
			this.id = id;
		}
	}

	public void setLoadConstraints(boolean loadConstraints) {
		this.loadConstraints = loadConstraints;
	}

	public void setRecordStatus(KRecordStatus recordStatus) {
		this.recordStatus = recordStatus;
	}

	/**
	 * Retourne une chaîne d'affichage de l'objet en utilisant le masque mask<br>
	 * mask doit utiliser les noms des membres de l'objet en les entourant avec
	 * { et }<br>
	 * Exemple : {id} {nom} - {prenom}
	 * 
	 * @param mask
	 *            masque d'affichage
	 * @return chaîne d'affichage
	 */
	/*
	 * public String showWithMask(String mask) { return showWithMask(mask, "{",
	 * "}"); }
	 */
	/**
	 * Retourne une chaîne d'affichage de l'objet en utilisant le masque
	 * <b>mask</b><br>
	 * mask doit utiliser les noms des membres de l'objet en les entourant avec
	 * { et }<br>
	 * Exemple : {id} {nom} - {prenom}
	 * 
	 * @param mask
	 *            masque d'affichage
	 * @return chaîne d'affichage
	 * @param koDisplay
	 *            instance de display à utiliser
	 */
	/*
	 * public String showWithMask(String mask, KObjectDisplay koDisplay) {
	 * return showWithMask(mask, "{", "}", koDisplay); }
	 */

	/**
	 * @param mask
	 *            masque d'affichage
	 * @return
	 */
	public String _showWithMask(String mask) {
		return _showWithMask(mask, "{", "}");
	}

	/**
	 * @param mask
	 * @return
	 */
	public String _constraintShowWithMask(String mask) {
		return _constraintShowWithMask(mask, "{", "}");
	}

	/**
	 * @param mask
	 * @param sepFirst
	 * @param sepLast
	 * @return
	 */
	public String _showWithMask(String mask, String sepFirst, String sepLast) {
		List<Field> lfields = getAllFields(new LinkedList<Field>(), getClass());

		for (Field field : lfields) {
			field.setAccessible(true);
			try {
				String name = field.getName();
				Object value = __getAttribute(field);
				mask = mask.replaceAll("(?i)" + "\\" + sepFirst + name + "\\" + sepLast, value + "");
			} catch (Exception e) {
			}
		}
		mask = mask.replaceAll("(?i)" + "\\" + sepFirst + "id" + "\\" + sepLast, id + "");

		Pattern p = Pattern.compile("\\" + sepFirst + "(.+?)" + "\\" + sepLast, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(mask);
		while (m.find())
			if (m.groupCount() > 0) {
				Object ret = new String("");
				String mName = m.group(1);
				try {
					ret = this.invoke(mName, this.getClass());
					mask = mask.replace(sepFirst + mName + sepLast, ret + "");
				} catch (Exception ex) {
				}
			}
		return mask;
	}

	/**
	 * @param mask
	 * @param sepFirst
	 * @param sepLast
	 * @return
	 */
	public String _constraintShowWithMask(String mask, String sepFirst, String sepLast) {
		List<Field> lfields = getAllFields(new LinkedList<Field>(), getClass());

		for (Field field : lfields) {
			field.setAccessible(true);
			try {
				String name = field.getName();
				Object value = field.get(this);
				mask = mask.replaceAll("(?i)" + "\\" + sepFirst + name + "\\" + sepLast, value + "");
			} catch (Exception e) {
			}
		}
		mask = mask.replaceAll("(?i)" + "\\" + sepFirst + "id" + "\\" + sepLast, id + "");
		return mask;
	}

	/*
	 * public String showWithMask(String mask, String sepFirst, String sepLast,
	 * KObjectDisplay koDisplay) { return getDisplay().showWithMask(this, mask,
	 * sepFirst, sepLast, koDisplay); }
	 * 
	 * 
	 * public String showWithMask(String mask, String sepFirst, String sepLast)
	 * { return getDisplay().showWithMask(this, mask, sepFirst, sepLast); }
	 */
	/**
	 * Retourne le résultat de l'appel de la méthode de nom methodName de la
	 * classe clazz sur l'objet en cours<br>
	 * Si la méthode n'existe pas dans la classe,elle est recherchée dans ses
	 * ancêtres
	 * 
	 * @param methodName
	 *            nom de la méthode à appeler
	 * @param clazz
	 *            classe déclarant la méthode à appeler
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object invoke(String methodName, Class clazz) {
		Object ret = new String("");
		try {
			Method method = clazz.getDeclaredMethod(methodName, new Class[] {});
			method.setAccessible(true);
			ret = method.invoke(this, new Object[] {});
		} catch (Exception e) {
			Class sClazz = clazz.getSuperclass();
			if (sClazz != null)
				return invoke(methodName, clazz.getSuperclass());
		}
		return ret;
	}

	/**
	 * Met le recordStatus à rsNew<br>
	 * L'objet ne sera inséré dans la base de données qu'en cas d'appel de la
	 * méthode updateToDb
	 */
	public void toAdd() {
		this.recordStatus = KRecordStatus.rsNew;
	}

	/**
	 * 
	 */
	public void toDelete() {
		this.recordStatus = KRecordStatus.rsDelete;
	}

	/**
	 * 
	 */
	public void toUpdate() {
		this.recordStatus = KRecordStatus.rsUpdate;
	}

	/**
	 * Détermine si l'objet a été chargé correctement depuis la base de données
	 * 
	 * @return vrai si l'objet a été chargé
	 */
	public boolean isLoaded() {
		return this.recordStatus == KRecordStatus.rsLoaded;
	}

	/**
	 * Retourne l'instance de display associée à l'objet<br>
	 * ou l'instance de KObjectDisplay par défaut si aucun display spécifique
	 * n'est associé à l'objet
	 * 
	 * @return instance de display
	 */
	public Display getDisplay() {
		try {
			__setRow(true);
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible d'appeler __setRow dans getDisplay", e);
		}
		Display disp = Display.getDefault();
		return disp;
	}

	/**
	 * Retourne un champ <b>input</b> de formulaire de type khcText permettant
	 * de mettre à jour le membre field<br>
	 * caption correspond au libelle du champ<br>
	 * type peut prendre une valeur parmi : khcText, khcHidden, khcCmb, khcList,
	 * khcRadio, khcCheckBox, khcDate, khcFile, khcImage, khcTextarea,
	 * khcPassWord
	 * 
	 * @param field
	 * @return control visuel input de type text
	 */
	public KFieldControl showAsInput(String field) {
		return getFcInput(field, field, "", HtmlControlType.khcText, "");
	}

	public KFieldControl getFcInput(String field, String id) {
		return getFcInput(field, id, "", HtmlControlType.khcText, "");
	}

	public KFieldControl getFcInput(String field, String id, String caption) {
		return getFcInput(field, id, caption, HtmlControlType.khcText, "");
	}

	public KFieldControl getFcInput(String field, String id, String caption, HtmlControlType type) {
		return getFcInput(field, id, caption, type, "");
	}

	public KFieldControl getFcInput(String field, String id, String caption, HtmlControlType type, String options) {
		return getDisplay().getFc(this, field, id, caption, type, options, null);
	}

	public KFieldControl getFcLabel(String field, String caption) {
		return getFcLabel(field, caption, "");
	}

	public KFieldControl getFcLabel(String field, String caption, String options) {
		return getDisplay().getFc(this, field, "", caption, HtmlControlType.khcLabel, options, null);
	}

	/**
	 * Retourne un champ <b>Select</b> de formulaire de type $type (khcList par
	 * défaut) permettant de mettre à jour la proprieté $field<br>
	 * caption correspond au libelle du champ<br>
	 * listObject est une collection (Klistobject), un tableau ou une chaîne
	 * représentant les élements de la liste<br>
	 * type peut prendre une valeur parmi : khcText, khcHidden, khcCmb, khcList,
	 * khcRadio, khcCheckBox, khcDate, khcFile, khcImage, khcTextarea,
	 * khcPassWord
	 * 
	 * @param field
	 * @param id
	 * @param listObject
	 * @param caption
	 * @param type
	 * @return control visuel de type Liste
	 */
	public KFieldControl getFcList(String field, String id, Object listObject, String caption, HtmlControlType type) {
		return getFcList(field, id, listObject, caption, type, "");
	}

	public KFieldControl getFcList(String field, String id, Object listObject, String caption) {
		return getFcList(field, id, listObject, caption, HtmlControlType.khcList, "");
	}

	public KFieldControl getFcList(String field, String id, Object listObject) {
		return getFcList(field, id, listObject, "", HtmlControlType.khcList, "");
	}

	public KFieldControl getFcList(String field, String id) {
		return getFcList(field, id, null, "", HtmlControlType.khcList, "");
	}

	public KFieldControl getFcList(String field, String id, Object listObject, String caption, HtmlControlType type, String options) {
		return getDisplay().getFc(this, field, id, caption, type, options, listObject);
	}

	/**
	 * Retourne un champ <b>input</b> de formulaire de type $type (khcRadio par
	 * défaut) permettant de mettre à jour la proprieté $field<br>
	 * caption correspond au libelle du champ<br>
	 * type peut prendre une valeur parmi : khcText, khcHidden, khcCmb, khcList,
	 * khcRadio, khcCheckBox, khcDate, khcFile, khcImage, khcTextarea,
	 * khcPassWord<br/>
	 * listObject est une collection (Klistobject), un tableau ou une chaîne
	 * représentant les options du groupe radio<br>
	 * 
	 * @param field
	 * @param id
	 * @param listObject
	 * @param caption
	 * @return control visuel bouton radio
	 */
	public KFieldControl getFcRadio(String field, String id, String listObject, String caption) {
		return getFcRadio(field, id, listObject, caption, "");
	}

	public KFieldControl getFcRadio(String field, String id, String listObject) {
		return getFcRadio(field, id, listObject, "", "");
	}

	public KFieldControl getFcRadio(String field, String id) {
		return getFcRadio(field, id, null, "", "");
	}

	public KFieldControl getFcRadio(String field, String id, String listObject, String caption, String options) {
		return getDisplay().getFc(this, field, id, caption, HtmlControlType.khcRadio, options, listObject);
	}

	/**
	 * Retourne un champ <b>input</b> de formulaire de type $type permettant de
	 * mettre à jour la proprieté $field<br>
	 * caption correspond au libelle du champ<br>
	 * type peut prendre une valeur parmi : khcText, khcHidden, khcCmb, khcList,
	 * khcRadio, khcCheckBox, khcDate, khcFile, khcImage, khcTextarea,
	 * khcPassWord listObject est une collection (Klistobject), un tableau ou
	 * une chaîne représentant les cases à cocher du groupe<br>
	 * 
	 * @param field
	 * @param id
	 * @param listObject
	 * @param caption
	 * @return control visuel
	 */
	public KFieldControl getFcCheckBox(String field, String id, Object listObject, String caption) {
		return getFcCheckBox(field, id, listObject, caption, "");
	}

	public KFieldControl getFcCheckBox(String field, String id, Object listObject) {
		return getFcCheckBox(field, id, listObject, "", "");
	}

	public KFieldControl getFcCheckBox(String field, String id) {
		return getFcCheckBox(field, id, null, "", "");
	}

	public KFieldControl getFcCheckBox(String field, String id, Object listObject, String caption, String options) {
		return getDisplay().getFc(this, field, id, caption, HtmlControlType.khcCheckBox, options, listObject);
	}

	public KFieldControl getFc(String field, String id, String caption, HtmlControlType type, String options, Object listObject) {
		return getDisplay().getFc(this, field, id, caption, type, options, listObject);
	}

	/**
	 * Retourne le contrôle visuel associé par défaut au membre de nom
	 * <b>field</b>
	 * 
	 * @param field
	 *            membre/champ
	 * @return contrôle visuel
	 */
	public KFieldControl getBestControl(String field) {
		return getDisplay().getBestControl(this, field);
	}

	/**
	 * Retourne le type de contrôle visuel associé par défaut au membre de nom
	 * <b>field</b>
	 * 
	 * @param field
	 *            membre/champ
	 * @return type de contrôle visuel
	 */
	public HtmlControlType getControlType(String field) {
		return getDisplay().getControlType(this, field);
	}

	/**
	 * Retourne la liste des contraintes définies dans le constructeur de
	 * l'objet
	 * 
	 * @return Liste des contraintes
	 */
	public KListConstraint getConstraints() {
		return constraints;
	}

	public void refresh() {
		try {
			__setRow(true);
		} catch (Exception e) {
		}
		;
	}

	@Override
	public boolean equals(Object obj) {
		KObject ko = (KObject) obj;
		return ko.getUniqueId().equals(getUniqueId());
	}

	/**
	 * Retourne l'instance de validation d'objet associée à l'objet
	 * 
	 * @return l'instance de validation associée à l'objet
	 */
	private KObjectController getKobjectController() {
		return Ko.kcontroller().getObjectController(this.getClass().getSimpleName());
	}

	/**
	 * Retourne l'instance de validation sur le membre de nom memberName
	 * 
	 * @param memberName
	 *            nom du membre
	 * @retur instance de validation sur membre
	 */
	public KObjectFieldController getKObjectFieldController(String memberName) {
		KObjectFieldController result = null;
		KObjectController koc = getController();
		if (koc != null)
			result = koc.getFieldController(memberName);
		return result;
	}

	/**
	 * Retourne l'instance de validation d'objet associée à l'objet
	 * 
	 * @return l'instance de validation associée à l'objet
	 */
	public KObjectController getController() {
		KObjectController result = controller;
		if (controller == null)
			result = getKobjectController();
		return result;
	}

	public void setController(KObjectController controller) {
		this.controller = controller;
	}

	public void onUpdate(Object sender) {

	}

	public static KObject getNewInstance(Class<? extends KObject> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			Ko.klogger().log(Level.WARNING, "Impossible de créer une instance de " + clazz, e);
			return null;
		}
	}

	public Map<String, Object> getFieldRow(boolean preserveId, KDataBase db) throws IllegalArgumentException, IllegalAccessException {
		__setRow(false, db);
		Map<String, Object> fieldRow = getMetaObject().getFieldMap().getFieldRow(row);
		if (!(preserveId && getKeyFields().contains("id")))
			fieldRow.remove("id");
		return fieldRow;
	}

	public void setLoaded() {
		recordStatus = KRecordStatus.rsLoaded;
	}

	public KMetaObject<? extends KObject> getMetaObject() {
		if (metaObject == null)
			metaObject = Ko.getMetaObject(getClass());
		return metaObject;
	}
}
