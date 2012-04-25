/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.planet.xml.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.profile.FilterCriterionImpl;
import uk.gov.nationalarchives.droid.profile.FilterImpl;

/**
 * @author Alok Kumar Dash
 * create planets xml using different mechanism now.
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring-jpa.xml",
        "classpath*:META-INF/spring-results.xml",
        "classpath*:META-INF/spring-test.xml" })
@TransactionConfiguration(defaultRollback = false)
public class JpaPlanetsXMLDaoTest {

    private static IDataSet testData;

    @Autowired
    private PlanetsXMLDao planetsDao;

    @Autowired
    private DataSource dataSource;

    private IDatabaseConnection conn;

    @BeforeClass
    public static void getTestData() throws Exception {
        testData = new FlatXmlDataSetBuilder().build(JpaPlanetsXMLDaoTest.class
                .getResource("planets-xml-test-data.xml"));
        System.setProperty("hibernate.generateDdl", "true");
    }

    @AfterClass
    public static void tesarDown() {
        System.clearProperty("hibernate.generateDdl");
    }

    @Test
    public void testPlanetDAOEndToEndWithFilter() {
        FilterImpl filter = new FilterImpl();
        filter.setEnabled(true);
        List<FilterCriterionImpl> filterCriteriaList = new ArrayList<FilterCriterionImpl>();
        
        
        
        FilterCriterionImpl filterCriteriaImpl = new FilterCriterionImpl();
        
        filterCriteriaList.add(filterCriteriaImpl);
        
        filterCriteriaImpl.setField(CriterionFieldEnum.FILE_SIZE);
        filterCriteriaImpl.setOperator(CriterionOperator.EQ);
        filterCriteriaImpl.setValueFreeText("230");
        
        filter.setCriteria(filterCriteriaList);
        
        PlanetsXMLData planetData = planetsDao
                .getDataForPlanetsXML(filter);

        assertEquals(new BigDecimal("230.0"), planetData.getProfileStat()
                .getProfileMeanSize());
        assertEquals(new BigInteger("230"), planetData.getProfileStat()
                .getProfileLargestSize());
        assertEquals(new BigInteger("230"), planetData.getProfileStat()
                .getProfileTotalSize());
        assertEquals(new BigInteger("230"), planetData.getProfileStat()
                .getProfileSmallestSize());

        assertEquals(new BigInteger("0"), planetData.getProfileStat()
                .getProfileTotalUnReadableFiles());
        assertEquals(new BigInteger("1"), planetData.getProfileStat()
                .getProfileTotalReadableFiles());
        assertEquals(new BigInteger("0"), planetData.getProfileStat()
                .getProfileTotalUnReadableFolders());

    }

    
    @Test
    public void testPlanetDAOEndToEndWithoutFilter() {
      
        FilterImpl filter = new FilterImpl();
        filter.setEnabled(false);
        List<FilterCriterionImpl> filterCriteriaList = new ArrayList<FilterCriterionImpl>();
        
        filter.setCriteria(filterCriteriaList);
        
        PlanetsXMLData planetData = planetsDao
                .getDataForPlanetsXML(filter);

        assertEquals(new BigDecimal("2147.0"), planetData.getProfileStat()
                .getProfileMeanSize());
        assertEquals(new BigInteger("8000"), planetData.getProfileStat()
                .getProfileLargestSize());
        assertEquals(new BigInteger("12882"), planetData.getProfileStat()
                .getProfileTotalSize());
        assertEquals(new BigInteger("140"), planetData.getProfileStat()
                .getProfileSmallestSize());

        assertEquals(new BigInteger("1"), planetData.getProfileStat()
                .getProfileTotalUnReadableFiles());
        assertEquals(new BigInteger("5"), planetData.getProfileStat()
                .getProfileTotalReadableFiles());
        assertEquals(new BigInteger("0"), planetData.getProfileStat()
                .getProfileTotalUnReadableFolders());

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
        return new DatabaseConnection(DataSourceUtils.getConnection(dataSource));
    }

}
