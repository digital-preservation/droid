package uk.gov.nationalarchives.droid.command.container;

import java.util.Map;

/**
 *
 * @author rbrennan
 */
public class ContainerContentIdentifierFactory {
    
    //key is containerType, value is bean to do the identification
    private Map<String, ContainerContentIdentifier> containerContentIdentifiers;

    public void setContainerContentIdentifiers(Map<String, ContainerContentIdentifier> containerContentIdentifiers) {
        this.containerContentIdentifiers = containerContentIdentifiers;
    }
    
    public ContainerContentIdentifier getContainerContentIdentifier(final String contentType) {
        return containerContentIdentifiers.get(contentType);
    }
}