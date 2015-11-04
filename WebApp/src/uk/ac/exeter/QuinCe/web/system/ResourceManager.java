package uk.ac.exeter.QuinCe.web.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import uk.ac.exeter.QuinCe.jobs.InvalidThreadCountException;
import uk.ac.exeter.QuinCe.jobs.JobThreadPool;

/**
 * Utility class for handling pools
 * @author Steve Jones
 *
 */
public class ResourceManager implements ServletContextListener {

	private static final String ATTRIBUTE_NAME = "pools";
	
	private DataSource dbDataSource;
	
	private Properties configuration;
	
	@Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        String databaseName = servletContext.getInitParameter("database.name");
        try {
        	dbDataSource = (DataSource) new InitialContext().lookup(databaseName);
        } catch (NamingException e) {
            throw new RuntimeException("Config failed: datasource not found", e);
        }
        
        try {
            String filePath = (String) servletContext.getInitParameter("configuration.path");
            configuration = new Properties();
            configuration.load(new FileInputStream(new File(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Config failed: could not read config file", e);
        }

        
        // Initialise the job thread pool
       	try {
			JobThreadPool.initialise(3);
		} catch (InvalidThreadCountException e) {
			// Do nothing for now
		}

       	// Register ourselves in the servlet context
        servletContext.setAttribute(ATTRIBUTE_NAME, this);
}

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // NOOP.
    }

    public DataSource getDBDataSource() {
        return dbDataSource;
    }
    
    public Properties getConfig() {
        return configuration;
    }

    public static ResourceManager getInstance(ServletContext servletContext) {
        return (ResourceManager) servletContext.getAttribute(ATTRIBUTE_NAME);
    }
}
