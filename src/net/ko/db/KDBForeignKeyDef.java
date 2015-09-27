package net.ko.db;

import java.sql.DatabaseMetaData;

public enum KDBForeignKeyDef {
	kdNoAction("NO ACTION"), kdRestrict("RESTRICT"), kdCascade("CASCADE"), kdSetNull("SET NULL"), kdSetDefault("SET DEFAULT");
	private String caption;

	private KDBForeignKeyDef(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}

	public static KDBForeignKeyDef getActionFromDb(short def) {
		KDBForeignKeyDef result = kdNoAction;
		switch (def) {
		case DatabaseMetaData.importedKeyNoAction:
			result = kdNoAction;
			break;
		case DatabaseMetaData.importedKeyRestrict:
			result = kdRestrict;
			break;
		case DatabaseMetaData.importedKeyCascade:
			result = kdCascade;
			break;
		case DatabaseMetaData.importedKeySetNull:
			result = kdNoAction;
			break;
		case DatabaseMetaData.importedKeySetDefault:
			result = kdSetDefault;
			break;
		default:
			break;
		}
		return result;
	}
}
