/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * @author rflitcroft
 *
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create .
     * new instances of schema derived classes for package: uk.gov.nationalarchives.droid.profile
     * 
     */
    public ObjectFactory() {
    }
    
    /**
     * Create an instance of {@link ProfileSpec }.
     * @return a new profile spec instance
     */
    public ProfileSpec createProfileSpec() {
        return new ProfileSpec();
    }

    /**
     * Create an instance of {@link ProfileSpec }.
     * @return a new profile instance
     * 
     */
    public ProfileInstance createProfile() {
        return new ProfileInstance();
    }
    
}
