package net.ko.bean;

import java.util.logging.Level;

import net.ko.framework.Ko;
import net.ko.kobject.KConstraintBelongsTo;
import net.ko.kobject.KObject;
import net.ko.persistence.annotation.Id;
import net.ko.utils.KString;

@SuppressWarnings("serial")
public class KClass extends KObject {
	@Id
	private Object idClasse;
	private transient Class<? extends KObject> clazz;
	private transient String caption;
	private transient String categorie;
	private transient String simpleName;

	public KClass() {
		super();
		caption = "";
	}

	public <T extends KObject> KClass(Class<T> clazz) {
		this();
		this.clazz = clazz;
		idClasse = clazz.getName();
		simpleName = clazz.getSimpleName();
		caption = getPluriel();
		categorie = "";
	}

	public String getTableName() {
		String result = "";
		if (clazz != null)
			try {
				result = clazz.newInstance().getTableName();
			} catch (InstantiationException | IllegalAccessException e) {
				Ko.klogger().log(Level.WARNING, "Impossible d'instancier : " + clazz, e);
			}
		return result;
	}

	@Override
	public String toString() {
		return getCaption();
	}

	public String getCaption() {
		String result = caption;
		if ("".equals(result))
			result = simpleName;
		return result;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getCategorie() {
		return categorie;
	}

	public void setCategorie(String categorie) {
		this.categorie = categorie;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	public String getPluriel() {
		String result = simpleName.replaceFirst("K(.*)", "$1");
		result = KString.capitalizeFirstLetter(result);
		result = KString.pluriel(result);
		return result;
	}

	public Class<? extends KObject> getClazz() {
		return clazz;
	}

	public Object getIdClasse() {
		return idClasse;
	}

	public void setIdClasse(Object idClasse) {
		this.idClasse = idClasse;
	}

	@Override
	public int compareTo(Object o) {
		int result = 0;
		if (o instanceof KClass) {
			KClass class2 = (KClass) o;
			KObject o1 = null;
			KObject o2 = null;
			try {
				o1 = clazz.newInstance();
				o2 = class2.getClazz().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			result = clazz.getSimpleName().compareTo(class2.getSimpleName());
			if (o1.hasConstraintWith(class2.getClazz(), KConstraintBelongsTo.class)) {
				Ko.klogger().log(Level.WARNING, clazz.getSimpleName() + " belongsTo " + class2.getSimpleName());
				result = 1;
			}
			else if (o2.hasConstraintWith(clazz, KConstraintBelongsTo.class)) {
				result = -1;
				Ko.klogger().log(Level.WARNING, class2.getSimpleName() + " belongsTo " + clazz.getSimpleName());
			} else
				Ko.klogger().log(Level.WARNING, class2.getSimpleName() + " === " + clazz.getSimpleName());
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KClass)
			return clazz.equals(((KClass) obj).getClazz());
		else
			return false;
	}

	@Override
	public int hashCode() {
		return clazz.hashCode();
	}
}
