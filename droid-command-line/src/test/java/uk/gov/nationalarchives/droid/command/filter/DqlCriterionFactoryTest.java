package uk.gov.nationalarchives.droid.command.filter;

import junit.framework.TestCase;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DqlCriterionFactoryTest {

    //TODO: test all for collections of values as well.

    @Test
    public void testFileExtension() {
        String value = "bmp";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("file_ext", "=", value);
        assertEquals(CriterionFieldEnum.FILE_EXTENSION, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(value.toUpperCase(), criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testLastModifiedBadDate() {
        String value = "NotADate";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("last_modified", "=", value);
        assertEquals(CriterionFieldEnum.LAST_MODIFIED_DATE, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(value.toUpperCase(), criterion.getValue());
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
    public void testFileFormat() {
        String value = "Open Office Writer";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("format_name", "<>", value);
        assertEquals(CriterionFieldEnum.FILE_FORMAT, criterion.getField());
        assertEquals(CriterionOperator.NE, criterion.getOperator());
        assertEquals(value.toUpperCase(), criterion.getValue());
    }

    @Test
    public void testFileName() {
        String value = "Test Document.odf";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("file_name", "contains", value);
        assertEquals(CriterionFieldEnum.FILE_NAME, criterion.getField());
        assertEquals(CriterionOperator.CONTAINS, criterion.getOperator());
        assertEquals(value.toUpperCase(), criterion.getValue());
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
    public void testFileSize() {
        String value = "1000000";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("file_size", "<=", value);
        assertEquals(CriterionFieldEnum.FILE_SIZE, criterion.getField());
        assertEquals(CriterionOperator.LTE, criterion.getOperator());
        assertEquals(Long.valueOf(1000000), criterion.getValue());
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
    public void testPUID() {
        String value = "fmt/111";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("PUID", "none", value);
        assertEquals(CriterionFieldEnum.PUID, criterion.getField());
        assertEquals(CriterionOperator.NONE_OF, criterion.getOperator());
        assertEquals(value, criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadIdentificationMethod() {
        String value = "NotAnIDMethod";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("method", "any", value);
        assertEquals(CriterionFieldEnum.IDENTIFICATION_METHOD, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertEquals(value, criterion.getValue());
    }

    @Test
    public void testIdentificationMethod() {
        String value = IdentificationMethod.CONTAINER.name();
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("method", "any", value);
        assertEquals(CriterionFieldEnum.IDENTIFICATION_METHOD, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertEquals(value, criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadJobStatus() {
        String value = "NotAJobStatus";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("status", "any", value);
        assertEquals(CriterionFieldEnum.JOB_STATUS, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertEquals(value, criterion.getValue());
    }

    @Test
    public void testJobStatus() {
        String value = "ERROR";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("status", "any", value);
        assertEquals(CriterionFieldEnum.JOB_STATUS, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertEquals(value, criterion.getValue());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadResourceType() {
        String value = "NotAResourceType";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("type", "any", value);
        assertEquals(CriterionFieldEnum.RESOURCE_TYPE, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertEquals(value, criterion.getValue());
    }

    @Test
    public void testResourceType() {
        String value = "FOLDER";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("type", "any", value);
        assertEquals(CriterionFieldEnum.RESOURCE_TYPE, criterion.getField());
        assertEquals(CriterionOperator.ANY_OF, criterion.getOperator());
        assertEquals(value, criterion.getValue());
    }

    @Test
    public void testExtensionMismatch() {
        String value = "true";
        FilterCriterion criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(Boolean.TRUE, criterion.getValue());

        value = "false";
        criterion = DqlCriterionFactory.newCriterion("extension_mismatch", "=", value);
        assertEquals(CriterionFieldEnum.EXTENSION_MISMATCH, criterion.getField());
        assertEquals(CriterionOperator.EQ, criterion.getOperator());
        assertEquals(Boolean.FALSE, criterion.getValue());
    }

}