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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class BasicFilterTest {

    private List<FilterCriterion> criteria = new ArrayList<>();
    FilterCriterion bmpCriterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_EXTENSION, CriterionOperator.EQ, "bmp");
    FilterCriterion sizeCriterion = new BasicFilterCriterion(CriterionFieldEnum.FILE_SIZE, CriterionOperator.LT, 1000L);

    @Before
    public void setup() {
        criteria.add(bmpCriterion);
        criteria.add(sizeCriterion);
    }

    @Test
    public void testEnabledByDefault() {
        BasicFilter filter = new BasicFilter(criteria, false);
        assertTrue(filter.isEnabled());
        filter = new BasicFilter(criteria, true);
        assertTrue(filter.isEnabled());
    }

    @Test
    public void getCriteria() {
        BasicFilter filter = new BasicFilter(criteria, false);
        assertEquals(criteria, filter.getCriteria());
    }

    @Test
    public void isEnabled() {
        BasicFilter filter = new BasicFilter(criteria, false, false);
        assertFalse(filter.isEnabled());
        filter = new BasicFilter(criteria, false, true);
        assertTrue(filter.isEnabled());
    }

    @Test
    public void setEnabled() {
        BasicFilter filter = new BasicFilter(criteria, false, false);
        filter.setEnabled(true);
        assertTrue(filter.isEnabled());

        filter = new BasicFilter(criteria, false, true);
        filter.setEnabled(false);
        assertFalse(filter.isEnabled());
    }

    @Test
    public void isNarrowed() {
        BasicFilter filter = new BasicFilter(criteria, false);
        assertFalse(filter.isNarrowed());
        filter = new BasicFilter(criteria, true);
        assertTrue(filter.isNarrowed());
    }

    @Test
    public void hasCriteria() {
        BasicFilter filter = new BasicFilter(criteria, false);
        assertTrue(filter.hasCriteria());

        filter = new BasicFilter(null, false);
        assertFalse(filter.hasCriteria());

        filter = new BasicFilter(new ArrayList<FilterCriterion>(), false);
        assertFalse(filter.hasCriteria());
    }

    @Test
    public void setNarrowed() {
        BasicFilter filter = new BasicFilter(criteria, false );
        filter.setNarrowed(true);
        assertTrue(filter.isNarrowed());

        filter = new BasicFilter(criteria, true);
        filter.setNarrowed(false);
        assertFalse(filter.isNarrowed());
    }

    @Test
    public void getFilterCriterion() {
        BasicFilter filter = new BasicFilter(criteria, false );
        FilterCriterion result = filter.getFilterCriterion(0);
        assertEquals(bmpCriterion, result);

        result = filter.getFilterCriterion(1);
        assertEquals(sizeCriterion, result);
    }

    @Test
    public void getNumberOfFilterCriterion() {
        BasicFilter filter = new BasicFilter(criteria, false );
        assertEquals(2, filter.getNumberOfFilterCriterion());

        criteria.clear();
        filter = new BasicFilter(criteria, false );
        assertEquals(0, filter.getNumberOfFilterCriterion());

        filter = new BasicFilter(null, false );
        assertEquals(0, filter.getNumberOfFilterCriterion());
    }

    @Test
    public void testClone() {
        testClone(new BasicFilter(criteria, false, false ));
        testClone(new BasicFilter(criteria, true, true ));
        testClone(new BasicFilter(criteria, false, true ));
        testClone(new BasicFilter(criteria, true, false ));
        testClone(new BasicFilter(null, true, false ));
        testClone(new BasicFilter(null, true, true ));
        testClone(new BasicFilter(null, false, true ));
        testClone(new BasicFilter(null, true, false ));

    }

    private void testClone(BasicFilter original) {
        Filter clone = original.clone();
        assertEquals(clone.isNarrowed(), original.isNarrowed());
        assertEquals(clone.isEnabled(), original.isEnabled());
        assertEquals(clone.getNumberOfFilterCriterion(), original.getNumberOfFilterCriterion());

        if (original.getCriteria() != Collections.EMPTY_LIST) {
            assertFalse(clone.getCriteria() == original.getCriteria());
        }

        for (int i = 0; i < original.getNumberOfFilterCriterion(); i++) {
            FilterCriterion clonecrit = clone.getFilterCriterion(i);
            FilterCriterion basiccrit = original.getFilterCriterion(i);
            assertEquals(clonecrit, basiccrit);
        }
    }
}