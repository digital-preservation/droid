/*
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
package uk.gov.nationalarchives.droid.core.interfaces.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicFilterCriterionTest {

    @Test
    public void getField() {
        for (CriterionFieldEnum field : CriterionFieldEnum.values()) {
            FilterCriterion criterion = new BasicFilterCriterion(field, CriterionOperator.EQ, "xxx");
            assertEquals(field, criterion.getField());
        }
    }

    @Test
    public void getOperator() {
        for (CriterionOperator operator : CriterionOperator.values()) {
            FilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, operator, "xxx");
            assertEquals(operator, criterion.getOperator());
        }
    }

    @Test
    public void getValue() {
        testValue("xxx");
        testValue(1000L);
        testValue(new Object[] {"xxx", "yyy"});
    }

    private void testValue(Object value) {
        FilterCriterion criterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_SIZE, CriterionOperator.NOT_CONTAINS, value);
        assertEquals(value, criterion.getValue());
    }


}