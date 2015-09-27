package net.ko.framework;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;

import net.ko.bean.KClass;
import net.ko.bean.KDbParams;
import net.ko.bean.KListClass;
import net.ko.cache.KCache;
import net.ko.cache.KCacheType;
import net.ko.controller.KMoxClass;
import net.ko.controller.KObjectController;
import net.ko.controller.KTransformer;
import net.ko.controller.KXmlControllers;
import net.ko.converters.DateFormatter;
import net.ko.dao.DaoList;
import net.ko.dao.DatabaseDAO;
import net.ko.dao.IGenericDao;
import net.ko.db.KDBSpec;
import net.ko.db.KDataBase;
import net.ko.db.provider.KDBMysql;
import net.ko.db.validation.KSchemaDDL;
import net.ko.debug.KDebugClient;
import net.ko.displays.KObjectDisplay;
import net.ko.events.KEventListener;
import net.ko.events.KEventType;
import net.ko.events.KFireEvent;
import net.ko.events.KObservable;
import net.ko.filters.EncodingFilter;
import net.ko.http.js.KJsVars;
import net.ko.http.servlets.KSValidator;
import net.ko.http.views.CssVars;
import net.ko.kobject.KObject;
import net.ko.kobject.KSession;
import net.ko.log.HTMLFormatter;
import net.ko.log.KLogger;
import net.ko.mapping.IMappingControl;
import net.ko.mapping.KFilterMappings;
import net.ko.mapping.KMapping;
import net.ko.mapping.KXmlMappings;
import net.ko.persistence.GenericDAOEngine;
import net.ko.persistence.orm.KMetaObject;
import net.ko.thread.KQueryThread;
import net.ko.thread.KQueryThreadParams;
import net.ko.utils.KProperties;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.validation.KValidator;

/**
 * Classe Singleton de lancement d'une application java de type KObject
 * 
 * @author jcheron
 * 
 */
public class Ko {
	/**
	 * Définit la profondeur de parcours des contraintes HasMany, belongsTo et
	 * HasAndBelongsToMany
	 */
	public static int ConstraintsDepth = 1;
	/**
	 * Détermine l'utilisation du cache pour les objets
	 */
	public static boolean useCache = false;
	public static KCacheType cacheType = KCacheType.ktListOnly;
	public static Map<String, String> styleSheets = new HashMap<>();
	public static KObservable observable = new KObservable();
	private static KCache kcache = null;
	protected boolean loaded = false;

	public KXmlControllers getController() {
		return controller;
	}

	private boolean webApp = false;
	protected static Ko instance;
	public static String configFile;
	private String validationFile = "conf/kox.xml";
	private String mappingFile = "conf/mox.xml";
	private String messagesFile = "conf/validation/messages.properties";
	private String erFile = "conf/validation/er.properties";
	private String cssFile = "css/css.properties";
	private KXmlControllers controller;
	private KFilterMappings mappings;
	private List<KMapping> additionalMappings;
	private KFilterMappings filters;
	private KProperties kpMessages;
	private KProperties kpERs;
	private String koDbServer;
	private String koDb;
	private String koDbPort;
	private String koDbUserName;
	private String koDbPassword;
	private String koDbOptions;
	private String koPath;
	private String koDbType;
	private String koPackage;
	private String koNullValue = "_";
	private String koRequestValueSep = ";";
	private String koHeaderURL = "";
	private String koFooterURL = "";
	private boolean koUseSetters = false;
	private boolean useLog = false;
	private String mainControlClassName = "";
	private IMappingControl mainControlInstance = null;
	private KLogger logger;
	protected CssVars cssVars;
	protected String jsValidatorVars = "";
	protected String mainServletMapping = "*.do";
	protected String actionsName = "action.do";
	public static String logFile;
	private static int tmpConstraintsDepth = 0;
	private static KObjectDisplay defaultKoDisplay;
	private static KTransformer transformer;
	private KDBSpec dbSpec;
	private static KoInstances koInstances;
	private GenericDAOEngine activeDaoEngine;
	private GenericDAOEngine mainDAOEngine;
	private GenericDAOEngine memoryDaoEngine;
	public static Map<String, KObjectDisplay> koDisplays;
	public static Map<Class<? extends KObject>, KMetaObject<? extends KObject>> metaObjects;
	private static Map<String, Object> appAttributes;
	protected KSValidator validatorServlet;
	private KSchemaDDL schemaDdl;
	private KQueryThread queriesThread;
	private KQueryThreadParams queriesThreadParams;
	private String memoryDbType;
	private KProperties kp;
	public static final String koLibVersion = "1.0.0.25f";
	public static String koPluginVersion;
	public static String encoding = "UTF-8";
	public static boolean koUseRequestVarsInMox = false;
	public static Class<KMoxClass> moxClass;

