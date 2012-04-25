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
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;

/**
 * @author rflitcroft, Alok Kumar Dash
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring-jpa.xml", "classpath*:META-INF/spring-results.xml",
        "classpath*:META-INF/spring-test.xml" })
@TransactionConfiguration(defaultRollback = true)
public class JpaProfileFilterTest {

    private static IDataSet testData;
    
    private FilterImpl filter;

    @Autowired
    private JpaProfileDaoImpl profileDao;
    
    @Autowired
    private DataSource dataSource;
    
    private IDatabaseConnection conn;

    @BeforeClass
    public static void getTestData() throws Exception {
        testData = new FlatXmlDataSetBuilder().build(
                JpaProfileFilterTest.class.getResource("results-test-data.xml"));
        System.setProperty("hibernate.generateDdl", "true");
        System.setProperty("maxBytesToScan", "65536");
        System.setProperty("matchAllExtensions", "false");
    }
    
    @AfterClass
    public static void tearDown() {
        System.clearProperty("hibernate.generateDdl");
    }

    @Test
    public void testFilterOnPuid() {
        
        filter = new FilterImpl();
        filter.setEnabled(true);
        filter.setNarrowed(true);
        
        FilterCriterionImpl criterion = new FilterCriterionImpl();
        criterion.setSelectedValues(new ArrayList<FilterValue>());
        
        criterion.setField(CriterionFieldEnum.PUID);
        criterion.setOperator(CriterionOperator.ANY_OF);
        criterion.addSelectedValue(new FilterValue(0, "", "fmt/alok"));
        
        filter.addFilterCiterion(criterion, 0);
        
        List<ProfileResourceNode> nodes = profileDao.findProfileResourceNodes(null);
        assertEquals(1, nodes.size());
        assertNotNull(nodes.get(0).getFormatIdentifications().get(0));
        assertNotNull(nodes.get(0).getFormatIdentifications().get(0));
        assertEquals("NONE", nodes.get(0).getFormatIdentifications().get(0).getPuid());

        nodes = profileDao.findProfileResourceNodes(1L, filter);
        assertEquals(2, nodes.size());
        assertNotNull(nodes.get(0).getFormatIdentifications().get(0));
        assertNotNull(nodes.get(0).getFormatIdentifications().get(0));
        assertEquals("fmt/alok", nodes.get(0).getFormatIdentifications().get(0).getPuid());

        assertNotNull(nodes.get(1).getFormatIdentifications().get(0));
        assertNotNull(nodes.get(1).getFormatIdentifications().get(0));
        assertEquals("fmt/alok", nodes.get(1).getFormatIdentifications().get(0).getPuid());

    }
    
    @Test
    public void testFilterOnExtension() {
        
        filter = new FilterImpl();
        filter.setEnabled(true);
        filter.setNarrowed(true);
        
        FilterCriterionImpl criterion = new FilterCriterionImpl();
        criterion.setSelectedValues(new ArrayList<FilterValue>());
        
        criterion.setField(CriterionFieldEnum.FILE_EXTENSION);
        criterion.setOperator(CriterionOperator.EQ);
        criterion.setValueFreeText("exe");
        filter.addFilterCiterion(criterion, 0);
        List<ProfileResourceNode> nodes = profileDao.findProfileResourceNodes(1L, filter);
        
        assertEquals(1, nodes.size());
        assertEquals("exe", nodes.get(0).getMetaData().getExtension());
    }

    
    @Test
    public void testFilterOnSize() {
        FilterCriterionImpl criterion = new FilterCriterionImpl();
        criterion.setField(CriterionFieldEnum.FILE_SIZE);
        criterion.setOperator(CriterionOperator.EQ);
        criterion.setValueFreeText("256");
        filter.addFilterCiterion(criterion, 0);
        List<ProfileResourceNode> nodes = profileDao.findProfileResourceNodes(1L, filter);
        
        assertEquals(2, nodes.size());
        
    }

    @Test
    public void testFilterOnFileName() {
        FilterCriterionImpl criterion = new FilterCriterionImpl();
        criterion.setField(CriterionFieldEnum.FILE_NAME);
        criterion.setOperator(CriterionOperator.STARTS_WITH);
        criterion.setValueFreeText("f");
        filter.addFilterCiterion(criterion, 0);
        List<ProfileResourceNode> nodes = profileDao.findProfileResourceNodes(1L, filter);
        assertEquals(2, nodes.size());
        
    }    
    
    @Before
    public void setupTestData() throws Exception {

        filter = new FilterImpl();
        filter.setEnabled(true);
        filter.setNarrowed(true);

        
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
