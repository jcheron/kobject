/**
 * Classe KClassCreator
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KClassCreator.java,v 1.7 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.ksql
 */
package net.ko.creator;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.ko.db.KDataBase;
import net.ko.db.KDbField;
import net.ko.db.creation.KUniqueConstraint;
import net.ko.events.EventFileListener;
import net.ko.kobject.KListObject;
import net.ko.utils.KApplication;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.KTemplateFile;

import org.w3c.dom.Element;

public class KClassCreator {
	private KDataBase db;
	private String tableName;
	private String packageName;
	private String className;
	private Map<String, KDbField> fields;
	private KTemplateFile tplFile;
	private String templateFolder;
	private Map<String, Object> fieldMap;
	private Set<String> imports;
	private Class[] manyClass;
	private String classNameTemplate = "K%className%";
	private boolean generateAnnotations = true;
	private boolean removeId = true;

	private EventFileListener eventFileListener;

	public void addFileListener(EventFileListener eventFileListener) {
		this.eventFileListener = eventFileListener;
	}

	public String getTemplateFolder() {
		return templateFolder;
	}

	public void setTemplateFolder(String templateFolder) {
		this.templateFolder = templateFolder;
		tplFile.open(getPathName(this.templateFolder + "/class.tpl"));
	}

	private String getPathName(String name) {
		String result = "";
		String rootPath = KApplication.getRootPath(KClassCreator.class);
		if (!rootPath.endsWith(".jar"))
			result = rootPath + "/" + name;
		else
			result = name;
		return result;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public KDataBase getDb() {
		return db;
	}

	public String getTableName() {
		return tableName;
	}

	@Override
	public String toString() {
		return tplFile.toString();
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setDb(KDataBase db) {
		this.db = db;
	}

	public KClassCreator(KDataBase db, String tableName, String templateFolder, boolean removeId) {
		super();
		this.removeId = removeId;
		imports = new LinkedHashSet<>();
		this.db = db;
		this.tableName = tableName;
		setFields();
		tplFile = new KTemplateFile();
		setTemplateFolder(templateFolder);
		this.manyClass = new Class[] { KListObject.class, KListObject.class };
	}

	public KClassCreator(KDataBase db, String tableName) {
		this(db, tableName, "net/ko/templates/java", false);
	}

	private void setFields() {
		try {
			fields = db.getFieldNamesAndTypes(tableName);
			if (removeId)
				fields.remove("id");
			fields = correctFields(fields);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String getAnnotations() {
		String result = "@Entity\n";
		result += "@Table(name=\"" + tableName + "\"";
		try {
			String strConstraints = "";
			String sep = "";
			List<KUniqueConstraint> constraints = db.getUniqueIndexes(tableName);
			for (KUniqueConstraint constraint : constraints) {
				if (!"".equals(strConstraints))
					sep = ",";
				String annotation = constraint.getAnnotation();
				if (!"".equals(annotation))
					strConstraints += sep + constraint.getAnnotation();
			}
			if (!"".equals(strConstraints)) {
				imports.add("import net.ko.persistence.annotation.UniqueConstraint;\n");
				strConstraints = ",uniqueConstraints = {" + strConstraints + "}";
			}
			result += strConstraints + ")";
		} catch (SQLException e) {

		}
		return result;
	}

	public String mkMembers() {
		// TODO à modifier
		String result = "";
		for (Map.Entry<String, KDbField> eFields : fields.entrySet()) {
			KDbField field = eFields.getValue();
			String memberName = eFields.getKey();
			if (generateAnnotations)
				result += field.getAnnotation(memberName, this);
			result += "\tprivate " + field.toString() + " " + memberName + ";\n";
		}
		return result;
		// return showWithMask("member.tpl", "\n");
	}

	public String mkGetters() {
		String getTpl = getTplAsString("get.tpl");
		String result = "";
		for (Map.Entry<String, KDbField> eFields : fields.entrySet()) {
			KDbField field = eFields.getValue();
			String memberName = eFields.getKey();
			String javaType = field.getJavaType();
			String getter = field.getterName(memberName);
			result += KString.showWithMask(getTpl, memberName, javaType, getter);
		}
		return result;
	}

	public String mkSetters() {
		return showWithMask("set.tpl", "\n");
	}

	public String mkGettersAndSetters() {
		return mkGetters() + mkSetters();
	}

	public String mkToString() {
		return "return " + showWithMask("tostring.tpl", "+", true);
	}

	public String getClassName() {
		if (className == null) {
			return classNameTemplate.replace("%className%", KString.capitalizeFirstLetter(tableName.replace(" ", "_")));
		}
		else
			return className;
	}

	private KTemplateFile mkTpl() {
		KTemplateFile tpl = tplFile.clone();
		tpl.parseWith("package", "kernel." + getDb().getBaseName());
		tpl.parseWith("className", getClassName());
		parseTpl(tpl);
		return tpl;
	}

	public String mkClass() {
		tplFile = mkTpl();
		return mkTpl().toString();
	}

	public String getTplAsString(String templateFile) {
		KTemplateFile tpl = new KTemplateFile();
		tpl.open(getPathName(templateFolder + "/" + templateFile));
		return tpl.getText();
	}

	private String parseTpl(KTemplateFile aTpl) {
		// aTpl.parseWith("tableName", tableName);
		aTpl.parseWith("members", mkMembers());
		aTpl.parseWith("setters", mkSetters());
		aTpl.parseWith("getters", mkGetters());
		if (generateAnnotations)
			aTpl.parseWith("annotations", getAnnotations());
		aTpl.parseWith("toString", mkToString());
		aTpl.parseWith("imports", mkImports());
		return aTpl.toString();
	}

	public String mkClassMembers() {
		return parseTpl(tplFile);
	}

	public String mkImports() {
		String result = "";
		for (String imp : imports) {
			result += imp;
		}
		return result;
	}

	private Map<String, KDbField> correctFields(Map<String, KDbField> fields) {
		Map<String, KDbField> ret = new LinkedHashMap<String, KDbField>();
		fieldMap = new HashMap<String, Object>();
		for (Map.Entry<String, KDbField> f : fields.entrySet()) {
			String newVal = f.getKey().replace(" ", "_");
			if (!newVal.equals(f.getKey()))
				fieldMap.put(newVal, f.getKey());
			ret.put(newVal, f.getValue());
		}
		return ret;
	}

	private String showWithMask(Map<String, KDbField> map, String templateFile, String glue) {
		KStrings ks = null;
		ks = new KStrings(map);
		KTemplateFile tpl = new KTemplateFile();
		tpl.open(getPathName(templateFolder + "/" + templateFile));
		String sTpl = tpl.getText();
		return ks.showWithMask(glue, sTpl);
	}

	private String showWithMask(String templateFile, String glue, boolean onlyPrimitive) {
		if (onlyPrimitive) {
			Map<String, KDbField> nFields = new HashMap<>();
			for (Map.Entry<String, KDbField> e : fields.entrySet()) {
				if (e.getValue().isPrimitive())
					nFields.put(e.getKey(), e.getValue());
			}
			return showWithMask(nFields, templateFile, glue);
		} else
			return showWithMask(fields, templateFile, glue);
	}

	private String showWithMask(String templateFile, String glue) {
		return showWithMask(templateFile, glue, false);
	}

	public void saveAs(String packageName) {
		saveAs(getPathName(""), packageName);
	}

	public void saveAs(String path, String packageName) {
		this.packageName = packageName;
		save(path);
	}

	public void save() {
		save(getPathName(""));
	}

	public void save(String path) {
		if (eventFileListener != null)
			tplFile.addFileListener(eventFileListener);
		tplFile.saveAs(path, packageName, getClassName());
	}

	public boolean existMember(String member) {
		return fields.containsKey(member);
	}

	private void addConstraint(String cName, String fkClassName, String fkMemberName) {
		KTemplateFile tpl = new KTemplateFile();
		tpl.open(getPathName(templateFolder + "/" + cName + ".tpl"));
		tpl.parseWith("fkClassName", fkClassName);
		tpl.parseWith("fkMemberName", fkMemberName);
		tpl.parseWith("manyClass", manyClass[0].getSimpleName());
		if (manyClass.length >= 2 && !manyClass[0].getName().equals(manyClass[1].getName()))
			tpl.parseWith("manyClassImpl", manyClass[1].getSimpleName());
		tplFile.parseWith("constraints", tpl.getText(), true);
	}

	public void addBelongsTo(String fkClassName, String fkMemberName) {
		addConstraint("belongsto", fkClassName, fkMemberName);
	}

	public void addHasMany(String fkClassName, String fkMemberName) {
		imports.add("import " + manyClass[0].getName() + ";\n");
		if (manyClass.length >= 2 && !manyClass[0].getName().equals(manyClass[1].getName()))
			imports.add("import " + manyClass[1].getName() + ";\n");
		addConstraint("hasmany", fkClassName, fkMemberName);
	}

	public void addFkMember(String fkTableName) {
		fields.put(fkTableName, new KDbField(fkTableName, classNameTemplate.replace("%className%", KString.capitalizeFirstLetter(fkTableName))));
	}

	public void addManyMember(String fkTableName) {
		fields.put(fkTableName + "s", new KDbField(fkTableName + "s", manyClass[0].getSimpleName() + "<" + classNameTemplate.replace("%className%", KString.capitalizeFirstLetter(fkTableName)) + ">"));
	}

	public void clean() {
		tplFile.clean();
	}

	public void mkController(KDataBase db, KControllerCreator cCreator, Element xmlElement) {
		setFields();
		for (Map.Entry<String, KDbField> entryFields : fields.entrySet()) {
			KDbField field = entryFields.getValue();
			cCreator.createMemberElement(xmlElement, field);
		}
	}

	public Set<String> getImports() {
		return imports;
	}

	public void setClassNameTemplate(String classNameTemplate) {
		this.classNameTemplate = classNameTemplate;
	}

	public void setManyClass(Class[] manyClass) {
		this.manyClass = manyClass;
	}

	public void setGenerateAnnotations(boolean generateAnnotations) {
		this.generateAnnotations = generateAnnotations;
	}

	public void setRemoveId(boolean removeId) {
		this.removeId = removeId;
	}
}
