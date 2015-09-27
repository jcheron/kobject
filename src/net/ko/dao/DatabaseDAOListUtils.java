package net.ko.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.ko.cache.KCache;
import net.ko.db.KDataBase;
import net.ko.db.KDbResultSet;
import net.ko.events.KEventType;
import net.ko.events.KEvents;
import net.ko.events.KFireEvent;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.kobject.KConstraint;
import net.ko.kobject.KConstraintBelongsTo;
import net.ko.kobject.KConstraintHasMany;
import net.ko.kobject.KDuoClasse;
import net.ko.kobject.KDuoClasseList;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.ksql.KParameterizedInstruction;
import net.ko.ksql.KPreparedStatement;
import net.ko.ksql.KSqlQuery;
import net.ko.list.MapArrayString;
import net.ko.utils.KArray;
import net.ko.utils.KStrings;
import net.ko.utils.KoUtils;

public class DatabaseDAOListUtils {

	/**
	 * Charge la liste depuis la base de données en utilisant son instruction
	 * SQL par défaut<br>
	 * db est automatiquement fermée après le chargement
	 * 
	 * @param db
	 *            instance de KDataBase
	 */
	public static void loadFromDb(DatabaseDAO<KObject> dao, KListObject<KObject> listObject) {
		loadFromDb(dao, listObject, "");
	}

	/**
	 * Charge la liste en la triant suivant le champ à la position
	 * sortedFieldNum depuis la base de données en utilisant son instruction SQL
	 * par défaut<br>
	 * db est automatiquement fermée après le chargement
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param sortedFieldNum
	 */
	public static void loadFromDb(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, int sortedFieldNum) {
		String sql = listObject.getSql();
		if (sortedFieldNum != -1)
			sql = listObject.setSql(sql + " ORDER BY " + Integer.toString(sortedFieldNum), dao.quote());
		loadFromDb(dao, listObject, sql);
	}

	/**
	 * Charge la liste depuis la base de données en utilisant l'instruction SQL
	 * passée en paramètre<br>
	 * db est automatiquement fermée après le chargement
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param sql
	 *            instruction SQL de chargement
	 */
	public static void loadFromDb(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, String sql) {
		loadFromDb(dao, listObject, sql, new KDuoClasseList());
	}

	/**
	 * Charge la liste depuis la base de données en utilisant l'instruction SQL
	 * de base auquel vient s'ajouter la clause where<br>
	 * db est automatiquement fermée après le chargement
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param where
	 *            clause where
	 */
	public static void loadFromDbWhere(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, String where) {
		String sql = listObject.setSql("", dao.quote());
		loadFromDb(dao, listObject, KSqlQuery.addWhere(sql, where));
	}

	/**
	 * Charge la liste depuis la base de données dans le cadre du chargement
	 * d'une contrainte
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param sql
	 *            instruction SQL
	 * @param duoClasseList
	 *            couple de classes chargées
	 */

	@SuppressWarnings("unchecked")
	public static void loadFromDb(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, String sql, KDuoClasseList duoClasseList) {
		Ko.observable.setChanged();
		Class<? extends KObject> clazz = listObject.getClazz();
		KEvents evt = KEvents.getEvent(listObject.getClazz(), KEventType.etLoadKlistObjectStart);
		Ko.fireEvent(new KFireEvent(evt, listObject));
		if (!evt.isCancel()) {
			KDataBase db = dao.getDatabase();
			sql = listObject.setSql(sql, db.QUOTE());
			KListObject<? extends KObject> kl = null;
			if (Ko.useCache) {
				kl = (KListObject<? extends KObject>) KCache.getObjectValue(listObject);
				if (kl != null)
					listObject.setItems((List<KObject>) kl.asAL());
			}
			if (db != null) {
				if (kl == null) {
					MapArrayString<KConstraint> membersKeys = new MapArrayString<>();
					try {
						KDbResultSet rs = db.sendQuery(sql);
						KDebugConsole.print(clazz.getSimpleName(), "KOBJECT", "KListObject<" + clazz.getSimpleName() + ">.loadFromDb");
						boolean next = false;
						do {
							KObject ko = clazz.newInstance();
							next = DatabaseDAOObjectUtils.loadFromDb(ko, rs, true);
							if (next) {
								listObject.getItems().add(ko);
								ko.getConstraints().preload(membersKeys);
							}
						} while (next);
						if (duoClasseList.size() < Ko.ConstraintsDepth)
							loadConstraintsFromList(dao, listObject, membersKeys, duoClasseList);

						if (Ko.useCache)
							KCache.put(listObject);
						rs.close();
					} catch (Exception e) {
						Ko.klogger().log(Level.WARNING, "Impossible de charger la liste :" + clazz, e);
					}
				}
				try {
					db.close();
				} catch (SQLException e) {
					Ko.klogger().log(Level.WARNING, "Impossible de fermer la connexion :" + db, e);
				}
			}
		}
		Ko.observable.setChanged();
		Ko.fireEvent(new KFireEvent(KEvents.getEvent(clazz, KEventType.etLoadKlistObjectEnd), listObject));
	}

