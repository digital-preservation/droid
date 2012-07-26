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
