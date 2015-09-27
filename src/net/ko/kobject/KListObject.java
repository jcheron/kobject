/**
 * Classe KlistObject
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2013
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  LGPL License
 * @version $Id: KListObject.java 55 2012-12-28 19:03:38Z jcheron $
 * @package ko.kobject
 */

package net.ko.kobject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import net.ko.displays.Display;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.interfaces.IKommande;
import net.ko.utils.KHash;
import net.ko.utils.KQuickSort;
import net.ko.utils.KReflection;

/**
 * Classe générique permettant de gérer une liste d'instances de KObject
 * Collection d'objets permettant la sérialisation avec une base de données
 * 
 * @author jcheron
 * 
 * @param <T>
 */
public class KListObject<T extends KObject> implements Iterable<T>,
		Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected List<T> items;
	private List<T> deletedItems;
	private final Class<T> clazz;
	private String sql;

	public KListObject(Class<T> clazz) {
		this.clazz = clazz;
		items = new ArrayList<T>();
		deletedItems = new ArrayList<T>();
		try {
			setSql("", Ko.getDao(clazz).quote());
		} catch (Exception e) {
		}
	}

	public KListObject(KListObject<T> list) {
		this.clazz = list.getClazz();
		items = new ArrayList<T>(list.getItems());
		deletedItems = new ArrayList<T>();
		try {
			setSql(list.getSql(), Ko.getDao(clazz).quote());
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("rawtypes")
	private static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}

	private Comparator<T> getComparator(final String[] attributes) {
		return getComparator(attributes, 0);
	}

	private Comparator<T> getComparator(final String[] attributes, KObjectDisplay kodDisplay) {
		return getComparator(attributes, 0, kodDisplay);
	}

	private Comparator<T> getComparator(final String[] attributes, final int profondeur) {
		Comparator<T> comp = new Comparator<T>() {

			@Override
			public int compare(KObject o1, KObject o2) {
				int result = 0;
				try {
					result = KQuickSort.compare(o1, o2, attributes[profondeur]);
					if (result == 0 && profondeur < attributes.length - 1)
						result = KQuickSort.compare(o1, o2, attributes[profondeur + 1]);

				} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
				}
				return result;
			}
		};
		return comp;
	}

	private Comparator<T> getComparator(final String[] attributes, final int profondeur, final KObjectDisplay koDisplay) {
		Comparator<T> comp = new Comparator<T>() {

			@Override
			public int compare(KObject o1, KObject o2) {
				int result = 0;
				try {
					result = KQuickSort.compare(o1, o2, attributes[profondeur], koDisplay);
					if (result == 0 && profondeur < attributes.length - 1)
						result = KQuickSort.compare(o1, o2, attributes[profondeur + 1], koDisplay);

				} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
				}
				return result;
			}
		};
		return comp;
	}

	private Comparator<T> getComparatorDesc(final String[] attributes) {
		return getComparatorDesc(attributes, 0);
	}

	private Comparator<T> getComparatorDesc(final String[] attributes, final KObjectDisplay koDisplay) {
		return getComparatorDesc(attributes, 0, koDisplay);
	}

	private Comparator<T> getComparatorDesc(final String[] attributes, final int profondeur) {
		Comparator<T> comp = new Comparator<T>() {

			@Override
			public int compare(KObject o1, KObject o2) {
				int result = 0;
				try {
					result = KQuickSort.compare(o1, o2, attributes[profondeur]);
					if (result == 0 && profondeur < attributes.length - 1)
						result = KQuickSort.compare(o1, o2, attributes[profondeur + 1]);

				} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
				}
				return -result;
			}
		};
		return comp;
	}

	private Comparator<T> getComparatorDesc(final String[] attributes, final int profondeur, final KObjectDisplay koDisplay) {
		Comparator<T> comp = new Comparator<T>() {

			@Override
			public int compare(KObject o1, KObject o2) {
				int result = 0;
				try {
					result = KQuickSort.compare(o1, o2, attributes[profondeur], koDisplay);
					if (result == 0 && profondeur < attributes.length - 1)
						result = KQuickSort.compare(o1, o2, attributes[profondeur + 1], koDisplay);

				} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
				}
				return -result;
			}
		};
		return comp;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	private String getGenericType() {
		//
		String ret = "";
		try {
			ret = getField(this.getClass(), "items").getGenericType().toString();
			Type type = getField(this.getClass(), "items").getGenericType();
			ParameterizedType pType = (ParameterizedType) type;
			Type[] types = pType.getActualTypeArguments();
			Class genC = (Class) types[0];
			ret = genC.getName();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public void updateItemsToDelete() {
		ArrayList<T> indToDelete = new ArrayList<T>();
		for (T ko : items) {
			if (ko.getRecordStatus() == KRecordStatus.rsDelete)
				indToDelete.add(ko);
		}
		for (T ko : indToDelete)
			remove(ko);
	}

	/**
	 * Insère l'objet ko à l'indice index de la liste
	 * 
	 * @param index
	 *            position de l'insertion
	 * @param ko
	 *            objet inséré
	 */
	@SuppressWarnings("unchecked")
	public void add(int index, KObject ko) {
		ko.toAdd();
		items.add(index, (T) ko);
	}

	/**
	 * Ajoute l'objet ko dans la liste
	 * 
	 * @param ko
	 *            objet inséré
	 */
	public void add(KObject ko) {
		add(ko, false);
	}

	/**
	 * Ajoute l'objet ko dans la liste, sans ajout correspondant dans la base de
	 * données si silent vaut vrai
	 * 
	 * @param ko
	 *            objet inséré
	 * @param silent
	 *            si vrai, l'objet n'est pas signalé comme devant être inséré
	 *            dans la base de données
	 */
	@SuppressWarnings("unchecked")
	public void add(KObject ko, boolean silent) {
		if (!silent)
			ko.toAdd();
		items.add((T) ko);
	}

	/**
	 * Ajoute une liste à la liste courante
	 * 
	 * @param alistObject
	 *            KListObject à ajouter
	 */
	public void addAll(KListObject<? extends KObject> alistObject) {
		if (clazz.isAssignableFrom(alistObject.getClazz())) {
			for (KObject o : alistObject) {
				items.add((T) o);
			}
		}
	}

	/**
	 * Exécute une commande sur l'ensemble des objets de la liste
	 * 
	 * @param commande
	 *            Commande à exécuter
	 */
	public void appyCommande(IKommande commande) {
		for (KObject o : items) {
			commande.execute(o);
		}
	}

	public List<T> asAL() {
		return items;
	}

	/**
	 * Supprime tous les éléments de la liste, sans agir sur la base de données
	 */
	public void clear() {
		items.clear();
	}

	/**
	 * Retourne le nombre d'éléments dans la liste
	 * 
	 * @return nombre d'éléments
	 */
	public int count() {
		return items.size();
	}

	/**
	 * Supprime l'élément de la liste à la position index
	 * 
	 * @param index
	 *            position de l'élément à supprimer
	 * @return vrai si l'élément a été supprimé
	 */
	public boolean delete(int index) {
		return remove(items.get(index));
	}

	/**
	 * Supprime les éléments de la liste dont la valeur de la première clé
	 * primaire est présente dans keysValues
	 * 
	 * @param keysValues
	 *            liste des clés
	 * @param sep
	 *            caractère de séparation entre les clés
	 */
	public void deleteByKeys(String keysValues, String sep) {
		deleteByKeys(keysValues, sep, "");
	}

	/**
	 * Supprime les éléments de la liste dont la valeur de la première clé
	 * primaire est présente dans keysValues, en préservant les clés se trouvant
	 * dans perserveKeys
	 * 
	 * @param keysValues
	 *            liste des clés
	 * @param sep
	 *            caractère de séparation entre les clés
	 * @param preserveKeys
	 *            liste des clés à ne pas supprimer
	 */
	public void deleteByKeys(String keysValues, String sep, String preserveKeys) {
		ArrayList<KObject> toDelete = new ArrayList<>();
		for (KObject klo : items) {
			if ("".equals(preserveKeys)) {
				if ((sep + keysValues + sep).contains(sep + klo.getFirstKeyValue() + sep))
					toDelete.add(klo);
			} else {
				if (!(sep + preserveKeys + sep).contains(sep + klo.getFirstKeyValue() + sep) && (sep + keysValues + sep).contains(sep + klo.getFirstKeyValue() + sep))
					toDelete.add(klo);
			}
		}
		for (int i = 0; i < toDelete.size(); i++)
			items.remove(toDelete.get(i));
	}

	/**
	 * Retourne l'objet de la liste à la position index
	 * 
	 * @param index
	 *            index de l'objet à retourner
	 * @return objet à la position index
	 */
	public T get(int index) {
		return items.get(index);
	}

	/**
	 * Retourne le nom complet de la classe des objets de la liste
	 * 
	 * @return nom de classe
	 */
	public String getClassName() {
		return clazz.getName();
	}

	/**
	 * Retourne la classe des objets
	 * 
	 * @return classe dérivée de KObject
	 */
	public Class<T> getClazz() {
		return clazz;
	}

	public static KListObject<? extends KObject> getListInstance(Class<? extends KObject> clazz) {
		return new KListObject<>(clazz);
	}

	/**
	 * Retourne les valeurs du membre fieldName pour l'ensemble des objets de la
	 * liste, séparés par separator
	 * 
	 * @param fieldName
	 *            nom du membre
	 * @param separator
	 *            separateur des valeurs
	 * @return liste des valeurs du membre fieldName, séparés par separator
	 */
	public String getFieldValues(String fieldName, String separator) {
		String result = "";
		for (KObject ko : items)
			try {
				result += ko.getAttribute(fieldName) + separator;
			} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				result = null;
			}
		return result;
	}

	/**
	 * Retourne la Liste des éléments de la collection
	 * 
	 * @return items
	 */
	public List<T> getItems() {
		return items;
	}

	/**
	 * Retourne les valeurs du membre défini en tant que clé primaire pour
	 * l'ensemble des objets de la liste, séparés par separator
	 * 
	 * @param separator
	 *            separateur des valeurs
	 * @return liste des valeurs du membre clé primaire, séparés par separator
	 */
	public String getKeyValuesForHasAndBelongsToMany(String separator) {
		String result = "";
		for (KObject ko : items)
			result += ko.getFirstKeyValue() + separator;
		return result;
	}

	/**
	 * Retourne une KListObject des valeurs du membre member, si celui-ci est de
	 * type KObject
	 * 
	 * @param member
	 *            nom du membre, instance de clazz
	 * @param clazz
	 *            classe dérivée de KObject
	 * @return liste des valeur du membre member de type KObject
	 */
	public KListObject<? extends KObject> getMemberAsKL(String member, Class<? extends KObject> clazz) {
		KListObject<? extends KObject> kl = new KListObject<>(clazz);
		for (KObject ko : items) {
			try {
				KObject o = (KObject) ko.getAttribute(member);
				if (o != null)
					kl.add(o, true);
				else
					return kl;
			} catch (Exception e) {
				return kl;
			}
		}
		return kl;
	}

	/**
	 * Retourne l'instruction SQL permettant de charger depuis la base de
	 * données les éléments de la liste
	 * 
	 * @return instruction SQL
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Retourne un identifiant unique pour la liste
	 * 
	 * @return identifiant
	 */
	public String getUniqueId() {
		String ret = "";
		try {
			ret = this.clazz + "#" + this.getSql();
			ret = ret.toLowerCase();
		} catch (Exception $e) {
		}
		return ret;
	}

	/**
	 * Retoune le hash MD5 de l'identifiant unique de la liste
	 * 
	 * @return hash MD5
	 */
	public String getUniqueIdHash() {
		return KHash.getMD5(getUniqueId());
	}

	/**
	 * Retourne l'index du premier élément de la liste satisfaisant criteria<br>
	 * ou -1 si aucun objet ne satisfait le critère
	 * 
	 * @param criteria
	 *            critére de sélection
	 * @return index de l'objet trouvé
	 */
	public int indexByCriteria(String criteria) {
		int i = 0;
		boolean trouve = false;
		while (i < items.size() && !trouve) {
			if (items.get(i).matchWith(criteria))
				trouve = true;
			i++;
		}
		if (trouve)
			return i - 1;
		else
			return -1;
	}

	/**
	 * Retourne l'index dans la liste de l'objet passé en paramètre
	 * 
	 * @param ko
	 *            objet recherché
	 * @return index
	 */
	public int indexOf(T ko) {
		return items.indexOf(ko);
	}

	/**
	 * retourne la concaténation du résultat de l'appel de memberName sur chacun
	 * des objets composant la liste<br>
	 * Chacune des valeurs est séparée par une fin de ligne
	 * 
	 * @param memberName
	 *            membre existant sur les instances présentes dans la liste
	 * @return chaîne concaténée
	 */
	public String invoke(String memberName) {
		String ret = "";
		for (T unT : items) {
			Object o = KReflection.getMemberValue(memberName, unT);
			if (o != null)
				ret += o + Display.getDefault().getEndLigne();
			else
				ret += Display.getDefault().getEndLigne();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<T> iterator() {
		return items.iterator();
	}

	/**
	 * @param keys
	 * @param separator
	 */
	public void markForHasAndBelongsToMany(String keys, String separator) {
		setRecordStatus(KRecordStatus.rsDelete);
		ArrayList<String> listKeys = new ArrayList<String>(Arrays.asList(keys.split(separator)));
		for (KObject ko : items) {
			String k = ko.getFirstKeyValue();
			if ((separator + keys + separator).contains(separator + k + separator)) {
				ko.setRecordStatus(KRecordStatus.rsNone);
				listKeys.remove(k);
			}
		}
		for (String k : listKeys) {
			KObject ko;
			try {
				if (!"".equals(k)) {
					ko = clazz.newInstance();
					ko.setFirstKeyValue(k);
					add(ko);
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Supprime l'objet ko passé en paramètre de la liste<br>
	 * La suppression ne sera effective dans la base de données qu'à l'appel de
	 * la méthode saveToDb
	 * 
	 * @param ko
	 *            objet à supprimer
	 * @return vrai si ko était présent dans la liste
	 */
	@SuppressWarnings("unchecked")
	public boolean remove(KObject ko) {
		boolean ret = items.remove(ko);
		if (ret)
			ko.toDelete();
		deletedItems.add((T) ko);
		return ret;
	}

	/**
	 * Retourne une nouvelle liste composée des objets satisfaisant le critère
	 * de sélection passé en paramètre
	 * 
	 * @param criteria
	 *            critère de sélection de la forme
	 *            nomMembre1=valeurMembre1;nomMembre2=valeurMembre2...
	 * @return liste des objets répondant aux critères
	 */
	public KListObject<T> select(String criteria) {
		KListObject<T> list = new KListObject<T>(clazz);
		for (T ko : items) {
			if (ko.matchWith(criteria))
				list.add(ko, true);
		}
		return list;
	}

	/**
	 * Retourne une nouvelle liste composée des objets contenant le paramètre
	 * value dans l'un de leurs membres
	 * 
	 * @param value
	 *            chaîne recherchée
	 * @return liste d'objets contenant value
	 */
	public KListObject<T> selectByName(String value) {
		KListObject<T> list = new KListObject<T>(clazz);
		for (T ko : items) {
			if (ko.matchWithValue(value))
				list.add(ko);
		}
		return list;
	}

	/**
	 * Retourne le premier objet satisfaisant le critère de sélection passé en
	 * paramètre
	 * 
	 * @param criteria
	 *            critère de sélection de la forme
	 *            nomMembre1=valeurMembre1;nomMembre2=valeurMembre2...
	 * @return objet répondant aux critères ou null si aucun objet ne correspond
	 */
	public T selectFirst(String criteria) {
		T result = null;
		for (T ko : items) {
			if (ko.matchWith(criteria)) {
				result = ko;
				break;
			}
		}
		return result;
	}

	/**
	 * Retourne l'objet dont l'id est passé en paramètre
	 * 
	 * @param id
	 * @return objet répondant aux critères ou null si aucun objet ne correspond
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public T selectById(Object id) throws InstantiationException, IllegalAccessException {
		T result = null;
		String key = KObject.getFirstKey(clazz);
		for (T ko : items) {
			if (ko.matchWith(key + "=" + id)) {
				result = ko;
				break;
			}
		}
		return result;
	}

	/**
	 * Remplace l'objet à la position index par l'objet ko passé en paramètre
	 * 
	 * @param index
	 *            index de l'objet à remplacer
	 * @param ko
	 *            objet remplaçant
	 */
	@SuppressWarnings("unchecked")
	public void set(int index, KObject ko) {
		items.set(index, (T) ko);
	}

	/**
	 * Affecte la valeur value au membre memberName de chacun des objets de la
	 * liste
	 * 
	 * @param memberName
	 *            Membre à modifier
	 * @param value
	 *            Valeur à affecter
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void setMemberValue(String memberName, Object value) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
		for (KObject o : items) {
			o.setAttribute(memberName, value, false);
		}
	}

	/**
	 * Modifie le recordStatus de tous les objets de la liste
	 * 
	 * @param recordStatus
	 */
	public void setRecordStatus(KRecordStatus recordStatus) {
		for (KObject ko : items)
			ko.setRecordStatus(recordStatus);
	}

	/**
	 * Modifie l'instruction SQL de la liste
	 * 
	 * @param sql
	 *            instruction SQL
	 */
	public String setSql(String sql, String quoteChar) {
		if (sql == "")
			try {
				this.sql = clazz.newInstance().getQuery("", quoteChar);
			} catch (Exception e) {
				Ko.klogger().log(Level.SEVERE, "Impossible d'instancier un objet de la classe " + clazz + " pour construire l'instruction sql", e);
			}
		else
			this.sql = sql;
		return this.sql;
	}

	/**
	 * Retoune une chaîne d'affichage des éléments de la liste établie à partir
	 * du masque passé en paramètre
	 * 
	 * @param mask
	 *            masque d'affichage
	 * @return chaîne d'affichage
	 */
	public String showWithMask(KMask mask) {
		return mask.show(this);
	}

	/**
	 * Retoune une chaîne d'affichage des éléments de la liste établie à partir
	 * de la chaîne de masque passée en paramètre<br>
	 * Le masque d'affichage doit faire mention des membres à afficher sous la
	 * forme <b>{memberName}</b>
	 * 
	 * @param mask
	 *            masque d'affichage
	 * @return chaîne d'affichage
	 */
	public String showWithMask(String mask) {
		return showWithMask(mask, "{", "}");
	}

	public String explode(String mask, String glue) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < items.size(); i++) {
			sb.append(items.get(i)._showWithMask(mask));
			if (i < items.size() - 1) {
				sb.append(glue);
			}
		}
		return sb.toString();
	}

	/**
	 * Retoune une chaîne d'affichage des éléments de la liste établie à partir
	 * du masque passé en paramètre
	 * 
	 * @param mask
	 *            masque d'affichage
	 * @param sepFirst
	 * @param sepLast
	 * @return chaîne d'affichage
	 */
	public String showWithMask(String mask, String sepFirst, String sepLast) {
		String ret = "";
		for (T ko : items) {
			ret += ko._showWithMask(mask, sepFirst, sepLast) + "\n";
		}
		return ret;
	}

	/**
	 * Retoune une chaîne d'affichage des éléments de la liste établie à partir
	 * du masque passé en paramètre en effectuant un regroupement sur le membre
	 * groupByAttribute
	 * 
	 * @param mask
	 *            masque d'affichage
	 * @param groupByAttribute
	 *            membre de regroupement
	 * @return chaîne d'affichage
	 */
	public String showWithMaskGroupBy(String mask, String groupByAttribute) {
		return showWithMaskGroupBy(mask, groupByAttribute, "{" + groupByAttribute + "}");
	}

	/**
	 * Retoune une chaîne d'affichage des éléments de la liste établie à partir
	 * du masque passé en paramètre en effectuant un regroupement sur le membre
	 * groupByAttribute, et affichant le regroupement à partir de groupByMask
	 * 
	 * @param mask
	 *            masque d'affichage
	 * @param groupByAttribute
	 *            membre de regroupement
	 * @param groupByMask
	 *            masque d'affichage pour le regroupement
	 * @return chaîne d'affichage
	 */
	public String showWithMaskGroupBy(String mask, String groupByAttribute, String groupByMask) {
		return showWithMaskGroupBy(mask, groupByAttribute, "<div name='grp-" + groupByAttribute + "' id='grp-{" + groupByAttribute + "}' class='gbHeader'>" + groupByMask + "</div><div id='grp-child-{" + groupByAttribute + "}' class='gbChild'>", true);
	}

	/**
	 * Retoune une chaîne d'affichage des éléments de la liste établie à partir
	 * du masque passé en paramètre en effectuant un regroupement sur le membre
	 * groupByAttribute, et affichant le regroupement à partir de groupByMask
	 * 
	 * @param mask
	 *            masque d'affichage
	 * @param groupByAttribute
	 *            membre de regroupement
	 * @param attributeMask
	 * @param hasJs
	 *            si vrai, les éléments enfants du regroupement apparaîssent
	 *            dans une zone js accordion
	 * @return
	 */
	public String showWithMaskGroupBy(String mask, String groupByAttribute, String attributeMask, boolean hasJs) {
		this.sortBy(groupByAttribute);
		String gb = "";
		String ret = "";
		String js = "<script type='text/javascript'>";
		boolean toClose = false;
		for (T ko : items) {
			try {
				String tmpGb = ko.getAttribute(groupByAttribute) + "";
				if (!gb.equals(tmpGb)) {
					if (toClose) {
						ret += "</div>";
						toClose = false;
					}
					ret += ko._showWithMask(attributeMask);
					if (hasJs)
						js += "(new Forms.Accordion($('grp-child-" + tmpGb + "'),5,24)).attach($('grp-" + tmpGb + "'));";
					toClose = true;
					gb = tmpGb;
				}
			} catch (Exception e) {
			}
			ret += ko._showWithMask(mask) + "\n";
		}
		if (toClose)
			ret += "</div>";
		if (hasJs)
			ret += js + "</script>";
		return ret;
	}

	/**
	 * Trie la collection en ordre croissant
	 */
	public void sort() {
		sort(true);
	}

	/**
	 * Trie la collection en invoquant la méthode compareTo de chaque objet
	 * 
	 * @param asc
	 *            sens, si vrai croissant
	 */
	@SuppressWarnings("unchecked")
	public void sort(boolean asc) {
		if (asc == true)
			Collections.sort(items);
		else
			Collections.reverse(items);
	}

	/**
	 * Trie la collection suivant le tableau attributes
	 * 
	 * @param attributes
	 *            liste des attributs suivant lesquels effectuer le tri
	 * @param asc
	 *            ordre de tri
	 */
	public void sort(String[] attributes, boolean asc) {
		if (asc == true)
			Collections.sort(items, getComparator(attributes));
		else
			Collections.sort(items, getComparatorDesc(attributes));
	}

	public void sort(String[] attributes, boolean asc, KObjectDisplay koDisplay) {
		if (asc == true)
			Collections.sort(items, getComparator(attributes, koDisplay));
		else
			Collections.sort(items, getComparatorDesc(attributes, koDisplay));
	}

	/**
	 * trie la collection suivant attribute dans l'ordre ascendant
	 * 
	 * @param attribute
	 */
	public void sortBy(String attribute) {
		sortBy(attribute, true);
	}

	public void sortBy(String attribute, KObjectDisplay koDisplay) {
		sortBy(attribute, true, koDisplay);
	}

	/**
	 * trie la collection suivant attribute dans l'ordre asc
	 * 
	 * @param attribute
	 *            attribut de tri
	 * @param asc
	 *            ordre de tri vrai=asc
	 */
	public void sortBy(String attribute, boolean asc) {
		KQuickSort.triRapide(this, attribute, asc);
	}

	public void sortBy(String attribute, boolean asc, KObjectDisplay koDisplay) {
		KQuickSort.triRapide(this, attribute, asc, koDisplay);
	}

	/**
	 * Echange la position de 2 objets
	 * 
	 * @param i
	 *            index i de l'objet à échanger
	 * @param j
	 *            index j de l'objet à échanger
	 */
	@SuppressWarnings("unchecked")
	public void swap(int i, int j) {
		KObject temporaire = items.set(i, items.get(j));
		items.set(j, (T) temporaire);
	}

	@Override
	public String toString() {
		String ret = "";
		for (T unT : items) {
			ret += unT.toString() + Display.getDefault().getEndLigne();
		}
		return ret;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public List<T> getDeletedItems() {
		return deletedItems;
	}

	public KListObject<T> subList(int start, int count) {
		KListObject<T> sub = new KListObject<>(clazz);
		int i = start;
		while (i < items.size() && i < start + count) {
			sub.add(items.get(i), true);
			i++;
		}
		return sub;
	}

}
