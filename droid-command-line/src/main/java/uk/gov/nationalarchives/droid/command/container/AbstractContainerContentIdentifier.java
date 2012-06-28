package uk.gov.nationalarchives.droid.command.container;

import uk.gov.nationalarchives.droid.container.IdentifierEngine;

/**
 *
 * @author rbrennan
 */
public abstract class AbstractContainerContentIdentifier implements ContainerContentIdentifier {
    private IdentifierEngine identifierEngine;

    public IdentifierEngine getIdentifierEngine() {
        return identifierEngine;
    }

    public void setIdentifierEngine(IdentifierEngine identifierEngine) {
        this.identifierEngine = identifierEngine;
    }
}