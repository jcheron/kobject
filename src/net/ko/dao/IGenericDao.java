package net.ko.dao;

import java.sql.SQLException;

import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.ksql.KParameterizedInstruction;

public interface IGenericDao<T extends KObject> {
	Object create(T newInstance) throws SQLException;

	T readById(Object id);

	T read(T object);

	T read(T object, KParameterizedInstruction condition);

	T read(T object, KParameterizedInstruction condition, int witch);

	KListObject<T> readAll();

	KListObject<T> readAll(KParameterizedInstruction condition);

	KListObject<T> readAll(KParameterizedInstruction condition, int orderBy);

	KListObject<T> readAll(Object id);

	KListObject<T> readAll(Object... ids);

	KListObject<T> readAll(Object id, boolean others);

	KListObject<T> readAll(Object id, boolean others, int depth);

	void update(T object) throws SQLException;

	void update(KListObject<? extends KObject> listObject) throws SQLException;

	void delete(T object) throws SQLException;

	void delete(Object id);

	boolean exists(T object);

	boolean exists(Object id);

	boolean exists(T object, KParameterizedInstruction condition);

	boolean exists(T object, String condition);

	void close();

	int count(Class<T> clazz, String condition) throws SQLException;

	int count(Class<T> clazz) throws SQLException;

	void setSelect(KListObject<T> listObject, String select);

	KParameterizedInstruction getSelect(T object, KParameterizedInstruction condition);

	String quote();

	String makeFilter(T object, String filter) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException;

	boolean updateToSupport(T object) throws SQLException;

	KListObject<T> gotoo(KListObject<T> listObject, int limit, int offset, String sortedField, boolean asc);
}
