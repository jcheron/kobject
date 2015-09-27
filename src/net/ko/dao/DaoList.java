package net.ko.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;

public class DaoList {
	private List<DatabaseDAO<KObject>> items;

	public DaoList() {
		this.items = new ArrayList<>();
	}

	public List<DatabaseDAO<KObject>> getItems() {
		return items;
	}

	public void setItems(List<DatabaseDAO<KObject>> items) {
		this.items = items;
	}

	public boolean update(KObject object) {
		boolean result = false;
		for (DatabaseDAO<KObject> dao : items)
			dao.update(object);
		return result;
	}

	public boolean updateToSupport(KObject object) {
		boolean result = false;
		for (DatabaseDAO<KObject> dao : items)
			dao.updateToSupport(object);
		return result;
	}

	public boolean update(KListObject<? extends KObject> listObject) {
		boolean result = false;
		for (DatabaseDAO dao : items)
			dao.update(listObject);
		return result;
	}

	public void add(DatabaseDAO<? extends KObject> dao) {
		items.add((DatabaseDAO<KObject>) dao);
	}

	public static DaoList getDefault(Class<? extends KObject> clazz, Class<? extends DatabaseDAO> class1) {
		DatabaseDAO<? extends KObject> dao = null;
		try {
			dao = class1.getDeclaredConstructor(Class.class).newInstance(clazz);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DaoList result = new DaoList();
		if (dao != null)
			result.add(dao);
		return result;
	}

	public void close() {
		for (DatabaseDAO dao : items)
			dao.close();
	}
}
