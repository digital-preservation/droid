/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.planets.xml;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;



import uk.gov.nationalarchives.droid.planet.xml.dao.GroupByPuidSizeAndCountRow;
import uk.gov.nationalarchives.droid.planet.xml.dao.GroupByYearSizeAndCountRow;
import uk.gov.nationalarchives.droid.planet.xml.dao.PlanetsXMLData;
import uk.gov.nationalarchives.droid.planet.xml.dao.ProfileStat;

/**
 * @author Alok Kumar Dash
 */
// CHECKSTYLE:OFF
public class PlanetsXMLGeneratorTest {
// CHECKSTYLE: ON

    private PlanetsXMLGenerator planetsGenerator;
    
    private String filename =System.getProperty("user.home") + "\\.droid\\tmp" + "planets-test.xml";
    
    private File destination = new File(filename);

    @Before
    public void setup() {
        destination.delete();
        assertFalse(destination.exists());
    }
    
    @After
    public void tearDown() {
        //destination.delete();
    }

    @Ignore
    @Test
    public void testPlanetsXMLEndToEnd() {
        String[] topLevelItems = new String[] {"foo", "bar"};

        PlanetsXMLData data = new PlanetsXMLData();

        data.setTopLevelItems(Arrays.asList(topLevelItems));

        List<GroupByYearSizeAndCountRow> dataList = new ArrayList<GroupByYearSizeAndCountRow>();
        GroupByYearSizeAndCountRow groupByYearSizeAndCountRow = new GroupByYearSizeAndCountRow();
        groupByYearSizeAndCountRow.setYear(1990);
        groupByYearSizeAndCountRow.setCount(new BigInteger("20"));
        groupByYearSizeAndCountRow.setSize(new BigDecimal("5000"));
        dataList.add(groupByYearSizeAndCountRow);

        data.setGroupByYear(dataList);

        List<GroupByPuidSizeAndCountRow> dataList1 = new ArrayList<GroupByPuidSizeAndCountRow>();
        GroupByPuidSizeAndCountRow groupByPuidSizeAndCountRow = new GroupByPuidSizeAndCountRow();
        groupByPuidSizeAndCountRow.setPuid("Xfmt/doc");
        groupByPuidSizeAndCountRow.setCount(new BigInteger("20"));
        groupByPuidSizeAndCountRow.setSize(new BigDecimal("50000"));
        dataList1.add(groupByPuidSizeAndCountRow);
        data.setGroupByPuid(dataList1);
        
        
        ProfileStat profileStat = new ProfileStat();
        
        profileStat.setProfileEndDate(new Date());
        profileStat.setProfileSaveDate(new Date());
        profileStat.setProfileStartDate(new Date());
        
        profileStat.setProfileLargestSize(new BigInteger("20"));
        profileStat.setProfileMeanSize(new BigDecimal("50000"));
        profileStat.setProfileSmallestSize(new BigInteger("20"));
        profileStat.setProfileTotalSize(new BigInteger("20"));
        profileStat.setProfileTotalReadableFiles(new BigInteger("20"));
        profileStat.setProfileTotalUnReadableFiles(new BigInteger("20"));
        profileStat.setProfileTotalUnReadableFolders(new BigInteger("20"));
        
        
        data.setProfileStat(profileStat);

        planetsGenerator = new PlanetsXMLGenerator(filename, data);
        planetsGenerator.generate();

        assertTrue(destination.exists());
    }
    
    @Ignore
    @Test
    public void testPlanetsWithNoElement() {
        String[] topLevelItems = new String[] {};

        PlanetsXMLData data = new PlanetsXMLData();

        data.setTopLevelItems(Arrays.asList(topLevelItems));

        List<GroupByYearSizeAndCountRow> dataList = new ArrayList<GroupByYearSizeAndCountRow>();
        //GroupByYearSizeAndCountRow groupByYearSizeAndCountRow = new GroupByYearSizeAndCountRow();
        //groupByYearSizeAndCountRow.setYear(0);
        //groupByYearSizeAndCountRow.setCount(null);
        //groupByYearSizeAndCountRow.setSize(null);
        //dataList.add(groupByYearSizeAndCountRow);

        data.setGroupByYear(dataList);

        List<GroupByPuidSizeAndCountRow> dataList1 = new ArrayList<GroupByPuidSizeAndCountRow>();
        GroupByPuidSizeAndCountRow groupByPuidSizeAndCountRow = new GroupByPuidSizeAndCountRow();
        groupByPuidSizeAndCountRow.setPuid("Xfmt/doc");
        groupByPuidSizeAndCountRow.setCount(new BigInteger("20"));
        groupByPuidSizeAndCountRow.setSize(new BigDecimal("50000"));
        dataList1.add(groupByPuidSizeAndCountRow);
        data.setGroupByPuid(dataList1);
        
        
        ProfileStat profileStat = new ProfileStat();
        
        profileStat.setProfileEndDate(new Date());
        profileStat.setProfileSaveDate(new Date());
        profileStat.setProfileStartDate(new Date());
        
        profileStat.setProfileLargestSize(new BigInteger("20"));
        profileStat.setProfileMeanSize(new BigDecimal("50000"));
        profileStat.setProfileSmallestSize(new BigInteger("20"));
        profileStat.setProfileTotalSize(new BigInteger("20"));
        profileStat.setProfileTotalReadableFiles(new BigInteger("20"));
        profileStat.setProfileTotalUnReadableFiles(new BigInteger("20"));
        profileStat.setProfileTotalUnReadableFolders(new BigInteger("20"));
        
        
        data.setProfileStat(profileStat);

        planetsGenerator = new PlanetsXMLGenerator(filename, data);
        planetsGenerator.generate();

        assertTrue(destination.exists());
    }
}
