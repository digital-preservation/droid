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
package uk.gov.nationalarchives.droid.command.filter;

import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DqlCriterionFactoryTest {

    //TODO: test all for collections of values as well with bad elements.

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyFileExtension() {
        String value = "";
        DqlCriterionFactory.newCriterion("file_ext", "=", value);
    }

    @Test
    public void testFileExtension() {
        String value = "bmp";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("file_ext", "=", value);
        assertEquals(CriterionFieldEnum.FILE_EXTENSION, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(value.toUpperCase(), criterion.getValue());
    }

    @Test
    public void testFileExtensionCollection() {
        Collection<String> values = Arrays.asList("BMP", "DOC", "ODF", "JPG");
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("file_ext", "any", values);
        assertEquals(CriterionFieldEnum.FILE_EXTENSION, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        testObjectCollection(String.class, values, criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadFileExtensionCollection() {
        Collection<String> values = Arrays.asList("BMP", "", "ODF", "JPG");
        DqlCriterionFactory.newCriterion("file_ext", "any", values);
    }

    private <T> void testObjectCollection(Class objectType, Collection<T> values, Object value) {
        assertEquals(Object[].class, value.getClass());
        Object[] criterionValues = (Object[]) value;
        assertEquals(values.size(), criterionValues.length);
        for (Object o : criterionValues) {
            assertEquals(objectType, o.getClass());
            assertTrue(values.contains((T) o));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testLastModifiedBadDate() {
        String value = "NotADate";
        DqlCriterionFactory.newCriterion("last_modified", "=", value);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyLastModifiedDate() {
        String value = "";
        DqlCriterionFactory.newCriterion("last_modified", "=", value);
    }

    @Test
    public void testLastModifiedDate() {
        String value = "2021-04-30";
        Date dateValue = ISODateTimeFormat.date().parseDateTime(value).toDate();
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("last_modified", "=", value);
        assertEquals(CriterionFieldEnum.LAST_MODIFIED_DATE, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(dateValue, criterion.getValue());
    }

    @Test
    public void testLastModifiedDateCollection() {
        Collection<String> values = Arrays.asList("2021-04-30", "1990-01-01", "1999-12-31");
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("last_modified", "none", values);
        assertEquals(CriterionFieldEnum.LAST_MODIFIED_DATE, criterion.getField());
        assertEquals(CriterionOperator.NONE_OF, criterion.getOperator());
        testObjectCollection(Date.class, buildDateList(values), criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyFileFormat() {
        String value = "";
        DqlCriterionFactory.newCriterion("format_name", "<>", value);
    }

    @Test
    public void testFileFormat() {
        String value = "Open Office Writer";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("format_name", "<>", value);
        assertEquals(CriterionFieldEnum.FILE_FORMAT, criterion.getField());
        assertEquals(CriterionOperator.NE, criterion.getOperator());
        assertEquals(value.toUpperCase(), criterion.getValue());
    }

    @Test
    public void testFileFormatCollection() {
        Collection<String> values = Arrays.asList("Open Office Writer".toUpperCase(),
                "Tagged Image File Format".toUpperCase(), "DROID profile".toUpperCase());
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("format_name", "<>", values);
        assertEquals(CriterionFieldEnum.FILE_FORMAT, criterion.getField());
        assertEquals(CriterionOperator.NE, criterion.getOperator());
        testObjectCollection(String.class, values, criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyFileName() {
        String value = "";
        DqlCriterionFactory.newCriterion("file_name", "contains", value);
    }

    @Test
    public void testFileNameCollection() {
        Collection<String> values = Arrays.asList("Test Document.odf".toUpperCase(),
                "Results Forecast.xlsx".toUpperCase(), "DqlCriterionFactoryTest.java".toUpperCase());
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("file_name", "none", values);
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.NONE_OF, criterion.getOperator());
        testObjectCollection(String.class, values, criterion.getValue());
    }

    @Test
    public void testFileName() {
        String value = "Test Document.odf";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("file_name", "contains", value);
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.CONTAINS, criterion.getOperator());
        assertEquals(value.toUpperCase(), criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadIdentificationCount() {
        String value = "NotANumber";
        DqlCriterionFactory.newCriterion("format_count", ">=", value);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyIdentificationCount() {
        String value = "";
        DqlCriterionFactory.newCriterion("format_count", ">=", value);
    }

    @Test
    public void testIdentificationCount() {
        String value = "2";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("format_count", ">=", value);
        assertEquals(CriterionFieldEnum.IDENTIFICATION_COUNT, criterion.getField());
        assertEquals(CriterionOperator.GTE, criterion.getOperator());
        assertEquals(Integer.valueOf(2), criterion.getValue());
    }

    @Test
    public void testIdentificationCountCollection() {
        Collection<String> values = Arrays.asList("1", "3", "10");
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("format_count", "any", values);
        assertEquals(CriterionFieldEnum.IDENTIFICATION_COUNT, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        testObjectCollection(Integer.class, buildIntegerList(values), criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyFileSize() {
        String value = "";
        DqlCriterionFactory.newCriterion("file_size", "<=", value);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadFileSize() {
        String value = "NotANumber";
        DqlCriterionFactory.newCriterion("file_size", "<=", value);
    }

    @Test
    public void testFileSize() {
        String value = "1000000";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("file_size", "<=", value);
        assertEquals(CriterionFieldEnum.FILE_SIZE, criterion.getField());
        assertEquals(CriterionOperator.LTE, criterion.getOperator());
        assertEquals(Long.valueOf(1000000), criterion.getValue());
    }

    @Test
    public void testFileSizeCollection() {
        Collection<String> values = Arrays.asList("1000234", "323", "1034323423");
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("file_size", "<=", values);
        assertEquals(CriterionFieldEnum.FILE_SIZE, criterion.getField());
        assertEquals(CriterionOperator.LTE, criterion.getOperator());
        testObjectCollection(Long.class, buildLongList(values), criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyMimeType() {
        String value = "";
        DqlCriterionFactory.newCriterion("mime_type", "any", value);
    }

    @Test
    public void testMimeType() {
        String value = "text/plain";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("mime_type", "any", value);
        assertEquals(CriterionFieldEnum.MIME_TYPE, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertEquals(value, criterion.getValue());
    }

    @Test
    public void testMimeTypeCollection() {
        Collection<String> values = Arrays.asList("text/plain", "image/gif", "audio/wav");
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("mime_type", "any", values);
        assertEquals(CriterionFieldEnum.MIME_TYPE, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        testObjectCollection(String.class, values, criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyPUID() {
        String value = "";
        DqlCriterionFactory.newCriterion("PUID", "none", value);
    }

    @Test
    public void testPUID() {
        String value = "fmt/111";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("PUID", "none", value);
        assertEquals(CriterionFieldEnum.PUID, criterion.getField());
        assertEquals(CriterionOperator.NONE_OF, criterion.getOperator());
        assertEquals(value, criterion.getValue());
    }

    @Test
    public void testPUIDCollection() {
        Collection<String> values = Arrays.asList("fmt/111", "xfmt/111", "fmt/302");
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("PUID", "none", values);
        assertEquals(CriterionFieldEnum.PUID, criterion.getField());
        assertEquals(CriterionOperator.NONE_OF, criterion.getOperator());
        testObjectCollection(String.class, values, criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadIdentificationMethod() {
        String value = "NotAnIDMethod";
        DqlCriterionFactory.newCriterion("method", "any", value);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyIdentificationMethod() {
        String value = "";
        DqlCriterionFactory.newCriterion("method", "any", value);
    }

    @Test
    public void testIdentificationMethod() {
        String value = IdentificationMethod.CONTAINER.name();
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("method", "any", value);
        assertEquals(CriterionFieldEnum.IDENTIFICATION_METHOD, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertEquals(IdentificationMethod.CONTAINER, criterion.getValue());
    }

    @Test
    public void testIdentificationMethodCollection() {
        Collection<String> values = Arrays.asList("CONTAINER", "BINARY_SIGNATURE");
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("method", "any", values);
        assertEquals(CriterionFieldEnum.IDENTIFICATION_METHOD, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        testObjectCollection(IdentificationMethod.class, buildEnumList(values, IdentificationMethod.class), criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadJobStatus() {
        String value = "NotAJobStatus";
        DqlCriterionFactory.newCriterion("status", "any", value);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyJobStatus() {
        String value = "";
        DqlCriterionFactory.newCriterion("status", "any", value);
    }

    @Test
    public void testJobStatus() {
        String value = "ERROR";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("status", "any", value);
        assertEquals(CriterionFieldEnum.JOB_STATUS, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertEquals(NodeStatus.ERROR, criterion.getValue());
    }

    @Test
    public void testJobStatusCollection() {
        Collection<String> values = Arrays.asList("ERROR", "DONE", "ACCESS_DENIED");
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("status", "any", values);
        assertEquals(CriterionFieldEnum.JOB_STATUS, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        testObjectCollection(NodeStatus.class, buildEnumList(values, NodeStatus.class), criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadResourceType() {
        String value = "NotAResourceType";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("type", "any", value);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyResourceType() {
        String value = "";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("type", "any", value);
    }

    @Test
    public void testResourceTypeCollection() {
        Collection<String> values = Arrays.asList("FOLDER", "CONTAINER", "FILE");
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("type", "any", values);
        assertEquals(CriterionFieldEnum.RESOURCE_TYPE, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        testObjectCollection(ResourceType.class, buildEnumList(values, ResourceType.class), criterion.getValue());
    }

    @Test
    public void testResourceType() {
        String value = "FOLDER";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("type", "any", value);
        assertEquals(CriterionFieldEnum.RESOURCE_TYPE, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertEquals(ResourceType.FOLDER, criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadExtensionMismatch() {
        String value = "NotABoolean";
        DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyExtensionMismatch() {
        String value = "";
        DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
    }

    @Test
    public void testExtensionMismatch() {
        String value = "true";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());

        value = "tRuE";
        criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(Boolean.TRUE, criterion.getValue());

        value = "false";
        criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(Boolean.FALSE, criterion.getValue());

        value = "FaLSe";
        criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(Boolean.FALSE, criterion.getValue());

        value = "yes";
        criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(Boolean.TRUE, criterion.getValue());

        value = "yeS";
        criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(Boolean.TRUE, criterion.getValue());

        value = "no";
        criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(Boolean.FALSE, criterion.getValue());

        value = "NO";
        criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(Boolean.FALSE, criterion.getValue());
    }

    @Test
    public void testExtensionMismatchCollection() {
        Collection<String> values = Arrays.asList("yes", "true", "no");
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "any", values);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        testObjectCollection(Boolean.class, buildBooleanList(values), criterion.getValue());
    }

    private Collection<Boolean> buildBooleanList(Collection<String> values) {
        Collection<Boolean> booleans = new ArrayList<>();
        for (String value : values) {
            Boolean parsedValue = value == "yes" || value == "true"
                    ? Boolean.TRUE
                    : value == "no" || value == "false"
                    ? Boolean.FALSE
                    : null;
            booleans.add(parsedValue);
        }
        return booleans;
    }

    private Collection<Integer> buildIntegerList(Collection<String> values) {
        Collection<Integer> integers = new ArrayList<>();
        for (String value : values) {
            integers.add(Integer.valueOf(value));
        }
        return integers;
    }

    private Collection<Long> buildLongList(Collection<String> values) {
        Collection<Long> longs = new ArrayList<>();
        for (String value : values) {
            longs.add(Long.valueOf(value));
        }
        return longs;
    }

    private Collection<Date> buildDateList(Collection<String> values) {
        Collection<Date> dates = new ArrayList<>();
        for (String value : values) {
            dates.add(ISODateTimeFormat.date().parseDateTime(value).toDate());
        }
        return dates;
    }

    private Collection<Enum> buildEnumList(Collection<String> values, Class enumType) {
        Collection<Enum> enums = new ArrayList<>();
        for (String value : values) {
            enums.add(Enum.valueOf(enumType, value));
        }
        return enums;
    }

}