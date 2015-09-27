package net.ko.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.ko.cache.KCache;
import net.ko.db.KDataBase;
import net.ko.db.KDataTypeConverter;
import net.ko.db.KDbConnectionPool;
import net.ko.db.KDbResultSet;
import net.ko.framework.Ko;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.ksql.KParameterizedExecute;
import net.ko.ksql.KParameterizedInstruction;
import net.ko.ksql.KPreparedStatement;
import net.ko.persistence.GenericDAOEngine;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;

@SuppressWarnings("unchecked")
public class DatabaseDAO<T extends KObject> extends KGenericDao<T> {
	private Class<T> clazz;
	protected KDataBase db;
	protected KDbConnectionPool dbPool;
	protected boolean loading;
	protected GenericDAOEngine engine;

	public DatabaseDAO(Class<T> clazz, KDbConnectionPool pool) {
		super();
		this.clazz = clazz;
		this.dbPool = pool;
	}

	public KDataBase getDatabase() {
		return getDatabase(true);
	}

	public KDataBase getDatabase(boolean connect) {
		try {
			db = dbPool.getConnection(connect);
		} catch (InstantiationException | IllegalAccessException e) {
			Ko.klogger().log(Level.SEVERE, "Impossible d'obtenir une connexion du pool", e);
		}
		return db;
	}

	public KDataBase getNewDatabase(boolean connect) {
		KDataBase database = dbPool.getNewConnection(connect);
		return database;
	}

