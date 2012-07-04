package uk.gov.nationalarchives.droid.command.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequest;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.container.FileFormatMapping;
import uk.gov.nationalarchives.droid.core.interfaces.*;

/**
 *
 * @author rbrennan
 */
public class ZipContainerContentIdentifier extends AbstractContainerContentIdentifier {

    private IdentificationRequest request;
    private Map<String, String> puidMap = new HashMap<String, String>();

    @Override
    public IdentificationResultCollection process (File file, IdentificationResultCollection containerResults) throws IOException {
        request = new ContainerFileIdentificationRequest(null);

        InputStream in = null;
        try {
            in = new FileInputStream(file);
        
            request.open(in);
            
            int maxBytesToScan = -1; //TODO dont hardcode this either! 
            ContainerSignatureMatchCollection matches =
                new ContainerSignatureMatchCollection(getContainerIdentifierInit().getContainerSignatures(),
                    getContainerIdentifierInit().getUniqueFileEntries(), maxBytesToScan);
        
            getIdentifierEngine().process(request, matches);
        
            puidMap.clear();        
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