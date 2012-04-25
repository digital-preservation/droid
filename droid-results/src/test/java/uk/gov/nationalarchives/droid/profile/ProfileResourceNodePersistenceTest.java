/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;



/**
 * @author rflitcroft
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring-jpa.xml", "classpath*:META-INF/spring-results.xml", 
        "classpath*:META-INF/spring-test.xml" })
@TransactionConfiguration(defaultRollback = true)
@Transactional
@Ignore
public class ProfileResourceNodePersistenceTest {

    @Autowired
    private DataSource dataSource;
    
    private IDatabaseConnection conn;

    @Autowired 
    private JpaProfileDaoImpl profileDao;
    
    @BeforeClass
    public static void getTestData() {
        System.setProperty("hibernate.generateDdl", "true");
    }
    
    @BeforeClass
    public static void setup() {
    }
    
    @AfterClass
    public static void tearDown() {
        System.clearProperty("hibernate.generateDdl");
    }
    

//    @Test
//    public void testCreateRootNode() throws Exception {
//        
//        URI uri = ProfileResourceNode.root().getUri();
//        
//        profileDao.createRootNode();
//        
//        profileDao.flush();
//
//        conn = getConnection();
//        ITable resourceNodeTable = conn.createTable("PROFILE_RESOURCE_NODE");
//        assertEquals(uri.toString(), resourceNodeTable.getValue(0, "URI"));
//
//        conn.close();
//
//    }
    
    protected IDatabaseConnection getConnection() throws Exception {
        return new DatabaseConnection(DataSourceUtils.getConnection(dataSource));
    }

}

