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
package uk.gov.nationalarchives.droid.export.interfaces;

/**
 * Export template column definition.
 */
public interface ExportTemplateColumnDef {
    /**
     * Returns the header label to be used in the output for this column
     * @return header
     */
    String getHeaderLabel();

    /**
     * Returns the well-known column names from one of the default headers
     * Throws an exception if the definition represents a non-profile column
     * @return origianl column name
     */
    String getOriginalColumnName();

    /**
     * Returns the data value, if any associated with this column definition.
     * Throws an exception if the data is coming from profile results
     * @return data value
     */
    String getDataValue();

    /**
     * Returns the column type for this column definition
     * @return column type
     */
    ColumnType getColumnType();

    /**
     * Returns the result after performing the specific operation on the input
     * @param input String representing input data
     * @return String after performing operation associated with this column definition.
     */
    String getOperatedValue(String input);

    /**
     * Column type as defined in the ExportTemplate. There are 3 types of columns.
     */
    enum ColumnType {
        /**
         * ProfileResourceNode - The data comes directly from the profile result.
         */
        ProfileResourceNode,
        /**
         * ConstantString - The data comes directly from the constant value in the template.
         */
        ConstantString,
        /**
         * DataModifier - The data is modified as per the operation associated with column.
         */
        DataModifier
    }

    /**
     * Data modification operations for the DataModifier columns type.
     * At this time, only case change is supported
     */
    enum DataModification {
        /**
         * Convert the given string value to lowercase.
         */
        LCASE("LCASE"),
        /**
         * Convert the given string value to uppercase.
         */
        UCASE("UCASE");

        private final String label;

        DataModification(String label) {
            this.label = label;
        }
    }
}
