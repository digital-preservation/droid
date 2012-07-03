package uk.gov.nationalarchives.droid.command.container;

import java.io.File;
import java.io.IOException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;

/**
 *
 * @author rbrennan
 */
public interface ContainerContentIdentifier {
    
    public void init(ContainerSignatureDefinitions defs, String containerType);
    
    public IdentificationResultCollection process(File file, String filePuid, IdentificationResultCollection containerResults) throws IOException ;
}