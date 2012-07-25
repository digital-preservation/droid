package uk.gov.nationalarchives.droid.command.container;

import java.io.IOException;
import java.io.InputStream;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;

/**
 *
 * @author rbrennan
 */
public interface ContainerContentIdentifier {
    
    public void init(ContainerSignatureDefinitions defs, String containerType);
    
    public IdentificationResultCollection process(InputStream in, IdentificationResultCollection containerResults) throws IOException ;
}