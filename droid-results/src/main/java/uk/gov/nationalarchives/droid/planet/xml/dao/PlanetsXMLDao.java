/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.planet.xml.dao;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;

/**
 * Planet XML DAO Interface.
 * @author Alok Kumar Dash
 */
public interface PlanetsXMLDao {

    /**
     * Returns Data for planets XML.
     * @param filter Filter to be applied.
     * @return PlanetsXMLData data required for planets xml 
     */
    PlanetsXMLData getDataForPlanetsXML(Filter filter);


}
