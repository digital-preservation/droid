/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;


/**
 * @author rflitcroft
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring-jpa.xml", "classpath*:META-INF/spring-results.xml",
        "classpath*:META-INF/spring-test.xml" })
@TransactionConfiguration(defaultRollback = true)
public class JpaProfileDaoTest {

    private static IDataSet testData;

    @Autowired
    private ProfileDao profileDao;
    
    @Autowired
    private DataSource dataSource;
    
    private IDatabaseConnection conn;

    @BeforeClass
    public static void getTestData() throws Exception {
        testData = new FlatXmlDataSetBuilder().build(
                JpaProfileDaoTest.class.getResource("results-test-data.xml"));
        System.setProperty("hibernate.generateDdl", "true");
        System.setProperty("maxBytesToScan", "65536");
        System.setProperty("matchAllExtensions", "false");
    }
    
    @AfterClass
    public static void tesarDown() {
        System.clearProperty("hibernate.generateDdl");
    }


    @Test
    public void testFindProfileResourceNodeGetsAllResultsInOneQuery() {
        
        List<ProfileResourceNode> nodes = profileDao.findProfileResourceNodes(1L);
        
        // navigate to a first level child's format
        assertEquals("fmt/alok", nodes.iterator().next()
                .getFormatIdentifications().iterator().next().getPuid());
        assertEquals(2, nodes.size());
        
    }
    
    @Test
    public void testFindRootNodeGetsAllResultsInOneQuery() throws Exception {
        conn = getConnection();
        
        Collection<ProfileResourceNode> nodes = 
            profileDao.findProfileResourceNodes(null);
        
        // navigate to the root level directory node
        ProfileResourceNode firstNode = nodes.iterator().next();
        assertNull(firstNode.getParentId());
        assertEquals(1, nodes.size());
        
    }

    
    @Before
    public void setupTestData() throws Exception {
        
        conn = getConnection();
        try {
            DatabaseOperation.CLEAN_INSERT.execute(conn, testData);
        } finally {
            conn.close();
        }
    }
    
    @After
    public void tearDownTestData() throws Exception {
        conn = getConnection();
        try {
            DatabaseOperation.DELETE.execute(conn, testData);
        } finally {
            conn.close();
        }
    }

    protected IDatabaseConnection getConnection() throws Exception {
        Connection con = DataSourceUtils.getConnection(dataSource);
        con.setAutoCommit(true);
        return new DatabaseConnection(con);
    }

}
