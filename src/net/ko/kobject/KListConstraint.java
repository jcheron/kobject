package net.ko.kobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import net.ko.dao.IGenericDao;
import net.ko.db.KDataBase;
import net.ko.list.MapArrayString;

public class KListConstraint implements Serializable, Iterable<KConstraint> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1549740076953468233L;
	private ArrayList<KConstraint> constraints;

	public void add(KConstraint constraint) {
		constraints.add(constraint);
	}

	public KListConstraint() {
		constraints = new ArrayList<KConstraint>();
	}

	public KConstraint get(int index) {
		return constraints.get(index);
	}

	public void load(KDataBase db) {
		load(db, new KDuoClasseList());
	}

	public void load(KDataBase db, KDuoClasseList duoClasseList) {
		load(db, duoClasseList, false);
	}

	@SuppressWarnings("unchecked")
	public void load(KDataBase db, KDuoClasseList duoClasseList, boolean fromList) {
		for (KConstraint c : constraints) {
			if (!(fromList)) {
				if (!c.isLazy()) {
					ArrayList<KDuoClasse> tmpList = (ArrayList<KDuoClasse>) duoClasseList.getDuoClasses().clone();
					c.load(db, duoClasseList);
					duoClasseList.setDuoClasses(tmpList);
				}
			}
		}
	}

	public void load(KListObject<? extends KObject> kl) {
		for (KConstraint c : constraints) {
			if (!c.isLazy())
				c.load(kl);
		}
	}

	public void preload(MapArrayString<KConstraint> membersKeys) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		for (KConstraint c : constraints) {
			// if(c instanceof KConstraintBelongsTo){
			c.preload(membersKeys);
			// membersKeys.put(c.getClazz(), c.getPkFieldValue()+"");
			// }
		}
	}

	public void save(IGenericDao<? extends KObject> dao) {
		for (KConstraint c : constraints) {
			c.save(dao);
		}
	}

	public void update() {
		for (KConstraint c : constraints) {
			if (c.isAutoUpdate())
				c.update();
		}
	}

	@SuppressWarnings("rawtypes")
	public KConstraint getConstraint(String member, Class constraintClass) {
		KConstraint result = null;
		for (KConstraint c : constraints) {
			String fieldName = c.getOwner().getFieldName(member).toLowerCase();
			if (constraintClass.equals(KConstraintBelongsTo.class)) {
				if (!fieldName.equals("id") && c.getFieldKey().toLowerCase().contains(fieldName.toLowerCase()) && c.getClass().equals(constraintClass)) {
					result = c;
					break;
				}
			} else {
				if (!fieldName.equals("id") && c.getMember().equals(member) && c.getClass().equals(constraintClass)) {
					result = c;
					break;
				}
			}
		}
		return result;
	}

	public KConstraint getConstraint(String member) {
		KConstraint result = null;
		for (KConstraint c : constraints) {
			String fieldName = c.getOwner().getFieldName(member).toLowerCase();
			if (!fieldName.equals("id") && (c.getFieldKey().equalsIgnoreCase(fieldName) && c.getClass().equals(KConstraintBelongsTo.class))) {
				result = c;
				break;
			} else if (c.getMember().equals(member) && (c.getClass().equals(KConstraintHasAndBelongsToMany.class) || c.getClass().equals(KConstraintHasManyBelongsTo.class) || c.getClass().equals(KConstraintHasMany.class))) {
				result = c;
				break;
			}
		}
		return result;
	}

	public KConstraint getManyConstraint(String member) {
		KConstraint constraint = null;
		constraint = getConstraint(member);
		if (!(constraint instanceof KConstraintBelongsTo))
			return constraint;
		else
			return null;
	}

	public String getMember(Class<? extends KObject> clazz, Class<? extends KConstraint> constraintClass) {
		String result = "";
		for (KConstraint c : constraints) {
			if (c.getClazz().equals(clazz) && constraintClass.equals(c.getClass())) {
				result = c.getMember();
				break;
			}
		}
		return result;
	}

	public boolean constraintExists(Class<? extends KObject> clazz, Class<? extends KConstraint> constraintClazz) {
		for (KConstraint c : constraints) {
			if (c.getClazz().equals(clazz) && constraintClazz.equals(c.getClass())) {
				return true;
			}
		}
		return false;
	}

	public boolean constraintExists(String memberName) {
		for (KConstraint c : constraints) {
			if (c.getMember().equals(memberName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<KConstraint> iterator() {
		return constraints.iterator();
	}

	public ArrayList<KConstraint> getConstraints() {
		return constraints;
	}
}
