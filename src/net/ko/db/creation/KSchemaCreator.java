package net.ko.db.creation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.ko.bean.KClass;
import net.ko.db.KDataBase;
import net.ko.framework.Ko;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.utils.KPackageUtils;

public class KSchemaCreator {
	protected String kpackage;
	protected KListObject<KClass> kclasses;
	protected Map<String, KForeignKey> foreignKeys;

	public KSchemaCreator() {
		this(Ko.kpackage());
	}

	public KSchemaCreator(String kpackage) {
		this.kpackage = kpackage;
		kclasses = getListClasses();
		foreignKeys = new HashMap<String, KForeignKey>();
	}

	public KListObject<KClass> getListClasses() {
		try {
			return KPackageUtils.getKClasses(kpackage);
		} catch (ClassNotFoundException | IOException e) {
			Ko.klogger().log(Level.WARNING, "Impossible de charger la liste des classes du package " + kpackage, e);
		}
		return new KListObject<>(KClass.class);
	}

	public String getScript(KDataBase db) {
		String result = "";
		for (KClass cls : kclasses) {
			KObject ko = Ko.getKoInstance(cls.getClazz());
			if ("".equals(ko.getSql()) || ko.getSql() == null) {
				KTableCreator tblCreator = new KTableCreator(cls.getClazz());
				tblCreator.setDb(db);
				result += tblCreator.getSQLCreate();
			}
		}
		return result;
	}

	public String getConstraints(KDataBase db) {
		String result = "";
		for (KClass cls : kclasses) {
			KObject ko = Ko.getKoInstance(cls.getClazz());
			if ("".equals(ko.getSql()) || ko.getSql() == null) {
				KTableCreator tblCreator = new KTableCreator(cls.getClazz());
				tblCreator.setDb(db);
				result += tblCreator.getPKConstraint();
				result += tblCreator.addAutoIncrement();
				for (KForeignKey fk : tblCreator.getForeignKeys()) {
					foreignKeys.put(fk.getFkName(db), fk);
				}
				result += tblCreator.alterTableAddUniqueConstraints(db);
			}
		}
		result += getForeignKeysScript(db);
		return result;
	}

	public String getForeignKeysScript(KDataBase db) {
		String result = "";
		for (Map.Entry<String, KForeignKey> e : foreignKeys.entrySet()) {
			result += e.getValue().getScript(db);
		}
		return result;
	}

	public boolean dataBaseExists(KDataBase db) {
		boolean result = false;
		if (!db.isValid())
			try {
				db.connect();
				result = true;
			} catch (ClassNotFoundException | SQLException e) {
				result = false;
				Ko.klogger().log(Level.WARNING, "Impossible de se connecter à la base de données " + db.getBase(), e);
			}

		return result;
	}

	public void createDatabase(KDataBase db) throws SQLException {
		String base = db.getParams().getBase();
		db.createDatabase(base);
	}

	public void execute(KDataBase db) throws SQLException {
		db.execute(getScript(db));
		db.execute(getConstraints(db));
	}
}
