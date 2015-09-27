package net.ko.check;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.ko.bean.KClass;
import net.ko.bean.KListClass;
import net.ko.controller.KObjectController;
import net.ko.controller.KObjectFieldController;
import net.ko.controller.KTransformer;
import net.ko.controller.KXmlControllers;
import net.ko.db.validation.KTableValidator;
import net.ko.displays.KObjectDisplay;
import net.ko.framework.Ko;
import net.ko.kobject.KConstraint;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.persistence.orm.KMetaField;
import net.ko.persistence.orm.KMetaObject;
import net.ko.utils.KReflection;
import net.ko.utils.KString;
import net.ko.validation.KValidator;

public class KCheckKox extends KAbstractCheck {
	private KXmlControllers xmlControllers;
	private String validationFile;

	public KCheckKox(HttpServletRequest request) {
		super(request);
		xmlControllers = Ko.kcontroller();
		validationFile = Ko.getInstance().getValidationFile();
	}

	protected void checkAllClass() {
		KListClass kclasses;
		try {
			KCheckMessageWithChilds cmAllClasses = null;
			KCheckMessageWithChilds cmDb = null;
			if (options.contains(KCheckOptions.coDb)) {
				cmDb = messageList.addMessageWithChild(KCheckMessageType.TITLE, "Vérification de la base de données " + Ko.getInstance().getKoDb());
				cmDb.setVisible(expand);
			}
			if (options.contains(KCheckOptions.coClasses)) {
				cmAllClasses = messageList.addMessageWithChild(KCheckMessageType.TITLE, "Vérification des classes");
				cmAllClasses.setVisible(expand);
			}
			kclasses = KListClass.kload();
			kclasses.sort();
			for (KClass kclass : kclasses.getItems()) {
				checkOneClass(kclass, cmAllClasses, cmDb);
			}
			if (cmDb != null)
				cmDb.setVisible(expand, true);
			if (cmAllClasses != null)
				cmAllClasses.setVisible(expand, true);
		} catch (ClassNotFoundException | IOException | InstantiationException | IllegalAccessException e1) {
			messageList.addMessage(KCheckMessageType.ERROR, e1.toString());
		}
	}

	protected void checkOneClass(KClass kclass, KCheckMessageWithChilds cmAllClasses, KCheckMessageWithChilds cmDb) throws InstantiationException, IllegalAccessException {
		KObjectController koc = xmlControllers.getObjectController(kclass.getSimpleName());
		Class<? extends KObject> clazz = kclass.getClazz();
		KObject ko = clazz.newInstance();
		KMetaObject<? extends KObject> metaObject = ko.getMetaObject();
		KCheckMessageWithChilds cmOneClasse = null;
		KCheckMessageWithChilds cmOneTable = null;
		KCheckMessageWithChilds cmFields = null;
		KCheckMessageWithChilds cmRelations = null;
		KCheckMessageWithChilds cmMembers = null;
		if (cmAllClasses != null)
			cmOneClasse = cmAllClasses.addMessageWithChild(KCheckMessageType.TITLE, "Classe : " + clazz.getName());
		if (cmDb != null) {
			cmOneTable = cmDb.addMessageWithChild(KCheckMessageType.TITLE, "Table : " + kclass.getTableName());
			cmFields = cmOneTable.addMessageWithChild(KCheckMessageType.TITLE, "Champs");
			cmFields.setId(kclass.getTableName());
		}
		KTableValidator tblValidator = null;
		if (cmOneTable != null)
			tblValidator = checkTable(kclass, cmOneTable);
		if (cmOneClasse != null) {
			if (!koc.isXmlExists()) {
				cmOneClasse.addMessage(KCheckMessageType.ERROR, "Impossible de trouver une entrée dans le fichier " + validationFile + " pour la classe " + kclass.getSimpleName());
			}
			if (metaObject.isEntity())
				cmOneClasse.addInfoMessage("Entité : oui");
			else
				cmOneClasse.addMessage(KCheckMessageType.WARNING, "Entité : non");
			cmOneClasse.addInfoMessage("Table : " + metaObject.getTableName());
			cmOneClasse.addInfoMessage("Key fields : " + metaObject.getKeyFields().toString());
			cmRelations = cmOneClasse.addMessageWithChild(KCheckMessageType.TITLE, "Relations sur " + kclass.getSimpleName());
			for (KConstraint constraint : ko.getConstraints().getConstraints()) {
				cmRelations.addInfoMessage(constraint.display());
			}
		}

		if (cmOneClasse != null) {
			cmMembers = cmOneClasse.addMessageWithChild(KCheckMessageType.TITLE, "Membres de " + kclass.getSimpleName());
			cmMembers.setId(kclass.getSimpleName());
		}
		for (Map.Entry<String, KMetaField> e : metaObject.getFields().entrySet()) {
			String fieldName = e.getValue().getFieldName();
			if (cmMembers != null) {
				checkOneClassMember(e.getValue(), cmMembers, ko);
			}
			if (cmFields != null) {
				if (tblValidator.getFieldsToRepare().containsKey(fieldName)) {
					KTableValidator.RepareOperation ro = tblValidator.getFieldsToRepare().get(fieldName);
					switch (ro) {
					case roAdd:
						cmFields.addMessage(KCheckMessageType.ERROR, fieldName + " absent");
						break;
					case roModify:
						cmFields.addMessage(KCheckMessageType.WARNING, e.getValue().getFieldType() + fieldName + " : type incompatible (db/java)");
						break;
					default:
						cmFields.addInfoMessage(e.getValue().getFieldType() + " " + fieldName + " : Ok");
						break;
					}
				} else
					cmFields.addInfoMessage(e.getValue().getFieldType() + " " + fieldName + " : Ok");
			}
			if (cmRelations != null) {
				Class<?> type = e.getValue().getField().getType();
				if (KListObject.class.isAssignableFrom(type) || KObject.class.isAssignableFrom(type)) {
					if (!ko.getConstraints().constraintExists(e.getValue().getMemberName()))
						cmRelations.addMessage(KCheckMessageType.WARNING, "Aucune relation sur le membre " + e.getValue().getMemberName());
				}
			}
		}
	}

