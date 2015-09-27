package net.ko.http.views;

import javax.servlet.http.HttpServletRequest;

import net.ko.dao.IGenericDao;
import net.ko.db.KDataBase;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.http.objects.KRequest;
import net.ko.kobject.KConstraint;
import net.ko.kobject.KConstraintHasAndBelongsToMany;
import net.ko.kobject.KConstraintHasMany;
import net.ko.kobject.KConstraintHasManyBelongsTo;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;

public abstract class KobjectHttpAbstractForm<T extends KObject> extends
		KobjectHttpAbstractView<T> {

	public KobjectHttpAbstractForm(T ko, HttpServletRequest request) {
		super(ko, request);
	}

	@Override
	protected KObjectDisplay getKobjectDisplay() {
		KObjectDisplay koDisplay = null;
		if (ko != null) {
			Class<? extends KObject> clazz = ko.getClass();
			koDisplay = Ko.koDisplays.get(clazz.getSimpleName());
			if (koDisplay == null)
				koDisplay = Ko.defaultKoDisplay();
		} else
			koDisplay = Ko.defaultKoDisplay();
		return koDisplay;
	}

	public KHttpListForm addHttpListForm(String member, boolean readonly) {
		return addHttpListForm(member, false, readonly);
	}

	@SuppressWarnings("unchecked")
	public KHttpListForm addHttpListForm(String member, boolean allValues, boolean readonly) {
		KHttpListForm klistform = null;
		boolean many = false;
		if (ko != null) {
			try {
				KConstraint koConstraint = null;
				koConstraint = ko.getConstraints().getConstraint(member);
				if (koConstraint != null) {
					many = koConstraint.getClass().equals(KConstraintHasManyBelongsTo.class);
					KListObject<KObject> kl = (KListObject<KObject>) ko.getAttribute(member);
					if (kl == null) {
						IGenericDao<T> dao = (IGenericDao<T>) Ko.getDao(ko.getClass());
						dao.read(ko);
						dao.close();
						// ko.loadOne(KoHttp.kdatabase());
						kl = (KListObject<KObject>) ko.getAttribute(member);
					}
					if (kl != null) {
						if (many && allValues) {
							KConstraintHasManyBelongsTo cbt = (KConstraintHasManyBelongsTo) koConstraint;
							klistform = new KHttpListForm(kl, request, member, cbt);
						} else {
							klistform = new KHttpListForm(kl, request, member);
							if (koConstraint != null && koConstraint instanceof KConstraintHasMany)
								klistform.setcHasMany((KConstraintHasMany) koConstraint);
						}
						klistform.setReadonly(readonly);
						klistform.init();
						klistform.setAllValues(allValues);
						klistform.setId(member);
						klistform.setHasFieldSet(false);
						klistform.setInnerForm(true);
						String fkField = koConstraint.getDestFieldKey();
						String pkField = koConstraint.getFieldKey();
						klistform.addWhere(fkField, ko.getAttribute(pkField) + "");

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return klistform;
	}

	/**
	 * Retourne une KPageList correspondant à une KListObject membre associée à
	 * une relation hasMany Le mode d'affichage par défaut de la liste est dans
	 * ce cas pris dans les paramètres de la requête
	 * 
	 * @param member
	 *            membre de données de la classe de l'objet ko de type
	 *            KListObject définit par une relation de type
	 *            hasAndBelongsToMany
	 * @return Liste des objets en relation (hasAndBelongsToMany) affichés dans
	 *         une KPageList
	 */
	public KPageList hasAndBelongsToManyList(String member) {
		return hasAndBelongsToManyList(member, KRequest.GETPOST("_mode", request, 1));
	}

	/**
	 * Retourne une KPageList correspondant à une KListObject membre associée à
	 * une relation hasAndBelongsToMany
	 * 
	 * @param member
	 *            membre de données de la classe de l'objet ko de type
	 *            KListObject définit par une relation de type
	 *            hasAndBelongsToMany
	 * @param mode
	 *            définit le mode d'affichage (1 pour liste, 2 pour formulaire,
	 *            3 pour mise à jour et affichage de message)
	 * @return Liste des objets en relation (hasAndBelongsToMany) affichés dans
	 *         une KPageList
	 */
	@SuppressWarnings("unchecked")
	public KPageList hasAndBelongsToManyList(String member, int mode) {
		KPageList kpage = null;
		if (ko != null) {
			KConstraint koConstraint = ko.getConstraints().getConstraint(member, KConstraintHasAndBelongsToMany.class);
			if (koConstraint != null) {
				try {
					kpage = new KPageList(koConstraint.getClazz(), request, "", "", mode);
					kpage.setDao(dao);
					if (kpage.getMode() == 1) {
						KDataBase db = dao.getDatabase();
						if (db != null) {
							koConstraint.load(db);
							db.close();
						}
						KListObject<KObject> kl = (KListObject<KObject>) ko.getAttribute(member);
						if (kl != null)
							kpage.setValue(kl.getKeyValuesForHasAndBelongsToMany(";"));
					}
					kpage.setFormModal(false);
					kpage.setEditable(true);
					kpage.setHasAddBtn(false);
					String tableName = ko.getSimpleTableName();
					String fkField = koConstraint.getFkField();
					kpage.removeField(tableName);
					kpage.removeField(fkField);
					if (kpage.getForm() != null) {
						kpage.formRemoveFieldControl(fkField);
						kpage.formRemoveFieldControl(tableName);
					}
					kpage.setName(member);
					kpage.setId(member);
					kpage.removeFieldControl("_filtre");
					kpage.removeFieldControl("_navBarre");
					kpage.removeFieldControl("_pageCounter");
					kpage.setLoaded(true);
					kpage.setChecked(true);
				} catch (Exception e) {
				}
			}
		}

		return kpage;
	}

	/**
	 * Retourne une KPageList correspondant à une KListObject membre associée à
	 * une relation hasMany
	 * 
	 * @param member
	 *            membre de données de la classe de l'objet ko de type
	 *            KListObject définit par une relation de type hasMany
	 * @return Liste des objets en relation (hasMany) affichés dans une
	 *         KPageList
	 */
	public KPageList hasManyList(String member) {
		return hasManyList(member, true);
	}

	/**
	 * Retourne une KPageList correspondant à une KListObject membre associée à
	 * une relation hasMany
	 * 
	 * @param member
	 *            membre de données de la classe de l'objet ko de type
	 *            KListObject définit par une relation de type hasMany
	 * @param addBtn
	 *            vrai si la liste doit afficher un bouton ajouter
	 * @return Liste des objets en relation (hasMany) affichés dans une
	 *         KPageList
	 */
	public KPageList hasManyList(String member, boolean addBtn) {
		KPageList kpage = null;
		if (ko != null) {
			KConstraint koConstraint = ko.getConstraints().getConstraint(member, KConstraintHasMany.class);
			if (koConstraint != null) {
				try {
					kpage = new KPageList(koConstraint.getClazz(), request, koConstraint.getSql(getDao().quote()));
					kpage.setFormModal(false);
					kpage.setEditable(true);
					kpage.setHasAddBtn(addBtn);
					String tableName = ko.getSimpleTableName();
					String fkField = koConstraint.getFkField();
					kpage.removeField(tableName);
					kpage.removeField(fkField);
					if (kpage.getForm() != null) {
						kpage.formRemoveFieldControl(fkField);
						kpage.formRemoveFieldControl(tableName);
					}
					kpage.removeFieldControl("_filtre");
					kpage.removeFieldControl("_navBarre");
					kpage.removeFieldControl("_pageCounter");
					kpage.setLoaded(true);
				} catch (Exception e) {
				}
			}
		}

		return kpage;
	}
}
