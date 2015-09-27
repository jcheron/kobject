package net.ko.dao;

import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.ksql.KParameterizedInstruction;

public interface IGenericDao<T extends KObject> {
	Object create(T newInstance);

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

	void update(T object);

	void update(KListObject<? extends KObject> listObject);

	void delete(T object);

	void delete(Object id);

	boolean exists(T object);

	boolean exists(Object id);

	boolean exists(T object, KParameterizedInstruction condition);

	boolean exists(T object, String condition);

	void close();

	void setSelect(KListObject<T> listObject, String select);

	KParameterizedInstruction getSelect(T object, KParameterizedInstruction condition);

	String quote();

	String makeFilter(T object, String filter) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException;

	boolean updateToSupport(T object);

	KListObject<T> gotoo(KListObject<T> listObject, int limit, int offset, String sortedField, boolean asc);
}
