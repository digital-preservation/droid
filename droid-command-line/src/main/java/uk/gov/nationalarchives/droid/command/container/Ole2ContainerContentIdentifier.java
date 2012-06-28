/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.gov.nationalarchives.droid.command.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.*;
import uk.gov.nationalarchives.droid.core.interfaces.*;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 *
 * @author rbrennan
 */
public class Ole2ContainerContentIdentifier extends AbstractContainerContentIdentifier {
    
    private IdentificationRequest request;
    private RequestMetaData requestMetaData;
    private RequestIdentifier requestIdentifier;
    private List<FileFormatMapping> fileFormatMapping;
    private Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>(); 
    
    @Override
    public void process (ContainerSignatureDefinitions defs, File file, File tmpDir) throws IOException {
        System.out.println("......got OLE2");
        
        URI resourceUri = file.toURI();

        RequestMetaData metaData = new RequestMetaData(file.length(),
                 file.lastModified(), file.getName());
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);
        request = new ContainerFileIdentificationRequest(tmpDir);
        System.out.println("......got request");
        
        
        
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        
            request.open(in);
            System.out.println("......opened request");
        
            List<ContainerSignature> containerSignatures = new ArrayList<ContainerSignature>();
            Set<String> uniqueFileSet = new HashSet<String>();
        
            //TODO defs should only be parsed here once, not per file!
            //@see uk.gov.nationalarchives.droid.container.AbstractContainerIdentifier#init()
            for (ContainerSignature sig : defs.getContainerSignatures()) {
                if (sig.getContainerType().equals("OLE2")) { //TODO dont hardcode OLE2
                    containerSignatures.add(sig);
                    uniqueFileSet.addAll(sig.getFiles().keySet());
                }
            }
            List<String> uniqueFileEntries = new ArrayList<String>(uniqueFileSet); 
        
             //TODO defs should only be parsed here once, not per file!
            //@see uk.gov.nationalarchives.droid.container.AbstractContainerIdentifier#init()
            for (FileFormatMapping fmt : defs.getFormats()) {
                List<FileFormatMapping> mappings = formats.get(fmt.getSignatureId());
                if (mappings == null) {
                    mappings = new ArrayList<FileFormatMapping>();
                    formats.put(fmt.getSignatureId(), mappings);
                }
                mappings.add(fmt);
                //droidCore.removeSignatureForPuid(fmt.getPuid());
            }
            
            int maxBytesToScan = -1; //TODO dont hardcode this either! 
            ContainerSignatureMatchCollection matches =
                new ContainerSignatureMatchCollection(containerSignatures, uniqueFileEntries, maxBytesToScan);
        
        
            getIdentifierEngine().process(request, matches);
            System.out.println("......ran request");
        
            fileFormatMapping = defs.getFormats();
            
            
            
            //IdentificationResultCollection results = new IdentificationResultCollection(request);
            for (ContainerSignatureMatch match : matches.getContainerSignatureMatches()) {
                if (match.isMatch()) {
                    
                    ContainerSignature conSig = match.getSignature();
                    
                    List<FileFormatMapping> mappings = formats.get(match.getSignature().getId());
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
