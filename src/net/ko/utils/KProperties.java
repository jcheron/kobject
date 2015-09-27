/**
 * Classe KProperties
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2012
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KProperties.java,v 1.3 2010/01/23 01:56:39 jcheron Exp $
 * @package ko.utils
 */
package net.ko.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import net.ko.creator.KClassCreator;
import net.ko.framework.KDebugConsole;
import net.ko.java.inheritance.SortedProperties;

public class KProperties implements Serializable {
	private static final long serialVersionUID = 1L;
	private String propertySep;
	private String propertyEqual;
	private String propertiesString;
	private Properties properties;
	private String fileName;

	public String getPropertiesString() {
		return propertiesString;
	}

	public void setPropertiesString(String propertiesString) {
		this.propertiesString = propertiesString;
	}

	public KProperties() {
		this(false);
	}

	public KProperties(boolean sorted) {
		propertySep = ";";
		propertyEqual = "=";
		if (sorted)
			properties = new SortedProperties();
		else
			properties = new Properties();
	}

	public KProperties(Properties properties) {
		propertySep = "\n";
		propertyEqual = "=";
		this.properties = properties;
	}

	public KProperties(String propertiesString, String sep, String equal) {
		this.propertiesString = propertiesString;
		propertySep = sep;
		propertyEqual = equal;
		properties = new Properties();
		try {
			StringReader str = new StringReader(propertiesString.replace(propertySep, "\n").replace(propertyEqual, "="));
			properties.load(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public KProperties(String propertiesString, String sep) {
		this(propertiesString, sep, "=");
	}

	public KProperties(String propertiesString) {
		this(propertiesString, ";", "=");
	}

	public Set<Entry<Object, Object>> getEntry() {
		return properties.entrySet();
	}

	public String getProperty(String key) {
		String result = properties.getProperty(key);
		KDebugConsole.print(key + ":" + result, "CONFIG", "getProperty");
		return result;
	}

	public String getProperty(String key, String defaultValue) {
		String result = properties.getProperty(key, defaultValue);
		KDebugConsole.print(key + ":" + result, "CONFIG", "getProperty");
		return result;
	}

	public boolean getProperty(String key, boolean defaultValue) {
		boolean result = defaultValue;
		String prop = properties.getProperty(key);
		if (KString.isBoolean(prop))
			result = KString.isBooleanTrue(prop);
		KDebugConsole.print(key + ":" + result, "CONFIG", "getProperty");
		return result;
	}

	public int getProperty(String key, int defaultValue) {
		int result = defaultValue;
		try {
			result = Integer.valueOf(properties.getProperty(key, defaultValue + ""));
		} catch (Exception e) {
			result = defaultValue;
		}
		KDebugConsole.print(key + ":" + result, "CONFIG", "getProperty");
		return result;
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public void add(String key, String value) {
		properties.put(key, value);
	}

	public String toString() {
		return properties.toString().replace(", ", "\n\n");
	}

	public boolean keyExist(String key) {
		return properties.containsKey(key.toLowerCase());
	}

	public void loadFromFile(String fileName) throws IOException {
		this.fileName = fileName;
		FileInputStream fis = new FileInputStream(fileName);
		InputStreamReader isr = new InputStreamReader(fis, "UTF8");
		Reader in = new BufferedReader(isr);
		properties.load(in);
		in.close();
		isr.close();
		fis.close();
	}

	public void loadFromRessource(String fileName) {
		String f = fileName.replace(net.ko.utils.KApplication.getRootPath(KClassCreator.class) + "/", "");
		String resultat = KTextFile.open(f);
		this.propertiesString = resultat;
		properties = new Properties();
		try {
			StringReader str = new StringReader(propertiesString.replace(propertySep, "\n").replace(propertyEqual, "="));
			properties.load(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveAs(String fileName) throws IOException {
		String path = KFileUtils.getPathName(fileName);
		// File file = new File(path);
		FileOutputStream fos = new FileOutputStream(path);
		Writer out = new OutputStreamWriter(fos, "UTF8");
		// FileWriter out = new FileWriter(file);
		properties.store(out, null);
		fos.close();
	}

	public void save() throws IOException {
		if (KString.isNotNull(fileName)) {
			File f = new File(fileName);
			if (f.exists())
				saveAs(fileName);
		}
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void addAll(KProperties kproperties) {
		for (Map.Entry<Object, Object> e : kproperties.getProperties().entrySet()) {
			properties.put(e.getKey(), e.getValue());
		}
	}

	public String getFileName() {
		return fileName;
	}
}
