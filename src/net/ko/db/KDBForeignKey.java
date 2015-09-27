/**
 * Classe KForeignKey
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KForeignKey.java,v 1.1 2010/01/18 01:01:29 jcheron Exp $
 * @package ko.ksql
 */
package net.ko.db;

import net.ko.db.creation.KForeignKey;

public class KDBForeignKey {
	private String fkName;
	private String pkTableName;
	private String pkFieldName;
	private String fkTableName;
	private String fkFieldName;
	private KDBForeignKeyDef updateRule;
	private KDBForeignKeyDef deleteRule;

	public KDBForeignKey(String fkName, String pkTableName, String pkFieldName, String fkTableName, String fkFieldName) {
		super();
		this.fkName = fkName;
		this.pkTableName = pkTableName;
		this.pkFieldName = pkFieldName;
		this.fkTableName = fkTableName;
		this.fkFieldName = fkFieldName;
	}

	public String getPkTableName() {
		return pkTableName;
	}

	public void setPkTableName(String pkTableName) {
		this.pkTableName = pkTableName;
	}

	public String getPkFieldName() {
		return pkFieldName;
	}

	public void setPkFieldName(String pkFieldName) {
		this.pkFieldName = pkFieldName;
	}

	public String getFkTableName() {
		return fkTableName;
	}

	public void setFkTableName(String fkTableName) {
		this.fkTableName = fkTableName;
	}

	public String getFkFieldName() {
		return fkFieldName;
	}

	public void setFkFieldName(String fkFieldName) {
		this.fkFieldName = fkFieldName;
	}

	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof KDBForeignKey) {
			KDBForeignKey dbFk = (KDBForeignKey) o;
			result = dbFk.getPkFieldName().equalsIgnoreCase(pkFieldName) && dbFk.getPkTableName().equalsIgnoreCase(pkTableName) && dbFk.getFkFieldName().equalsIgnoreCase(fkFieldName) && dbFk.getFkTableName().equalsIgnoreCase(fkTableName);
		}
		if (o instanceof KForeignKey) {
			KForeignKey fk = (KForeignKey) o;
			result = fk.getReferencesField().equalsIgnoreCase(pkFieldName) && fk.getReferencesTable().equalsIgnoreCase(pkTableName) && fk.getFkField().equalsIgnoreCase(fkFieldName) && fk.getTableName().equalsIgnoreCase(fkTableName);
		}
		return result;
	}

	public String getFkName() {
		return fkName;
	}

	public void setFkName(String fkName) {
		this.fkName = fkName;
	}

	public KDBForeignKeyDef getUpdateRule() {
		return updateRule;
	}

	public void setUpdateRule(KDBForeignKeyDef updateRule) {
		this.updateRule = updateRule;
	}

	public KDBForeignKeyDef getDeleteRule() {
		return deleteRule;
	}

	public void setDeleteRule(KDBForeignKeyDef deleteRule) {
		this.deleteRule = deleteRule;
	}

}
