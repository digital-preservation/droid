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
