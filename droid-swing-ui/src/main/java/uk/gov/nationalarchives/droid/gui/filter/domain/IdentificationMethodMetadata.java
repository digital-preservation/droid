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

import org.apache.commons.lang.StringUtils;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;


/**
 * Identification method metatdata.
 * @author adash
 *
 */
public class IdentificationMethodMetadata extends GenericMetadata {

    /**
     * Default constructor.
     */
    public IdentificationMethodMetadata() {
        super(CriterionFieldEnum.IDENTIFICATION_METHOD);
        addOperation(CriterionOperator.ANY_OF);
        addOperation(CriterionOperator.NONE_OF);
        int index = 0;
        for (IdentificationMethod identificationMethod : IdentificationMethod.values()) {
            if (!identificationMethod.equals(IdentificationMethod.NULL)) {
                addPossibleValue(new FilterValue(index++, identificationMethod
                        .getMethod(), String.valueOf(identificationMethod.ordinal())));
            }
        }
    }


    @Override
    public boolean isFreeText() {
        return false;
    }

    
    
    @Override
    public void validate(String stringTovalidate) throws FilterValidationException {
        if (StringUtils.isBlank(stringTovalidate)) {
            throw new FilterValidationException("Identification method can not be blank");
        }
    }
}
