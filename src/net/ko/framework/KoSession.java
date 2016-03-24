package net.ko.framework;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import net.ko.dao.DaoList;
import net.ko.dao.IGenericDao;
import net.ko.db.KDataBase;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.kobject.KRecordStatus;
import net.ko.ksql.KParameterizedInstruction;
import net.ko.ksql.KSqlParameter;

@SuppressWarnings("unchecked")
public class KoSession {

	public static <T extends KObject> T kloadOne(Class<T> clazz, Object... ids) {
		T result = null;
		try {
			result = clazz.newInstance();
			result.setKeyValues(new ArrayList<>(Arrays.asList(ids)));
			result = (T) kloadOne(result);
		} catch (InstantiationException | IllegalAccessException | SecurityException | NoSuchFieldException e) {
			result = null;
		}
		return result;
	}

	public static <T extends KObject> T kloadOne(T object) {
		IGenericDao<T> dao = (IGenericDao<T>) getDao(object.getClass());
		try {
			dao.read(object);
		} finally {
			dao.close();
		}
		return object;
	}

	public static <T extends KObject> T kloadOne(Class<T> clazz, String condition) {
		T result = null;
		IGenericDao<T> dao = getDao(clazz);
		try {
			result = clazz.newInstance();
			KParameterizedInstruction instruction = new KParameterizedInstruction(dao.quote(), result.getTableName(), condition);
			dao.read(result, instruction, 0);
		} catch (InstantiationException | IllegalAccessException e) {
			result = null;
		} finally {
			dao.close();
		}
		return result;
	}

	public static <T extends KObject> T kloadOne(Class<T> clazz, KSqlParameter... parameters) {
		T result = null;
		IGenericDao<T> dao = getDao(clazz);
		try {
			result = clazz.newInstance();
			KParameterizedInstruction instruction = new KParameterizedInstruction(dao.quote(), KSqlParameter.parametersMap(parameters));
			instruction.setTableName(result.getTableName());
			dao.read(result, instruction, 0);
		} catch (InstantiationException | IllegalAccessException e) {
			result = null;
		} finally {
			dao.close();
		}
		return result;
	}

	public static <T extends KObject> T kloadOne(Class<T> clazz, String condition, Object... conditionValues) {
		T result = null;
		IGenericDao<T> dao = getDao(clazz);
		try {
			result = clazz.newInstance();
			KParameterizedInstruction instruction = new KParameterizedInstruction(dao.quote(), condition, new ArrayList<>(Arrays.asList(conditionValues)));
			instruction.setTableName(result.getTableName());
			dao.read(result, instruction, 0);
		} catch (InstantiationException | IllegalAccessException e) {
			result = null;
		} finally {
			dao.close();
		}
		return result;
	}

	public static <T extends KObject> KListObject<T> kloadMany(Class<T> clazz) {
		return kloadMany(clazz, "", "");
	}

	public static <T extends KObject> KListObject<T> kloadMany(Class<T> clazz, String condition) {
		return kloadMany(clazz, condition, "");
	}

	public static <T extends KObject> KListObject<T> kloadMany(Class<T> clazz, Object... ids) {
		KListObject<T> result = null;
		IGenericDao<T> dao = getDao(clazz);
		try {
			result = (KListObject<T>) dao.readAll(ids);
		} finally {
			dao.close();
		}
		return result;
	}

	public static <T extends KObject> KListObject<T> kloadMany(Class<T> clazz, String condition, String orderBy) {
		KListObject<? extends KObject> result = null;
		IGenericDao<T> dao = getDao(clazz);
		try {
			KParameterizedInstruction instruction = new KParameterizedInstruction(dao.quote(), condition);
			instruction.setOrderBy(orderBy);
			result = dao.readAll(instruction);
		} finally {
			dao.close();
		}
		return (KListObject<T>) result;
	}

	public static <T extends KObject> KListObject<T> kloadMany(Class<T> clazz, KSqlParameter... parameters) {
		KListObject<T> result = null;
		IGenericDao<T> dao = getDao(clazz);
		try {
			KParameterizedInstruction instruction = new KParameterizedInstruction(dao.quote(), KSqlParameter.parametersMap(parameters));
			result = (KListObject<T>) dao.readAll(instruction);
		} finally {
			dao.close();
		}
		return result;
	}

	public static <T extends KObject> KListObject<T> kloadMany(Class<T> clazz, String condition, Object... conditionValues) {
		KListObject<T> result = null;
		IGenericDao<T> dao = getDao(clazz);
		try {
			KParameterizedInstruction instruction = new KParameterizedInstruction(dao.quote(), condition, new ArrayList<>(Arrays.asList(conditionValues)));
			result = dao.readAll(instruction);
		} finally {
			dao.close();
		}
		return result;
	}

	public static <T extends KObject> void add(T object) throws SQLException {
		object.toAdd();
		persist(object);
	}

	public static <T extends KObject> void update(T object) throws SQLException {
		object.toUpdate();
		persist(object);
	}

	public static <T extends KObject> void delete(T object) throws SQLException {
		object.toDelete();
		persist(object);
	}

	public static <T extends KObject> void persist(T object) throws SQLException {
		if (KRecordStatus.rsNone.equals(object.getRecordStatus()))
			object.toAdd();
		IGenericDao<T> dao = (IGenericDao<T>) getDao(object.getClass());
		try {
			dao.updateToSupport(object);
		} finally {
			dao.close();
		}
	}

	public static <T extends KObject> void persist(KListObject<T> list) throws SQLException {
		IGenericDao<T> dao = getDao(list.getClazz());
		try {
			dao.update(list);
		} finally {
			dao.close();
		}
	}

	public static <T extends KObject> IGenericDao<T> getDao(Class<T> clazz) {
		return Ko.getDao(clazz);
	}

	public static DaoList getDaoList(Class<? extends KObject> clazz) {
		return Ko.getDaoList(clazz);
	}

	public static KDataBase kdefaultDatabase(Class<? extends KObject> clazz) {
		return Ko.kdefaultDatabase(clazz);
	}

	public static <T extends KObject> int count(Class<T> clazz, String condition) throws SQLException {
		return Ko.getDao(clazz).count(clazz, condition);
	}

	public static <T extends KObject> int count(Class<T> clazz) throws SQLException {
		return Ko.getDao(clazz).count(clazz);
	}
}
