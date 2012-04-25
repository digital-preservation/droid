/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.filter;

import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;

/**
 * Parser for DQL (DROID query language).
 * @author rflitcroft
 *
 */
public interface DqlFilterParser {

    /**
     * Parses a DQL string into a Filter object.
     * @param dql the droid query language to parse
     * @return a FilterCriterion object
     */
    FilterCriterion parse(String dql);
}
