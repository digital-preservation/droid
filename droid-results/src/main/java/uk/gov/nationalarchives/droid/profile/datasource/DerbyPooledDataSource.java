/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