	public static void setTempConstraintDeph(int value) {
		tmpConstraintsDepth = ConstraintsDepth;
		ConstraintsDepth = value;
	}

	public static void restoreConstraintDeph() {
		ConstraintsDepth = tmpConstraintsDepth;
	}

	protected static void setInstance(Ko instance) {
		Ko.instance = instance;
	}

	protected Ko() {
		if (configFile == null)
			configFile = "config.ko";
		logger = new KLogger();
		appAttributes = new HashMap<>();
		additionalMappings = new ArrayList<KMapping>();
	}

	public String getKoDbServer() {
		return koDbServer;
	}

	public static String getPath() {
		Ko inst = getInstance();
		if (inst.getKoPath() != null)
			return inst.getKoPath();
		else if (inst instanceof KoHttp)
			return ((KoHttp) getInstance()).getServletContext().getRealPath("/") + "/";
		else
			return System.getProperty("user.dir") + "/";
	}

	public void setKoDbServer(String koDbServer) {
		this.koDbServer = koDbServer;
	}

	public String getKoDb() {
		return koDb;
	}

	public void setKoDb(String koDb) {
		this.koDb = koDb;
	}

	public String getKoDbPort() {
		return koDbPort;
	}

	public void setKoDbPort(String koDbPort) {
		this.koDbPort = koDbPort;
	}

	public String getKoDbUserName() {
		return koDbUserName;
	}

	public void setKoDbUserName(String koDbUserName) {
		this.koDbUserName = koDbUserName;
	}

	public String getKoDbPassword() {
		return koDbPassword;
	}

	public void setKoDbPassword(String koDbPassword) {
		this.koDbPassword = koDbPassword;
	}

	public String getKoDbOptions() {
		return koDbOptions;
	}

	public void setKoDbOptions(String koDbOptions) {
		this.koDbOptions = koDbOptions;
	}

	public String getConfigFile() {
		return configFile;
	}

