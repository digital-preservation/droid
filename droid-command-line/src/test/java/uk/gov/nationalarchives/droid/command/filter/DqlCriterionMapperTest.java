/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.filter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import static uk.gov.nationalarchives.droid.command.filter.DqlCriterionMapper.allDqlOperators;
import static uk.gov.nationalarchives.droid.command.filter.DqlCriterionMapper.forField;
import static uk.gov.nationalarchives.droid.command.filter.DqlCriterionMapper.forOperator;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;

/**
 * @author rflitcroft
 *
 */
public class DqlCriterionMapperTest {

    
    @Test
    public void testAllFieldMappings() {

        String[] allDqlFields = new String[] {
            "file_ext",
            "file_name",
            "file_size",
            "format_count",
            "format_name",
            "last_modified",
            "method",
            "mime_type",
            "puid",
            "status",
            "type",
        };
        
        
        int i = 0;
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.FILE_EXTENSION);
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.FILE_NAME);
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.FILE_SIZE);
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.IDENTIFICATION_COUNT);
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.FILE_FORMAT);
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.LAST_MODIFIED_DATE);
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.IDENTIFICATION_METHOD);
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.MIME_TYPE);
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.PUID);
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.JOB_STATUS);
        assertEquals(forField(allDqlFields[i++]), CriterionFieldEnum.RESOURCE_TYPE);
        
        final CriterionFieldEnum[] fieldValues = CriterionFieldEnum.values();
        assertEquals(allDqlFields.length, fieldValues.length);

        assertEquals(fieldValues.length, i);
        assertArrayEquals(allDqlFields, DqlCriterionMapper.allDqlFields());
        
    }

    @Test
    public void testAllOperationMappings() {

        String[] allDqlOperators = new String[] {
            "<",
            "<=",
            "=",
            ">=",
            ">",
            "<>",
            "starts",
            "ends",
            "contains",
            "any",
            "none",
            "not contains",
            "not starts",
            "not ends",
        };
        
        final CriterionOperator[] operatorValues = CriterionOperator.values();
        assertEquals(allDqlOperators().length, operatorValues.length);
        
        int i = 0;
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.LT);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.LTE);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.EQ);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.GTE);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.GT);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.NE);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.STARTS_WITH);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.ENDS_WITH);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.CONTAINS);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.ANY_OF);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.NONE_OF);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.NOT_CONTAINS);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.NOT_STARTS_WITH);
        assertEquals(forOperator(allDqlOperators[i++]), CriterionOperator.NOT_ENDS_WITH);
        
        assertEquals(operatorValues.length, i);
        assertArrayEquals(allDqlOperators, DqlCriterionMapper.allDqlOperators());
        
    }
}
