package uk.gov.nationalarchives.droid.command.container;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequest;
import uk.gov.nationalarchives.droid.container.ContainerIdentifierInit;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.container.FileFormatMapping;
import uk.gov.nationalarchives.droid.container.IdentifierEngine;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;

/**
 *
 * @author rbrennan
 */
public abstract class AbstractContainerContentIdentifier implements ContainerContentIdentifier {
    
    private ContainerIdentifierInit containerIdentifierInit;
    private IdentifierEngine identifierEngine;
    
    private Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>(); 
    
    /**
     * Get the Identifier engine.
     * 
     * @return The identifier engine
     */
    public IdentifierEngine getIdentifierEngine() {
        return identifierEngine;
    }

    /**
     * @param identifierEngine The identifier engine
     */
    public void setIdentifierEngine(final IdentifierEngine identifierEngine) {
        this.identifierEngine = identifierEngine;
    }
    
    /**
     * @param defs The Container Signature Definitions
     * @param containerType The Container Type
     */
    @Override
    public void init(final ContainerSignatureDefinitions defs, final String containerType) {
        containerIdentifierInit = new ContainerIdentifierInit();
        containerIdentifierInit.init(defs, containerType, formats, null);
    }

    /**
     * @return The Container Identifier Initializer
     */
    public ContainerIdentifierInit getContainerIdentifierInit() {
        return containerIdentifierInit;
    }

    /**
     * @return The File Format mappings
     */
    public Map<Integer, List<FileFormatMapping>> getFormats() {
        return formats;
    }
    
    /**
     * @param in The input stream to identify
     * @param containerResults The results object to populate
     *
     * @return The identified results
     * 
     * @throws IOException If an error occurs with reading the input stream
     */
    @Override
    public IdentificationResultCollection process(
        final InputStream in, final IdentificationResultCollection containerResults) throws IOException {
        
        final IdentificationRequest request = new ContainerFileIdentificationRequest(null);

        try {
            request.open(in);
            
            int maxBytesToScan = -1;
            ContainerSignatureMatchCollection matches =
                new ContainerSignatureMatchCollection(getContainerIdentifierInit().getContainerSignatures(),
                    getContainerIdentifierInit().getUniqueFileEntries(), maxBytesToScan);
        
            getIdentifierEngine().process(request, matches);
        
            final Map<String, String> puidMap = new HashMap<String, String>();      
            for (ContainerSignatureMatch match : matches.getContainerSignatureMatches()) {
                if (match.isMatch()) {
                    List<FileFormatMapping> mappings = getFormats().get(match.getSignature().getId());
                    for (FileFormatMapping mapping : mappings) {
                        IdentificationResultImpl result = new IdentificationResultImpl();
                        result.setMethod(IdentificationMethod.CONTAINER);
                        result.setRequestMetaData(request.getRequestMetaData());
                        String puid = mapping.getPuid();
                        result.setPuid(mapping.getPuid());
                        if (!puidMap.containsKey(puid)) {
                            puidMap.put(puid, "");
                            containerResults.addResult(result);
                        }
                    }
                }
            }
            request.close();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return containerResults;
    }
}
