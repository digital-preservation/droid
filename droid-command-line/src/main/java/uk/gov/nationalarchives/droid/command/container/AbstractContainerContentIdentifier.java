package uk.gov.nationalarchives.droid.command.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.gov.nationalarchives.droid.container.*;
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
    
    @Override
    public IdentificationResultCollection process (File file, IdentificationResultCollection containerResults) throws IOException {
        final IdentificationRequest request = new ContainerFileIdentificationRequest(null);

        InputStream in = null;
        try {
            in = new FileInputStream(file);
        
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