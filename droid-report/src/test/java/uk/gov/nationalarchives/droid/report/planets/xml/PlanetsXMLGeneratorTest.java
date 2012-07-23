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
