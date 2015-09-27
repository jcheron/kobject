package net.ko.check;

import javax.servlet.http.HttpServletRequest;

import net.ko.db.KDataBase;
import net.ko.framework.Ko;
import net.ko.mapping.IMappingControl;
import net.ko.thread.KQueryThread;
import net.ko.utils.KString;

public class KCheckConfig extends KAbstractCheck {

	public KCheckConfig(HttpServletRequest request) {
		super(request);
	}

	@Override
	public void checkAll() {
		String configFile = Ko.configFile;
		KCheckMessageWithChilds cmConfig = messageList.addMessageWithChild(KCheckMessageType.TITLE, "Fichier de configuration : " + configFile);
		checkFile(configFile, "Fichier : ", cmConfig);
		validation(cmConfig);
		control(cmConfig);
		views(cmConfig);
		Db(cmConfig);
		memoryDb(cmConfig);
		cmConfig.setVisible(expand, true);
	}

	protected void control(KCheckMessageWithChilds messageParent) {
		KCheckMessageWithChilds cmControl = messageParent.addMessageWithChild(KCheckMessageType.TITLE, "Contrôleur");
		checkFile(Ko.getInstance().getMappingFile(), "Fichier contrôleur : ", cmControl);
		String mainControlClassName = Ko.getInstance().getMainControlClassName();
		if (KString.isNotNull(mainControlClassName)) {
			IMappingControl mainControlInstance = Ko.getInstance().getMainControlInstance();
			if (mainControlInstance != null)
				cmControl.addInfoMessage("mainControl : " + mainControlClassName);
			else
				cmControl.addMessage(KCheckMessageType.ERROR, "mainControl : " + mainControlClassName + " non instanciée");
		}
	}

	protected void validation(KCheckMessageWithChilds messageParent) {
		KCheckMessageWithChilds cmValidation = messageParent.addMessageWithChild(KCheckMessageType.TITLE, "Validation & classes");

		checkFile(Ko.getInstance().getValidationFile(), "Fichier de validation : ", cmValidation);
		checkFile(Ko.getInstance().getMessagesFile(), "Fichier de messages : ", cmValidation);
		checkFile(Ko.getInstance().getErFile(), "Fichier expressions régulières : ", cmValidation);
		Package pk = Package.getPackage(Ko.kpackage());

		if (null != pk) {
			cmValidation.addInfoMessage("Package métier : " + Ko.kpackage() + " Ok");
		} else {
			cmValidation.addMessage(KCheckMessageType.ERROR, "Package métier : " + Ko.kpackage() + " inexistant");
		}
	}

	protected void views(KCheckMessageWithChilds messageParent) {
		KCheckMessageWithChilds cmGUI = messageParent.addMessageWithChild(KCheckMessageType.TITLE, "GUI");
		checkFile(Ko.kHeaderURL(), "Fichier header : ", cmGUI);
		checkFile(Ko.kFooterURL(), "Fichier footer : ", cmGUI);
		checkFile(Ko.getInstance().getCssFile(), "Fichier properties CSS : ", cmGUI);
	}

	protected void memoryDb(KCheckMessageWithChilds messageParent) {
		String memDbType = Ko.getInstance().getMemoryDbType();
		if (KString.isNotNull(memDbType)) {
			KCheckMessageWithChilds cmMemoryDb = messageParent.addMessageWithChild(KCheckMessageType.TITLE, "Database cache");
			KCheckMessageWithChilds cmMemType = cmMemoryDb.addMessageWithChild(KCheckMessageType.INFO, "memory.dbType : " + memDbType);
			try {
				Class<?> cls = Class.forName("net.ko.db.provider.KDB" + KString.capitalizeFirstLetter(memDbType));
				cmMemType.addInfoMessage("Classe : " + cls.getName());
			} catch (ClassNotFoundException e) {
				cmMemType.addMessage(KCheckMessageType.ERROR, e.getMessage());
			}
			if (Ko.kmemoryDaoEngine() != null) {
				cmMemoryDb.addInfoMessage("Memory db instance : " + Ko.kmemoryDaoEngine().getClass().getName());
				KCheckMessageWithChilds cmThread = cmMemoryDb.addMessageWithChild(KCheckMessageType.TITLE, "Query thread");
				KQueryThread qt = Ko.getInstance().getQueriesThread();
				if (qt != null) {
					cmThread.addInfoMessage("State : " + qt.getState().toString());
					cmThread.addInfoMessage("Interval : " + qt.getInterval() + " ms");
					cmThread.addInfoMessage("Min interval : " + qt.getMinInterval() + " ms");
					cmThread.addInfoMessage("Max interval : " + qt.getMaxInterval() + " ms");
				} else {
					cmThread.addMessage(KCheckMessageType.WARNING, "Thread non actif, les requêtes seront exécutées à la fermeture de l'application");
				}
				cmThread.addInfoMessage("Requêtes en attente : " + Ko.kmemoryDaoEngine().getQueries().size());
			} else
				cmMemoryDb.addMessage(KCheckMessageType.ERROR, "Memory db non instanciée");
		}
	}

	protected void Db(KCheckMessageWithChilds messageParent) {
		KCheckMessageWithChilds cmDb = messageParent.addMessageWithChild(KCheckMessageType.TITLE, "Database infos");
		KCheckMessageWithChilds cmDbType = cmDb.addMessageWithChild(KCheckMessageType.INFO, "Db type : " + Ko.getInstance().getKoDbType());
		try {
			Class<?> cls = Class.forName("net.ko.db.provider.KDB" + KString.capitalizeFirstLetter(Ko.getInstance().getKoDbType()));
			cmDbType.addInfoMessage("Classe : " + cls.getName());
			KDataBase db = Ko.kdatabase();
			if (db != null) {
				try {
					cmDbType.addInfoMessage("Connection class name : " + Ko.kdatabase().getConnection().getClass().getName());
				} catch (Exception e) {
					cmDbType.addMessage(KCheckMessageType.ERROR, "Connexion impossible à la base de données");
				}
			}
			else
				cmDbType.addMessage(KCheckMessageType.ERROR, "Impossible d'obtenir une instance de connexion à la base");
		} catch (ClassNotFoundException e) {
			cmDbType.addMessage(KCheckMessageType.ERROR, e.getMessage());
		}
		cmDb.addInfoMessage("Db name : " + Ko.getInstance().getKoDb());
		cmDb.addInfoMessage("Db port : " + Ko.getInstance().getKoDbPort());
		cmDb.addInfoMessage("Db user : " + Ko.getInstance().getKoDbUserName());
		cmDb.addInfoMessage("Db password : " + KString.repeat("*", Ko.getInstance().getKoDbPassword().length()));
		cmDb.addInfoMessage("Db options : " + Ko.getInstance().getKoDbOptions());

	}

	@Override
	public boolean isActive() {
		return options.contains(KCheckOptions.coConfig);
	}

}
