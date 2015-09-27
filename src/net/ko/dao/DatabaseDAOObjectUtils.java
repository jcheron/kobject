package net.ko.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.ko.cache.KCache;
import net.ko.cache.KCacheType;
import net.ko.db.KDataBase;
import net.ko.db.KDataBaseConnection;
import net.ko.db.KDbResultSet;
import net.ko.events.KEventType;
import net.ko.events.KEvents;
import net.ko.events.KFireEvent;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.kobject.KDuoClasseList;
import net.ko.kobject.KObject;
import net.ko.kobject.KRecordStatus;
import net.ko.ksql.KParameterizedDelete;
import net.ko.ksql.KParameterizedExecute;
import net.ko.ksql.KParameterizedInsertInto;
import net.ko.ksql.KParameterizedInstruction;
import net.ko.ksql.KParameterizedUpdate;
import net.ko.ksql.KPreparedStatement;
import net.ko.ksql.KSqlQuery;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;

public class DatabaseDAOObjectUtils {
	/**
	 * Charge l'objet depuis la base de données à partir de la valeur passée en
	 * paramètre de son membre id
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param id
	 *            valeur de l'id
	 * @return instance d'objet chargée
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws SQLException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static KObject loadOneById(KObject object, KDataBase db, Object id) throws SecurityException, IllegalArgumentException, SQLException, NoSuchFieldException, IllegalAccessException {
		if (id != null) {
			object.setFirstKeyValue(id);
			loadOne(object, db);
		}
		return object;
	}

	/**
	 * Charge l'objet depuis la base de données à partir des valeurs passées en
	 * paramètre de ses membres appartenant à la clé primaire
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param keyValues
	 * @return instance d'objet chargée
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws SQLException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static KObject loadOneByPriKeys(KObject object, KDataBase db, Object[] keyValues) throws SecurityException, IllegalArgumentException, SQLException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
		List<String> keyFields = object.getKeyFields();
		int i = 0;
		for (String key : keyFields) {
			if (i < keyFields.size())
				object.setAttribute(key, keyValues[i], true);
			i++;
		}
		return loadOne(object, db);
	}

	/**
	 * Charge une instance d'objet depuis la base dans le cadre du chargement
	 * d'une contrainte
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param where
	 *            condition SQL
	 * @param duoClasseList
	 *            Liste des couples de classes déjà chargés
	 * @return objet chargé
	 * @throws SQLException
	 */
	public static KObject kloadOneForConstraint(KObject object, KDataBase db, KParameterizedInstruction sqlQuery, KDuoClasseList duoClasseList) throws SQLException {
		KPreparedStatement st = null;
		KDbResultSet rs = null;
		try {
			st = new KPreparedStatement(db);
			rs = st.sendQuery(sqlQuery);
			loadFromDb(object, rs, true, duoClasseList);
		} finally {
			if (rs != null)
				rs.close();
			st.close();
		}
		return object;
	}

	/**
	 * Charge depuis la base de données un objet à partir de la position witch
	 * 
	 * @param witch
	 *            position de l'objet à charger
	 * @param clazz
	 *            Classe dérivée de KObject
	 * @param db
	 *            Instance de KDataBase
	 * @param where
	 *            Condition SQL
	 * @return l'instance de KObject chargée
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static KObject find(int witch, Class<? extends KObject> clazz, KDataBase db, String where) throws SQLException, InstantiationException, IllegalAccessException {
		KObject result = KObject.getNewInstance(clazz);
		loadOne(result, db, where + db.limit(witch, 1));
		return result;
	}

	private static boolean loadFromDb(KObject object, KDbResultSet result, boolean constraint, KDuoClasseList duoClasseList) throws SQLException {
		return loadFromDb(object, result, constraint, duoClasseList, false);
	}

	/**
	 * Charge l'objet depuis la base de données à partir d'un resultset
	 * 
	 * @param result
	 *            instance de KDbResultSet
	 * @return vrai si l'objet a été chargé
	 * @throws SQLException
	 */
	public static boolean loadFromDb(KObject object, KDbResultSet result) throws SQLException {
		return loadFromDb(object, result, false, new KDuoClasseList(), false);
	}

	public static boolean loadFromDb(KObject object, KDbResultSet result, boolean constraint, KDuoClasseList duoClasseList, boolean fromList) throws SQLException {
		Ko.observable.setChanged();
		Ko.fireEvent(new KFireEvent(KEvents.getEvent(object.getClass(), KEventType.etLoadKobjectStart), object, new Object[] { result }));
		boolean ret = false;
		ret = result.next();
		if (ret) {
			Map<String, Object> row = result.toMap();
			try {
				object.setAttributes(row, true);
				Map<String, Object> keyValues = object.getKeyValues(true, true);
				object.setRecordStatus(KRecordStatus.rsLoaded);
				KDebugConsole.print(object.getKeyValues() + "", "KOBJECT", "DatabaseDAOObject.loadFromDb");
				if (duoClasseList.size() < Ko.ConstraintsDepth && !fromList)
					object.getConstraints().load(new KDataBaseConnection(result), duoClasseList, fromList);
			} catch (Exception e) {
				Ko.klogger().log(Level.SEVERE, "Impossible de charger l'instance de " + object.getClass() + " depuis la base de données", e);
			}
			if (Ko.useCache && Ko.cacheType.equals(KCacheType.ktListAndObjects)) {
				KCache.put(object);
			}
		}
		Ko.observable.setChanged();
		Ko.fireEvent(new KFireEvent(KEvents.getEvent(object.getClass(), KEventType.etLoadKobjectEnd), object, new Object[] { result }));
		return ret;
	}

