/**
 * Classe KApplication
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KApplication.java,v 1.5 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.run
 */
package net.ko.run;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.ko.creator.KClassCreator;
import net.ko.creator.KernelCreator;
import net.ko.db.KDataBase;
import net.ko.db.KDbResultSet;
import net.ko.db.provider.KDBCustom;
import net.ko.db.provider.KDBMysql;
import net.ko.db.provider.KDBOdbc;
import net.ko.db.provider.KDBOracle;
import net.ko.kobject.KObject;
import net.ko.utils.KProperties;
import net.ko.utils.KTextFile;

public class KApplication extends KObject {
	private KDataBase db;
	private String dbType;
	private String host;
	private String user;
	private String password;
	private String base;
	private String create;
	private String pack;
	private String port;
	@SuppressWarnings("unused")
	private String connect;
	private String url;
	private String driver;
	private KProperties errors;

	@SuppressWarnings("unchecked")
	private void __setProperties(KProperties saisie) {
		@SuppressWarnings("rawtypes")
		HashMap<String, Object> map = new HashMap<String, Object>((Map) saisie.getProperties());
		try {
			setAttributes(map, false);
			Console.out.println(getDbStatus());
		} catch (Exception e) {
		}
		showAttributes(map);
	}

	private void showAttributes(HashMap<String, Object> map) {

		Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> pairs = (Map.Entry<String, Object>) it.next();
			String variable = pairs.getKey().toString();
			if (existsVariable(variable))
				System.out.println("\n" + pairs.getKey() + " = " + getVariable(variable));
		}
	}

	private String getVariable(String variable) {
		String ret = "Aucune variable correspondant à " + variable;
		try {
			ret = __getAttribute(variable).toString();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return ret;
	}

	private boolean existsVariable(String variable) {
		boolean ret = true;
		try {
			__getAttribute(variable);
		} catch (SecurityException e) {
			ret = false;
		} catch (IllegalArgumentException e) {
			ret = false;
		} catch (NoSuchFieldException e) {
			ret = false;
		} catch (IllegalAccessException e) {
			ret = false;
		}
		return ret;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
		db = null;
		if (dbType != null) {
			if (dbType.equalsIgnoreCase("odbc"))
				db = new KDBOdbc();
			if (dbType.equalsIgnoreCase("mysql"))
				db = new KDBMysql();
			if (dbType.equalsIgnoreCase("oracle"))
				db = new KDBOracle();
			if (dbType.equalsIgnoreCase("custom"))
				db = new KDBCustom();
		}
	}

	public void send(KProperties saisie) {
		__setProperties(saisie);
		if (saisie.keyExist("sql")) {
			String sql = new KProperties(saisie.getPropertiesString()).getProperty("sql");
			sendQuery(sql);
		}
		if (saisie.keyExist("?")) {
			String variable = new KProperties(saisie.getPropertiesString()).getProperty("?");
			Console.out.println(getVariable(variable));
		}
		if (saisie.keyExist("create")) {
			create();
		}
		if (saisie.keyExist("help")) {
			help(saisie);
		}
		if (saisie.keyExist("connect"))
			if (connect())
				System.out.println("connexion réussie");
		if (saisie.keyExist("disconnect"))
			if (disconnect())
				System.out.println("Déconnexion");
	}

	public void help(KProperties saisie) {
		String hlp = "Aucune aide disponible sur cette rubrique";
		String str = KTextFile.open(net.ko.utils.KApplication.getRootPath(KClassCreator.class) + "/" + "net/ko/run/msg/help.properties");
		if (!saisie.getProperty("help").equals("")) {
			String strHlp = new KProperties(str).getProperty(saisie.getProperty("help"));
			if (strHlp != null)
				hlp = strHlp;
		}
		else
			hlp = new KProperties(str).toString();
		Console.out.println(hlp);
	}

	public void create() {
		if (create.equals(""))
			create = ".*";
		KernelCreator knl = new KernelCreator(create, new ArrayList<String>(Arrays.asList(create.split(","))));
		try {
			if (db != null) {
				knl.connect(db);
				knl.createClasses();
				knl.saveAs(pack);
				// knl.createClasses();
			}
			else
				System.out.println(errors.getProperty("dbNull"));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendQuery(String sql) {
		try {
			KDbResultSet rs = db.sendQuery(sql);
			Console.out.println("Résultat de la requête : " + sql);
			Console.out.println(rs);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean connect() {
		setDbType(dbType);
		boolean ret = false;
		if (db != null) {
			try {
				if (db.getClass().getName().equalsIgnoreCase("kdbcustom")) {
					KDBCustom d = (KDBCustom) db;
					d.connect(driver, url);
					db = d;
				}
				else {
					if (port.equals(""))
						db.connect(host, user, password, base);
					else
						db.connect(host, user, password, base, port);
				}
				ret = true;
			} catch (ClassNotFoundException e) {
				ret = false;
				e.printStackTrace();
			} catch (SQLException e) {
				ret = false;
				e.printStackTrace();
			}
		}
		Console.out.println(getDbStatus());
		return ret;
	}

	public boolean disconnect() {
		boolean ret = false;
		if (db != null) {
			try {
				db.close();
				ret = true;
			} catch (SQLException e) {
				ret = false;
			}
		}
		return ret;
	}

	public KApplication() {
		host = "localhost";
		user = "root";
		base = "mysql";
		password = "";
		dbType = "mysql";
		pack = "kernel.pojo";
		create = ".*";
		port = "3306";
		String str = KTextFile.open(net.ko.utils.KApplication.getRootPath(KClassCreator.class) + "/" + "net/ko/run/msg/error.properties");
		errors = new KProperties(str);
		connect = "Connect commande";
	}

	public String getDbStatus() {
		String ret = "dbStatus : non connecté";
		if (db != null) {
			ret = "Connecté au serveur " + host;
			String b = db.getBase();
			if (b != null)
				ret += "\nBase active : " + b;
		}
		return ret;
	}

}
