package uk.gov.nationalarchives.droid.signature;


import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;

import java.io.IOException;

public class SignatureIdentifier extends BinarySignatureIdentifier {

    private ContainerIdentifierFactory containerIdentifierFactory;

    /**
     * Empty bean constructor.
     */
    public SignatureIdentifier() {
    }

    public SignatureIdentifier(ArchiveFormatResolver containerFormatResolver,
                               ContainerIdentifierFactory containerIdentifierFactory) {
        setContainerFormatResolver(containerFormatResolver);
        setContainerIdentifierFactory(containerIdentifierFactory);
    }

    public void setContainerIdentifierFactory(ContainerIdentifierFactory containerIdentifierFactory) {
        this.containerIdentifierFactory = containerIdentifierFactory;
    }

    @Override
    public IdentificationResultCollection matchContainerSignatures(IdentificationRequest request, String containerType) throws IOException {
        if (containerType != null) {
            ContainerIdentifier containerIdentifier = containerIdentifierFactory.getIdentifier(containerType);
            //containerIdentifier.setMaxBytesToScan(maxBytesToScan);
            IdentificationResultCollection containerResults = containerIdentifier.submit(request);
            containerResults.setFileLength(request.size());
            containerResults.setRequestMetaData(request.getRequestMetaData());
            return containerResults;
        }
        return null;
    }



}
