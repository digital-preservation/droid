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
