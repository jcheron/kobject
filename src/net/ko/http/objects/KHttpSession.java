/**
 * Classe KHttpSession
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2011
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KHttpSession.java,v 1.3 2011/01/05 00:59:48 jcheron Exp $
 * @package ko.kobject
 */
package net.ko.http.objects;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.ko.bean.KTester;
import net.ko.controller.KObjectController;
import net.ko.controller.KObjectFieldController;
import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.kobject.KConstraint;
import net.ko.kobject.KConstraintHasAndBelongsToMany;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.kobject.KSession;
import net.ko.utils.KString;

/**
 * Contient une session Http<br/>
 * permet de manipuler cookies, session et application
 * 
 * @author jcheron
 * @version %I%, %G%
 * @since 1.0.0.13-beta1
 */
public class KHttpSession extends KSession {
	/**
	 * HttpServletSession
	 */
	private HttpSession httpSession;

	/**
	 * Instancie un objet KSession (inutile si démarrage de l'application
	 * KObject)
	 * 
	 * @param session
	 */
	public KHttpSession(HttpSession session) {
		super();
		httpSession = session;
	}

	/**
	 * @return la variable session http
	 */
	public HttpSession getHttpSession() {
		return httpSession;
	}

	/**
	 * @param httpSession
	 */
	public void setHttpSession(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	/**
	 * Retourne la variable de session nommée name
	 * 
	 * @param name
	 *            nom de la variable
	 * @return variable de session
	 */
	public Object sessionGet(String name) {
		return httpSession.getAttribute(name);
	}

	/**
	 * @param name
	 * @param dValue
	 * @return
	 */
	public String sessionGet(String name, String dValue) {
		String result = httpSession.getAttribute(name) + "";
		if (httpSession.getAttribute(name) == null)
			result = dValue;
		return result;
	}

	/**
	 * @param name
	 * @param dValue
	 * @return
	 */
	public boolean sessionGet(String name, boolean dValue) {
		boolean result = dValue;
		if (KString.isBoolean(httpSession.getAttribute(name) + ""))
			result = KString.isBooleanTrue(httpSession.getAttribute(name) + "");
		return result;
	}

	/**
	 * @param name
	 * @param dValue
	 * @return
	 */
	public int sessionGet(String name, int dValue) {
		int result = dValue;
		try {
			result = Integer.valueOf(httpSession.getAttribute(name) + "");
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * Ajoute une variable de session
	 * 
	 * @param name
	 *            nom de la variable à créer
	 * @param obj
	 *            variable à stocker
	 */
	public void sessionAdd(String name, Object obj) {
		httpSession.setAttribute(name, obj);
	}

	/**
	 * Supprime une variable de session
	 * 
	 * @param name
	 *            nom de la variable à supprimer
	 */
	public void sessionDelete(String name) {
		httpSession.removeAttribute(name);
	}

	/**
	 * Termine la session Http et supprime les variables de session
	 */
	public void sessionInvalidate() {
		httpSession.invalidate();
	}

	/**
	 * @return Une map des variables de session
	 */
	public Map<String, Object> sessionVariables() {
		Map<String, Object> result = new HashMap<String, Object>();
		Enumeration<String> names = httpSession.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			result.put(name, httpSession.getAttribute(name));
		}
		return result;
	}

	/**
	 * retourne la liste des noms des variables de session dans une énumération
	 * 
	 * @return énumération des noms de variables de session
	 */
	public Enumeration<String> sessionNames() {
		return httpSession.getAttributeNames();
	}

	/**
	 * @return l'identifiant de session
	 */
	public String sessionId() {
		return httpSession.getId();
	}

	/**
	 * Ajoute dans un cookie nommé name le toString de obj
	 * 
	 * @param name
	 *            nom du cookie
	 * @param obj
	 *            valeur à stocker
	 * @param response
	 *            objet response
	 */
	public void cookieAdd(String name, Object obj, HttpServletResponse response) {
		Cookie co = new Cookie(name, obj.toString());
		response.addCookie(co);
	}

	/**
	 * Retourne la valeur contenue dans le cookie nommé name
	 * 
	 * @param name
	 *            nom du cookie
	 * @param request
	 *            requête http
	 * @return le texte du cookie correspondant
	 */
	public String cookieGet(String name, HttpServletRequest request) {
		boolean trouve = false;
		String result = null;
		int i = 0;
		Cookie[] cookies = request.getCookies();
		while (!trouve && i < cookies.length) {
			trouve = cookies[i].getName().equals(name);
			i++;
		}
		if (trouve) {
			result = cookies[i - 1].getValue();
		}
		return result;
	}

	/**
	 * Retourne le tableau des cookies
	 * 
	 * @param request
	 *            requête http
	 * @return tableau des cookies
	 */
	public Cookie[] cookieGetAll(HttpServletRequest request) {
		return request.getCookies();
	}

	/**
	 * retourne une map de toutes les variables contenues dans les cookies
	 * 
	 * @param request
	 *            requête http
	 * @return map des couples clé et valeur des cookies
	 */
	public Map<String, String> cookieVariables(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		Map<String, String> result = new HashMap<String, String>();
		for (Cookie co : cookies)
			result.put(co.getName(), co.getValue());
		return result;
	}

	/**
	 * Supprime un cookie par son nom
	 * 
	 * @param name
	 *            nom du cookie à supprimer
	 * @param response
	 *            objet response envoyée au client
	 * @param request
	 *            requête http
	 * @return vrai si le cookie a été supprimé
	 */
	public boolean cookieDelete(String name, HttpServletResponse response, HttpServletRequest request) {
		boolean trouve = false;
		int i = 0;
		Cookie[] cookies = request.getCookies();
		while (!trouve && i < cookies.length) {
			trouve = cookies[i].getName() == name;
			i++;
		}
		if (trouve) {
			cookies[i - 1].setMaxAge(0);
			response.addCookie(cookies[i - 1]);
		}
		return trouve;
	}

	/**
	 * Supprime tous les cookies
	 * 
	 * @param response
	 *            objet response envoyé au client
	 * @param request
	 *            requête http
	 */
	public void cookieDeleteAll(HttpServletResponse response, HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		for (Cookie co : cookies) {
			co.setMaxAge(0);
			response.addCookie(co);
		}
	}

	public void setQueryString(KObject o, String queryString) {
		String[] namesAndValues = queryString.split("\\&");
		for (int i = 0; i < namesAndValues.length; i++) {
			String[] namesVal = namesAndValues[i].split("=");
			if (namesVal.length > 1) {
				try {
					o.setAttribute(namesVal[0], namesVal[1], false);
				} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
				}
			}
		}
	}

	/**
	 * Affecte les paramètres de la requête à un objet de type KObject
	 * (affectation des paramètres aux membres de même nom)
	 * 
	 * @param o
	 *            objet instance d'une classe dérivée de KObject à valoriser
	 * @param request
	 *            requête http
	 */
	@SuppressWarnings("rawtypes")
	public void setRequestParametersTo(KObject o, HttpServletRequest request, boolean beforeSubmit) {
		KObjectController koc = o.getController();
		Enumeration<String> params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String param = params.nextElement();
			String value = request.getParameter(param);
			if ("_keyValues".equals(param)) {
				if (KString.isNotNull(value)) {
					List<Object> keyValues = new ArrayList<Object>(Arrays.asList(value.split(",")));
					try {
						o.setKeyValues(keyValues);
					} catch (SecurityException | NoSuchFieldException | IllegalAccessException e) {
						Ko.klogger().log(Level.WARNING, "Paramètre _keyValues incompatible dans setRequestParametersTo : " + value, e);
					}
				}
			} else if (!param.startsWith("_") || o instanceof KTester) {
				try {
					if (koc == null)
						o.setAttribute(param, value, false);
					else {
						KObjectFieldController kofc = koc.getFieldController(param);
						if (kofc != null) {
							value = kofc.getValue(request);
							if ((value == null && !kofc.isRequired() || kofc.isAllowNull()) || value != null) {
								if (!kofc.isMultiple())
									o.setAttribute(param, value, false);
								else {
									if (!beforeSubmit) {
										KConstraint kc = o.getConstraints().getConstraint(param, KConstraintHasAndBelongsToMany.class);
										if (kc != null) {
											((KListObject) o.getAttribute(kc.getMember())).markForHasAndBelongsToMany(value, ";");
										} else {
											o.setAttribute(param, value, false);
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					if (e instanceof NoSuchFieldException)
						KDebugConsole.print(param + " envoyé non présent dans les membres de l'objet", "REQUEST", "KHttpSession.setRequestParametersTo");
					else
						Ko.klogger().log(Level.WARNING, "Paramètre posté incompatible dans setRequestParametersTo : " + param, e);
				}
			}
		}
	}

	/**
	 * Ajoute dans la requête un attribut pour chacun des membres de l'objet de
	 * type KObject
	 * 
	 * @param o
	 *            objet instance d'une classe dérivée de KObject à valoriser
	 * @param request
	 *            requête http
	 */
	public void toRequestAttributes(KObject o, HttpServletRequest request) {
		o.refresh();
		for (Map.Entry<String, Object> entry : o.getAttributes().entrySet())
			request.setAttribute(entry.getKey(), entry.getValue());
	}
}
