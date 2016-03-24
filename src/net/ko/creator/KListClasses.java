/**
 * Classe KListClasses
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KListClasses.java,v 1.2 2010/01/23 01:56:39 jcheron Exp $
 * @package ko.ksql
 */
package net.ko.creator;

import java.util.ArrayList;

import net.ko.db.KDBForeignKey;
import net.ko.db.KDBForeignKeyList;
import net.ko.db.KDataBase;
import net.ko.events.EventFileListener;

import org.w3c.dom.Element;

public class KListClasses {
	private ArrayList<KClassCreator> classes;
	private KDBForeignKeyList foreignKeys;
	private EventFileListener eventFileListener;

	public void addFileListener(EventFileListener eventFileListener) {
		this.eventFileListener = eventFileListener;
	}

	public KDBForeignKeyList getForeignKeys() {
		return foreignKeys;
	}

	public void setForeignKeys(KDBForeignKeyList foreignKeys) {
		this.foreignKeys = new KDBForeignKeyList();
		add(foreignKeys);
	}

	public void add(KClassCreator classe) {
		classes.add(classe);
	}

	public void add(KDBForeignKey fk) {
		foreignKeys.add(fk);
	}

	public void add(KDBForeignKeyList fks) {
		for (KDBForeignKey fk : fks) {
			if (!existConstraint(fk))
				foreignKeys.add(fk);
		}
		// foreignKeys.addAll(fks);
	}

	public KListClasses() {
		classes = new ArrayList<KClassCreator>();
		foreignKeys = new KDBForeignKeyList();
	}

	public KClassCreator getClass(String tableName) {
		int i = 0;
		boolean trouve = false;
		while (i < classes.size() && !trouve) {
			trouve = classes.get(i).getTableName().equalsIgnoreCase(tableName);
			i++;
		}
		if (trouve)
			return classes.get(i - 1);
		else
			return null;
	}

	public void addConstraints() {
		for (KDBForeignKey fk : foreignKeys) {
			KClassCreator pkClass = getClass(fk.getPkTableName());
			KClassCreator fkClass = getClass(fk.getFkTableName());
			if (pkClass != null && fkClass != null) {
				pkClass.addHasMany(fkClass.getClassName(), fkClass.getTableName() + "s");
				fkClass.addBelongsTo(pkClass.getClassName(), pkClass.getTableName());

				pkClass.addManyMember(fk.getFkTableName());
				fkClass.addFkMember(fk.getPkTableName());
			}
		}
	}

	public boolean existConstraint(KDBForeignKey fk) {
		boolean trouve = false;
		int i = 0;
		while (i < foreignKeys.size() && !trouve) {
			trouve = foreignKeys.get(i).equals(fk);
			i++;
		}
		return trouve;
	}

	public void mkClasses() {
		for (KClassCreator cls : classes) {
			cls.mkClassMembers();
		}
		// addConstraints();
	}

	public String toString() {
		String ret = "";
		for (KClassCreator cls : classes) {
			ret += cls.toString();
		}
		return ret;
	}

	public void clean() {
		for (KClassCreator cls : classes) {
			cls.clean();
		}
	}

	public void saveAs(String packageName) {
		for (KClassCreator cls : classes) {
			if (eventFileListener != null)
				cls.addFileListener(eventFileListener);
			cls.saveAs(packageName);
		}
	}

	public void saveAs(String path, String packageName) {
		for (KClassCreator cls : classes) {
			if (eventFileListener != null)
				cls.addFileListener(eventFileListener);
			cls.saveAs(path, packageName);
		}
	}

	public ArrayList<KClassCreator> getClasses() {
		return classes;
	}

	public void mkController(KDataBase db, KControllerCreator cCreator) {
		for (KClassCreator cls : classes) {
			if (isTableSelected(cls.getTableName()) == true) {
				Element classElement = cCreator.createClassElement(cls.getClassName());
				cls.mkController(db, cCreator, classElement);
			}
		}
	}

	private boolean isTableSelected(String tableName) {
		return getClass(tableName) != null;
	}
}
