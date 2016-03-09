/**
 * Classe KConstraintHasAndBelongsToMany
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KConstraintHasAndBelongsToMany.java,v 1.4 2011/01/21 01:22:52 jcheron Exp $
 * @package ko.kobject
 */
package net.ko.kobject;

import java.util.logging.Level;

import net.ko.dao.DatabaseDAO;
import net.ko.dao.DatabaseDAOListUtils;
import net.ko.dao.IGenericDao;
import net.ko.db.KDataBase;
import net.ko.framework.Ko;
import net.ko.ksql.KSqlQuery;
import net.ko.persistence.orm.KMetaObject;

public class KConstraintHasAndBelongsToMany extends KConstraint {
	private static final long serialVersionUID = 1L;
	private Class<? extends KObject> joinClass;
	private String fkWhere;
	private String joinDestFieldKey;
	private String joinFieldKey;
	private String joinTable;

	public Class<? extends KObject> getJoinClass() {
		return joinClass;
	}

	public Class<? extends KObject> getClazzList() {
		return joinClass;
	}

	public void setJoinClass(Class<? extends KObject> joinClass) {
		this.joinClass = joinClass;
	}

	public KConstraintHasAndBelongsToMany(String member, Class<? extends KObject> clazz, Class<? extends KObject> joinClass, String fieldKey, String destTable, String destFieldKey, String joinFieldKey, String joinDestFieldKey) {
		super(member, clazz, fieldKey, destTable, destFieldKey);
		this.joinClass = joinClass;
		this.joinDestFieldKey = joinDestFieldKey;
		this.joinFieldKey = joinFieldKey;
		lazy = false;
	}

	public KConstraintHasAndBelongsToMany(String member, Class<? extends KObject> clazz, String koWhere) {
		this(member, clazz, koWhere, "");
	}

	public KConstraintHasAndBelongsToMany(String member, Class<? extends KObject> clazz, String koWhere, String fkWhere) {
		super(member, clazz, koWhere);
		this.fkWhere = fkWhere;
		lazy = false;
	}

	private String getFkWhere(String quote) {
		return quote + getJoinTable() + quote + "." + quote + joinDestFieldKey + quote + "=" + quote + destTable + quote + "." + quote + destFieldKey + quote;
	}

	@Override
	public void load(KDataBase db, KDuoClasseList duoClasseList) {
		if (!duoClasseList.exists(getDuoClasse())) {
			super.load(db, duoClasseList);
			try {
				owner.__setAttribute(member, DatabaseDAOListUtils.kloadForConstraint(clazz, db, getSql(db.QUOTE()), duoClasseList), false);
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Impossible d'affecter la liste au membre " + member, e);
			}
		}
	}

	@Override
	public void load(KListObject<? extends KObject> kl) {
		super.load(kl);
		try {
			String where = getObjectWhere();
			KListObject<? extends KObject> joinKl = kl.select(owner._constraintShowWithMask(where.replace("'", "")));
			// Erreur de s
			String memberJoinKl = KSqlQuery.getTableName(Ko.getKoInstance(clazz).getTableName());
			owner.__setAttribute(member, joinKl.getMemberAsKL(memberJoinKl, clazz), false);
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible d'affecter la liste au membre " + member, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void save(IGenericDao dao) {
		try {
			KListObject<? extends KObject> kl = (KListObject<? extends KObject>) owner.__getAttribute(member);
			if (kl != null) {
				fkWhere = getFkWhere(((DatabaseDAO<KObject>) dao).quote());
				KListObject<? extends KObject> relKl = new KListObject<KObject>((Class<KObject>) joinClass);
				for (KObject ko : kl) {
					KObject relKo = joinClass.newInstance();
					relKo.setAttribute(joinDestFieldKey, ko.getFirstKeyValue(), false);
					relKo.setAttribute(joinFieldKey, owner.getFirstKeyValue(), false);
					relKo.setRecordStatus(ko.recordStatus);
					relKl.add(relKo, true);
				}
				dao.update(relKl);
			}
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible de sauvegarder la liste pour le membre " + member, e);
		}

	}

	@Override
	public String getSql(String quote) throws InstantiationException, IllegalAccessException {
		String select = null;
		String table = KSqlQuery.getTableName(Ko.getKoInstance(clazz).getTableName());
		fkWhere = getFkWhere(quote);
		String tWhere = getWhere(quote) + " AND " + fkWhere;
		select = "SELECT " + quote + table + quote + ".* FROM " + quote + getJoinTable() + quote + "," + quote + table + quote + " WHERE " + owner._constraintShowWithMask(tWhere);
		return select;
	}

	/*
	 * public String getFkField(){ String fkWhere=getFkWhere(); String
	 * result=""; result=fkWhere.substring(0,fkWhere.indexOf("=")).trim();
	 * return result; }
	 */
	public String getJoinTable() {
		KMetaObject<? extends KObject> joinMetaObject = Ko.getMetaObject(joinClass);
		if (joinTable == null || "".equals(joinTable)) {
			// joinTable =
			// KSqlQuery.getTableName(Ko.getKoInstance(joinClass).getTableName());
			joinTable = joinMetaObject.getTableName();
		}
		return joinTable;
	}

	@Override
	public String getWhere(String quote) {
		return quote + getJoinTable() + quote + "." + quote + joinFieldKey + quote + "='{" + fieldKey + "}'";
	}

	@Override
	public String getObjectWhere() {
		return joinFieldKey + "={" + fieldKey + "}";
	}

	public String getJoinDestFieldKey() {
		return joinDestFieldKey;
	}

	public void setJoinDestFieldKey(String joinDestFieldKey) {
		this.joinDestFieldKey = joinDestFieldKey;
	}

	public String getJoinFieldKey() {
		return joinFieldKey;
	}

	public void setJoinFieldKey(String joinFieldKey) {
		this.joinFieldKey = joinFieldKey;
	}

	@Override
	public String getFkField() {
		return joinFieldKey;
	}
}
