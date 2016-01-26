/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;

/**
 * @author Brian O'Reilly 
 * 14/03/2014
 */
public class ExtensionMismatchMetadata extends GenericMetadata {

    private static final String DISPLAY_NAME = "File Ext-PUID mismatch";
    private static final Pattern BOOLEAN_STR_REGEX = Pattern.compile("true|false|yes|no", Pattern.CASE_INSENSITIVE);
    private static final Matcher BOOLEAN_MATCHER = BOOLEAN_STR_REGEX.matcher("");

    /** */
    public ExtensionMismatchMetadata() {
        super(CriterionFieldEnum.EXTENSION_MISMATCH);
        addOperation(CriterionOperator.EQ);

        addPossibleValue(new FilterValue(1, Boolean.toString(Boolean.TRUE), Boolean.toString(Boolean.TRUE)));
        addPossibleValue(new FilterValue(2, Boolean.toString(Boolean.FALSE), Boolean.toString(Boolean.FALSE)));
    }


    @Override
    public void validate(String stringToValidate) throws FilterValidationException {
        BOOLEAN_MATCHER.reset(stringToValidate);
        if (!BOOLEAN_MATCHER.matches()) {
            throw new FilterValidationException("Extension Mismatch must be true or false!");
        }
    }

    @Override
    public boolean isFreeText() {
        return false;
    }
}
