package uk.gov.nationalarchives.droid.command.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.gov.nationalarchives.droid.container.ContainerIdentifierInit;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.FileFormatMapping;
import uk.gov.nationalarchives.droid.container.IdentifierEngine;

/**
 *
 * @author rbrennan
 */
public abstract class AbstractContainerContentIdentifier implements ContainerContentIdentifier {
    
    private ContainerIdentifierInit containerIdentifierInit;
    private IdentifierEngine identifierEngine;
    
    private Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>(); 
    
    public IdentifierEngine getIdentifierEngine() {
        return identifierEngine;
    }

    public void setIdentifierEngine(IdentifierEngine identifierEngine) {
        this.identifierEngine = identifierEngine;
    }
    
    @Override
    public void init(final ContainerSignatureDefinitions defs, final String containerType) {
        containerIdentifierInit = new ContainerIdentifierInit();
        containerIdentifierInit.init(defs, containerType, formats, null);
    }

    public ContainerIdentifierInit getContainerIdentifierInit() {
        return containerIdentifierInit;
    }

    public Map<Integer, List<FileFormatMapping>> getFormats() {
        return formats;
    }
}