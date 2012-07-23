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
package uk.gov.nationalarchives.droid.gui.filter.domain;

import java.util.ArrayList;
import java.util.List;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;

/**
 * The filter domain - for getting filter reference data.
 * @author Alok Kumar Dash
 *
 */
public class FilterDomain {

    private List<GenericMetadata> possibleValues = new ArrayList<GenericMetadata>();

    private List<GenericMetadata> selectedValues = new ArrayList<GenericMetadata>();

    /**
     * @return all possible Metadata. 
     */
    public List<GenericMetadata> getPossibleValues() {
        return possibleValues;
    }

    /**
     * @return all selected values 
     */
    public List<GenericMetadata> getSelectedValues() {
        return selectedValues;
    }

    /**
     * @param metadata Generic Metadata.
     */
    public void addFilterCondition(GenericMetadata metadata) {
        possibleValues.add(metadata);
    }

    /**
     * 
     * @return the meta data names.
     */
    public CriterionFieldEnum[] getMetaDataNames() {

        CriterionFieldEnum[] names = new CriterionFieldEnum[possibleValues.size()];
        for (int i = 0; i < possibleValues.size(); i++) {
            names[i] = possibleValues.get(i).getMetadataName();
        }
        return names;

    }

    /**
     * @param selectedMetadataString metadata selection string
     * @return something
     */
    public GenericMetadata getMetaDataFromFieldType(CriterionFieldEnum selectedMetadataString) {
        GenericMetadata selectedMetadata = null;

        for (int i = 0; i < possibleValues.size(); i++) {
            if (selectedMetadataString.equals(possibleValues.get(i).getMetadataName())) {
                selectedMetadata = possibleValues.get(i);
            }
        }
        return selectedMetadata;
    }

}
