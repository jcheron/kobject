package net.ko.db.validation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import net.ko.bean.KClass;
import net.ko.db.KDBForeignKeyList;
import net.ko.db.KDataBase;
import net.ko.db.creation.KSchemaCreator;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.kobject.KObject;

public class KSchemaValidator extends KSchemaCreator {
	private boolean autoRepare;
	private String alterRepareScript;
	private KDBForeignKeyList dbForeignKeys;

	public KSchemaValidator(String kpackage) {
		super(kpackage);
		autoRepare = false;
		alterRepareScript = "";
		dbForeignKeys = new KDBForeignKeyList();
	}

	@Override
	public String getScript(KDataBase db) {
		String result = "";
		alterRepareScript = "";
		ArrayList<KTableValidator> tblValidators = new ArrayList<>();
		for (KClass cls : kclasses) {
			KObject ko = Ko.getKoInstance(cls.getClazz());
			if ("".equals(ko.getSql()) || ko.getSql() == null) {
				KTableValidator tblValidator = new KTableValidator(cls.getClazz());
				tblValidator.setDb(db);
				tblValidator.execute(autoRepare);
				result += tblValidator.getRepareScript();
				alterRepareScript += tblValidator.getAlterRepareScript();
				tblValidators.add(tblValidator);
				dbForeignKeys.add(tblValidator.getDbForeignKeys());
			}
		}
		for (KTableValidator tblValidator : tblValidators) {
			tblValidator.checkTableFk(dbForeignKeys);
			try {
				tblValidator.checkTableUniqueIndexes(db.getUniqueIndexes(tblValidator.getTableCreator().getTableName()));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			alterRepareScript += tblValidator.getFkScript();
		}

		return result;
	}

	@Override
	public void execute(KDataBase db) throws SQLException {
		KDebugConsole.print("Vérification du schéma de la base " + db.getBase(), "FRAMEWORK", "KSchemaValidator.execute");
		if (autoRepare) {
			if (db.implementsCreateDb())
				if (!dataBaseExists(db))
					createDatabase(db);
		}
		if (!db.isValid())
			try {
				db.connect();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		String sql = getScript(db);
		if ("".equals(sql.trim()) && "".equals(alterRepareScript.trim())) {
			Ko.klogger().log(Level.INFO, "Structure de la BDD Ok");
		} else {
			if (autoRepare) {
				db.executeBatch(sql);
				db.executeBatch(alterRepareScript);
			}
		}
	}

	public boolean isAutoRepare() {
		return autoRepare;
	}

	public void setAutoRepare(boolean autoRepare) {
		this.autoRepare = autoRepare;
	}
}
