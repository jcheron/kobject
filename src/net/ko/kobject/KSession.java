/**
 * Classe KSession
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2016
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  LGPL License
 * @version $Id: KSession.java,v 1.4 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.kobject
 */
package net.ko.kobject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.cache.KCache;
import net.ko.dao.DaoList;
import net.ko.dao.DatabaseDAO;
import net.ko.dao.IGenericDao;
import net.ko.db.KDataBase;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.http.objects.KHttpSession;
import net.ko.http.objects.KRequest;
import net.ko.ksql.KParameterizedInstruction;

/**
 * Classe d'accès aux principales fonctionnalités de l'application :<br/>
 * Connexion à la base de données, chargement d'objets ou de listes d'objets
 * depuis la base, sauvegarde<br/>
 * Activation, gestion du cache
 * 
 * @author jcheron
 * 
 */
public class KSession {
	/**
	 * Instance de connexion à la base de données
	 */
	protected KDataBase db;

	/**
	 * @return l'instance de connexion à la base active
	 */
	public KDataBase getDb() {
		return db;
	}

	/**
	 * Attribue une connexion existante à la session
	 * 
	 * @param db
	 *            connexion à une base de données
	 */
	public void setDb(KDataBase db) {
		this.db = db;
	}

	private KDataBase getDbInstance() {
		if (db != null)
			return db;
		else
			try {
				return Ko.kdatabase();
			} catch (Exception e) {
				Ko.klogger().log(Level.SEVERE, "Impossible de fournir la connexion à la base de données", e);
			}
		return null;
	}

	/**
	 * Etablit une connexion à une base de données
	 * 
	 * @param host
	 *            serveur
	 * @param user
	 *            utilisateur
	 * @param pass
	 *            mot de passe utilisateur
	 * @param base
	 *            base de données
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void connect(String host, String user, String pass, String base) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		KDataBase kdb = getDbInstance();
		if (kdb != null)
			kdb.connect(host, user, pass, base);
	}

	/**
	 * Etablit une connexion à une base de données
	 * 
	 * @param host
	 *            serveur
	 * @param user
	 *            utilisateur
	 * @param pass
	 *            mot de passe utilisateur
	 * @param base
	 *            base de données
	 * @param port
	 *            port de connexion utilisé
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public KDataBase connect(String host, String user, String pass, String base, String port) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		KDataBase kdb = getDbInstance();
		if (kdb != null)
			kdb.connect(host, user, pass, base, port);
		return kdb;
	}

	public KDataBase connect(KDataBase kdb, String host, String user, String pass, String base, String port) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		// this.db=db;
		kdb.connect(host, user, pass, base, port);
		return kdb;
	}

	public synchronized KDataBase connect(KDataBase kdb, String host, String user, String pass, String base, String port, String options) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		kdb.connect(host, user, pass, base, port, options);
		KDebugConsole.print("Connection à " + kdb.getURL(), "SQL", "KSession.connect");
		return kdb;
	}

	public KDataBase connect(KDataBase kdb, String host, String user, String pass, String base) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		// this.db=db;
		kdb.connect(host, user, pass, base);
		return kdb;
	}

	/**
	 * Etablit la connexion à la base passée en paramètre
	 * 
	 * @param kdb
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void connect(KDataBase kdb) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		// this.db=db;
		if (!KDataBase.isValid(kdb))
			kdb.connect();
	}

	/**
	 * met à jour l'objet dans la base de données en utilisant sa propriété
	 * recordstatus
	 * 
	 * @see net.ko.kobject.KRecordStatus
	 * @param object
	 * @throws SQLException
	 */
	public void saveToDb(KObject object) throws SQLException {
		DaoList daoList = Ko.getDaoList(object.getClass());
		daoList.updateToSupport(object);
		daoList.close();
	}

	/**
	 * Ajoute l'objet dans la base de données
	 * 
	 * @param object
	 *            objet à ajouter
	 * @throws SQLException
	 */
	public void insertToDb(KObject object) throws SQLException {
		updateRsToDb(object, KRecordStatus.rsNew);
	}

	/**
	 * Modifie l'objet dans la base de données
	 * 
	 * @param object
	 * @throws SQLException
	 */
	public void updateToDb(KObject object) throws SQLException {
		updateRsToDb(object, KRecordStatus.rsUpdate);
	}

	/**
	 * Met à jour la liste d'objets dans la base de données en appliquant à
	 * chacun des objets qu'elle contient l'opération correpondant à son
	 * recordStatus
	 * 
	 * @see net.ko.kobject.KRecordStatus
	 * @param list
	 *            Liste d'objets
	 * @throws SQLException
	 */
	public void updateToDb(KListObject<? extends KObject> list) throws SQLException {
		KDataBase kdb = getDbInstance();
		if (KDataBase.isValid(kdb))
			getDao(list.getClazz()).update(list);
		closeDb(kdb);
	}

