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
package uk.gov.nationalarchives.droid.export.template;

import org.junit.Test;
import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplateColumnDef;

import static org.junit.Assert.*;

public class ConstantStringColumnDefTest {
    @Test
    public void should_operate_on_null_value_and_return_empty_string() {
        ConstantStringColumnDef def = new ConstantStringColumnDef("English (UK)", "Language");
        assertEquals("", def.getOperatedValue(null));
    }

    @Test
    public void should_return_input_as_it_is_when_asked_for_operated_value() {
        ConstantStringColumnDef def = new ConstantStringColumnDef("English (UK)", "Language");
        assertEquals("FoRMat", def.getOperatedValue("FoRMat"));
    }

    @Test
    public void should_return_header_label_as_set_when_defining_the_column_definition() {
        ConstantStringColumnDef def = new ConstantStringColumnDef("English (UK)", "Language");
        assertEquals("Language", def.getHeaderLabel());
    }
    @Test
    public void should_throw_exception_when_asking_for_original_column_name() {
        ConstantStringColumnDef def = new ConstantStringColumnDef("English (UK)", "Language");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> def.getOriginalColumnName());
        assertEquals("Constant String Columns do not have an associated original column name", ex.getMessage());
    }

    @Test
    public void should_return_data_as_set_in_the_definition() {
        ConstantStringColumnDef def = new ConstantStringColumnDef("English (UK)", "Language");
        assertEquals("English (UK)", def.getDataValue());
    }

    @Test
    public void should_return_column_type_as_data_modifier_column_def() {
        ConstantStringColumnDef def = new ConstantStringColumnDef("English (UK)", "Language");
        assertEquals(ExportTemplateColumnDef.ColumnType.ConstantString, def.getColumnType());
    }
}