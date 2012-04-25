/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

/**
 * @author rflitcroft
 *
 */
public interface ContainerIdentifierFactory {

    /**
     * Returns the Container Identifier for the format given.
     * @param format the container format
     * @return the Container Identifier for the format given.
     */
    ContainerIdentifier getIdentifier(String format);

    /**
     * Registers a container identifier against a container type.
     * @param containerType the container type
     * @param containerIdentifier the container identifier
     */
    void addContainerIdentifier(String containerType, ContainerIdentifier containerIdentifier);

}
