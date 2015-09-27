/**
 * Classe KConstraintHasMany
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KConstraintHasMany.java,v 1.4 2011/01/21 01:22:52 jcheron Exp $
 * @package ko.kobject
 */
package net.ko.kobject;

import java.util.logging.Level;

import net.ko.dao.DatabaseDAOListUtils;
import net.ko.dao.IGenericDao;
import net.ko.db.KDataBase;
import net.ko.framework.Ko;
import net.ko.ksql.KSqlQuery;

@SuppressWarnings("serial")
public class KConstraintHasMany extends KConstraint {

	public KConstraintHasMany(String member, Class<? extends KObject> clazz, String fieldKey, String destTable, String destFieldKey) {
		super(member, clazz, fieldKey, destTable, destFieldKey);
		lazy = false;
	}

	public KConstraintHasMany(String member, Class<? extends KObject> clazz, String where) {
		super(member, clazz, where);
		lazy = false;
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
			owner.__setAttribute(member, kl.select(owner._constraintShowWithMask(where.replace("'", ""))), false);
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible d'affecter la liste au membre " + member, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void save(IGenericDao dao) {
		try {
			KListObject<? extends KObject> kl = (KListObject<? extends KObject>) owner.__getAttribute(member);
			if (kl != null)
				dao.update(kl);
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible de sauvegarder la liste pour le membre " + member, e);
		}
	}

	@Override
	public String getSql(String quote) {
		String select = null;
		select = Ko.getKoInstance(clazz).getSql(quote);
		String where = getWhere(quote);
		select = KSqlQuery.addWhere(select, owner._constraintShowWithMask(where));
		return select;
	}

	@Override
	public String getWhere(String quote) {
		return quote + destTable + quote + "." + quote + destFieldKey + quote + "='{" + fieldKey + "}'";
	}

	@Override
	public String getObjectWhere() {
		return destFieldKey + "={" + fieldKey + "}";
	}

}
