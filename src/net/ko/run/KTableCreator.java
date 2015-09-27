/**
 * Classe KTableCreator
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KTableCreator.java,v 1.5 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.ksql
 */
package net.ko.run;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import net.ko.db.KDataBase;
import net.ko.kobject.KObject;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;

public class KTableCreator {
	private final Class<? extends KObject> clazz;
	private HashMap<String, Object> fields;
	private String tableName;
	private KObject instance;

	private String getPKConstraint() {
		return " ALTER TABLE `" + instance.getTableName() + "` ADD PRIMARY KEY (" + new KStrings(instance.getKeyFields()).implode_param(",", "`", "", KGlueMode.VALUE, false) + ");";
	}

	private String getSQLCreate() {
		return "CREATE TABLE `" + tableName + "` (" + KStrings.implode_param(fields, ",", "", " ", KGlueMode.KEY_AND_VALUE, false) + ");";
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setFieldType(String fieldName, String fieldType) {
		fields.put(fieldName, fieldType);
	}

	public KTableCreator(Class<? extends KObject> clazz) {
		super();
		this.clazz = clazz;
		fields = new HashMap<String, Object>();
		__setFields();
		instance = null;
		try {
			instance = clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		tableName = instance.getTableName();
	}

	private void __setFields() {
		fields.put("id", javaTypeToDbType(int.class));
		Field[] sfields = clazz.getDeclaredFields();
		for (int i = 0; i < sfields.length; i++) {
			sfields[i].setAccessible(true);
			String t = javaTypeToDbType(sfields[i].getType());
			if (t != null)
				fields.put(sfields[i].getName(), t);
		}
	}

	public static String javaTypeToDbType(Class<? extends Object> aClazz) {
		if (aClazz.equals(Boolean.class) || aClazz.equals(boolean.class))
			return "BIT";
		if (aClazz.equals(Integer.class) || aClazz.equals(int.class))
			return "INTEGER";
		if (aClazz.equals(Long.class) || aClazz.equals(long.class))
			return "BIGINT";
		if (aClazz.equals(Short.class) || aClazz.equals(short.class))
			return "SMALLINT";
		if (aClazz.equals(Double.class) || aClazz.equals(double.class))
			return "DOUBLE";
		if (aClazz.equals(Float.class) || aClazz.equals(float.class))
			return "REAL";
		if (aClazz.equals(Byte.class) || aClazz.equals(byte.class))
			return "TINYINT";
		if (aClazz.equals(Date.class))
			return "DATE";
		if (aClazz.equals(java.sql.Date.class))
			return "DATE";
		if (aClazz.equals(java.sql.Time.class))
			return "TIME";
		if (aClazz.equals(String.class))
			return "VARCHAR(50)";
		if (aClazz.equals(char.class) || aClazz.equals(Character.class))
			return "VARCHAR(1)";
		return null;
	}

	@Override
	public String toString() {
		return getSQLCreate() + "\n" + getPKConstraint();
	}

	public boolean execute(KDataBase db) {
		return execute(db, true);
	}

	public boolean execute(KDataBase db, boolean dropBefore) {
		boolean ret = false;
		boolean execute = false;
		try {
			if (dropBefore) {
				if (db.tableExist(tableName)) {
					execute = false;
					String rep = "";
					System.out.println("Supprimer la table " + tableName + "?Y/N");
					BufferedReader inStr = new BufferedReader(new InputStreamReader(System.in));
					do {
						try {
							rep = inStr.readLine();
						} catch (Exception e) {
						}
					} while (!(rep.equalsIgnoreCase("y") || rep.equalsIgnoreCase("n")));
					if (rep.equalsIgnoreCase("y")) {
						dropTable(db);
						execute = true;
					}
				} else
					execute = true;

			} else
				execute = true;
			if (execute) {
				System.out.println(toString());
				db.execute(getSQLCreate());
				db.execute(getPKConstraint());
			}
		} catch (SQLException e) {
			ret = false;
			e.printStackTrace();
		}
		return ret;
	}

	public void dropTable(KDataBase db) {
		try {
			db.execute("DROP TABLE `" + tableName + "`");
			System.out.println("Suppression de ta table `" + tableName + "`...");
		} catch (SQLException e) {
		}
	}
}
