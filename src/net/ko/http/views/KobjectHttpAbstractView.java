package net.ko.http.views;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import net.ko.dao.DatabaseDAO;
import net.ko.dao.IGenericDao;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.http.objects.KHttpSession;
import net.ko.interfaces.IKobjectDisplay;
import net.ko.kobject.KObject;

/**
 * Vue HTML associée à un objet de type KObject (pour afficher ou modifier
 * l'objet)
 * 
 * @author jcheron
 * 
 */
public abstract class KobjectHttpAbstractView<T extends KObject> extends
		KAbstractView<T> {
	protected T ko;
	protected boolean insertMode = false;

	public KobjectHttpAbstractView(T ko, HttpServletRequest request) {
		super(request);
		this.ko = ko;
		initKobjectController(ko);
	}

	public boolean load() throws SecurityException, IllegalArgumentException, SQLException, NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		return load(false, false);
	}

	/**
	 * Charge l'objet depuis la base de données, en fonction des paramètres
	 * passés dans l'URL pour un fonctionnement correct, toutes les valeurs des
	 * champs contenus dans la clé primaire doivent être passées
	 * 
	 * @param noDb
	 *            détermine si l'objet doit être chargé depuis la base de
	 *            données
	 * @return vrai si l'objet a été chargé
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws SQLException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public boolean load(boolean noDb, boolean beforeSubmit) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		boolean result = false;
		IKobjectDisplay display = getDisplayInstance();
		if (!(display.getClass().equals(KObjectDisplay.class))) {
			display.beforeLoading(ko, this, request);
		}
		if (request.getParameterMap().size() > 0) {
			KHttpSession ksession = new KHttpSession(request.getSession());
			ksession.setRequestParametersTo(ko, request, beforeSubmit);
			if (!noDb) {
				IGenericDao<T> dao = (IGenericDao<T>) Ko.getDao(ko.getClass());
				dao.read(ko);
				dao.close();
			}
			result = this.ko.isLoaded();
		} else
			result = false;
		if (!(display.getClass().equals(KObjectDisplay.class))) {
			display.afterLoading(ko, this, request);
		}
		return result;
	}

	/**
	 * @return l'objet associé à la vue
	 */
	public KObject getKobject() {
		return this.ko;
	}

	/*
	 * public String showWithMask(String aMask, String sepFirst, String sepLast)
	 * { return this.ko.showWithMask(aMask, sepFirst, sepLast); }
	 * 
	 * public String showWithMask(String aMask) { return
	 * this.ko.showWithMask(aMask, "{", "}"); }
	 */
	public boolean isInsertMode() {
		return insertMode;
	}

	public void setInsertMode(boolean insertMode) {
		this.insertMode = insertMode;
	}

	@Override
	public DatabaseDAO<T> getDao() {
		if (dao == null) {
			if (daoClassName != null & !"".equals(daoClassName)) {
				try {
					Class<DatabaseDAO<T>> classDao = (Class<DatabaseDAO<T>>) Class.forName(daoClassName);
					return classDao.newInstance();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					if (ko != null) {
						dao = (DatabaseDAO<T>) Ko.getDao(ko.getClass());
					}
				}
			} else {
				if (ko != null) {
					dao = (DatabaseDAO<T>) Ko.getDao(ko.getClass());
				}
			}
		}
		return dao;
	}
}
