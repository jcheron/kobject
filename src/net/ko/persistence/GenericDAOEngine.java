package net.ko.persistence;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.ko.bean.KClass;
import net.ko.bean.KDbParams;
import net.ko.bean.KListClass;
import net.ko.dao.DaoList;
import net.ko.dao.DatabaseDAO;
import net.ko.db.KDataBase;
import net.ko.db.KDbConnectionPool;
import net.ko.db.validation.KSchemaDDL;
import net.ko.db.validation.KSchemaValidator;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.ksql.KParameterizedExecute;
import net.ko.ksql.KPreparedStatement;

@SuppressWarnings("unchecked")
public class GenericDAOEngine {
	private KDbConnectionPool pool;
	private Map<Class<? extends KObject>, DatabaseDAO<? extends KObject>> daosObject;
	private Map<Class<? extends KObject>, DaoList> daoLists;
	private KDbParams dbParams;
	private List<KParameterizedExecute> queries;

	public GenericDAOEngine(final String dbType) {
		this(dbType, "", "", "", "", "", "");
	}

	public GenericDAOEngine(final String dbType, String host, String base, String user, String pass, String port, String options) {
		dbParams = new KDbParams(host, user, pass, base, port, options);
		queries = new ArrayList<>();
		daosObject = new HashMap<Class<? extends KObject>, DatabaseDAO<? extends KObject>>();
		daoLists = new HashMap<>();
		pool = new KDbConnectionPool() {

			@Override
			protected KDataBase createDbPoolInstance(boolean connect) {
				Class<? extends KDataBase> c = KDataBase.getDbClassInstance(dbType);
				if (c == null)
					return null;
				KDataBase db = null;
				try {
					db = c.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					Ko.klogger().log(Level.SEVERE, "Impossible d'instancier la classe du driver : " + c.getClass(), e);
				}
				db.setParams(dbParams);
				if (connect) {
					try {
						db.connect();
					} catch (ClassNotFoundException | SQLException e) {
						Ko.klogger().log(Level.SEVERE, "Connexion impossible à la base de données " + db.getURL(), e);
					}
				}
				return db;
			}
		};
	}

	public void init(KSchemaDDL schemaDdl) {
		if (!KSchemaDDL.sdNone.equals(schemaDdl)) {
			KDataBase db = null;
			try {
				db = kdatabase(false);
				KSchemaValidator sCreator = new KSchemaValidator(Ko.kpackage());
				sCreator.setAutoRepare(schemaDdl.equals(KSchemaDDL.sdUpdate));
				sCreator.execute(db);
				Ko.klogger().log(Level.INFO, "Réparation terminée avec succès");
			} catch (SQLException e) {
				Ko.klogger().log(Level.SEVERE, "Impossible vérifier le schéma de la base de données", e);
				try {
					db.rollBackTrans();
				} catch (SQLException e1) {
				}
			} finally {
				try {
					if (db != null)
						db.close();
				} catch (SQLException e) {

				}
			}
		}
	}

	public KDataBase kdatabase() {
		return kdatabase(true);
	}

	public KDataBase kdatabase(boolean connect) {
		KDataBase db = null;
		try {
			db = pool.kconnection(connect);
		} catch (InstantiationException | IllegalAccessException e) {
			Ko.klogger().log(Level.SEVERE, "Impossible de créer la connexion à la base de données", e);
		}
		return db;
	}

	public boolean isValid() {
		KDataBase db = kdatabase(false);
		return db != null;
	}

	public void removeAll() {
		pool.removeAll();
	}

	public <T extends KObject> DatabaseDAO<T> getDao(Class<T> clazz) {
		DatabaseDAO<T> result;
		if (!daosObject.containsKey(clazz)) {
			result = new DatabaseDAO<>(clazz, pool);
			result.setEngine(this);
			daosObject.put(clazz, result);
		} else
			result = (DatabaseDAO<T>) daosObject.get(clazz);
		return result;
	}

	public DaoList getDaoList(Class<? extends KObject> clazz) {
		DaoList result;
		if (!daoLists.containsKey(clazz)) {
			result = new DaoList();
			daoLists.put(clazz, result);
		} else
			result = daoLists.get(clazz);
		return result;
	}

	public DaoList getDefault(Class<? extends KObject> clazz) {
		DaoList result;
		if (!daoLists.containsKey(clazz)) {
			result = new DaoList();
			result.add(getDao(clazz));
			daoLists.put(clazz, result);
		} else
			result = daoLists.get(clazz);
		return result;
	}

