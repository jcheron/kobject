package net.ko.listeners;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.persistence.GenericDAOEngine;

/**
 * Application Lifecycle Listener implementation class KApplicationStop
 * 
 */
public class KoApplicationListener implements ServletContextListener {

	/**
	 * Default constructor.
	 */
	public KoApplicationListener() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent contextEvent) {
		String configFile = contextEvent.getServletContext().getInitParameter("configFile");
		if (configFile != null)
			KoHttp.configFile = configFile;
		KoHttp.kstart(contextEvent.getServletContext());
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		KoHttp.shutDown();
		GenericDAOEngine engine = Ko.kmemoryDaoEngine();
		if (engine != null) {
			engine.saveAllDataTo(Ko.kmainDAOEngine());
		}
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				Ko.klogger().log(Level.INFO, String.format("Désenregistrement du driver JDBC %s", driver));
			} catch (SQLException e) {
				Ko.klogger().log(Level.SEVERE, String.format("Impossible de désenregistrer le driver %s", driver), e);
			}

		}
	}

}
