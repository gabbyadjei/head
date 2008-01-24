package org.mifos.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.sql.SQLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.mifos.application.accounts.financial.util.helpers.FinancialInitializer;
import org.mifos.application.configuration.business.MifosConfiguration;
import org.mifos.config.ClientRules;
import org.mifos.config.Localization;
import org.mifos.config.ProcessFlowRules;
import org.mifos.framework.components.audit.util.helpers.AuditConfigurtion;
import org.mifos.framework.components.batchjobs.MifosScheduler;
import org.mifos.framework.components.configuration.business.Configuration;
import org.mifos.framework.components.logger.LoggerConstants;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.exceptions.AppNotConfiguredException;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.HibernateProcessException;
import org.mifos.framework.exceptions.HibernateStartUpException;
import org.mifos.framework.exceptions.LoggerConfigurationException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.exceptions.XMLReaderException;
import org.mifos.framework.hibernate.HibernateStartUp;
import org.mifos.framework.hibernate.helper.HibernateUtil;
import org.mifos.framework.persistence.DatabaseVersionPersistence;
import org.mifos.framework.security.authorization.AuthorizationManager;
import org.mifos.framework.security.authorization.HierarchyManager;
import org.mifos.framework.struts.plugin.helper.EntityMasterData;
import org.mifos.framework.struts.tags.XmlBuilder;
import org.mifos.framework.util.helpers.FilePaths;
import org.mifos.framework.util.helpers.ResourceLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Locale;

/**
 * This class should prepare all the sub-systems that are required by the app.
 * Cleanup should also happen here, when the application is shutdown. 
 * */
public class ApplicationInitializer implements ServletContextListener, ServletRequestListener {

	private static Throwable databaseVersionError;

	public void contextInitialized(ServletContextEvent ctx) {
		init();
	}
	
	public void init() {
		try{
			synchronized(ApplicationInitializer.class){
				initializeLogger();
				initializeHibernate();
				MifosLogManager.getLogger(LoggerConstants.FRAMEWORKLOGGER).info(
						"Logger has been initialised", false, null);
				try{
					syncDatabaseVersion();
				}catch(Throwable t){
					databaseVersionError = t;
					MifosLogManager.getLogger(LoggerConstants.FRAMEWORKLOGGER).fatal(
							"Failed to upgrade/downgrade database version", false, null, t);
				}
				if(databaseVersionError==null){
					// this method is called so that supported locales will be loaded
					// from db and stored in cache for later use
					Localization.getInstance().init(); 
					// Check ClientRules configuration in db and config file(s)
					// for errors. Also caches ClientRules values.
					ClientRules.init();
					// Check ProcessFlowRules configuration in db and config
					// file(s) for errors.
					ProcessFlowRules.init();
					initializeSecurity();

					FinancialInitializer.initialize();
					EntityMasterData.getInstance().init();
					initializeEntityMaster();
					(new MifosScheduler()).registerTasks();
					
					// 1/4/08 Hopefully a temporary change to force Spring
					// to initialize here (rather than in struts-config.xml
					// prior to loading label values into a 
					// cache in MifosConfiguration.  When the use of the 
					// cache is refactored away, we should be able to move
					// back to the struts based Spring initialization
					initializeSpring();
					
					Configuration.getInstance();
					MifosConfiguration.getInstance().init();
					configureAuditLogValues(Localization.getInstance().getMainLocale());
					
				}
			}
		}catch(Exception e){
			throw new Error(e);
		}
	}
	
	public static void printDatabaseError(XmlBuilder xml) {
		synchronized(ApplicationInitializer.class){
	        if(databaseVersionError != null) {
	        	xml.startTag("p");
	        	xml.text("Here are details of what went wrong:");
	        	xml.endTag("p");

	        	xml.startTag("pre");
	        	StringWriter stackTrace = new StringWriter();
				databaseVersionError.printStackTrace(new PrintWriter(stackTrace));
				xml.text("\n" + stackTrace.toString());
	        	xml.endTag("pre");
			}
	        else {
	        	xml.startTag("p");
	        	xml.text("I don't have any further details, unfortunately.");
	        	xml.endTag("p");
	        }
	    }
	}
	
	public static void setDatabaseVersionError(Throwable error) {
		databaseVersionError = error;
	}
	
	/**
	 * Initializes Hibernate by making it read the hibernate.cfg file and also
	 * setting the same with hibernate session factory.
	 */
	public static void initializeHibernate()
			throws AppNotConfiguredException {
		try {
			String hibernatePropertiesPath = FilePaths.HIBERNATE_PROPERTIES;
			try {
				if (ResourceLoader
						.getURI(FilePaths.CONFIGURABLEMIFOSDBPROPERTIESFILE) != null)
					hibernatePropertiesPath = FilePaths.CONFIGURABLEMIFOSDBPROPERTIESFILE;
			} catch (URISyntaxException e) {
				throw new AppNotConfiguredException(e);
			}
			HibernateStartUp.initialize(hibernatePropertiesPath);
		} catch (HibernateStartUpException e) {
			throw new AppNotConfiguredException(e);
		}
	}

	/**
	 * Initializes the logger using loggerconfiguration.xml
	 * 
	 * @throws AppNotConfiguredException -
	 *             IF there is any exception while configuring the logger
	 */
	private void initializeLogger() throws AppNotConfiguredException {
		try {
			MifosLogManager.configure(FilePaths.LOGFILE);
		} catch (LoggerConfigurationException lce) {

			lce.printStackTrace();
			throw new AppNotConfiguredException(lce);
		}

	}

	/**
	 * This function initialize and bring up the authorization and
	 * authentication services
	 * 
	 * @throws AppNotConfiguredException -
	 *             IF there is any failures during init
	 */
	private void initializeSecurity() throws AppNotConfiguredException {
		try {

			AuthorizationManager.getInstance().init();

			HierarchyManager.getInstance().init();

		} catch (XMLReaderException e) {

			throw new AppNotConfiguredException(e);
		} catch (ApplicationException ae) {
			throw new AppNotConfiguredException(ae);
		} catch (SystemException se) {
			throw new AppNotConfiguredException(se);
		}

	}

	private void initializeEntityMaster() throws HibernateProcessException {
		EntityMasterData.getInstance().init();
	}
	
	private void syncDatabaseVersion() throws SQLException, Exception {
		DatabaseVersionPersistence persistance = new DatabaseVersionPersistence();
		persistance.upgradeDatabase();
	}

	private void configureAuditLogValues(Locale locale) throws SystemException {
		AuditConfigurtion.init(locale);
	}
	
	public void contextDestroyed(ServletContextEvent ctx) {
		
	}

    public void requestDestroyed(ServletRequestEvent event) {
        HibernateUtil.closeSession();
    }

    public void requestInitialized(ServletRequestEvent event) {
        
    }
    
	static ApplicationContext context = null;
	public static void initializeSpring() {
			context = new ClassPathXmlApplicationContext(
				"org/mifos/config/applicationContext.xml");
	}
    
}