	/**
	 * Supprime l'objet dans la base de données
	 * 
	 * @param object
	 *            objet à supprimer
	 * @throws SQLException
	 */
	public void deleteFromDb(KObject object) throws SQLException {
		updateRsToDb(object, KRecordStatus.rsDelete);
	}

	/**
	 * Charge un enregistrement depuis la base de données répondant aux critères
	 * définis dans le paramètre where et retourne l'instance de clazz
	 * correpondante<br/>
	 * Préciser dans le paramètre where une condition portant sur les valeurs
	 * des clés primaires de façon à n'obtenir qu'un seul enregistrement<br/>
	 * Si le paramètre where permet d'obtenir plusieurs enregistrement, utiliser
	 * findObj
	 * 
	 * @see net.ko.kobject.KSession.#findObj(int, Class, String)
	 * @param clazz
	 *            classe à instancier
	 * @param where
	 *            condition where spécifiée en sql
	 * @return instance de clazz (classe dérivée de KObject)
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public KObject kloadOneFromDb(Class<? extends KObject> clazz, KParameterizedInstruction condition) throws SQLException, InstantiationException, IllegalAccessException {
		KDataBase kdb = getDbInstance();
		KObject result = KObject.getNewInstance(clazz);
		getDao(clazz).read(result, condition);
		return result;
	}

	public KListObject<KObject> kloadMany(String shortClassName) {
		return kloadMany(shortClassName, "");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public KListObject<KObject> kloadMany(String shortClassName, String sql) {
		KListObject<KObject> result = null;
		shortClassName = Ko.getCompleteClassName(shortClassName);
		try {
			Class clazz = Class.forName(shortClassName);
			result = (KListObject<KObject>) kloadFromDb(clazz, sql);
		} catch (Exception e) {
		}
		return result;
	}

	public String kloadAndShowMany(String shortClassName, String sql, String mask) {
		KListObject<KObject> kl = kloadMany(shortClassName, sql);
		return kl.showWithMask(mask);
	}

	public String kloadAndShowMany(String shortClassName, String mask) {
		KListObject<KObject> kl = kloadMany(shortClassName);
		return kl.showWithMask(mask);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public KObject kloadOne(String shortClassName, KParameterizedInstruction condition) {
		KObject result = null;
		shortClassName = Ko.getCompleteClassName(shortClassName);
		try {
			Class clazz = Class.forName(shortClassName);
			result = kloadOneFromDb(clazz, condition);
		} catch (Exception e) {
			Ko.klogger().log(Level.SEVERE, "Impossible de charger l'objet ", e);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public KObject kloadOne(String shortClassName, String queryString, boolean fromRequest) {
		KObject result = null;
		KHttpSession ksession = new KHttpSession(null);
		shortClassName = Ko.getCompleteClassName(shortClassName);
		try {
			Class clazz = Class.forName(shortClassName);
			result = (KObject) clazz.newInstance();
			ksession.setQueryString(result, queryString);
			DatabaseDAO<KObject> dao = (DatabaseDAO<KObject>) Ko.getDao(clazz);
			dao.read(result);
		} catch (Exception e) {
			Ko.klogger().log(Level.SEVERE, "Impossible de charger l'objet : ", e);
		}
		return result;
	}

	public KObject kloadOne(String shortClassName, HttpServletRequest request) {
		return kloadOne(shortClassName, request.getQueryString(), true);
	}

	public KObject kloadOne(Class<? extends KObject> clazz, HttpServletRequest request) {
		KObject result = null;
		KHttpSession ksession = new KHttpSession(request.getSession());
		String queryString = request.getQueryString();
		try {
			result = (KObject) clazz.newInstance();
			ksession.setQueryString(result, queryString);
			DatabaseDAO<KObject> dao = (DatabaseDAO<KObject>) Ko.getDao(clazz);
			dao.read(result);
		} catch (Exception e) {
			Ko.klogger().log(Level.SEVERE, "Impossible de charger l'objet : ", e);
		}
		return result;
	}

	public String kloadAndShowOne(String shortClassName, KParameterizedInstruction condition, String mask) throws InstantiationException, IllegalAccessException, SQLException {
		KObject o = kloadOne(shortClassName, condition);
		String result = "";
		if (o != null)
			result = o._showWithMask(mask);
		return result;
	}

	public String kloadAndShowOne(String shortClassName, String mask, String queryString, boolean fromRequest) {
		KObject o = kloadOne(shortClassName, queryString, fromRequest);
		String result = "";
		if (o != null)
			result = o._showWithMask(mask);
		return result;
	}

	public String kloadList(String listUrl, String wherePost, HttpServletRequest request, HttpServletResponse response) {
		String result = "";
		try {
			result = KRequest.includeResponse(listUrl, request, response, "_where=" + URLEncoder.encode(wherePost, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
		}
		return result;
	}

	/**
	 * Charge un enregistrement depuis la base de données comme étant à la
	 * position witch de ceux répondant aux critères définis dans le paramètre
	 * where et retourne l'instance de clazz correpondante Il est dans ce cas
	 * possible de préciser dans le paramètre where une condition permettant
	 * d'obtenir plusieurs enregistrements en réponse
	 * 
	 * @param witch
	 * @param clazz
	 *            classe à instancier
	 * @param where
	 *            condition where spécifiée en sql
	 * @return instance de clazz (classe dérivée de KObject)
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public KObject findObj(int witch, Class<? extends KObject> clazz, KParameterizedInstruction condition) throws InstantiationException, IllegalAccessException, SQLException {
		KObject result = null;
		DatabaseDAO<KObject> dao = (DatabaseDAO<KObject>) Ko.getDao(clazz);
		result = KObject.getNewInstance(clazz);
		result = dao.read(result, condition, witch);
		return result;
	}

	/**
	 * Retourne la liste complète des enregistrements de la base correpondant à
	 * la classe passée en paramètre<br/>
	 * La liste de réponse est composée d'instances de KObject de type clazz
	 * 
	 * @param clazz
	 *            classe générique à utiliser
	 * @return liste des objets
	 */
	public KListObject<? extends KObject> kloadFromDb(Class<? extends KObject> clazz) {
		KDataBase kdb = getDbInstance();
		DatabaseDAO<KObject> dao = (DatabaseDAO<KObject>) Ko.getDao(clazz);
		if (KDataBase.isValid(kdb))
			return dao.readAll();
		else
			return null;
	}

