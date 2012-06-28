package uk.gov.nationalarchives.droid.container;

import java.util.*;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;

/**
 *
 * @author rbrennan
 */
public class ContainerIdentifierInit {
    
    private List<String> uniqueFileEntries;
    private List<ContainerSignature> containerSignatures = new ArrayList<ContainerSignature>();
    
    /**
     * @param defs
     * @param containerType
     * @param droidCore if not null, then removeSignatureForPuid will be called for each container signature match
     */
    public void init(final ContainerSignatureDefinitions defs, final String containerType, final Map<Integer, List<FileFormatMapping>> formats, final DroidCore droidCore) {
        
        final Set<String> uniqueFileSet = new HashSet<String>();
        
        for(final ContainerSignature sig : defs.getContainerSignatures()) {
            if(sig.getContainerType().equals(containerType)) {
                addContainerSignature(sig);
                uniqueFileSet.addAll(sig.getFiles().keySet());
            }
        }
        uniqueFileEntries = new ArrayList<String>(uniqueFileSet); 
        
        for(final FileFormatMapping fmt : defs.getFormats()) {
            List<FileFormatMapping> mappings = formats.get(fmt.getSignatureId());
            if (mappings == null) {
                mappings = new ArrayList<FileFormatMapping>();
                formats.put(fmt.getSignatureId(), mappings);
            }
            mappings.add(fmt);

            if(droidCore != null) {
                droidCore.removeSignatureForPuid(fmt.getPuid());
            }
        }
    }
    
    public void addContainerSignature(final ContainerSignature containerSignature) {
        containerSignatures.add(containerSignature);
    }

    public List<ContainerSignature> getContainerSignatures() {
        return containerSignatures;
    }
    
    public List<String> getUniqueFileEntries() {
        return uniqueFileEntries;
    }   
}