package uk.gov.nationalarchives.droid.container.gz;

import uk.gov.nationalarchives.droid.container.AbstractContainerIdentifier;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;

import java.io.IOException;

public class GzIdentifier extends AbstractContainerIdentifier {

    public GzIdentifier() {
        setIdentifierEngine(new GzIdentifierEngine());
    }

    @Override
    protected void process(IdentificationRequest request, ContainerSignatureMatchCollection matches) throws IOException {
        getIdentifierEngine().process(request, matches);
    }
}
