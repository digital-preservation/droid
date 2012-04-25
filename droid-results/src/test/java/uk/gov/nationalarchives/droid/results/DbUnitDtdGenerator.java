/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