	public KDbConnectionPool getPool() {
		return pool;
	}

	public void setPool(KDbConnectionPool pool) {
		this.pool = pool;
	}

	public Map<Class<? extends KObject>, DatabaseDAO<? extends KObject>> getDaosObject() {
		return daosObject;
	}

	public void setDbParams(KDbParams dbParams) {
		this.dbParams = dbParams;
	}

	public Map<Class<? extends KObject>, KListObject<? extends KObject>> readEntities(KListClass listClass) {
		Ko.setTempConstraintDeph(0);
		listClass.sort();
		Map<Class<? extends KObject>, KListObject<? extends KObject>> lists = new LinkedHashMap<>();
		for (KClass cls : listClass) {
			KListObject<? extends KObject> list = getDao(cls.getClazz()).readAll();
			lists.put(cls.getClazz(), list);
		}
		Ko.restoreConstraintDeph();
		return lists;
	}

	public void insertEntities(Map<Class<? extends KObject>, KListObject<? extends KObject>> lists) {
		DatabaseDAO<? extends KObject> dao = null;
		for (Map.Entry<Class<? extends KObject>, KListObject<? extends KObject>> e : lists.entrySet()) {
			KObject ko = Ko.getKoInstance(e.getKey());
			if ("".equals(ko.getSql()) || ko.getSql() == null) {
				KListObject<? extends KObject> list = e.getValue();
				dao = getDao(list.getClazz());
				dao.setLoading(true);
				getDao(list.getClazz()).update(list);
				dao.setLoading(false);
			}
		}
	}

	public void readEntitiesAndInsertTo(KListClass listClass, GenericDAOEngine daoEngine) {
		daoEngine.init(KSchemaDDL.sdUpdate);
		daoEngine.insertEntities(readEntities(listClass));
	}

	public synchronized void addQuery(KParameterizedExecute query) {
		queries.add(query);
	}

	public void saveAllDataTo(GenericDAOEngine otherEngine) {
		KDataBase db = otherEngine.kdatabase();
		KPreparedStatement st = null;
		try {
			db.beginTrans();
			for (KParameterizedExecute query : queries) {
				st = new KPreparedStatement(db);
				query.setQuote(db.QUOTE());
				st.executeUpdate(query);
			}
			db.commitTrans();
			KDebugConsole.print("Transfert des données ok", "DAOEngine", "GenericDAOEngine.saveAllDataTo");
		} catch (SQLException e) {
			try {
				db.rollBackTrans();
			} catch (SQLException e1) {
				Ko.klogger().log(Level.WARNING, "Rollback impossible", e1);
			}
		} finally {
			try {
				if (st != null)
					st.close();
				db.close();
				removeAll();
				otherEngine.removeAll();
			} catch (SQLException e) {
				Ko.klogger().log(Level.WARNING, "Fermeture de la base impossible", e);
			}
		}
	}

	public void saveDataTo(GenericDAOEngine otherEngine, int count) {
		if (queries.size() > 0) {
			KDataBase db = otherEngine.kdatabase();
			List<KParameterizedExecute> queriesToDelete = new ArrayList<>();
			KPreparedStatement st = null;
			try {
				db.beginTrans();
				int i = 0;
				while ((i < count) && (i < queries.size())) {
					KParameterizedExecute query = queries.get(i);
					st = new KPreparedStatement(db);
					query.setQuote(db.QUOTE());
					st.executeUpdate(query);
					i++;
					queriesToDelete.add(query);
				}
				db.commitTrans();
				KDebugConsole.print("Transfert des données ok", "DAOEngine", "GenericDAOEngine.saveAllDataTo");
				for (KParameterizedExecute query : queriesToDelete) {
					queries.remove(query);
					Ko.klogger().log(Level.INFO, "Requête supprimée de la liste " + query);
				}
			} catch (SQLException e) {
				Ko.klogger().log(Level.SEVERE, "Mise à jour impossible ", e);
				try {
					db.rollBackTrans();
				} catch (SQLException e1) {
					Ko.klogger().log(Level.WARNING, "Rollback impossible", e1);
				}
			} finally {
				try {
					if (st != null)
						st.close();
					db.close();
				} catch (SQLException e) {
					Ko.klogger().log(Level.WARNING, "Fermeture de la base impossible", e);
				}
			}
		}
	}

	public List<KParameterizedExecute> getQueries() {
		return queries;
	}
}
