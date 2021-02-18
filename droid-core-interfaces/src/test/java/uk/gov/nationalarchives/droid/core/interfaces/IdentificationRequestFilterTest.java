/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
package uk.gov.nationalarchives.droid.core.interfaces;

import net.byteseek.io.reader.WindowReader;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.filter.BasicFilter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.BasicFilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;


import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

public class IdentificationRequestFilterTest {

    @Test
    public void testNullFilterNotFiltered() {
        IdentificationRequest request = new TestIdentificationRequest("test.bmp", 1000L, 0L);
        IdentificationRequestFilter requestFilter = new IdentificationRequestFilter(null);
        assertTrue(requestFilter.passesFilter(request));
    }

    @Test
    public void testEmptyConstructorNotFiltered() {
        IdentificationRequest request = new TestIdentificationRequest("test.bmp", 1000L, 0L);
        IdentificationRequestFilter requestFilter = new IdentificationRequestFilter();
        assertTrue(requestFilter.passesFilter(request));
    }

    @Test
    public void testEmptyCriteriaNotFiltered() {
        IdentificationRequest request = new TestIdentificationRequest("test.bmp", 1000L, 0L);
        IdentificationRequestFilter requestFilter = new IdentificationRequestFilter(new BasicFilter());
        assertTrue(requestFilter.passesFilter(request));
    }

    /*
      Filter out requests which do not have a size set if we have size filter criteria.
     */
    @Test
    public void testNullSizeFiltered() {
        IdentificationRequest request = new TestIdentificationRequest(null, null, null);
        IdentificationRequestFilter filter = createSizeFilter(CriterionOperator.LT,100);
        assertFalse(filter.passesFilter(request));
        filter = createSizeFilter(CriterionOperator.LTE,100);
        assertFalse(filter.passesFilter(request));
        filter = createSizeFilter(CriterionOperator.EQ,100);
        assertFalse(filter.passesFilter(request));
        filter = createSizeFilter(CriterionOperator.NE,100);
        assertFalse(filter.passesFilter(request));
        filter = createSizeFilter(CriterionOperator.GT,100);
        assertFalse(filter.passesFilter(request));
        filter = createSizeFilter(CriterionOperator.GTE,100);
        assertFalse(filter.passesFilter(request));
    }

