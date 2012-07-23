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
package uk.gov.nationalarchives.droid.results;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * @author rflitcroft
 *
 */
public final class DbUnitDtdGenerator {

    /** DTD filename. */
    public static final String DTD_FILENAME = "src/test/resources/droidDbUnit.dtd";

    private DbUnitDtdGenerator() { } 
    
    /**
     * Writes a DTD from whatever database hibernate created.
     * @param args command line args
     * @throws SQLException 
     * @throws SQLException if anything went wrong
     */
    public static void main(String[] args) throws SQLException {
        
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {
            "META-INF/spring-jpa.xml",
            "META-INF/spring-test.xml",
        });
        
        DataSource dataSource = (DataSource) ctx.getBean("dataSource");
        Connection conn = DataSourceUtils.getConnection(dataSource);
        // CHECKSTYLE:OFF
        System.out.println("Writing DTD...");
        // CHECKSTYLE:ON
        try {
            IDatabaseConnection connection = new DatabaseConnection(conn);
            FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream(DTD_FILENAME));
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (DatabaseUnitException e) {
            throw new RuntimeException(e);
        } finally {
            // CHECKSTYLE:OFF
            System.out.println("Written DTD.");
            // CHECKSTYLE:ON
            conn.close();
        }
    }
}
