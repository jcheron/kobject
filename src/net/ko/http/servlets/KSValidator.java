package net.ko.http.servlets;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.http.objects.KRequest;
import net.ko.inheritance.KReflectObject;
import net.ko.validation.KValidator;

/**
 * Servlet implementation class KSValidator
 */
@WebServlet(name = "KSValidator", urlPatterns = { "/KSValidator.frm" })
public class KSValidator extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Map<String, Class<? extends KValidator>> classesValidators;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KSValidator() {
		super();
		Ko.setValidatorServlet(this);
		classesValidators = new HashMap<String, Class<? extends KValidator>>();
	}

	@SuppressWarnings("unchecked")
	public KValidator getValidator(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<? extends KValidator> classValidator = classesValidators.get(className);
		KValidator validator = null;
		if (classValidator == null) {
			classValidator = (Class<? extends KValidator>) Class.forName(className);
			classesValidators.put(className, classValidator);
		}
		validator = classValidator.newInstance();
		return validator;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String validatorString = KRequest.GETPOST("_validatorString", request);
		if (validatorString != null) {
			validatorString = validatorString.replace("!", ".");
			String field = KRequest.GETPOST("_field", request);
			String vStrings[] = validatorString.split("#");
			if (vStrings.length == 2) {
				try {
					try {
						KValidator validator = getValidator(vStrings[0]);
						boolean mustDoInit = KRequest.GETPOST("_init", request, false);
						validator.setRequest(request);
						if (mustDoInit)
							validator.setInitialValues();
						else {
							validator.setResponse(response);
							validator.setFieldName(field);
							try {
								KReflectObject.kinvoke(vStrings[1], validator, new Object[] {});
								validator.flush();
							} catch (IllegalArgumentException | InvocationTargetException e) {
								Ko.klogger().log(Level.WARNING, "Impossible d'appeler la méthode " + vStrings[1] + " sur l'instance de la classe " + vStrings[0], e);
							}
						}
					} catch (InstantiationException | IllegalAccessException e) {
						Ko.klogger().log(Level.WARNING, "Impossible de créer une instance de la classe " + vStrings[0], e);
					}
				} catch (ClassNotFoundException e) {
					Ko.klogger().log(Level.WARNING, "Impossible de trouver la classe " + vStrings[0], e);
				} catch (ClassCastException e2) {
					Ko.klogger().log(Level.WARNING, "Impossible de caster la classe " + vStrings[0] + " en KValidator", e2);
				}
			} else {
				Ko.klogger().log(Level.WARNING, "Le validateur du membre " + field + " n'est pas conforme : " + validatorString);
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