	/**
	 * Charge l'objet depuis la base de données à partir d'un resultset
	 * 
	 * @param result
	 *            instance de KDbResultSet
	 * @param fromList
	 *            vrai si l'objet est chargé depuis une liste
	 * @return vrai si l'objet a été chargé
	 * @throws SQLException
	 */
	public static boolean loadFromDb(KObject object, KDbResultSet result, boolean fromList) throws SQLException {
		return loadFromDb(object, result, false, new KDuoClasseList(), fromList);
	}

	/**
	 * Charge l'objet depuis la base de données à partir d'un resultset dans le
	 * cadre du chargement d'une contrainte
	 * 
	 * @param result
	 *            instance de KDbResultSet
	 * @param duoClasseList
	 *            Liste des couples de classes déjà chargés
	 * @return vrai si l'objet a été chargé
	 * @throws SQLException
	 */
	public static boolean loadFromDbForConstraint(KObject object, KDbResultSet result, KDuoClasseList duoClasseList) throws SQLException {
		return loadFromDb(object, result, true, duoClasseList, false);
	}

	/**
	 * Charge l'objet depuis la base de données en utilisant les valeurs
	 * actuelles de ses membres appartenant à la clé primaire
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @return instance d'objet chargée
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws SQLException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static KObject loadOne(KObject object, KDataBase db) throws SecurityException, IllegalArgumentException, SQLException, NoSuchFieldException, IllegalAccessException {
		return loadOne(object, db, object.makeWhere(db.QUOTE()));
	}

	/**
	 * Teste l'existance de l'objet dans la base de données en utilisant les
	 * valeurs actuelles de ses membres appartenant à la clé primaire
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @return vrai si l'objet existe dans la base de données
	 */
	public static boolean exists(KObject object, KDataBase db) {
		try {
			loadOne(object, db);
		} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException | SQLException e) {
			object.setRecordStatus(KRecordStatus.rsImpossible);
		}
		return object.getRecordStatus().equals(KRecordStatus.rsLoaded);
	}

	/**
	 * Charge l'objet depuis la base de données à partir d'une condition SQL
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param where
	 *            instruction SQL
	 * @return instance d'objet chargée
	 * @throws SQLException
	 */
	public static KObject loadOne(KObject object, KDataBase db, String where) throws SQLException {
		KObject result = null;
		if (Ko.useCache)
			result = object.loadFromCache();
		if (db != null) {
			if (result == null) {
				String query = object.getQuery(where, db.QUOTE());
				KDbResultSet rs = db.sendQuery(query);
				loadFromDb(object, rs);
				db.close();
				result = object;
			}
		}
		return result;
	}

	/**
	 * Teste l'existance d'un objet dans la base de données à partir d'une
	 * condition SQL
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param where
	 * @return
	 */
	public static boolean exists(KObject object, KDataBase db, String where) {
		try {
			loadOne(object, db, where);
		} catch (SQLException e) {
			object.setRecordStatus(KRecordStatus.rsImpossible);
		}

		return object.getRecordStatus().equals(KRecordStatus.rsLoaded);
	}

	/**
	 * Met à jour l'objet en cours dans la base de données en tenant compte de
	 * son membre recordStatus
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @return vrai si l'objet a pu être mis à jour dans la base
	 */
	public static boolean updateToDb(KObject object, KDataBase db, DatabaseDAO<? extends KObject> dao) {
		return updateToDb(object, db, false, dao);
	}

	private static Map<String, Object> getPreparedMap(Map<String, Object> fieldRow) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (String k : fieldRow.keySet()) {
			result.put(k, " ? ");
		}
		return result;
	}

	/**
	 * Met à jour l'objet en cours dans la base de données en tenant compte de
	 * son membre recordStatus
	 * 
	 * @param db
	 *            instance de KDataBase
	 * @param fromList
	 *            vrai si l'objet est mis à jour depuis une liste d'objets
	 * @return vrai si l'objet a pu être mis à jour dans la base
	 */
	public static boolean updateToDb(KObject object, KDataBase db, boolean fromList, DatabaseDAO<? extends KObject> dao) {
		boolean ret = false;
		if (db != null) {
			Ko.observable.setChanged();
			KRecordStatus recordStatus = object.getRecordStatus();
			Ko.fireEvent(new KFireEvent(KEvents.getEvent(object.getClass(), KEventType.etUpdateKobjectStart), object, new Object[] { recordStatus }));
			String query = "";
			String QUOTE = db.QUOTE();
			String table = object.getTableName();
			Map<String, Object> fieldRow = null;
			KParameterizedExecute updateInstruction = null;

			try {
				object.getConstraints().update();
				fieldRow = object.getFieldRow(dao.isLoading(), db);
				if (dao.isLoading())
					recordStatus = KRecordStatus.rsNew;
				switch (recordStatus) {
				case rsNew:
					updateInstruction = new KParameterizedInsertInto(QUOTE, table, fieldRow);
					// query = "INSERT INTO " + QUOTE + table + QUOTE + "("
					// +
					// KStrings.implode_param(fieldRow, ",", QUOTE,
					// KGlueMode.KEY, false) + ") VALUES(" +
					// KStrings.implode_param(preparedMap, ",", "",
					// KGlueMode.VALUE, false) + ")";
					break;
				case rsUpdate:
					updateInstruction = new KParameterizedUpdate(QUOTE, table, fieldRow, object.getKeyValues(false, true));
					// query = "UPDATE " + QUOTE + table + QUOTE + " SET " +
					// KStrings.implode_param(preparedMap, ",", "", "=",
					// KGlueMode.KEY_AND_VALUE, QUOTE, false) + " WHERE " +
					// object.makeWhere(QUOTE);
					break;
				case rsDelete:
					updateInstruction = new KParameterizedDelete(QUOTE, table, object.getKeyValues(false, true));
					// query = "DELETE FROM " + QUOTE + table + QUOTE +
					// " WHERE " + object.makeWhere(QUOTE);
					break;
				default:
					// query = "";
					break;
				}
			} catch (Exception e) {
				Ko.klogger().log(Level.SEVERE, "Impossible d'affecter les valeurs des membres", e);
			}
			KPreparedStatement st = null;
			try {
				if (updateInstruction != null) {
					st = new KPreparedStatement(db);
					st.executeUpdate(updateInstruction);
					if (recordStatus.equals(KRecordStatus.rsNew)) {
						object.setKeyValues(st.getGeneratedKeys());
					}

					Map<String, Object> keyValues = object.getKeyValues(true, true);
					KDebugConsole.print(keyValues + "", "KOBJECT", object.getClass().getSimpleName() + ".updateToDb");
					object.setLoaded();

					if (!dao.isLoading()) {
						if (recordStatus.equals(KRecordStatus.rsNew))
							updateInstruction.addFields(object.getKeyValues());
						dao.memoriseQuery(updateInstruction);
					}
					// TODO à vérifier...pourquoi cette ligne ?
					// if (dao.getEngine() != null && Ko.kmemoryDaoEngine() ==
					// null)
					object.getConstraints().save(dao);

					if (Ko.useCache)
						KCache.replace(object, fromList);
					ret = true;
				}
				object.setLoaded();
			} catch (Exception e) {
				Ko.klogger().log(Level.SEVERE, "Impossible d'exécuter la requête : " + query, e);
			} finally {
				try {
					if (st != null)
						st.close();
				} catch (SQLException e) {
					Ko.klogger().log(Level.SEVERE, "Impossible de fermer le statement : " + query, e);
				}
			}
			Ko.observable.setChanged();
			Ko.fireEvent(new KFireEvent(KEvents.getEvent(object.getClass(), KEventType.etUpdateKobjectEnd), object));
		}
		return ret;
	}

	/**
	 * Retourne la liste des objets chargés depuis la base de données ne faisant
	 * pas partie des clés primaires passées en paramètre
	 * 
	 * @param clazz
	 *            classe dérivée de KObject
	 * @param db
	 *            instance de KDataBase
	 * @param primaryKeyValues
	 *            tableau des valeurs des clés primaires à exclure
	 * @return liste des objets
	 */
	public static ArrayList<Object> loadAllPriKeysNotIn(Class<? extends KObject> clazz, KDataBase db, String[] primaryKeyValues) {
		ArrayList<Object> result = new ArrayList<>();
		try {
			String key = KObject.getFirstKey(clazz);
			String where = KStrings.implode(primaryKeyValues, key, " AND ", "'", "!=", KGlueMode.KEY_AND_VALUE, true);
			String sql = KObject.getQuery(clazz, where, db.QUOTE());
			sql = KSqlQuery.getSelectWithNewFields(sql, key);
			if (db != null) {
				KDbResultSet rs = db.sendQuery(sql);
				while (rs.next()) {
					result.add(rs.getObject(1));
				}
				db.close();
			}
		} catch (InstantiationException | IllegalAccessException | SQLException e) {
			Ko.klogger().log(Level.SEVERE, "Impossible de charger les objets : " + clazz, e);
		}
		return result;
	}
}