	@Override
	public void close() {
		if (db != null)
			try {
				db.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public Object create(T newInstance) {
		newInstance.toAdd();
		updateToSupport(newInstance);
		return newInstance.getFirstKeyValue();
	}

	@Override
	public T readById(Object id) {
		T result = null;
		try {
			result = clazz.newInstance();
			if (id instanceof Object[]) {
				try {
					DatabaseDAOObjectUtils.loadOneByPriKeys(result, getDatabase(), (Object[]) id);
				} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | InvocationTargetException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					DatabaseDAOObjectUtils.loadOneById(result, getDatabase(), id);
				} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public KListObject<T> readAll() {
		KListObject<T> listObject = (KListObject<T>) KListObject.getListInstance(clazz);
		DatabaseDAOListUtils.loadFromDb((DatabaseDAO<KObject>) this, (KListObject<KObject>) listObject);
		return listObject;
	}

	@Override
	public void update(T object) {
		object.toUpdate();
		updateToSupport(object);
	}

	@Override
	public void delete(T object) {
		object.toDelete();
		updateToSupport(object);
	}

	@Override
	public void delete(Object id) {
		Object[] ids = null;
		if (id instanceof Object[]) {
			ids = (Object[]) id;
		} else {
			ids = new Object[] { id };
		}
		KDataBase db = getDatabase();
		KObject ko = Ko.getKoInstance(clazz);
		if (ko != null) {
			String tableName = ko.getTableName();
			String sql = "DELETE FROM " + quote() + tableName + quote() + " WHERE " + KStrings.implode((String[]) ids, Ko.getKoInstance(clazz).getFirstKey(), " OR ", "'", "=", KGlueMode.KEY_AND_VALUE, quote(), true);
			try {
				db.execute(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					db.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public KParameterizedInstruction getObjectCondition(T object) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		return new KParameterizedInstruction(quote(), object.getKeyValues(false, true));
	}

	@Override
	public T read(T object) {
		try {
			return read(object, getObjectCondition(object));
		} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
			return read(object, new KParameterizedInstruction(quote()));
		}
	}

	@Override
	public T read(T object, KParameterizedInstruction condition) {
		return read(object, condition, 0);
	}

	@Override
	public T read(T object, KParameterizedInstruction condition, int witch) {
		T result = null;
		if (Ko.useCache)
			result = (T) object.loadFromCache();

		if (result == null) {
			KDataBase db = getDatabase();
			condition.setTableName(object.getTableName());
			condition.setLimit(db.limit(witch, 1));
			String query = condition.parse();
			KDbResultSet rs = null;
			KPreparedStatement st = null;
			try {
				st = new KPreparedStatement(db, query);
				rs = st.sendQuery(condition.getFields());
				if (DatabaseDAOObjectUtils.loadFromDb(object, rs))
					result = object;
			} catch (SQLException e) {
				e.printStackTrace();
				Ko.klogger().log(Level.SEVERE, "Impossible d'exécuter la requête : " + query, e);
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (st != null)
						st.close();
					db.close();
				} catch (SQLException e) {
				}
			}

		}
		return result;
	}

	@Override
	public KListObject<T> readAll(KParameterizedInstruction condition) {
		return readAll(condition, -1);
	}

	@Override
	public KListObject<T> readAll(KParameterizedInstruction condition, int orderBy) {
		KListObject<T> listObject = (KListObject<T>) KListObject.getListInstance(clazz);
		condition.setSql(listObject.getSql());
		// TODO A Modifier
		listObject.setSql(condition.parse(), quote());
		DatabaseDAOListUtils.loadFromDb((DatabaseDAO<KObject>) this, (KListObject<KObject>) listObject, orderBy);
		return listObject;
	}

	@Override
	public KListObject<T> readAll(Object id) {
		return readAll(id, false);
	}

	@Override
	public KListObject<T> readAll(Object id, boolean others) {
		KListObject<T> result = (KListObject<T>) KListObject.getListInstance(clazz);
		if (id instanceof Object[]) {
			List<Object> keys = new ArrayList<>(Arrays.asList((Object[]) id));
			if (others)
				DatabaseDAOListUtils.loadFromDbByPriKeysNotIn((DatabaseDAO<KObject>) this, (KListObject<KObject>) result, keys);
			else
				DatabaseDAOListUtils.loadFromDbByPriKeys((DatabaseDAO<KObject>) this, (KListObject<KObject>) result, keys);
		} else {
			KObject o = readById(id);
			result.add(o, true);
		}
		return result;
	}

	@Override
	public KListObject<T> readAll(Object id, boolean others, int depth) {
		Ko.setTempConstraintDeph(depth);
		KListObject<T> result = readAll(id, others);
		Ko.restoreConstraintDeph();
		return result;
	}

	@Override
	public boolean exists(T object) {
		read(object);
		return object.isLoaded();
	}

	@Override
	public boolean exists(Object id) {
		KObject object = readById(id);
		return object.isLoaded();
	}

	@Override
	public boolean exists(T object, KParameterizedInstruction condition) {
		read(object, condition);
		return object.isLoaded();
	}

	@Override
	public boolean updateToSupport(T object) {
		return updateToSupport(object, false);
	}

	protected boolean updateToSupport(T object, boolean fromList) {
		return DatabaseDAOObjectUtils.updateToDb(object, getDatabase(), fromList, this);
	}

	@Override
	public void update(KListObject<? extends KObject> listObject) {
		listObject.updateItemsToDelete();
		for (KObject ko : listObject) {
			updateToSupport((T) ko, true);
		}
		for (KObject ko : listObject.getDeletedItems()) {
			updateToSupport((T) ko, true);
		}
		if (Ko.useCache)
			KCache.replace(listObject);
	}

	@Override
	public void setSelect(KListObject<T> listObject, String select) {
		listObject.setSql(select, quote());
		DatabaseDAOListUtils.loadFromDb((DatabaseDAO<KObject>) this, (KListObject<KObject>) listObject, listObject.getSql());

	}

	@Override
	public KParameterizedInstruction getSelect(T object, KParameterizedInstruction condition) {
		condition.setSql(object.getSql(quote()));
		return condition;
	}

	/**
	 * Retourne l'instruction permettant de filtrer les objets dont les membres
	 * contiennent la valeur du <b>filtre</b> passé en paramètre
	 * 
	 * @param filter
	 *            valeur du filtre à appliquer
	 * @return instruction SQL
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	@Override
	public String makeFilter(T object, String filter) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		Map<String, Object> values = new HashMap<String, Object>();
		for (String field : object.getFieldNames()) {
			if (KDataTypeConverter.isSerializable(object.getFieldType(field)))
				values.put(field, "'%" + filter + "%'");
		}
		String where = KStrings.implode_param(values, " or ", "", " like ", KGlueMode.KEY_AND_VALUE, quote(), false);
		return object.getQuery("where " + where, quote());
	}

	@Override
	public KDaoSpec getDaoSpec() {
		if (daoSpec == null)
			daoSpec = getDatabase(false).getDbSpec();
		return daoSpec;
	}

	@Override
	public String quote() {
		return getDaoSpec().getProtectChar();
	}

	@Override
	public KListObject<T> readAll(Object... ids) {
		return readAll(ids, false);
	}

	public void setDb(KDataBase db) {
		this.db = db;
	}

	@Override
	public KListObject<T> gotoo(KListObject<T> listObject, int limit, int offset, String sortedField, boolean asc) {
		String orderBy = "";
		// String strSortedSens = "ASC";
		KDataBase db = getDatabase();
		/*
		 * if (!asc) strSortedSens = "DESC"; if (sortedField != null &&
		 * !"".equals(sortedField)) orderBy = " ORDER BY " + sortedField + " " +
		 * strSortedSens; else orderBy = " ORDER BY 1 ASC";
		 */
		String sql = listObject.getSql();
		if (limit != -1) {
			sql += db.limit(limit, offset);
		}
		// String sql = KSqlQuery.addOrderBy(listObject.getSql(), orderBy);
		setSelect(listObject, sql);
		listObject.sortBy(sortedField, asc);
		return listObject;
	}

	@Override
	public boolean exists(T object, String condition) {
		KStrings strs = new KStrings(condition, ",", "=");
		return exists(object, new KParameterizedInstruction(quote(), strs.getStrings()));
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	public void memoriseQuery(KParameterizedExecute query) {
		if (engine != null) {
			engine.addQuery(query);
		}
	}

	public GenericDAOEngine getEngine() {
		return engine;
	}

	public void setEngine(GenericDAOEngine engine) {
		this.engine = engine;
	}
}
