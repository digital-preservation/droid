/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.domain;


/**
 * Dummy metatdata.
 * 
 * @author adash
 * 
 */
public class DummyMetadata extends GenericMetadata {

    /**
     * Default constructor.
     */
    public DummyMetadata() {
        super(null);
    }

    @Override
    public boolean isFreeText() {
        return false;
    }

}