	public static void loadFromDb(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, KParameterizedInstruction sqlQuery) {
		loadFromDb(dao, listObject, sqlQuery, new KDuoClasseList());
	}

	public static void loadFromDb(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, KParameterizedInstruction sqlQuery, KDuoClasseList duoClasseList) {
		Ko.observable.setChanged();
		KDataBase db = dao.getDatabase();
		Class<? extends KObject> clazz = listObject.getClazz();
		Ko.fireEvent(new KFireEvent(KEvents.getEvent(listObject.getClazz(), KEventType.etLoadKlistObjectStart), listObject));
		// sql = listObject.setSql(sql, db.QUOTE());
		KListObject<? extends KObject> kl = null;
		if (Ko.useCache) {
			kl = (KListObject<? extends KObject>) KCache.getObjectValue(listObject);
			if (kl != null)
				listObject.setItems((List<KObject>) kl.asAL());
		}
		if (db != null) {
			KPreparedStatement st = null;
			KDbResultSet rs = null;
			if (kl == null) {
				MapArrayString<KConstraint> membersKeys = new MapArrayString<>();
				try {
					st = new KPreparedStatement(db);
					rs = st.sendQuery(sqlQuery);
					KDebugConsole.print(clazz.getSimpleName(), "KOBJECT", "KListObject<" + clazz.getSimpleName() + ">.loadFromDb");
					boolean next = false;
					do {
						KObject ko = clazz.newInstance();
						next = DatabaseDAOObjectUtils.loadFromDb(ko, rs, true);
						if (next) {
							listObject.getItems().add(ko);
							ko.getConstraints().preload(membersKeys);
						}
					} while (next);
					if (duoClasseList.size() < Ko.ConstraintsDepth)
						loadConstraintsFromList(dao, listObject, membersKeys, duoClasseList);

					if (Ko.useCache)
						KCache.put(listObject);

				} catch (Exception e) {
					Ko.klogger().log(Level.WARNING, "Impossible de charger la liste :" + clazz, e);
				} finally {
					try {
						if (rs != null)
							rs.close();
						if (st != null)
							st.close();
						db.close();
					} catch (SQLException e) {
						Ko.klogger().log(Level.WARNING, "Impossible de fermer la connexion :" + db, e);
					}
				}
			}

		}
		Ko.observable.setChanged();
		Ko.fireEvent(new KFireEvent(KEvents.getEvent(clazz, KEventType.etLoadKlistObjectEnd), listObject));
	}

	/**
	 * @param db
	 * @param primaryKeyValues
	 * @param field
	 */
	public static void loadFromDbByFieldKeys(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, List<Object> primaryKeyValues, String field) {
		loadFromDbByFieldKeys(dao, listObject, primaryKeyValues, field, new KDuoClasseList());
	}

	/**
	 * @param db
	 * @param primaryKeyValues
	 * @param field
	 * @param duoClassList
	 */
	public static void loadFromDbByFieldKeys(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, List<Object> primaryKeyValues, String field, KDuoClasseList duoClassList) {
		String quote = dao.quote();
		Object[] cond = KArray.arrayOfSameValue(quote + field + quote + "= ?", primaryKeyValues.size());
		String condition = KStrings.implode(" OR ", cond);
		KParameterizedInstruction sqlQuery = new KParameterizedInstruction(dao.quote(), condition);
		sqlQuery.setFields(primaryKeyValues);
		sqlQuery.setTableName(KoUtils.getTableName(listObject.getClazz()));
		loadFromDb(dao, listObject, sqlQuery, duoClassList);
	}

