/**
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
package uk.gov.nationalarchives.droid.gui.filter.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * Format/PUID metadata.
 * @author adash
 */

public class PUIDMetadata extends GenericMetadata {

    private static final String DISPLAY_NAME = "PUID";
    private static final FilterValueComparator PUID_SORT = new FilterValueComparator();

    /**
     * @param data
     *            a list of formats
     */
    public PUIDMetadata(List<Format> data) {
        super(CriterionFieldEnum.PUID);
        addOperation(CriterionOperator.ANY_OF);
        addOperation(CriterionOperator.NONE_OF);
        int index = 0;

        final List<FilterValue> possibleFilterValues = new ArrayList<>();
        for (final Format format : data) {

            String formatWithVersion = "";
            if (format.getVersion() != null) {
                formatWithVersion = " ( " + format.getName() + " - " + format.getVersion() + " )";
            } else {
                formatWithVersion = " (" + format.getName() + ")";
            }

            if (format.getPuid() != null) {
                possibleFilterValues.add(new FilterValue(index++, format.getPuid() + formatWithVersion, format.getPuid()));
            }
        }

        possibleFilterValues.sort(PUID_SORT);

        for (final FilterValue value : possibleFilterValues) {
            addPossibleValue(value);
        }
    }

    @Override
    public boolean isFreeText() {
        return false;
    }

    @Override
    public void validate(String stringTovalidate) throws FilterValidationException {
        if (StringUtils.isBlank(stringTovalidate)) {
            throw new FilterValidationException("PUID can not be blank");
        }
    }

    private static class FilterValueComparator implements Comparator<FilterValue> {

        @Override
        public int compare(FilterValue o1, FilterValue o2) {
            final String puid1 = o1.getQueryParameter();
            final String puid2 = o2.getQueryParameter();
            final int separator1Pos = puid1.indexOf('/');
            final int separator2Pos = puid2.indexOf('/');

            int result = -1;

            // If the PUIDs have a / in them (they all should be we'll be careful).
            if (separator1Pos >= 0 && separator2Pos >= 0) {

                // If they aren't equal, the shorter header is less than the longer header.
                if (separator1Pos != separator2Pos) {
                    result = separator1Pos - separator2Pos;
                } else {

                    // If the strings aren't the same, we return the string comparision of the headers.
                    final String puid1header = puid1.substring(0, separator1Pos);
                    final String puid2header = puid2.substring(0, separator2Pos);
                    if (!puid1header.equals(puid2header)) {
                        result = puid1.compareTo(puid2);
                    } else {

                        // The headers are equal, we'll sort based on the PUID number (if it has numbers)
                        final String puid1ID = puid1.substring(separator1Pos + 1);
                        final String puid2ID = puid2.substring(separator2Pos + 1);
                        final int puid1Num = getInteger(puid1ID);
                        final int puid2Num = getInteger(puid2ID);

                        // If they could both parse as numbers, return the numeric comparison.
                        if (puid1Num >= 0 && puid2Num >= 0) {
                            result = puid1Num - puid2Num;
                        }
                    }
                }
            } else {

                // Can't compare on numbers or headers alone, just return string comparison.
                result = puid1.compareTo(puid2);
            }

            return result;
        }

        private int getInteger(String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }
}
