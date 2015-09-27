package net.ko.bean;

import java.io.IOException;

import net.ko.comparators.HierarchicalComparator;
import net.ko.framework.Ko;
import net.ko.kobject.KConstraintBelongsTo;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.utils.KHierarchicalSort;
import net.ko.utils.KPackageUtils;

public class KListClass extends KListObject<KClass> {
	private static final long serialVersionUID = 1L;

	public KListClass() {
		super(KClass.class);
	}

	public static KListClass kload() throws ClassNotFoundException, IOException {
		return KListClass.kload(Ko.kpackage());
	}

	public static KListClass kload(String packageName) throws ClassNotFoundException, IOException {
		return KPackageUtils.getKClasses(packageName);
	}

	public void setCategorie(String classSimpleName, String categorie) {
		KClass clazz = get(classSimpleName);
		if (clazz != null)
			clazz.setCategorie(categorie);
	}

	public void setCaption(String classSimpleName, String caption) {
		KClass clazz = get(classSimpleName);
		if (clazz != null)
			clazz.setCaption(caption);
	}

	public void setCaptions(String[] captions) {
		for (int i = 0; i < captions.length; i++) {
			if (i < items.size())
				items.get(i).setCaption(captions[i]);
			else
				break;
		}
	}

	public void setCaptionAndCategorie(String classSimpleName, String caption, String categorie) {
		KClass clazz = get(classSimpleName);
		if (clazz != null) {
			clazz.setCaption(caption);
			clazz.setCategorie(categorie);
		}
	}

	public void setCategorie(String[] classSimpleNames, String categorie) {
		for (int i = 0; i < classSimpleNames.length; i++) {
			setCategorie(classSimpleNames[i], categorie);
		}
	}

	public KClass get(String classSimpleName) {
		return (KClass) selectFirst("simpleName=" + classSimpleName);
	}

	public void deleteBySimpleNames(String[] classSimpleNames) {
		for (int i = 0; i < classSimpleNames.length; i++) {
			KClass clazz = get(classSimpleNames[i]);
			if (clazz != null)
				items.remove(clazz);
		}
	}

	@Override
	public void sort() {
		KHierarchicalSort.sort(items, new HierarchicalComparator<KClass>() {

			@Override
			public boolean isParent(KClass class1, KClass class2) {
				KObject o2 = Ko.getKoInstance(class2.getClazz());
				return o2.hasConstraintWith(class1.getClazz(), KConstraintBelongsTo.class);
			}
		});

		// Collections.sort(items, new Comparator<KClass>() {
		//
		// @Override
		// public int compare(KClass class1, KClass class2) {
		// return
		// class1.compareTo(class2);
		// }
		// });
	}
}
