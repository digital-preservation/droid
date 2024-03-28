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
package uk.gov.nationalarchives.droid.gui.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SortedComboBoxModelTest {
    @Test
    public void should_show_the_string_entries_in_sorted_order() {
        List<String> items = Arrays.asList("First Item", "0th item", "last item", "First item");
        SortedComboBoxModel<String> model = new SortedComboBoxModel<>(items);
        assertEquals("0th item", model.getElementAt(0));
        assertEquals("First Item", model.getElementAt(1));
        assertEquals("First item", model.getElementAt(2));
        assertEquals("last item", model.getElementAt(3));
    }

    @Test
    public void should_show_the_numeric_entries_in_sorted_order() {
        List<Integer> items = Arrays.asList(12, 3, 4, 0);
        SortedComboBoxModel<Integer> model = new SortedComboBoxModel<>(items);
        assertEquals(0, model.getElementAt(0));
        assertEquals(3, model.getElementAt(1));
        assertEquals(4, model.getElementAt(2));
        assertEquals(12, model.getElementAt(3));
    }

    @Test
    public void should_show_the_entries_based_on_custom_sorted_order_based_on_comparable_implemntation() {
        List<IntAsStringSortedItem> items = Arrays.asList(new IntAsStringSortedItem(12),
                new IntAsStringSortedItem(3),
                new IntAsStringSortedItem(4),
                new IntAsStringSortedItem(0));
        SortedComboBoxModel<IntAsStringSortedItem> model = new SortedComboBoxModel<>(items);
        assertEquals("0", ((IntAsStringSortedItem)model.getElementAt(0)).valAsString);
        assertEquals("12", ((IntAsStringSortedItem)model.getElementAt(1)).valAsString);
        assertEquals("3", ((IntAsStringSortedItem)model.getElementAt(2)).valAsString);
        assertEquals("4", ((IntAsStringSortedItem)model.getElementAt(3)).valAsString);
    }

    static class IntAsStringSortedItem implements Comparable<Object> {
        private final String valAsString;

        IntAsStringSortedItem(Integer someVal) {
            this.valAsString = someVal.toString();
        }
        @Override
        public int compareTo(Object o) {
            IntAsStringSortedItem that = (IntAsStringSortedItem) o;
            return this.valAsString.compareTo(that.valAsString);
        }
    }
}