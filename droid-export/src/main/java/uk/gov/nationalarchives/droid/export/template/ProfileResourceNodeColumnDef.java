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
 * Class for a column definition representing an underlying profile resource node column.
 * e.g. identifier: $ID
 * In such case, "identifier" is the header for this column and the data is retrieved from the
 * ID column in the results .
 */
public class ProfileResourceNodeColumnDef implements ExportTemplateColumnDef {

    private final String originalHeaderLabel;
    private final String headerLabel;

    public ProfileResourceNodeColumnDef(String originalHeaderLabel, String headerLabel) {
        this.originalHeaderLabel = originalHeaderLabel;
        this.headerLabel = headerLabel;
    }

    /**
     * Returns the header label associated with this column definition.
     * @return header label
     */
    @Override
    public String getHeaderLabel() {
        return headerLabel;
    }

    /**
     * Returns the original column name
     * @return The well-known name of column as it appears in the profile results
     */
    @Override
    public String getOriginalColumnName() {
        return originalHeaderLabel;
    }

    /**
     * This type of column does not have data associated with it.
     * As a result, this method simply throws an exception if a consumer tries to get data from it
     * @return nothing
     */
    @Override
    public String getDataValue() {
        throw new RuntimeException("Profile resource node column uses data from the profile results");
    }

    @Override
    public ColumnType getColumnType() {
        return ColumnType.ProfileResourceNode;
    }

    /**
     * This type of column does not have any associated operation, hence returns the input value as it is.
     * @param input String representing input data
     * @return the input value as it is
     */
    @Override
    public String getOperatedValue(String input) {
        return input;
    }
}