    @Test
    public void testSizeFilterLessThan() {
        IdentificationRequest sizerequest = new TestIdentificationRequest(100000L);
        IdentificationRequestFilter sizeFilter = createSizeFilter(CriterionOperator.LT,100);
        assertFalse(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(99L);
        assertTrue(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(100L);
        assertFalse(sizeFilter.passesFilter(sizerequest));
    }

    @Test
    public void testSizeFilterLessThanEqual() {
        IdentificationRequest sizerequest = new TestIdentificationRequest(100000L);
        IdentificationRequestFilter sizeFilter = createSizeFilter(CriterionOperator.LTE,100);
        assertFalse(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(99L);
        assertTrue(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(100L);
        assertTrue(sizeFilter.passesFilter(sizerequest));
    }

    @Test
    public void testSizeFilterEqual() {
        IdentificationRequest sizerequest = new TestIdentificationRequest(100000L);
        IdentificationRequestFilter sizeFilter = createSizeFilter(CriterionOperator.EQ,100);
        assertFalse(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(99L);
        assertFalse(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(100L);
        assertTrue(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(101L);
        assertFalse(sizeFilter.passesFilter(sizerequest));
    }

    @Test
    public void testSizeFilterNotEqual() {
        IdentificationRequest sizerequest = new TestIdentificationRequest(100000L);
        IdentificationRequestFilter sizeFilter = createSizeFilter(CriterionOperator.NE,100);
        assertTrue(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(99L);
        assertTrue(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(100L);
        assertFalse(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(101L);
        assertTrue(sizeFilter.passesFilter(sizerequest));
    }

    @Test
    public void testSizeFilterGreaterThan() {
        IdentificationRequest sizerequest = new TestIdentificationRequest(100000L);
        IdentificationRequestFilter sizeFilter = createSizeFilter(CriterionOperator.GT,100);
        assertTrue(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(100L);
        assertFalse(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(101L);
        assertTrue(sizeFilter.passesFilter(sizerequest));
    }

    @Test
    public void testSizeFilterGreaterThanEqual() {
        IdentificationRequest sizerequest = new TestIdentificationRequest(100000L);
        IdentificationRequestFilter sizeFilter = createSizeFilter(CriterionOperator.GTE,100);
        assertTrue(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(100L);
        assertTrue(sizeFilter.passesFilter(sizerequest));
        sizerequest = new TestIdentificationRequest(99L);
        assertFalse(sizeFilter.passesFilter(sizerequest));
    }

    /*
      Filter out requests which do not have a filename set if we have filename filter criteria.
     */
    @Test
    public void testNullFilenameFiltered() {
        IdentificationRequest request = new TestIdentificationRequest(null, null, null);
        IdentificationRequestFilter filter = createNameFilter(CriterionOperator.LT,"ABCDEF");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.LTE,"ABCDE");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.EQ,"ABCDE");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.NE,"ABCDE");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.GT,"ABCDE");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.GTE,"ABCDE");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.STARTS_WITH,"ABCDE");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.NOT_STARTS_WITH,"ABCDE");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.ENDS_WITH,"ABCDE");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.NOT_ENDS_WITH,"ABCDE");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.CONTAINS,"ABCDE");
        assertFalse(filter.passesFilter(request));
        filter = createNameFilter(CriterionOperator.NOT_CONTAINS,"ABCDE");
        assertFalse(filter.passesFilter(request));
    }

    @Test
    public void testFilenameLessThan() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.LT,"ABCDEF");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertTrue(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("abcdef");
        assertFalse(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("abcdefg");
        assertFalse(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameLessThanEqual() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.LTE,"ABCDE");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("ABC");
        assertTrue(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("abcdef");
        assertFalse(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("abcdefg");
        assertFalse(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameEqual() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.EQ,"ABCDE");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertTrue(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("abcdef");
        assertFalse(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameNotEqual() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.NE,"ABCDE");
        assertFalse(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertFalse(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("abcdef");
        assertTrue(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameGreaterThan() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.GT,"ABCDEF");
        assertFalse(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertFalse(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("abcdefg");
        assertTrue(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameGreaterThanEqual() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.GTE,"ABCDE");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("ABC");
        assertFalse(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameStartsWith() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.STARTS_WITH,"AB");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("ABC");
        assertTrue(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("AABC");
        assertFalse(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameNotStartsWith() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.NOT_STARTS_WITH,"AB");
        assertFalse(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertFalse(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("ABC");
        assertFalse(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("BC");
        assertTrue(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameEndsWith() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.ENDS_WITH,"DE");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("ABCDDFFFDDE");
        assertTrue(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("abcdef");
        assertFalse(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("ABCDEF");
        assertFalse(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameNotEndsWith() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.NOT_ENDS_WITH,"DE");
        assertFalse(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertFalse(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("ABCDDFFFDDE");
        assertFalse(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("abcdef");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("ABCDEF");
        assertTrue(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameContains() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.CONTAINS,"CD");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertTrue(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("ABCDDFFFDDE");
        assertTrue(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("ABCEDFDCE");
        assertFalse(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testFilenameNotContains() {
        IdentificationRequest namerequest = new TestIdentificationRequest("ABCDE");
        IdentificationRequestFilter nameFilter = createNameFilter(CriterionOperator.NOT_CONTAINS,"CD");
        assertFalse(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("abcde");
        assertFalse(nameFilter.passesFilter(namerequest));
        namerequest = new TestIdentificationRequest("ABCDDFFFDDE");
        assertFalse(nameFilter.passesFilter(namerequest));

        namerequest = new TestIdentificationRequest("ABCEDFDCE");
        assertTrue(nameFilter.passesFilter(namerequest));
    }

    @Test
    public void testLastModifiedNullFiltered() {
        IdentificationRequest request = new TestIdentificationRequest(null, null, null);
        IdentificationRequestFilter filter = createDateFilter(CriterionOperator.LT, new Date(9999));
        assertFalse(filter.passesFilter(request));
        filter = createDateFilter(CriterionOperator.LTE, new Date(9999));
        assertFalse(filter.passesFilter(request));
        filter = createDateFilter(CriterionOperator.EQ, new Date(9999));
        assertFalse(filter.passesFilter(request));
        filter = createDateFilter(CriterionOperator.NE, new Date(9999));
        assertFalse(filter.passesFilter(request));
        filter = createDateFilter(CriterionOperator.GT, new Date(9999));
        assertFalse(filter.passesFilter(request));
        filter = createDateFilter(CriterionOperator.GTE, new Date(9999));
        assertFalse(filter.passesFilter(request));
    }

    //TODO: check - is SQL filtering working on midnight 00:00 for year/month/day?

    @Test
    public void testLastModifiedLessThan() {
        IdentificationRequestFilter lastModFilter = createDateFilter(CriterionOperator.LT, getDate(2021, 2, 1));

        // Several years before is less than, but with same month day and time.
        IdentificationRequest lastModrequest = createDaterequest(getDate(2001, 2, 1, 0));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // Day before - is less than.
        lastModrequest = createDaterequest(getDate(2021, 1, 31, 11));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // same day and time - should fail.
        lastModrequest = createDaterequest(getDate(2021, 2, 1));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // later in the same day - should fail.
         lastModrequest = createDaterequest(getDate(2021, 2,1, 12));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // next day - should fail
        lastModrequest = createDaterequest(getDate(2021, 2, 2, 11));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // Nearly the 22nd century - should fail.
        lastModrequest = createDaterequest(getDate(2099, 12, 31, 23));
        assertFalse(lastModFilter.passesFilter(lastModrequest));
    }

    @Test
    public void testLastModifiedLessThanEqual() {
        IdentificationRequestFilter lastModFilter = createDateFilter(CriterionOperator.LTE, getDate(2021, 2, 1));

        // Several years before is less than, but with same month day and time - pass
        IdentificationRequest lastModrequest = createDaterequest(getDate(2001, 2, 1, 0));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // Day before - pass
        lastModrequest = createDaterequest(getDate(2021, 1, 31, 11));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // same day and time - pass.
        lastModrequest = createDaterequest(getDate(2021, 2, 1));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // later on the same day - should pass (day is still equal)
        lastModrequest = createDaterequest(getDate(2021, 2,1, 12));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // next day - fail
        lastModrequest = createDaterequest(getDate(2021, 2, 2, 11));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // Nearly the 22nd century - should fail.
        lastModrequest = createDaterequest(getDate(2099, 12, 31, 23));
        assertFalse(lastModFilter.passesFilter(lastModrequest));
    }

    @Test
    public void testLastModifiedEqual() {
        IdentificationRequestFilter lastModFilter = createDateFilter(CriterionOperator.EQ, getDate(2021, 2, 1));

        // Several years before is less than, but with same month day and time - fail
        IdentificationRequest lastModrequest = createDaterequest(getDate(2001, 2, 1, 0));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // Day before - fail
        lastModrequest = createDaterequest(getDate(2021, 1, 31, 11));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // same day and time - pass.
        lastModrequest = createDaterequest(getDate(2021, 2, 1));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // later on the same day - should pass (day is still equal)
        lastModrequest = createDaterequest(getDate(2021, 2,1, 12));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // next day - fail
        lastModrequest = createDaterequest(getDate(2021, 2, 2, 11));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // Nearly the 22nd century - should fail.
        lastModrequest = createDaterequest(getDate(2099, 12, 31, 23));
        assertFalse(lastModFilter.passesFilter(lastModrequest));
    }

    @Test
    public void testLastModifiedNotEqual() {
        IdentificationRequestFilter lastModFilter = createDateFilter(CriterionOperator.NE, getDate(2021, 2, 1));

        // Several years before is less than, but with same month day and time - pass
        IdentificationRequest lastModrequest = createDaterequest(getDate(2001, 2, 1, 0));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // Day before - pass
        lastModrequest = createDaterequest(getDate(2021, 1, 31, 11));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // same day and time - fail.
        lastModrequest = createDaterequest(getDate(2021, 2, 1));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // later on the same day - fail (day is still equal)
        lastModrequest = createDaterequest(getDate(2021, 2,1, 12));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // next day - pass
        lastModrequest = createDaterequest(getDate(2021, 2, 2, 11));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // Nearly the 22nd century - pass.
        lastModrequest = createDaterequest(getDate(2099, 12, 31, 23));
        assertTrue(lastModFilter.passesFilter(lastModrequest));
    }

    @Test
    public void testLastModifiedGreaterThan() {
        IdentificationRequestFilter lastModFilter = createDateFilter(CriterionOperator.GT, getDate(2021, 2, 1));

        // Several years before is less than, but with same month day and time - fail
        IdentificationRequest lastModrequest = createDaterequest(getDate(2001, 2, 1, 0));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // Day before - fail
        lastModrequest = createDaterequest(getDate(2021, 1, 31, 11));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // same day and time - fail.
        lastModrequest = createDaterequest(getDate(2021, 2, 1));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // later on the same day - fail (day is still equal)
        lastModrequest = createDaterequest(getDate(2021, 2,1, 12));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // next day - pass
        lastModrequest = createDaterequest(getDate(2021, 2, 2, 11));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // Nearly the 22nd century - pass.
        lastModrequest = createDaterequest(getDate(2099, 12, 31, 23));
        assertTrue(lastModFilter.passesFilter(lastModrequest));
    }

    @Test
    public void testLastModifiedGreaterThanEqual() {
        IdentificationRequestFilter lastModFilter = createDateFilter(CriterionOperator.GTE, getDate(2021, 2, 1));

        // Several years before is less than, but with same month day and time - fail
        IdentificationRequest lastModrequest = createDaterequest(getDate(2001, 2, 1, 0));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // Day before - fail
        lastModrequest = createDaterequest(getDate(2021, 1, 31, 11));
        assertFalse(lastModFilter.passesFilter(lastModrequest));

        // same day and time - pass.
        lastModrequest = createDaterequest(getDate(2021, 2, 1));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // later on the same day - pass (day is still equal)
        lastModrequest = createDaterequest(getDate(2021, 2,1, 12));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // next day - pass
        lastModrequest = createDaterequest(getDate(2021, 2, 2, 11));
        assertTrue(lastModFilter.passesFilter(lastModrequest));

        // Nearly the 22nd century - pass.
        lastModrequest = createDaterequest(getDate(2099, 12, 31, 23));
        assertTrue(lastModFilter.passesFilter(lastModrequest));
    }

    @Test
    public void testExtensionNullFiltered() {
        IdentificationRequest request = new TestIdentificationRequest(null, null, null);
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.EQ, "bmp");
        assertFalse(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.NE, "bmp");
        assertFalse(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.STARTS_WITH, "bmp");
        assertFalse(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.NOT_STARTS_WITH, "bmp");
        assertFalse(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.ENDS_WITH, "bmp");
        assertFalse(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.NOT_ENDS_WITH, "bmp");
        assertFalse(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.CONTAINS, "bmp");
        assertFalse(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.NOT_CONTAINS, "bmp");
        assertFalse(filter.passesFilter(request));
    }

    @Test
    public void testSingleExtensionEqualTo() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.EQ, "bmp");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmp");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.BMP");
        assertTrue(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.EQ, "BMP");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmp");
        assertTrue(filter.passesFilter(request));
    }

    @Test
    public void testMultiExtensionEqualTo() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.EQ, "bmp", "jpg");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.JPG");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.BMP");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.PDF");
        assertFalse(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.EQ, "bmp", "jpg", "pdf");
        assertTrue(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.EQ, "BMP", "jpg", "PDF");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bMp");
        assertTrue(filter.passesFilter(request));
    }

    @Test
    public void testSingleExtensionNotEqualTo() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.NE, "bmp");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmp");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.BMP");
        assertFalse(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.NE, "BMP");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmp");
        assertFalse(filter.passesFilter(request));
    }

    @Test
    public void testMultiExtensionNotEqualTo() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.NE, "bmp", "jpg");
        System.out.println(filter.passesFilter(request));
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.JPG");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.BMP");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.PDF");
        assertTrue(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.NE, "bmp", "jpg", "pdf");
        assertFalse(filter.passesFilter(request));
        filter = createExtensionFilter(CriterionOperator.NE, "BMP", "jpg", "PDF");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bMp");
        assertFalse(filter.passesFilter(request));
    }

    @Test
    public void testExtensionStartsWith() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.STARTS_WITH, "b");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmp");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.Bmp");
        assertTrue(filter.passesFilter(request));
    }

    @Test
    public void testExtensionNotStartsWith() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.NOT_STARTS_WITH, "b");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmp");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.Bmp");
        assertFalse(filter.passesFilter(request));
    }

    @Test
    public void testMultiExtensionStartsWith() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.STARTS_WITH, "b", "jp");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmp");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.Bmp");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.JPg");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.pdf");
        assertFalse(filter.passesFilter(request));
    }

    @Test
    public void testMultiExtensionNotStartsWith() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.NOT_STARTS_WITH, "b", "jp");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmp");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.Bmp");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.JPg");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.pdf");
        assertTrue(filter.passesFilter(request));
    }

    @Test
    public void testExtensionEndsWith() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.ENDS_WITH, "p");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmp");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.Bmp");
        assertTrue(filter.passesFilter(request));
    }

    @Test
    public void testExtensionNotEndsWith() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.NOT_ENDS_WITH, "g");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.JPG");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmp");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.Bmp");
        assertTrue(filter.passesFilter(request));
    }

    @Test
    public void testMultiExtensionEndsWith() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.ENDS_WITH, "z", "pg");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.xyz");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.jpG");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.JPg");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.pdf");
        assertFalse(filter.passesFilter(request));
    }

    @Test
    public void testMultiExtensionNotEndsWith() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.NOT_ENDS_WITH, "b", "pg");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.xyz");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.jpG");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.JPg");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.pdf");
        assertTrue(filter.passesFilter(request));
    }

    @Test
    public void testExtensionContains() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.CONTAINS, "pg");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.abiggerwithpginthemiddle");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.abiggerwithPGinthemiddle");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmgp");
        assertFalse(filter.passesFilter(request));
    }

    @Test
    public void testExtensionNotContains() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.NOT_CONTAINS, "pg");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.abiggerwithpginthemiddle");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.abiggerwithPGinthemiddle");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmgp");
        assertTrue(filter.passesFilter(request));
    }

    @Test
    public void testMultiExtensionContains() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.CONTAINS, "pg", "mp");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.abiggerwithpginthemiddle");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.abiggerwithMpinthemiddle");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.abiggerwithmpinthemiddle");
        assertTrue(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmgp");
        assertFalse(filter.passesFilter(request));
    }

    @Test
    public void testMultiExtensionNotContains() {
        IdentificationRequest request = new TestIdentificationRequest("test.jpg");
        IdentificationRequestFilter filter = createExtensionFilter(CriterionOperator.NOT_CONTAINS, "pg", "mp");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.abiggerwithpginthemiddle");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.abiggerwithMpinthemiddle");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.abiggerwithmpinthemiddle");
        assertFalse(filter.passesFilter(request));
        request = new TestIdentificationRequest("test.bmgp");
        assertTrue(filter.passesFilter(request));
    }

    @Test
    public void testWidenFiltering() {
        IdentificationRequest request = new TestIdentificationRequest("test.bmp", 1000L, 0L);

        FilterCriterion extBmp = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, CriterionOperator.EQ, "bmp");
        FilterCriterion extJpg = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, CriterionOperator.EQ, "jpg");

        FilterCriterion sizeCritGTE = new BasicFilterCriterion(CriterionFieldEnum.FILE_SIZE, CriterionOperator.GTE, 1000L);
        FilterCriterion sizeCritLT = new BasicFilterCriterion(CriterionFieldEnum.FILE_SIZE, CriterionOperator.LT, 1000L);
        FilterCriterion sizeCritEQ = new BasicFilterCriterion(CriterionFieldEnum.FILE_SIZE, CriterionOperator.EQ,1000L);

        IdentificationRequestFilter filter = createMultiFilter(false, extBmp, sizeCritGTE);
        assertTrue(filter.passesFilter(request));

        filter = createMultiFilter(false, extBmp, sizeCritLT);
        assertTrue(filter.passesFilter(request));

        filter = createMultiFilter(false, extJpg, sizeCritGTE);
        assertTrue(filter.passesFilter(request));

        filter = createMultiFilter(false, extJpg, sizeCritLT);
        assertFalse(filter.passesFilter(request));

        filter = createMultiFilter(false, extJpg, sizeCritEQ);
        assertTrue(filter.passesFilter(request));
    }

    @Test
    public void testNarrowFiltering() {
        IdentificationRequest request = new TestIdentificationRequest("test.bmp", 1000L, 0L);

        FilterCriterion extBmp = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, CriterionOperator.EQ, "bmp");
        FilterCriterion extJpg = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, CriterionOperator.EQ, "jpg");

        FilterCriterion sizeCritGTE = new BasicFilterCriterion(CriterionFieldEnum.FILE_SIZE, CriterionOperator.GTE, 1000L);
        FilterCriterion sizeCritLT = new BasicFilterCriterion(CriterionFieldEnum.FILE_SIZE, CriterionOperator.LT, 1000L);
        FilterCriterion sizeCritEQ = new BasicFilterCriterion(CriterionFieldEnum.FILE_SIZE, CriterionOperator.EQ,1000L);

        IdentificationRequestFilter filter = createMultiFilter(true, extBmp, sizeCritGTE);
        assertTrue(filter.passesFilter(request));

        filter = createMultiFilter(true, extBmp, sizeCritLT);
        assertFalse(filter.passesFilter(request));

        filter = createMultiFilter(true, extJpg, sizeCritGTE);
        assertFalse(filter.passesFilter(request));

        filter = createMultiFilter(true, extJpg, sizeCritLT);
        assertFalse(filter.passesFilter(request));

        filter = createMultiFilter(true, extJpg, sizeCritEQ);
        assertFalse(filter.passesFilter(request));

        filter = createMultiFilter(true, extBmp, sizeCritEQ);
        assertTrue(filter.passesFilter(request));
    }

    /*
     * Helper methods to construct requests with metadata and filters.
     */

    private IdentificationRequestFilter createMultiFilter(boolean narrowed, FilterCriterion... criteria) {
        Filter filter = new BasicFilter(Arrays.asList(criteria), narrowed);
        return new IdentificationRequestFilter(filter);
    }
    

    private IdentificationRequestFilter createMimeTypeFilter(CriterionOperator operator, String... values) {
        final BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.MIME_TYPE, operator, convertStringArray(values));
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createFormatFilter(CriterionOperator operator, String value) {
        final BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_FORMAT, operator, value);
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createPuidrequestFilter(CriterionOperator operator, String... values) {

        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.PUID, operator, convertStringArray(values));
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createExtensionMismatchFilter(CriterionOperator operator, boolean value) {
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.EXTENSION_MISMATCH, operator, value);
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createIdCountFilter(CriterionOperator operator, int count) {
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.IDENTIFICATION_COUNT, operator, count);
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createExtensionFilter(CriterionOperator operator, String... values) {
        final BasicFilterCriterion criterion;
        if (values.length == 1) {
            criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, operator, values[0]);
        } else {
            criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, operator, convertStringArray(values));
        }
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createNodeStatusFilter(CriterionOperator operator, NodeStatus... statuses) {
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.JOB_STATUS, operator, convertObjectArray(statuses));
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createResourceTypeFilter(CriterionOperator operator, ResourceType... resourceTypes) {
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.RESOURCE_TYPE, operator, convertObjectArray(resourceTypes));
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createIdMethodFilter(CriterionOperator operator, IdentificationMethod... methods) {
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.IDENTIFICATION_METHOD, operator, convertObjectArray(methods));
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createDateFilter(CriterionOperator operator, Date date) {
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.LAST_MODIFIED_DATE, operator, date);
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createNameFilter(CriterionOperator operator, String name) {
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_NAME, operator, name);
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequestFilter createSizeFilter(CriterionOperator operator, long size) {
        BasicFilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_SIZE, operator, size);
        Filter filter = new BasicFilter(criterion);
        return new IdentificationRequestFilter(filter);
    }

    private IdentificationRequest createDaterequest(Date date) {
        return new TestIdentificationRequest("test.bmp", 100L, date.getTime());
    }

    private Object convertObjectArray(Enum<?>[] enums) {
        Object[] result = new Object[enums.length];
        for (int i = 0; i < enums.length; i++) {
            result[i] = enums[i];
        }
        return result;
    }

    private Object convertStringArray(String[] values) {
        Object[] result = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i];
        }
        return result;
    }

    private Date getDate(int year, int month, int day, int hour) {
        return new LocalDateTime(year, month, day, hour, 0).toDate();
    }

    private Date getDate(int year, int month, int day) {
        return new LocalDateTime(year, month, day, 0, 0).toDate();
    }

    /*
     *  Test implementations of IdentificationRequest.
     */


    private static class TestIdentificationRequest implements IdentificationRequest {

        private String filename;
        private String extension;
        private RequestMetaData requestMetaData;

        public TestIdentificationRequest(Long size) {
            this("test.bmp", size, 0L);
        }
        
        public TestIdentificationRequest(String filename) {
            this(filename, 0L, 0L);
        }

        public TestIdentificationRequest(Long size, Long Date) {
            this("test.bmp", 0L, 0L);
        }
        
        public TestIdentificationRequest(String filename, Long size, Long time) {
            this.filename = filename;
            setExtension(filename);
            this.requestMetaData = new RequestMetaData(size, time, filename);
        }

        private void setExtension(String filename) {
            if (filename != null) {
                int fullstop = filename.lastIndexOf('.');
                if (fullstop > 0 && fullstop < filename.length() - 1) {
                    extension = filename.substring(fullstop + 1);
                }
            }
        }

        @Override
        public byte getByte(long position) throws IOException {
            return 0;
        }

        @Override
        public WindowReader getWindowReader() {
            return null;
        }

        @Override
        public String getFileName() {
            return filename;
        }

        @Override
        public long size() {
            return requestMetaData.getSize();
        }

        @Override
        public String getExtension() {
            return extension;
        }

        @Override
        public InputStream getSourceInputStream() throws IOException {
            return null;
        }

        @Override
        public void open(Object bytesource) throws IOException {

        }

        @Override
        public RequestMetaData getRequestMetaData() {
            return requestMetaData;
        }

        @Override
        public RequestIdentifier getIdentifier() {
            return null;
        }

        @Override
        public void close() throws IOException {

        }
    }


}