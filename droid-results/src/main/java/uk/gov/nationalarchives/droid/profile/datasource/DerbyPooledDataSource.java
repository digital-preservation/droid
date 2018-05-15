/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author rflitcroft
 *
 */
public class DerbyPooledDataSource extends HikariDataSource {

    private static final long serialVersionUID = -8613139738021279720L;

    private final Log log = LogFactory.getLog(getClass());
    private final HikariConfig config;

    /**
     * Constructor.
     *
     * @param config The configuration for the pool
     */
    public DerbyPooledDataSource(final HikariConfig config) {
        super(config);
        this.config = config;
    }

    /**
    * Shuts down the database.  
    * Derby throws a SQLNonTransientConnectionException on a SUCCESSFUL shutdown of the
    * database (with SQLstate 08006), so we catch this and log as debug, otherwise as an error.
    * @throws SQLException if the database could not be shutdown.
    */
    @Override
    public void close() {

        log.debug(String.format("Closing database [%s]", config.getJdbcUrl()));
        super.close();

        String url = config.getJdbcUrl() + ";shutdown=true";

        try {
            DriverManager.getConnection(url);
        } catch (SQLException e) {
            if ("08006".equals(e.getSQLState())) {
                log.debug(e.getMessage());
            } else {
                log.error(e.getMessage(), e);
            }
        }
    }

    
    /**
     * Stop writes to the database to allow copying. 
     */
    public void freeze() {
        
        log.debug(String.format("Freezing database [%s]", config.getJdbcUrl()));
        
        try (final Statement s = DriverManager.getConnection(config.getJdbcUrl()).createStatement()) {
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_FREEZE_DATABASE()");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * Allow writes to a previously frozen database. 
     */
    public void thaw() {
        
        log.debug(String.format("Derby thawing database [%s]", config.getJdbcUrl()));
        
        try (final Statement s = DriverManager.getConnection(config.getJdbcUrl()).createStatement()) {
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_UNFREEZE_DATABASE()");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

}
