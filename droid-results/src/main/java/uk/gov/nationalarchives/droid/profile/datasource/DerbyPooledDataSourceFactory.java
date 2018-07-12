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

import java.nio.file.Paths;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.zaxxer.hikari.HikariConfig;

import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;



/**
 * Spring Factory bean to create instance of DerbyPooledDataSource.
 */
public class DerbyPooledDataSourceFactory implements FactoryBean<DerbyPooledDataSource> {

    private static final String NO_CREATE_URL = "{none}";

    private String createUrl = NO_CREATE_URL;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private HikariConfig config;

    @Override
    public DerbyPooledDataSource getObject() throws Exception {
        String droidLogDir = System.getProperty(RuntimeConfig.LOG_DIR);
        System.setProperty("derby.stream.error.file",
                Paths.get(droidLogDir, "derby.log").toAbsolutePath().toString());
        //setPoolPreparedStatements(true);
        //setInitialSize(20); // initial size of connection pool.
        log.debug(String.format("Booting database [%s]", config.getJdbcUrl()));
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

        return new DerbyPooledDataSource(config);
    }

    private String getDriverClassName() {
        return config.getDriverClassName();
    }


    private String getCreateURL() {
        String url = config.getJdbcUrl() + ";create=true";
        if (createUrl != null && !createUrl.isEmpty() && !NO_CREATE_URL.equals(createUrl)) {
            url = url + ";" + createUrl;
        }
        return url;
    }

    /**
     * URL to create derby database.
     * @param createUrl parameter.
     */
    public void setCreateUrl(String createUrl) {
        this.createUrl = createUrl;
    }

    /**
     * HikariConfig config.
     * @param config Database pool configuration.
     */
    public void setConfig(HikariConfig config) {
        this.config = config;
    }

    @Override
    public Class<?> getObjectType() {
        return DerbyPooledDataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }


}