	/**
	 * Retourne la liste sélective des enregistrements de la base correpondant à
	 * la classe passée en paramètre, et répondant à l'instruction sql<br/>
	 * La liste de réponse est composée d'instances de KObject de type clazz
	 * 
	 * @param clazz
	 *            classe générique à utiliser
	 * @param sql
	 *            instruction sql de sélection des enregistrements
	 * @return liste des objets
	 */
	public KListObject<? extends KObject> kloadFromDb(Class<? extends KObject> clazz, String sql) {
		KDataBase kdb = getDbInstance();
		if (KDataBase.isValid(kdb)) {
			KDebugConsole.print("c:" + kdb.getConnection(), "POOL", "KSession.kloadFromDb");
			DatabaseDAO<KObject> dao = (DatabaseDAO<KObject>) Ko.getDao(clazz);
			KListObject<KObject> list = (KListObject<KObject>) KListObject.getListInstance(clazz);
			dao.setSelect(list, sql);
			return dao.readAll();
		} else
			return null;
	}

	/**
	 * Retourne la liste complète des enregistrements de la base correpondant à
	 * la classe passée en paramètre, triée sur le numéro du champ
	 * sortedFieldNum
	 * 
	 * @param clazz
	 *            classe générique à utiliser
	 * @param sortedFieldNum
	 *            numéro du champ à utiliser pour le tri
	 * @return liste des objets
	 */
	public KListObject<? extends KObject> kloadFromDb(Class<? extends KObject> clazz, int sortedFieldNum) {
		DatabaseDAO<KObject> dao = (DatabaseDAO<KObject>) Ko.getDao(clazz);
		return dao.readAll("", sortedFieldNum);
	}

	/**
	 * @param object
	 * @param rs
	 * @throws SQLException
	 */
	private void updateRsToDb(KObject object, KRecordStatus rs) throws SQLException {
		object.setRecordStatus(rs);
		DaoList daoList = Ko.getDaoList(object.getClass());
		daoList.updateToSupport(object);
		daoList.close();
	}

	/**
	 * Active la mise en cache des objets et des listes d'objet
	 */
	public static void cacheActivate() {
		Ko.useCache = true;
	}

	/**
	 * Désactive temporairement la mise en cache des objets et des listes
	 * d'objet
	 */
	public static void cacheDeactivate() {
		Ko.useCache = false;
	}

	/**
	 * Ferme le cache, et arrête la mise en cache définitivement
	 */
	public static void cacheShutdown() {
		KCache.shutdown();
	}

	public void closeDb(KDataBase kdb) {
		try {
			kdb.close();
		} catch (SQLException e) {
			Ko.klogger().log(Level.WARNING, "KSession:Impossible de fermer la connexion à la base " + kdb, e);
		}
	}

	public void close() {
		if (db != null) {
			try {
				db.close();
				db.clear();
			} catch (SQLException e) {
				Ko.klogger().log(Level.WARNING, "KSession:Impossible de fermer la connexion à la base " + db, e);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static IGenericDao getDao(Class<? extends KObject> clazz) {
		return Ko.getDao(clazz);
	}
}
