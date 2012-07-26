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
    
     /**
     * @param defs The Container Signature Definitions
     * @param containerType The Container Type
     */
    void init(ContainerSignatureDefinitions defs, String containerType);
    
    /**
     * @param in The input stream to identify
     * @param containerResults The results object to populate
     *
     * @return The identified results
     * 
     * @throws IOException If an error occurs with reading the input stream
     */
    IdentificationResultCollection process(InputStream in,
        IdentificationResultCollection containerResults) throws IOException;
}
