package uk.gov.nationalarchives.droid.command.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequest;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.container.FileFormatMapping;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

/**
 *
 * @author rbrennan
 */
public class Ole2ContainerContentIdentifier extends AbstractContainerContentIdentifier {
    
    private IdentificationRequest request;
    //private RequestMetaData requestMetaData;
    //private RequestIdentifier requestIdentifier;
    //private List<FileFormatMapping> fileFormatMapping;
    
    
    
    @Override
    public void process (File file, File tmpDir) throws IOException {
        System.out.println("......got OLE2");
        
        URI resourceUri = file.toURI();

        //RequestMetaData metaData = new RequestMetaData(file.length(),
          //       file.lastModified(), file.getName());
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);
        request = new ContainerFileIdentificationRequest(tmpDir);
        System.out.println("......got request");
        
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        
            request.open(in);
            System.out.println("......opened request");
            
            int maxBytesToScan = -1; //TODO dont hardcode this either! 
            ContainerSignatureMatchCollection matches =
                new ContainerSignatureMatchCollection(getContainerIdentifierInit().getContainerSignatures(), getContainerIdentifierInit().getUniqueFileEntries(), maxBytesToScan);
        
        
            getIdentifierEngine().process(request, matches);
            System.out.println("......ran request");
        
            //fileFormatMapping = defs.getFormats();
            
            
            
            //IdentificationResultCollection results = new IdentificationResultCollection(request);
            for (ContainerSignatureMatch match : matches.getContainerSignatureMatches()) {
                if (match.isMatch()) {
                    
                    //ContainerSignature conSig = match.getSignature();
                    
                    List<FileFormatMapping> mappings = getFormats().get(match.getSignature().getId());
                    for (FileFormatMapping mapping : mappings) {
                        IdentificationResultImpl result = new IdentificationResultImpl();
                        result.setMethod(IdentificationMethod.CONTAINER);
                        result.setRequestMetaData(request.getRequestMetaData());
                        result.setPuid(mapping.getPuid());
                        //results.addResult(result);
                        System.out.println(file.getAbsolutePath() + "," + mapping.getPuid() );
                    }
                }
            }
            
            System.out.println("......finished request");
            request.close();
        } finally {
            if (in != null) {
                //try {
                    in.close();
                //} catch (IOException e) {
                //    throw new CommandExecutionException(e);
                //}
            }
        }
    }
}
