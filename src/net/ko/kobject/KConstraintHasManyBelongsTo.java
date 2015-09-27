package net.ko.kobject;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import net.ko.framework.Ko;
import net.ko.utils.KoUtils;

public class KConstraintHasManyBelongsTo extends KConstraintHasMany {
	private static final long serialVersionUID = 1L;

	private Class<? extends KObject> belongsToClass;
	private String belongsToField;

	public KConstraintHasManyBelongsTo(String member, Class<? extends KObject> clazz, String fieldKey, String destTable, String destFieldKey, Class<? extends KObject> belongsToClass, String belongsToField) {
		super(member, clazz, fieldKey, destTable, destFieldKey);
		this.belongsToClass = belongsToClass;
		this.belongsToField = belongsToField;
		lazy = false;
	}

	public KConstraintHasManyBelongsTo(String member, Class<? extends KObject> clazz, String where) {
		super(member, clazz, where);
		lazy = false;
	}

	public Class<? extends KObject> getJoinClass() {
		return belongsToClass;
	}

	public void setJoinClass(Class<? extends KObject> joinClass) {
		this.belongsToClass = joinClass;
	}

	public String getBelongsToField() {
		String result = belongsToField;
		if (result == null || "".equals(result))
			if (clazz != null) {
				try {
					KObject ko = Ko.getKoInstance(clazz);
					result = ko.getConstraints().getMember(belongsToClass, KConstraintBelongsTo.class);
					result = KoUtils.getFkFieldName(result);
				} catch (Exception e) {
					Ko.klogger().log(Level.WARNING, "Impossible de déterminer belongsToField", e);
				}
			}
		return result;
	}

	public void setKeyValue(KObject relKo) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
		// String koField = where.substring(0, where.indexOf("=")).trim();
		// Erreur ? à vérifier
		relKo.setAttribute(destFieldKey, owner.getFirstKeyValue(), false);
	}

	public Class<? extends KObject> getBelongsToClass() {
		return belongsToClass;
	}

	public void setBelongsToClass(Class<? extends KObject> belongsToClass) {
		this.belongsToClass = belongsToClass;
	}

	public void setBelongsToField(String belongsToField) {
		this.belongsToField = belongsToField;
	}

	@Override
	public String getFkField() {
		return getDestFieldKey();
	}

	@Override
	public String getPkField() {
		return getFieldKey();
	}
}
