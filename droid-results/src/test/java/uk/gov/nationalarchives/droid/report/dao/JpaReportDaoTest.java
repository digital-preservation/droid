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
package uk.gov.nationalarchives.droid.report.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.dbunit.DatabaseUnitException;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Criterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Restrictions;

import javax.sql.DataSource;

/**
 * @author Alok Kumar Dash, Brian O Reilly
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring-jpa.xml", "classpath*:META-INF/spring-results.xml",
        "classpath*:META-INF/spring-test.xml" })
public class JpaReportDaoTest {

    private static IDataSet testData;

    private List<ReportLineItem> reportData;

    private Criterion filter = newFilter();

    @Autowired
    private ReportDao reportDao;

    //@Autowired
    //private DataSource dataSource;

    //private IDatabaseConnection conn;
    private static IDatabaseConnection conn;

    @BeforeClass
    public static void getTestData() throws Exception {
        testData = new FlatXmlDataSetBuilder().build(JpaReportDaoTest.class.getResource("report-test-data-sans-formats.xml"));
        //System.setProperty("hibernate.generateDdl", "true");
        System.setProperty("matchAllExtensions", "false");
        conn = TestDatabase.getConnection();
    }

    @AfterClass
    public static void tearDown() {
        //System.clearProperty("hibernate.generateDdl");
        TestDatabase.closeResources();
    }

    // Without Filter

    @Test
    public void testReportForFileSize() {
        reportData = reportDao.getReportData(null, ReportFieldEnum.FILE_SIZE);
        assertEquals(0, reportData.get(0).getGroupByValues().size());
        assertEquals(23L, reportData.get(0).getCount().longValue());
        assertEquals(1116117.0D, reportData.get(0).getAverage().doubleValue(), 0.00001);
        assertEquals(25670702L, reportData.get(0).getSum().longValue());
        assertEquals(16857600L, reportData.get(0).getMaximum().longValue());
        assertEquals(614L, reportData.get(0).getMinimum().longValue());
        printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByFileFormat() {
        reportData = reportDao.getReportData(null, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.FILE_FORMAT));

        ReportLineItem plainTextFileItem = null;
        for (ReportLineItem item : reportData) {
            if ("Plain Text File".equals(getGroupValue(item, 0))) {
                plainTextFileItem = item;
            }
        }

        assertEquals("Plain Text File", getGroupValue(plainTextFileItem, 0));
        assertEquals(5L, plainTextFileItem.getCount().longValue());
        assertEquals(4697.0D, plainTextFileItem.getAverage().doubleValue(), 0.0001);
        assertEquals(23486L, plainTextFileItem.getSum().longValue());
        assertEquals(12933L, plainTextFileItem.getMaximum().longValue());
        assertEquals(614L, plainTextFileItem.getMinimum().longValue()); //
        printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByPUID() {
        reportData = reportDao.getReportData(null, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.PUID));
        assertTrue(reportData.size() > 0);
        //printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByMonthOfLastModifiedDate() {
        reportData = reportDao.getReportData(null, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.LAST_MODIFIED_DATE, "month"));
        assertTrue(reportData.size() > 0);
        //printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByYearOfLastModifiedDate() {
        reportData = reportDao.getReportData(null, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.LAST_MODIFIED_DATE, "year"));
        assertTrue(reportData.size() > 0);
        printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByFileExtension() {
        reportData = reportDao.getReportData(null, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.FILE_EXTENSION));
        assertTrue(reportData.size() > 0);
        printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByUpperCaseOfFileExtension() {
        reportData = reportDao.getReportData(null, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.FILE_EXTENSION, "upper"));
        assertTrue(reportData.size() > 0);
        //printReportData(reportData);
    }

    // With Filter.

    @Test
    public void testReportForFileSizeWithFilter() {

        reportData = reportDao.getReportData(filter, ReportFieldEnum.FILE_SIZE);
        assertEquals("", getGroupValue(reportData.get(0), 0));
        assertEquals(7L, reportData.get(0).getCount().longValue());
        assertEquals(5180.0D, reportData.get(0).getAverage().doubleValue(), 0.0001);
        assertEquals(36265L, reportData.get(0).getSum().longValue());
        assertEquals(8704L, reportData.get(0).getMaximum().longValue());
        assertEquals(614L, reportData.get(0).getMinimum().longValue());

        assertTrue(reportData.size() > 0);
        //printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByFileFormatWithFilter() {
        reportData = reportDao.getReportData(filter, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.FILE_FORMAT));
        assertTrue(reportData.size() > 0);
        //printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByPUIDWithFilter() {
        reportData = reportDao.getReportData(filter, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.PUID));
        assertTrue(reportData.size() > 0);
        //printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByMonthOfLastModifiedDateWithFilter() {
        reportData = reportDao.getReportData(filter, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.LAST_MODIFIED_DATE, "month"));
        assertTrue(reportData.size() > 0);
        //printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByYearOfLastModifiedDateWithFilter() {
        reportData = reportDao.getReportData(filter, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.LAST_MODIFIED_DATE, "year"));
        assertTrue(reportData.size() > 0);
        //printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByFileExtensionWithFilter() {
        reportData = reportDao.getReportData(filter, ReportFieldEnum.FILE_SIZE,
                getGroupByFieldList(ReportFieldEnum.FILE_EXTENSION));
        assertTrue(reportData.size() > 0);
        //printReportData(reportData);
    }

    @Test
    public void testReportForFileSizeGroupByUpperCaseOfFileExtensionWithFilter() {
        reportData = reportDao
                .getReportData(filter, ReportFieldEnum.FILE_SIZE,
                        getGroupByFieldList(ReportFieldEnum.FILE_EXTENSION, "upper"));
        assertTrue(reportData.size() > 0);
        //printReportData(reportData);
    }

    @Before
    public void setupTestData() throws Exception {

        //conn = getConnection();
        if(conn == null) {
            conn = TestDatabase.getConnection();
        }
        try {
            DatabaseOperation.CLEAN_INSERT.execute(conn, testData);
        } finally {
            //conn.close();
        }
    }

    @After
    public void tearDownTestData() throws Exception {
        //conn = getConnection();
        if(conn == null) {
            conn = TestDatabase.getConnection();
            if(this.reportDao instanceof SqlReportDaoImpl) {
                ((SqlReportDaoImpl)reportDao).getDatasource().getConnection().close();
            }
        }
        try {
            DatabaseOperation.DELETE.execute(conn, testData);
        } finally {
            //conn.close();
        }
    }


    private void printReportData(List<ReportLineItem> myReportData) {
        for (ReportLineItem item : myReportData) {
            System.out.println("GroupBy Value:" + getGroupValue(item, 0) + "   Count:" + item.getCount() + "   Average:"
                    + item.getAverage() + "   Sum:" + item.getSum() + "  Max:  "
                    + item.getMaximum() + "   Min:   " + item.getMinimum());
        }
    }

//    private FilterImpl getFilter() {
//        FilterImpl myfilter = new FilterImpl();
//        myfilter.setEnabled(true);
//        List<FilterCriterionImpl> filterCriteriaList = new ArrayList<FilterCriterionImpl>();
//        FilterCriterionImpl filterCriteriaImpl = new FilterCriterionImpl();
//        filterCriteriaList.add(filterCriteriaImpl);
//        filterCriteriaImpl.setField(CriterionFieldEnum.FILE_SIZE);
//        filterCriteriaImpl.setOperator(CriterionOperator.LT);
//        filterCriteriaImpl.setValueFreeText("10000");
//        myfilter.setCriteria(filterCriteriaList);
//
//        return myfilter;
//    }

    private Criterion newFilter() {
        return Restrictions.lt("metaData.size", 10000L);
    }

    private List<GroupByField> getGroupByFieldList(ReportFieldEnum reportField) {
        return getGroupByFieldList(reportField, "");
    }

    private List<GroupByField> getGroupByFieldList(ReportFieldEnum reportField, String function) {
        GroupByField grouper = new GroupByField();
        grouper.setGroupByField(reportField);
        grouper.setFunction(function);
        List<GroupByField> result = new ArrayList<GroupByField>();
        result.add(grouper);
        return result;
    }

    private String getGroupValue(ReportLineItem item, int index) {
        return item.getGroupByValues().size() > index
                ? item.getGroupByValues().get(index)
                : "";
    }

    private static class TestDatabase {

        private static IDatabaseConnection conn;
        private static Connection con;

        public static IDatabaseConnection getConnection() {

            if(conn == null) {
                InputStream in = null;
                final HikariConfig config = new HikariConfig();

                try {

                    Properties prop = new Properties();
                    in = JpaReportDaoTest.class.getResourceAsStream("/jpa-test.properties");

                    prop.load(in);
                    config.setDriverClassName(prop.getProperty("datasource.driverClassName"));
                    config.setJdbcUrl(prop.getProperty("datasource.url"));
                    config.setUsername(prop.getProperty("datasource.username"));
                    config.setPassword(prop.getProperty("datasource.password"));
                    config.setMaximumPoolSize(Integer.parseInt(prop.getProperty("datasource.maxActive")));
                } catch(IOException e) {
                    e.printStackTrace();
                } finally {
                    if(in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                final DataSource dataSource = new HikariDataSource(config) ;
                try {
                    con = dataSource.getConnection();
                    con.setAutoCommit(true);
                    conn = new DatabaseConnection(con);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch(DatabaseUnitException e) {
                    e.printStackTrace();
                }
            }
            return conn;
        }

        public static void closeResources() {

            try {
                if(conn != null) {
                    conn.close();
                }

                if(con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
