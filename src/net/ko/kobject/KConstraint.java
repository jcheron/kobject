/**
 * Classe KConstraint
 * Représente une contrainte entre objets (Relation de la base de données)
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2013
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  LGPL License
 * @version $Id: KConstraint.java 53 2012-12-26 17:37:45Z jcheron $
 * @package net.ko.kobject
 */

package net.ko.kobject;

import java.io.Serializable;

import net.ko.dao.IGenericDao;
import net.ko.db.KDataBase;
import net.ko.db.creation.KForeignKey;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.ksql.KSqlQuery;
import net.ko.list.MapArrayString;

public abstract class KConstraint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String where;
	protected String fieldKey;
	protected String destFieldKey;
	protected String destTable;
	protected Class<? extends KObject> clazz;
	protected String tableName;
	protected KObject owner;
	protected String member;
	protected boolean lazy;
	protected int depth;
	protected boolean autoUpdate;
	protected transient KForeignKey foreignKey;

	public KObject getOwner() {
		return owner;
	}

	public void setOwner(KObject owner) {
		this.owner = owner;
	}

	public KConstraint(String member, Class<? extends KObject> clazz, String where) {
		super();
		this.where = where;
		this.member = member;
		this.clazz = clazz;
		this.lazy = false;
		depth = -1;
		autoUpdate = false;
	}

	public KConstraint(String member, Class<? extends KObject> clazz, String fieldKey, String destTable, String destFieldKey) {
		super();
		this.fieldKey = fieldKey;
		this.destFieldKey = destFieldKey;
		this.destTable = destTable;
		this.member = member;
		this.clazz = clazz;
		this.lazy = false;
		depth = -1;
		autoUpdate = false;
	}

	public String getTableName() {
		if (tableName == null || "".equals(tableName)) {
			tableName = KSqlQuery.getTableName(Ko.getKoInstance(clazz).getTableName());
		}
		return tableName;
	}

	public abstract String getWhere(String quote);

	public abstract String getObjectWhere();

	public void setWhere(String where) {
		this.where = where;
	}

	public Class<? extends KObject> getClazz() {
		return clazz;
	}

	public Class<? extends KObject> getClazzList() {
		return clazz;
	}

	public void setClazz(Class<? extends KObject> clazz) {
		this.clazz = clazz;
	}

	public void load(KDataBase db) {
		load(db, new KDuoClasseList());
	}

	public void load(KDataBase db, KDuoClasseList duoClasseList) {
		KDebugConsole.print(this.clazz + "", "KOBJECT", this.getClass().getSimpleName() + ".load");
		duoClasseList.add(owner.getClass(), this.clazz);
	}

	public void load(KListObject<? extends KObject> kl) {
		KDebugConsole.print(this.clazz + "", "KOBJECT", this.getClass().getSimpleName() + ".loadByKL");
	}

	public void preload(MapArrayString<KConstraint> membersKeys) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		membersKeys.put(this, getFieldKeyValue());
	}

	public abstract void save(IGenericDao dao);

	public String getMember() {
		return member;
	}

	public KDuoClasse getDuoClasse() {
		return new KDuoClasse(owner.getClass(), this.clazz);
	}

	public boolean isMultiple() {
		return !getClass().equals(KConstraintBelongsTo.class);
	}

	public abstract String getSql(String quote) throws InstantiationException, IllegalAccessException;

	public String getFkField() {
		String result = "";
		if (where != null & !"".equals(where))
			result = (where.substring(0, where.indexOf("="))).trim();
		return result;
	}

	public String getPkField() {
		String result = "";
		if (where != null & !"".equals(where))
			result = (where.substring(where.indexOf("{") + 1, where.indexOf("}"))).trim();
		return result;
	}

	public Object getFieldKeyValue() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Object result = "";
		if (owner != null)
			result = owner.getAttribute(fieldKey, true);
		return result;
	}

	public Object getDestFieldKeyValue() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Object result = "";
		if (owner != null)
			result = owner.getAttribute(destFieldKey, true);
		return result;
	}

	public boolean isLazy() {
		return lazy;
	}

	public KConstraint setLazy(boolean lazy) {
		this.lazy = lazy;
		return this;
	}

	@Override
	public String toString() {
		return member;
	}

	public void update() {

	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof KConstraint)
			result = clazz.equals(((KConstraint) obj).getClazz()) && member.equals(((KConstraint) obj).getMember());
		return result;
	}

	@Override
	public int hashCode() {
		return (clazz + member).hashCode();
	}

	public int getDepth() {
		return depth;
	}

	public KConstraint setDepth(int depth) {
		this.depth = depth;
		return this;
	}

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public KConstraint setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
		return this;
	}

	public String getFieldKey() {
		return fieldKey;
	}

	public void setFieldKey(String fieldKey) {
		this.fieldKey = fieldKey;
	}

	public String getDestFieldKey() {
		return destFieldKey;
	}

	public void setDestFieldKey(String destFieldKey) {
		this.destFieldKey = destFieldKey;
	}

	public String getDestTable() {
		return destTable;
	}

	public void setDestTable(String destTable) {
		this.destTable = destTable;
	}

	public void setMember(String member) {
		this.member = member;
	}

	public KForeignKey getForeignKey() {
		return foreignKey;
	}

	public String display() {
		String result = getClass().getSimpleName() + " : " + member + "(" + fieldKey + "->" + destTable + "." + destFieldKey + ")";
		return result;
	}
}
