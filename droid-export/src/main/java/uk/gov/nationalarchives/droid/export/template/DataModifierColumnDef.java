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

import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplateColumnDef;

/**
 * Class for a column definition representing data modifier column in the export template.
 * e.g. name: LCASE($FILE_PATH)
 * In such case, "name" is the header for this column and the value is returned by performing
 * an associated operation on the value retrieved from the underlying profile data.
 * This type of column definition always has an underlying ProfileResourceNodeColumnDef
 */
public class DataModifierColumnDef implements ExportTemplateColumnDef {

    private final DataModification operation;
    private final ProfileResourceNodeColumnDef innerDef;

    public DataModifierColumnDef(ProfileResourceNodeColumnDef innerDef, DataModification operation) {
        this.innerDef = innerDef;
        this.operation = operation;
    }

    /**
     * Returns the header label associated with this column definition.
     * @return header label
     */
    @Override
    public String getHeaderLabel() {
        return innerDef.getHeaderLabel();
    }

    /**
     * Returns the original column name from an underlying definition.
     * @return original name of underlying column.
     */
    @Override
    public String getOriginalColumnName() {
        return innerDef.getOriginalColumnName();
    }

    /**
     * Return data value from the underlying definition
     * @return data value from underlying definition
     */
    @Override
    public String getDataValue() {
        return innerDef.getDataValue();
    }

    @Override
    public ColumnType getColumnType() {
        return ColumnType.DataModifier;
    }

    /**
     * Returns a value after performing an associated operation on the input value.
     * e.g. If the operation is LCASE, it will convert the supplied value to lowercase
     * @param input String representing input data
     * @return String value after performing associated operation.
     * Note:
     * We only support LCASE and UCASE at the moment, if more operations need to be supported.
     * they can be defined in the DataModification enum and appropriate conversion can be
     * implemented here. We only support an operation returning String by taking in a single
     * String parameter.
     *
     */
    @Override
    public String getOperatedValue(String input) {
        switch(operation) {
            case LCASE:
                return input.toLowerCase();
            case UCASE:
                return input.toUpperCase();
            default:
                throw new RuntimeException("Value conversion for operation " + operation + " is not implemented");
        }
    }
}