	protected KTableValidator checkTable(KClass kclass, KCheckMessageWithChilds cmOneTable) {
		KTableValidator tblValidator = new KTableValidator(kclass.getClazz());
		try {
			tblValidator.setDb(Ko.kdatabase());
			if (!tblValidator.checkTableExist()) {
				cmOneTable.addMessage(KCheckMessageType.ERROR, "La table " + kclass.getTableName() + " n'existe pas");
			} else
				cmOneTable.addInfoMessage("Table " + kclass.getTableName() + " Ok");
			tblValidator.checkTableFields();
		} catch (Exception e) {
			cmOneTable.addMessage(KCheckMessageType.ERROR, "Table " + kclass.getTableName() + " : " + e.getMessage());
		}
		return tblValidator;
	}

	protected KCheckMessageWithChilds checkOneClassMember(KMetaField field, KCheckMessageWithChilds cmMembers, KObject ko) {
		KCheckMessageWithChilds cmOneField = cmMembers.addMessageWithChild(KCheckMessageType.TITLE, field.getMemberName());
		cmOneField.setId(cmMembers.getId());
		cmOneField.addInfoMessage("field name : " + field.getFieldName());
		cmOneField.addInfoMessage("java type : " + field.getField().toGenericString());
		cmOneField.addInfoMessage("Serializable : " + KObject.isSerializable(ko.getClass(), field.getFieldName()));
		cmOneField.addInfoMessage("Nullable : " + field.isNullable());
		if (field.isNullable()) {
			try {
				ko.setAttribute(field.getFieldName(), null, false);
			} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
				cmOneField.addMessage(KCheckMessageType.WARNING, "Impossible d'affecter null à " + field.getFieldName());
			}
		}
		return cmOneField;
	}

	protected void checkAllControllers() {
		KCheckMessageWithChilds cmControllers = messageList.addMessageWithChild(KCheckMessageType.TITLE, "Vérification des entrées dans le fichier " + validationFile);
		checkFile(validationFile, "Fichier validation : ", cmControllers);
		cmControllers.setVisible(expand);
		for (Map.Entry<String, KObjectController> e : xmlControllers.getKobjectControllers().entrySet()) {
			if (e.getValue().isXmlExists())
				checkController(e.getValue(), cmControllers);
		}
		cmControllers.setVisible(expand, true);
	}

	protected void checkController(KObjectController koController, KCheckMessageWithChilds cmControllers) {
		KCheckMessageWithChilds cmController = cmControllers.addMessageWithChild(KCheckMessageType.TITLE, "Classe " + koController.getClassName());
		cmController.setId(koController.getClassName() + "controller");
		addInfo(cmController, "Caption : ", koController.getCaption());
		String display = koController.getDisplay();
		if (KString.isNotNull(display)) {
			if (!KObjectDisplay.class.equals(koController.getDisplayInstance().getClass()))
				cmController.addInfoMessage("Display : " + display);
			else
				cmController.addMessage(KCheckMessageType.ERROR, "Display : " + display + " introuvable");
		}
		String transformer = koController.getStrTransformer();
		if (KString.isNotNull(transformer)) {
			if (!KTransformer.class.equals(koController.getTransformer().getClass()))
				cmController.addInfoMessage("Transformer : " + transformer);
			else
				cmController.addMessage(KCheckMessageType.ERROR, "Transformer : " + transformer + " introuvable");
		}
		Object validator = null;
		String strValidator = koController.getValidatorClassName();
		if (KString.isNotNull(strValidator)) {
			Class<?> classValidator = null;
			try {
				classValidator = Class.forName(strValidator);
				cmController.addInfoMessage("Validator : classe " + strValidator);
			} catch (ClassNotFoundException e1) {
				cmController.addMessage(KCheckMessageType.ERROR, "Validator : classe " + strValidator + " introuvable");
			}
			if (classValidator != null) {
				if (!KValidator.class.isAssignableFrom(classValidator))
					cmController.addMessage(KCheckMessageType.ERROR, "Validator : " + classValidator + " doit dériver de KValidator");
				try {
					validator = classValidator.newInstance();
				} catch (InstantiationException | IllegalAccessException e1) {
					cmController.addMessage(KCheckMessageType.ERROR, "Validator : impossible de créer une instance de " + classValidator);
				}
			}

		}
		for (Map.Entry<String, KObjectFieldController> e : koController.getMembers().entrySet()) {
			KCheckMessageWithChilds cmFieldController = cmController.addMessageWithChild(KCheckMessageType.TITLE, "Membre " + e.getValue().getName());
			cmFieldController.setId(e.getKey() + koController.getClassName() + "controller");
			checkFieldController(e.getValue(), cmFieldController, validator);
		}
	}

	protected void checkFieldController(KObjectFieldController koFieldController, KCheckMessageWithChilds cmFieldController, Object validator) {
		addInfo(cmFieldController, "Caption : ", koFieldController.getCaption());
		addInfo(cmFieldController, "Control : ", koFieldController.getControl());
		addInfo(cmFieldController, "Type : ", koFieldController.getType());
		addInfo(cmFieldController, "Required : ", koFieldController.isRequired());
		addInfo(cmFieldController, "Nullable : ", koFieldController.isAllowNull());
		addInfo(cmFieldController, "Multiple : ", koFieldController.isMultiple());
		addInfo(cmFieldController, "List : ", koFieldController.getList());
		addInfo(cmFieldController, "Options : ", koFieldController.getOptions());

		if (validator != null && KString.isNotNull(koFieldController.getValidatorClassName()) && KString.isNotNull(koFieldController.getValidateMethodName())) {
			try {
				KReflection.invoke(koFieldController.getValidateMethodName(), validator);
				addInfo(cmFieldController, "Validate : ", koFieldController.getValidateMethodName() + "()");
			} catch (IllegalArgumentException e) {
				cmFieldController.addMessage(KCheckMessageType.ERROR, "Validate : méthode " + koFieldController.getValidateMethodName() + "() introuvable");
			}
		}
		if (koFieldController.getMax() != -1)
			addInfo(cmFieldController, "Max : ", koFieldController.getMax() + "");
		if (koFieldController.getMin() != -1)
			addInfo(cmFieldController, "Min : ", koFieldController.getMin() + "");
	}

	@Override
	public void checkAll() {
		if (options.contains(KCheckOptions.coClasses) || options.contains(KCheckOptions.coDb))
			checkAllClass();
		if (options.contains(KCheckOptions.coControllers))
			checkAllControllers();
	}

	@Override
	public boolean isActive() {
		return options.contains(KCheckOptions.coClasses) || options.contains(KCheckOptions.coDb) || options.contains(KCheckOptions.coControllers);
	}

}
