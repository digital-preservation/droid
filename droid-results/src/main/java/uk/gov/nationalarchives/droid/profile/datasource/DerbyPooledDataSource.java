/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile.datasource;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;

/**
 * @author rflitcroft
 *
 */
public class DerbyPooledDataSource extends BasicDataSource {
 
    
    private static final long serialVersionUID = -8613139738021279720L;
    private static final String NO_CREATE_URL = "{none}";
    
    private final Log log = LogFactory.getLog(getClass());
    
    private String createUrl = NO_CREATE_URL;
    
    /**
     * Starts the database.
     * @throws SQLException if the database could not be booted.
     */
    public void init() throws SQLException {
        String droidLogDir = System.getProperty(RuntimeConfig.LOG_DIR);
        System.setProperty("derby.stream.error.file", 
                new File(droidLogDir, "derby.log").getPath());
        //setPoolPreparedStatements(true);
        setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        setDefaultAutoCommit(false);
        log.debug(String.format("Booting database [%s]", getUrl()));
        String url = getCreateURL();
        String driverClassName = getDriverClassName();
        try {
            Class.forName(driverClassName);
            DriverManager.getConnection(url).close();
        } catch (ClassNotFoundException e) {
            String message = String.format("Invalid driver class name: %s", driverClassName);
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
    
    private String getCreateURL() {
        String url = getUrl() + ";create=true";
        if (createUrl != null && !createUrl.isEmpty() && !NO_CREATE_URL.equals(createUrl)) {
            url = url + ";" + createUrl;
        }
        return url;
    }
    
    /**
     * Shuts down the database.  
     * Derby throws a SQLNonTransientConnectionException on a SUCCESSFUL shutdown of the
     * database (with SQLstate 08006), so we catch this and log as debug, otherwise as an error.
     * @throws SQLException if the database could not be shutdown.
     * @throws Exception
     */
    public void shutdown() throws SQLException {
        
        log.debug(String.format("Closing database [%s]", getUrl()));
        close();

        String url = getUrl() + ";shutdown=true";
        
        try {
            DriverManager.getConnection(url);
        } catch (SQLNonTransientConnectionException e) {
            if ("08006".equals(e.getSQLState())) {
                log.debug(e.getMessage());
            } else {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 
     * @param createUrl Sets the create Url to use when creating the database.
     */
    public void setCreateUrl(final String createUrl) {
        this.createUrl = createUrl;
    }
    
}
