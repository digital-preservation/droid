package uk.gov.nationalarchives.droid.container;

import java.io.IOException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;

/**
 *
 * @author rbrennan
 */
public interface IdentifierEngine {

    public void process(IdentificationRequest request, ContainerSignatureMatchCollection matches) throws IOException;
}