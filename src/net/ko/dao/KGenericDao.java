package net.ko.dao;

import net.ko.kobject.KObject;

public abstract class KGenericDao<T extends KObject> implements IGenericDao<T> {
	protected KDaoSpec daoSpec;

	public KDaoSpec getDaoSpec() {
		return daoSpec;
	}

	public void setDaoSpec(KDaoSpec daoSpec) {
		this.daoSpec = daoSpec;
	}
}
