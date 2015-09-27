/**
 * Classe KConstraintBelongsTo
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2013
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  LGPL License
 * @version $Id: KConstraintBelongsTo.java,v 1.4 2011/01/21 01:22:52 jcheron Exp $
 * @package ko.kobject
 */
package net.ko.kobject;

import java.util.logging.Level;

import net.ko.dao.DatabaseDAOObjectUtils;
import net.ko.dao.IGenericDao;
import net.ko.db.KDBForeignKeyDef;
import net.ko.db.KDataBase;
import net.ko.db.creation.KForeignKey;
import net.ko.framework.Ko;
import net.ko.ksql.KParameterizedInstruction;

@SuppressWarnings("serial")
public class KConstraintBelongsTo extends KConstraint {
	private KDBForeignKeyDef onDeleteAction;
	private KDBForeignKeyDef onUpdateAction;

	public KConstraintBelongsTo(String member, Class<? extends KObject> clazz, String fieldKey, String destTable, String destFieldKey) {
		super(member, clazz, fieldKey, destTable, destFieldKey);
		foreignKey = new KForeignKey(fieldKey, destTable, destFieldKey);
	}

	@Override
	public void load(KDataBase db, KDuoClasseList duoClasseList) {
		if (!duoClasseList.exists(getDuoClasse())) {
			super.load(db, duoClasseList);
			KObject o = KObject.getNewInstance(clazz);
			try {
				String where = getWhere(db.QUOTE());
				KParameterizedInstruction sqlQuery = new KParameterizedInstruction(db.QUOTE(), o.getSql(db.QUOTE()), where, owner.getAttribute(fieldKey));
				o = DatabaseDAOObjectUtils.kloadOneForConstraint(o, db, sqlQuery, duoClasseList);
				owner.__setAttribute(member, o, false);
			} catch (Exception e) {
				Ko.klogger().log(Level.WARNING, "Impossible d'affecter l'objet chargé " + o + " au membre " + member, e);
			}
		}
	}

	@Override
	public void load(KListObject<? extends KObject> kl) {
		super.load(kl);
		KObject o = null;
		try {
			String where = getObjectWhere();
			o = kl.selectFirst(owner._constraintShowWithMask(where.replace("'", "")));
			owner.__setAttribute(member, o, false);
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible d'affecter l'objet " + o + " au membre " + member, e);
		}
	}

	@Override
	public void save(IGenericDao dao) {
		KObject ko = null;
		try {
			ko = (KObject) owner.__getAttribute(member);
			if (ko != null)
				dao.updateToSupport(ko);
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible de sauvegarder " + ko + " pour le membre " + member, e);
		}
	}

	@Override
	public String getSql(String quote) throws InstantiationException, IllegalAccessException {
		return null;
	}

	@Override
	public void update() {
		KObject ko = null;
		try {
			ko = (KObject) owner.getAttribute(member);
			if (ko != null)
				owner.__setAttribute(fieldKey, ko.getFirstKeyValue(), false);
		} catch (SecurityException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
			Ko.klogger().log(Level.WARNING, "Impossible de mettre à jour " + ko + " pour le membre " + member, e);
		}
	}

	@Override
	public String getWhere(String quote) {
		return quote + destTable + quote + "." + quote + destFieldKey + quote + "=?";
	}

	@Override
	public String getObjectWhere() {
		return destFieldKey + "={" + fieldKey + "}";
	}

	public KDBForeignKeyDef getOnDeleteAction() {
		return onDeleteAction;
	}

	public KConstraintBelongsTo onDeleteAction(KDBForeignKeyDef onDeleteAction) {
		this.onDeleteAction = onDeleteAction;
		if (foreignKey != null)
			foreignKey.setOnDeleteAction(onDeleteAction);
		return this;
	}

	public KDBForeignKeyDef getOnUpdateAction() {
		return onUpdateAction;
	}

	public KConstraintBelongsTo onUpdateAction(KDBForeignKeyDef onUpdateAction) {
		this.onUpdateAction = onUpdateAction;
		if (foreignKey != null)
			foreignKey.setOnUpdateAction(onUpdateAction);
		return this;
	}

}