	public KDataBase getDatabase(boolean connect) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		return mainDAOEngine.kdatabase(connect);
	}

	public KDataBase getDatabase() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		return getDatabase(true);
	}

	public KSession getKsession(Object s) {
		return new KSession();
	}

	/**
	 * @return la Ksession active
	 */
	public static KSession ksession(Object s) {
		return getInstance().getKsession(s);
	}

	/**
	 * @return la connexion à la base de données active
	 */
	public static KDataBase kdatabase() {
		return kdatabase(true);
	}

	public static String kQuoteChar() {
		String quote = "";
		if (getInstance().dbSpec != null)
			quote = instance.dbSpec.getProtectChar();
		return quote;
	}

	public static KDBSpec kDbSpec() {
		return getInstance().dbSpec;
	}

	/**
	 * @return la connexion à la base de données active
	 */
	public static KDataBase kdatabase(boolean connect) {
		KDataBase db = null;
		try {
			db = getInstance().getDatabase(connect);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			klogger().log(Level.SEVERE, "Impossible de se connecter à la base de données", e);
		}
		return db;
	}

	/**
	 * @return l'instance de contrôleur active
	 */
	public static KXmlControllers kcontroller() {
		return getInstance().controller;
	}

	public static String getNullValue() {
		return getInstance().getKoNullValue();
	}

	public static String kpackage() {
		return getInstance().getKoPackage();
	}

	/**
	 * @return l'instance d'application Ko
	 */
	public static Ko getInstance() {
		if (instance == null)
			instance = new Ko();
		return instance;
	}

	public KDataBase connectToDb() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		return kdatabase();
	}

	public void connectToDb(KDataBase kdb) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		KDebugConsole.print("Connection à " + kdb.getURL(), "SQL", "Ko.connectToDb");
		kdb.connect(koDbServer, koDbUserName, koDbPassword, koDb, koDbPort, koDbOptions);
	}

	/**
	 * Définit le fichier de configuration pour l'application config.ko par
	 * défaut
	 * 
	 * @param fileName
	 *            fichier de configuration
	 */
	public void setConfigFile(String fileName) {
		configFile = fileName;
		kp = null;
		try {
			kp = openFile(configFile);
			readProperties(kp);
		} catch (IOException e) {
			klogger().log(Level.SEVERE, "Impossible de lire le fichier de configuration config.ko", e);
		}
	}

	/**
	 * Définit le fichier xml centralisant les contrôles de données pour
	 * l'application<br/>
	 * kox.xml par défaut
	 * 
	 * @param fileName
	 *            fichier xml contrôleur
	 */
	public void setValidationFile(String fileName) {
		boolean fileExists = false;
		validationFile = fileName;
		controller = new KXmlControllers();
		controller.loadFromFile(getPath() + fileName, true);
		fileExists = setMessagesFile(messagesFile);
		fileExists = fileExists && setErFile(erFile);
		controller.setMessages(kpMessages);
		controller.setEr(kpERs);
		if (fileExists)
			loadJsValidatorVars();

	}

	public void setValidationFile() {
		setValidationFile(validationFile);
	}

	/**
	 * Définit le fichier contenant les messages d'erreur en cas de non respect<br/>
	 * des règles de validation des champs/membres définies dans le fichier
	 * contrôleur
	 * 
	 * @param messagesFile
	 *            fichier properties de messages
	 */
	public boolean setMessagesFile(String messagesFile) {
		boolean result = false;
		this.messagesFile = messagesFile;
		kpMessages = new KProperties();
		try {
			kpMessages.loadFromFile(getPath() + messagesFile);
			result = true;
		} catch (IOException e) {
			result = false;
			klogger().log(Level.WARNING, "Impossible de lire le fichier des messages : " + messagesFile, e);
		}
		return result;
	}

	public boolean setMessagesFile() {
		return setMessagesFile(messagesFile);
	}

	/**
	 * Définit le fichier contenant la liste des expressions régulières
	 * permettant de contrôler la validité des champs/membres
	 * 
	 * @param erFile
	 *            fichier properties d'expressions régulières
	 */
	public boolean setErFile(String erFile) {
		boolean result = false;
		this.erFile = erFile;
		kpERs = new KProperties();
		try {
			kpERs.loadFromFile(getPath() + erFile);
			result = true;
		} catch (IOException e) {
			klogger().log(Level.WARNING, "Impossible de lire le fichier des expressions régulières : " + erFile, e);
			result = false;
		}
		return result;
	}

	public boolean setErFile() {
		return setErFile(erFile);
	}

	private void readProperties(KProperties kp) {
		KDebugConsole.setOptions(kp.getProperty("debug", "false"));
		useLog = kp.getProperty("useLog", false);
		koDb = kp.getProperty("base", "mysql");
		logFile = kp.getProperty("logFile", getPath() + "WEB-INF" + File.separator + "log" + File.separator + KString.cleanHTMLAttribute(koDb) + "-App.log");
		if (useLog) {
			String logFormat = kp.getProperty("logFormat", "simple");

			try {
				Formatter format = null;
				switch (logFormat) {
				case "html":
					format = new HTMLFormatter();
					break;

				case "xml":
					format = new XMLFormatter();
					break;

				default:
					format = new SimpleFormatter();
					break;
				}
				logger.setFileHandler(logFile, format);
				logger.setLevel(Level.ALL);

			} catch (SecurityException | IOException e) {
				KDebugConsole.print("Impossible de créer le fichier de log " + logFile, "FRAMEWORK", "Ko.readProperties");
			}
		}
		KDebugConsole.print("db:" + koDb, "CONFIG", "Ko.Kstart");
		koDbServer = kp.getProperty("host", "localhost");
		koDbPort = kp.getProperty("port", "3306");
		koDbUserName = kp.getProperty("user", "root");
		koDbPassword = kp.getProperty("password", "");
		koDbType = kp.getProperty("dbType", "mysql");
		koDbOptions = kp.getProperty("dbOptions", "");
		validationFile = kp.getProperty("validationFile", "");
		erFile = kp.getProperty("erFile", "");
		messagesFile = kp.getProperty("messagesFile", "");
		mappingFile = kp.getProperty("mappingFile", "");
		webApp = kp.getProperty("webApp", true);
		koPackage = kp.getProperty("package", "net.kernel");
		koNullValue = kp.getProperty("nullValue", "_");
		koRequestValueSep = kp.getProperty("request.valuesSeparator", ";");
		koHeaderURL = kp.getProperty("headerURL", "");
		koFooterURL = kp.getProperty("footerURL", "");
		mainControlClassName = kp.getProperty("controlClass", "");
		cssFile = kp.getProperty("cssFile", "");
		mainServletMapping = kp.getProperty("mainServletMapping", "*.do");
		koUseSetters = kp.getProperty("useSetters", false);
		useCache = kp.getProperty("useCache", false);
		cacheType = KCacheType.getCacheTypeByIndex(kp.getProperty("cacheType", "0"));
		ConstraintsDepth = Integer.valueOf(kp.getProperty("constraintsDepth", "1"));
		actionsName = kp.getProperty("actionsName", "action.do");
		schemaDdl = KSchemaDDL.getValue(kp.getProperty("schema.ddl", "validate"));
		memoryDbType = kp.getProperty("memory.dbType");
		String strTransformer = kp.getProperty("transformer", "");
		queriesThreadParams = new KQueryThreadParams(kp.getProperty("queryThread.maxInterval", 120), kp.getProperty("queryThread.minInterval", 60), kp.getProperty("queryThread.queriesCount", 5), kp.getProperty("queryThread.start", false));
		koPluginVersion = kp.getProperty("pluginVersion", "?.?.?.?");
		encoding = kp.getProperty("encoding", "UTF-8");
		EncodingFilter.encoding = encoding;
		setTransformer(strTransformer);
		koUseRequestVarsInMox = kp.getProperty("requestVarsInMox", false);
		if (koUseRequestVarsInMox) {
			String moxClass = kp.getProperty("moxClass");
			if (KString.isNotNull(moxClass)) {
				try {
					this.moxClass = (Class<KMoxClass>) Class.forName(moxClass);
				} catch (ClassNotFoundException cnfe) {
					klogger().log(Level.WARNING, "Classe " + moxClass + " introuvable", cnfe);
				}
			}
		}

		KDebugClient.setOptions(kp.getProperty("clientDebugOptions", "false"));
		KDebugClient.setActive(kp.getProperty("clientDebug", false));
		DateFormatter.koDTFormat = kp.getProperty("koDateTime", DateFormatter.koDTFormat);
		DateFormatter.sqlDateTimeFormat = kp.getProperty("sqlDateTime", DateFormatter.sqlDateTimeFormat);
		if (webApp)
			loadCssVars();
	}

	private KProperties openFile(String fileName) throws IOException {
		KProperties kp = new KProperties();
		kp.loadFromFile(getPath() + fileName);
		return kp;
	}

	/**
	 * Démarre l'application Ko Analyse les paramètres définis dans le fichier
	 * de configuration de l'application
	 */
	public void start() {
		start(false);
	}

	public void stop() {
		if (mainDAOEngine != null) {
			if (webApp) {
				KXmlMappings.stop();
				Ko.styleSheets = new HashMap<>();
				Ko.koDisplays = new HashMap<>();
			}
			mainDAOEngine.removeAll();
			if (memoryDaoEngine != null) {
				memoryDaoEngine.removeAll();
			}

			instance.loaded = false;
			instance.logger.clear();
			KDebugConsole.print("Arrêt de KObject", "FRAMEWORK", "Ko.stop");
		}
		// if(useCache)
		// KCache.shutdown();
	}

	public void restart() {
		stop();
		start();
	}

	/**
	 * Démarre l'application Ko, avec ou sans établir de connexion à la base
	 * 
	 * @param noDb
	 *            si vrai, aucune connexion à la base de données n'est effectuée
	 */
	public void start(boolean noDb) {
		if (!loaded) {
			koDisplays = new HashMap<>();
			koInstances = new KoInstances();
			metaObjects = new HashMap<>();

			setConfigFile(configFile);

			mainDAOEngine = new GenericDAOEngine(koDbType);
			mainDAOEngine.setDbParams(new KDbParams(koDbServer, koDbUserName, koDbPassword, koDb, koDbPort, koDbOptions));
			activeDaoEngine = mainDAOEngine;
			setValidationFile(validationFile);
			if (isWebApp()) {
				setMappingFile(mappingFile);
			}
			KDebugConsole.print("Démarrage de KObject", "FRAMEWORK", "Ko.start");
		}
		mainDAOEngine.init(schemaDdl);
		if (KDataBase.getDbClassInstance(memoryDbType) != null) {
			memoryDaoEngine = new GenericDAOEngine(memoryDbType);
			if (memoryDaoEngine.isValid()) {
				KListClass kclasses;
				try {
					kclasses = KListClass.kload();
					mainDAOEngine.readEntitiesAndInsertTo(kclasses, memoryDaoEngine);
					activeDaoEngine = memoryDaoEngine;
					if (queriesThreadParams.isStart()) {
						startThread();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// if(useCache){
		// KCache.loadAllCache();
		loaded = true;
	}

	private void loadJsValidatorVars() {
		jsValidatorVars = KJsVars.getMessagesAndERs(kpMessages, kpERs);
	}

	/**
	 * Démarre l'application Ko, en créant si nécessaire l'instance
	 * d'application active Ko
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void kstart() {
		getInstance().start();
	}

	public static void kstop() {
		getInstance().stop();
	}

	public static void krestart() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, IOException {
		getInstance().restart();
	}

	public static KFilterMappings kmappings() {
		return getInstance().getMappings();
	}

	public static KFilterMappings kfilters() {
		return getInstance().getFilters();
	}

	public static String kjsValidatorVars() {
		return getInstance().getJsValidatorVars();
	}

	/**
	 * Retourne l'instance de contrôleur associée à la classe passée en
	 * paramétre
	 * 
	 * @param className
	 *            Nom de la classe dérivée de KObject
	 * @return contrôleur associé à la classe
	 */
	public KObjectController getKobjectController(String className) {
		KObjectController result = null;
		if (controller != null)
			result = controller.getObjectController(className);
		return result;
	}

	/**
	 * Retourne la chaîne JSON de contrôle associée à une classe, permettant
	 * d'effectuer les contrôles de validité côté client
	 * 
	 * @param className
	 *            Nom de la classe dérivée de KObject
	 * @return chaîne JSON
	 */
	public String getJSONControllerString(String className) {
		String result = "";
		KObjectController koc = getKobjectController(className);
		if (koc != null)
			result = koc.toJSON();
		return result;
	}

	/**
	 * @return vrai si l'application en cours est une application web
	 */
	public boolean isWebApp() {
		return webApp;
	}

	/**
	 * Active la mise en cache des KObjects avec ehCache
	 */
	public static void cacheActivate() {
		Ko.useCache = true;
		KCache.loadAllCache();
	}

	public static KDataBase kcreateDbInstance() throws InstantiationException, IllegalAccessException {
		return getInstance().createDbInstance();
	}

	@SuppressWarnings("rawtypes")
	public KDataBase createDbInstance() {
		String dbClassName = "net.ko.db.provider.";
		if (koDbType == null || "".equalsIgnoreCase(koDbType))
			koDbType = "mysql";
		String koDbType = this.koDbType.toLowerCase();
		dbClassName += "KDB" + KString.capitalizeFirstLetter(koDbType);

		Class c = null;
		try {
			c = Class.forName(dbClassName);
		} catch (ClassNotFoundException e) {
			c = KDBMysql.class;
		}
		KDataBase db = null;
		try {
			db = (KDataBase) c.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			klogger().log(Level.SEVERE, "Impossible dinstancier la classe du driver : " + dbClassName, e);
		}
		dbSpec = db.getDbSpec();
		return db;
	}

	/**
	 * Désactive la mise en cache des KObjects
	 */
	public static void cacheDeactivate() {
		Ko.useCache = false;
	}

	/**
	 * Arrête la mise en cache
	 */
	public static void cacheShutdown() {
		KCache.shutdown();
	}

	public static void setCache(KCache kcache) {
		Ko.kcache = kcache;
	}

	public static KCache getCache() {
		if (kcache == null)
			kcache = KCache.getInstance();
		return kcache;
	}

	public String getKoPath() {
		return koPath;
	}

	public void setKoPath(String koPath) {
		this.koPath = koPath;
	}

	public String getKoDbType() {
		return koDbType;
	}

	public void setKoDbType(String koDbType) {
		this.koDbType = koDbType;
	}

	public String getKoPackage() {
		return koPackage;
	}

	public void setKoPackage(String koPackage) {
		this.koPackage = koPackage;
	}

	public String getKoNullValue() {
		return koNullValue;
	}

	public void setKoNullValue(String koNullValue) {
		this.koNullValue = koNullValue;
	}

	public String getKoRequestValueSep() {
		return koRequestValueSep;
	}

	public void setKoRequestValueSep(String koRequestValueSep) {
		this.koRequestValueSep = koRequestValueSep;
	}

	public static String krequestValueSep() {
		return getInstance().getKoRequestValueSep();
	}

	public static String kHeaderURL() {
		return getInstance().koHeaderURL;
	}

	public static String kFooterURL() {
		return getInstance().koFooterURL;
	}

	public static KStrings kCssVars() {
		return getInstance().getCssVars();
	}

	public static Object getVariable(Object caller, String varName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Object result = null;
		switch (varName.toLowerCase()) {
		case "ksession":
			// TODO
			// result = ksession();
			break;
		case "kcontroller":
			result = kcontroller();
			break;
		case "kdatabase":
			result = kdatabase();
			break;
		case "this":
			result = caller;
			break;
		default:
			break;
		}
		return result;
	}

	public static String getCompleteClassName(String shortClassName) {
		shortClassName = shortClassName.replaceFirst("^k(.+?)", "$1");
		return kpackage() + ".K" + KString.capitalizeFirstLetter(shortClassName.toLowerCase());
	}

	@SuppressWarnings("unchecked")
	public IMappingControl getMainControlInstance() {
		if (mainControlInstance == null && mainControlClassName != null && !"".equals(mainControlClassName)) {
			try {
				Class<IMappingControl> clazz = (Class<IMappingControl>) Class.forName(mainControlClassName);
				mainControlInstance = clazz.newInstance();
			} catch (Exception e) {
			}
		}
		return mainControlInstance;
	}

	public static IMappingControl kcontrolInstance() {
		return getInstance().getMainControlInstance();
	}

	public static String kactionsName() {
		return getInstance().getActionsName();
	}

	public void setMainControlInstance(IMappingControl mainControlInstance) {
		this.mainControlInstance = mainControlInstance;
	}

	public String getMappingFile() {
		return mappingFile;
	}

	public KXmlMappings setMappingFile(String mappingFile) {
		this.mappingFile = mappingFile;
		additionalMappings = new ArrayList<>();
		KXmlMappings xmlMappings = KXmlMappings.getInstance();
		xmlMappings.loadFromFile(getPath() + mappingFile, true);
		mappings = xmlMappings.getMappings();
		filters = xmlMappings.getFilters();
		for (KMapping mapping : additionalMappings)
			mappings.add(mapping);
		return xmlMappings;
	}

	public KXmlMappings setMappingFile() {
		return setMappingFile(mappingFile);
	}

	public KFilterMappings getMappings() {
		return mappings;
	}

	public KFilterMappings getFilters() {
		return filters;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	protected void loadCssVars() {
		KProperties cssP;
		try {
			cssP = openFile(cssFile);
			cssVars = new CssVars(cssP.getProperties());
		} catch (IOException e) {
			logger.log(Level.WARNING, "Impossible de charger le fichier css : " + cssFile, e);
			cssVars = new CssVars();
		}
		parseCssVars();
	}

	public void loadCssVars(KProperties cssP) {
		Ko.styleSheets = new HashMap<>();
		cssVars = new CssVars(cssP.getProperties());
		parseCssVars();
	}

	public KStrings getCssVars() {
		return cssVars;
	}

	public void setCssVars(CssVars cssVars) {
		this.cssVars = cssVars;
		parseCssVars();
	}

	public String getJsValidatorVars() {
		return jsValidatorVars;
	}

	public void setJsValidatorVars(String jsValidatorVars) {
		this.jsValidatorVars = jsValidatorVars;
	}

	private void parseCssVars() {
		for (String key : cssVars) {
			String value = cssVars.getRealValue(key);
			if (value != null && !value.equals(cssVars.get(key)))
				cssVars.put(key, value);
		}
	}

	public static KObjectDisplay defaultKoDisplay() {
		if (defaultKoDisplay == null)
			defaultKoDisplay = new KObjectDisplay();
		return defaultKoDisplay;
	}

	public static boolean isKoUseSetters() {
		return getInstance().koUseSetters;
	}

	public static void setKoUseSetters(boolean koUseSetters) {
		getInstance().koUseSetters = koUseSetters;
	}

	public static void loadAllEntities() {
		if (useCache) {
			try {
				KListClass kClasses = KListClass.kload();
				for (KClass cls : kClasses) {
					if (cls.getClazz() != null && KObject.class.equals(cls.getClazz().getSuperclass()))
						getDao(cls.getClazz()).readAll();
				}
			} catch (ClassNotFoundException | IOException e) {
			}
		}
	}

	public static void fireEvent(KFireEvent fireEvt) {
		Ko.observable.notifyObservers(fireEvt);
	}

	public static void addListener(Object object, KEventType evtType, KEventListener listener) {
		Ko.observable.addListener(object, evtType, listener);
	}

	public static KLogger klogger() {
		return getInstance().getLogger();
	}

	public KLogger getLogger() {
		return logger;
	}

	public boolean isUseLog() {
		return useLog;
	}

	public void setUseLog(boolean useLog) {
		this.useLog = useLog;
	}

	public static boolean kuseLog() {
		return getInstance().isUseLog();
	}

	@SuppressWarnings("unchecked")
	public void setTransformer(String strTransformer) {
		if (strTransformer != null && !"".equals(strTransformer)) {
			try {
				Class<KTransformer> transformerClass = (Class<KTransformer>) Class.forName(strTransformer);
				transformer = transformerClass.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				transformer = new KTransformer();
			}
		} else
			transformer = new KTransformer();
	}

	public static KTransformer ktransformer() {
		return transformer;
	}

	public String getActionsName() {
		return actionsName;
	}

	public static <T extends KObject> IGenericDao<T> getDao(Class<T> clazz) {
		return getInstance().activeDaoEngine.getDao(clazz);
	}

	public static DaoList getDaoList(Class<? extends KObject> clazz) {
		return getInstance().activeDaoEngine.getDefault(clazz);
	}

	public static <T extends KObject> KDataBase kdefaultDatabase(Class<T> clazz) {
		DatabaseDAO<T> dao = (DatabaseDAO<T>) Ko.getDao(clazz);
		return dao.getDatabase();
	}

	public static <T extends KObject> KDataBase kNewDatabase(Class<T> clazz) {
		DatabaseDAO<T> dao = (DatabaseDAO<T>) Ko.getDao(clazz);
		return dao.getNewDatabase(true);
	}

	public static <T extends KObject> T getKoInstance(Class<T> clazz) {
		return koInstances.getInstance(clazz);
	}

	public static GenericDAOEngine kmemoryDaoEngine() {
		return getInstance().memoryDaoEngine;
	}

	public static GenericDAOEngine kmainDAOEngine() {
		return getInstance().mainDAOEngine;
	}

	public void startThread() {
		if (queriesThread == null) {
			queriesThread = new KQueryThread(Ko.kmemoryDaoEngine());
			queriesThread.setParams(queriesThreadParams);
			queriesThread.start();
		}
	}

	public void stopThread() {
		if (queriesThread != null)
			queriesThread.setSuspended(true);
		queriesThread = null;
	}

	public static void shutDown() {
		Ko inst = getInstance();
		if (inst.queriesThread != null)
			inst.stopThread();
	}

	@SuppressWarnings("unchecked")
	public static <T extends KObject> KMetaObject<T> getMetaObject(Class<T> clazz) {
		KMetaObject<T> result = null;
		if (metaObjects.containsKey(clazz))
			result = (KMetaObject<T>) metaObjects.get(clazz);
		else {
			result = new KMetaObject<>(clazz);
			// try {
			// result.parse();
			metaObjects.put(clazz, result);
			// } catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return result;
	}

	public static List<KMapping> kadditionalMappings() {
		return getInstance().additionalMappings;
	}

	public static KValidator getValidator(String shortClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		KValidator result = null;
		if (getInstance().validatorServlet != null)
			result = getInstance().validatorServlet.getValidator(shortClassName);
		return result;
	}

	public static void setValidatorServlet(KSValidator validatorServlet) {
		Ko.getInstance().validatorServlet = validatorServlet;
	}

	public String getValidationFile() {
		return validationFile;
	}

	public String getCssFile() {
		return cssFile;
	}

	public String getMessagesFile() {
		return messagesFile;
	}

	public String getErFile() {
		return erFile;
	}

	public void setKpMessages(KProperties kpMessages) {
		this.kpMessages = kpMessages;
	}

	public void setKpERs(KProperties kpERs) {
		this.kpERs = kpERs;
	}

	public String getMainControlClassName() {
		return mainControlClassName;
	}

	public KQueryThread getQueriesThread() {
		return queriesThread;
	}

	public String getMemoryDbType() {
		return memoryDbType;
	}

	public static Object getAppAttribute(String name) {
		return appAttributes.get(name);
	}

	public static void setAppAttribute(String name, Object o) {
		appAttributes.put(name, o);
	}

	public static List<String> getAppAttributesNames() {
		return new ArrayList<>(appAttributes.keySet());
	}

	public static KObjectDisplay getKobjectDisplay(String className) {
		KObjectDisplay koDisplay = null;
		if (className != null) {
			koDisplay = Ko.koDisplays.get(className);
			if (koDisplay == null)
				koDisplay = Ko.defaultKoDisplay();
		} else
			koDisplay = Ko.defaultKoDisplay();
		return koDisplay;
	}

	public static String getConfigValue(String key, String defaultValue) {
		return getInstance().kp.getProperty(key, defaultValue);
	}

	public static boolean getConfigValue(String key, boolean defaultValue) {
		return getInstance().kp.getProperty(key, defaultValue);
	}

	public static int getConfigValue(String key, int defaultValue) {
		return getInstance().kp.getProperty(key, defaultValue);
	}

	public static String getConfigValue(String key) {
		return getInstance().kp.getProperty(key);
	}
}
