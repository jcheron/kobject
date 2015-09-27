/**
 * Classe KernelCreator
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KernelCreator.java,v 1.5 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.ksql
 */
package net.ko.creator;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import net.ko.db.KDataBase;
import net.ko.events.EventFileListener;
import net.ko.utils.KStrings;

public class KernelCreator {

	private KDataBase db;
	private KListClasses classes;
	private ArrayList<String> tables;
	private String matchWith;
	private ArrayList<String> tablesToCreate;
	private String templateFolder;

	private EventFileListener eventFileListener = null;
	private boolean hasController = false;
	private String controllerFullPath = null;

	public String getControllerFullPath() {
		return controllerFullPath;
	}

	public void setControllerFullPath(String controllerFullPath) {
		this.controllerFullPath = controllerFullPath;
	}

	public boolean isHasController() {
		return hasController;
	}

	public void setHasController(boolean hasController) {
		this.hasController = hasController;
	}

	public void addFileListener(EventFileListener eventFileListener) {
		this.eventFileListener = eventFileListener;
	}

	public KernelCreator() {
		this(".*", new ArrayList<String>());
	}

	public KernelCreator(String matchWith) {
		this(matchWith, new ArrayList<String>());
	}

	public KernelCreator(String matchWith, ArrayList<String> tablesToCreate) {
		this.matchWith = matchWith;
		this.tablesToCreate = tablesToCreate;
	}

	public KDataBase getDb() {
		return db;
	}

	public void setDb(KDataBase db) {
		this.db = db;
	}

	public void connect(String host, String user, String pass, String base) throws ClassNotFoundException, SQLException {
		if (db != null)
			db.connect(host, user, pass, base);
	}

	public void connect(String host, String user, String pass, String base, String port) throws ClassNotFoundException, SQLException {
		if (db != null)
			db.connect(host, user, pass, base, port);
	}

	public void connect(KDataBase db, String host, String user, String pass, String base, String port) throws ClassNotFoundException, SQLException {
		this.db = db;
		connect(host, user, pass, base, port);
	}

	public void connect(KDataBase db, String host, String user, String pass, String base) throws ClassNotFoundException, SQLException {
		this.db = db;
		connect(host, user, pass, base);
	}

	public void connect(KDataBase db) throws ClassNotFoundException, SQLException {
		this.db = db;
		if (!KDataBase.isValid(db))
			db.connect();
	}

	public void createClasses(String templateFolder) {
		this.templateFolder = templateFolder;
		tables = new ArrayList<String>();
		classes = new KListClasses();
		if (eventFileListener != null)
			classes.addFileListener(eventFileListener);
		try {
			tables = db.getTableNames(db.getBaseName());
			KStrings strs = new KStrings(tablesToCreate);
			for (String table : tables) {
				if (table.matches(matchWith) || strs.contains(table))
					classes.add(new KClassCreator(db, table, templateFolder));
			}
			addConstraints();
			classes.addConstraints();
			classes.mkClasses();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createClasses() {
		createClasses("net/ko/templates/java");
	}

	public void addConstraints() {
		for (String table : tables) {
			try {
				classes.add(db.getForeignKeys(table));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void saveAs(String path, String packageName) {
		if (controllerFullPath == null) {
			File d = new File(path);
			controllerFullPath = d.getParent() + "/kox.xml";
		}
		classes.saveAs(path, packageName);
		createTestClass(path, packageName);
		createTestClasses(path, packageName);
		if (hasController)
			createController();
	}

	public void saveAs(String packageName) {
		classes.saveAs(packageName);
		createTestClass("", packageName);
		createTestClasses("", packageName);
		if (hasController)
			createController();
	}

	public String toString() {
		return classes.toString();
	}

	public void createTestClass(String path, String packageName) {
		KTestClassesCreator kt = new KTestClassesCreator("Test_" + db.getBaseName(), packageName, templateFolder);
		kt.setDbParams(db.getParams().getHost(), db.getParams().getUser(), db.getParams().getPass(), db.getBaseName());
		kt.setTables(classes);
		kt.mkTpl();
		if (path.equals(""))
			kt.save();
		else
			kt.save(path);
	}

	public void createTestClasses(String path, String packageName) {
		for (KClassCreator cls : classes.getClasses()) {
			KTestClassesCreator kt = new KTestClassesCreator("Test_" + db.getBaseName() + "_" + cls.getClassName(), packageName, templateFolder);
			kt.setDbParams(db.getParams().getHost(), db.getParams().getUser(), db.getParams().getPass(), db.getBaseName());
			kt.setTable(cls);
			kt.mkTpl();
			if (path.equals(""))
				kt.save();
			else
				kt.save(path);
		}
	}

	public void createController() {
		KControllerCreator cCreator = new KControllerCreator();
		cCreator.loadFromFile(templateFolder + "/controller.tpl");
		classes.mkController(db, cCreator);
		cCreator.saveAs(controllerFullPath);
	}
}