	/**
	 * @param db
	 * @param primaryKeyValues
	 */
	public static void loadFromDbByPriKeys(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, List<Object> primaryKeyValues) {
		try {
			String key = KObject.getFirstKey(listObject.getClazz());
			loadFromDbByFieldKeys(dao, listObject, primaryKeyValues, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param db
	 * @param primaryKeyValues
	 * @param duoClassList
	 */
	public static void loadFromDbByPriKeys(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, List<Object> primaryKeyValues, KDuoClasseList duoClassList) {
		try {
			String key = KObject.getFirstKey(listObject.getClazz());
			loadFromDbByFieldKeys(dao, listObject, primaryKeyValues, key, duoClassList);
		} catch (Exception e) {
		}
	}

	/**
	 * @param db
	 * @param primaryKeyValues
	 */
	public static void loadFromDbByPriKeysNotIn(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, List<Object> primaryKeyValues) {
		String quote = dao.quote();
		Class<? extends KObject> clazz = listObject.getClazz();
		String key;
		try {
			key = KObject.getFirstKey(clazz);
			Object[] cond = KArray.arrayOfSameValue(quote + key + quote + "!= ?", primaryKeyValues.size());
			String condition = KStrings.implode(" AND ", cond);
			KParameterizedInstruction sqlQuery = new KParameterizedInstruction(dao.quote(), condition);
			sqlQuery.setFields(primaryKeyValues);
			sqlQuery.setTableName(KoUtils.getTableName(listObject.getClazz()));
			loadFromDb(dao, listObject, sqlQuery);
		} catch (InstantiationException | IllegalAccessException e) {
			Ko.klogger().log(Level.WARNING, "Impossible de charger la liste " + listObject.getClazz(), e);

		}

	}

	@SuppressWarnings("unchecked")
	public static void loadConstraintsFromList(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, MapArrayString<KConstraint> membersKeys, KDuoClasseList duoClasseList) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		for (KConstraint preloadConstraint : membersKeys) {
			Class<? extends KObject> clazz = preloadConstraint.getClazzList();
			Class<? extends KConstraint> constraintClazz = preloadConstraint.getClass();
			KListObject<? extends KObject> kl = new KListObject<>(clazz);
			ArrayList<KDuoClasse> tmpList = (ArrayList<KDuoClasse>) duoClasseList.getDuoClasses().clone();
			duoClasseList.add(preloadConstraint.getClazz(), listObject.getClazz());
			if (constraintClazz.equals(KConstraintBelongsTo.class))
				loadFromDbByPriKeys(dao, (KListObject<KObject>) kl, membersKeys.get(preloadConstraint), duoClasseList);
			else if (constraintClazz.equals(KConstraintHasMany.class))
				loadFromDbByFieldKeys(dao, (KListObject<KObject>) kl, membersKeys.get(preloadConstraint), preloadConstraint.getDestFieldKey(), duoClasseList);
			else
				loadFromDbByFieldKeys(dao, (KListObject<KObject>) kl, membersKeys.get(preloadConstraint), preloadConstraint.getFkField(), duoClasseList);
			duoClasseList.setDuoClasses(tmpList);
			for (KObject ko : listObject.getItems()) {
				KConstraint co = ko.getConstraints().getConstraint(preloadConstraint.getMember(), preloadConstraint.getClass());
				if (co != null)
					co.load(kl);
			}
		}
	}

	private static void loadFromDbForConstraint(KListObject<KObject> listObject, KDataBase db, String sql, KDuoClasseList duoClasseList) {
		sql = listObject.setSql(sql, db.QUOTE());
		if (db != null) {
			try {
				KDbResultSet rs = db.sendQuery(sql);
				boolean next = false;
				do {
					KObject ko = listObject.getClazz().newInstance();
					next = DatabaseDAOObjectUtils.loadFromDbForConstraint(ko, rs, duoClasseList);
					if (next)
						listObject.getItems().add(ko);
				} while (next);
			} catch (Exception e) {
				Ko.klogger().log(Level.SEVERE, "Impossible de charger la liste pour la constrainte :" + listObject.getClazz(), e);
			}
		}
	}

	/**
	 * Charge et retourne depuis la base de données la liste des objets ayant lz
	 * chaîne recherchée dans l'un de leur membre
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param filter
	 *            chaîne recherchée
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 */
	public static void filterFromDb(DatabaseDAO<KObject> dao, KListObject<KObject> listObject, String filter) throws InstantiationException, IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
		String sql = KObject.makeSQLFilter_(filter, listObject.getClazz(), dao.quote());
		loadFromDb(dao, listObject, sql);
	}

	// **********************************
	/**
	 * Charge depuis la base de données tous les objets de la classe passée en
	 * paramétre
	 * 
	 * @param clazz
	 *            classe dérivée de KObject
	 * @param db
	 *            instance de KDataBase
	 * @return liste d'objets chargée
	 */
	@SuppressWarnings("unchecked")
	public static KListObject<? extends KObject> kload(DatabaseDAO<KObject> dao, Class<? extends KObject> clazz) {
		KListObject<? extends KObject> result = KListObject.getListInstance(clazz);
		loadFromDb(dao, (KListObject<KObject>) result);
		return result;
	}

	/**
	 * Charge depuis la base de données tous les objets de la classe passée en
	 * paramétre et trie la liste suivant le champ sortedFieldNum
	 * 
	 * @param clazz
	 *            classe dérivée de KObject
	 * @param db
	 *            instance de KDataBase
	 * @param sortedFieldNum
	 *            numéro d'ordre du champ sur lequel la liste est triée
	 * @return liste d'objets chargée
	 */
	@SuppressWarnings("unchecked")
	public static KListObject<? extends KObject> kload(DatabaseDAO<KObject> dao, Class<? extends KObject> clazz, int sortedFieldNum) {
		KListObject<? extends KObject> list = KListObject.getListInstance(clazz);
		loadFromDb(dao, (KListObject<KObject>) list, sortedFieldNum);
		return list;
	}

	/**
	 * Charge depuis la base de données les objets de la classe passée en
	 * paramétre à partir de l'instruction SQL
	 * 
	 * @param clazz
	 *            classe dérivée de KObject des objets à charger
	 * @param db
	 *            instance de KDataBase
	 * @param sql
	 *            Instruction SQL
	 * @return liste d'objets chargée
	 */
	@SuppressWarnings("unchecked")
	public static KListObject<? extends KObject> kload(DatabaseDAO<KObject> dao, Class<? extends KObject> clazz, String sql) {
		KListObject<? extends KObject> list = KListObject.getListInstance(clazz);
		loadFromDb(dao, (KListObject<KObject>) list, sql);
		return list;
	}

	/**
	 * Charge depuis la base de données les objets de la classe passée en
	 * paramétre à partir de l'instruction SQL auquel s'ajoute la clause where
	 * 
	 * @param clazz
	 *            classe dérivée de KObject des objets à charger
	 * @param db
	 *            instance de KDataBase
	 * @param where
	 *            clause WHERE ajoutée à l'instruction SQL de base
	 * @return liste d'objets chargée
	 */
	@SuppressWarnings("unchecked")
	public static KListObject<? extends KObject> kloadWhere(DatabaseDAO<KObject> dao, Class<? extends KObject> clazz, String where) {
		KListObject<? extends KObject> list = KListObject.getListInstance(clazz);
		loadFromDbWhere(dao, (KListObject<KObject>) list, where);
		return list;
	}

	/**
	 * Charge depuis la base de données les objets de la classe passée en
	 * paramétre à partir de l'instruction SQL dans le cadre du chargement d'une
	 * contrainte
	 * 
	 * @param clazz
	 *            classe dérivée de KObject
	 * @param db
	 *            instance de KDataBase
	 * @param sql
	 *            Instruction SQL
	 * @param duoClasseList
	 *            Couple de classes actuellement chargé
	 * @return liste d'objets chargée
	 */
	@SuppressWarnings("unchecked")
	public static KListObject<? extends KObject> kloadForConstraint(Class<? extends KObject> clazz, KDataBase db, String sql, KDuoClasseList duoClasseList) {
		KListObject<? extends KObject> list = KListObject.getListInstance(clazz);
		loadFromDbForConstraint((KListObject<KObject>) list, db, sql, duoClasseList);
		return list;
	}
}
