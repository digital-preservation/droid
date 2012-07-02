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
public class Ole2ContainerContentIdentifier extends AbstractContainerContentIdentifier {
    
    private IdentificationRequest request;
    private Map<String, IdentificationResult> puidMap = new HashMap<String, IdentificationResult>();
    
    @Override
    public void process (File file, String filePuid, File tmpDir) throws IOException {
        
        request = new ContainerFileIdentificationRequest(tmpDir);
        
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
                            puidMap.put(puid, result);
                            System.out.println(file.getAbsolutePath() + "," + puid + ",OLE2 container");
                        }
                    }
                }
            }
            request.close();
            if (puidMap.isEmpty()) {
                System.out.println(file.getAbsolutePath() + "," + filePuid +",OLE2 container not further identified");
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
