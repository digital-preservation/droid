/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rflitcroft
 *
 */
public class ContainerIdentifierFactoryImpl implements ContainerIdentifierFactory {

    private Map<String, ContainerIdentifier> containerIdentifiers = new HashMap<String, ContainerIdentifier>();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerIdentifier getIdentifier(String format) {
        return containerIdentifiers.get(format);
    }
    
    /**
     * @param containerIdentifiers the containerIdentifiers to set
     */
    public void setContainerIdentifiers(Map<String, ContainerIdentifier> containerIdentifiers) {
        this.containerIdentifiers = containerIdentifiers;
    }
    
    /**
     * Registers a container identifier against a container type.
     * @param containerType the container type
     * @param containerIdentifier the container identifier
     */
    @Override
    public void addContainerIdentifier(String containerType, ContainerIdentifier containerIdentifier) {
        containerIdentifiers.put(containerType, containerIdentifier);
    }
}
