package uk.gov.nationalarchives.droid.command.container;

import java.util.Map;

import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;

/**
 *
 * @author rbrennan
 */
public class ContainerContentIdentifierFactory {
    
    /** key is containerType, value is bean to do the identification */
    private Map<String, ContainerContentIdentifier> containerContentIdentifiers;

    /**
     * @param containerContentIdentifiers key is containerType, value is bean to do the identification
     */
    public void setContainerContentIdentifiers(
        final Map<String, ContainerContentIdentifier> containerContentIdentifiers) {
        
        this.containerContentIdentifiers = containerContentIdentifiers;
    }
    
    /**
     * @param containerType The container type
     * @param containerSignatureDefinitions the container signature definitions for that container type
     *
     * @return The Container Content Identifier
     */
    public ContainerContentIdentifier getContainerContentIdentifier(
        final String containerType, final ContainerSignatureDefinitions containerSignatureDefinitions) {
        
        final ContainerContentIdentifier containerContentIdentifier = containerContentIdentifiers.get(containerType);
        containerContentIdentifier.init(containerSignatureDefinitions, containerType);
        return containerContentIdentifier;
    }
}
