/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import java.util.ArrayList;
import java.util.List;

/**
 * @author a-mpalmer
 *
 */
public class ContainerSignatureMatchCollection {

    private final List<ContainerSignatureMatch> matches = new ArrayList<ContainerSignatureMatch>();
    private final List<String> allFileEntries; 
    
    /**
     * 
     * @param signatures     The signatures from which to contruct our match objects.
     * @param allFileEntries A list of all the unique file entries used in the signatures.
     * @param maxBytesToScan - the max bytes to binary match on, or negative meaning unlimited.
     */
    public ContainerSignatureMatchCollection(final List<ContainerSignature> signatures, 
        final List<String> allFileEntries, final long maxBytesToScan) {
        for (ContainerSignature sig : signatures) {
            ContainerSignatureMatch match = new ContainerSignatureMatch(sig, maxBytesToScan);
            matches.add(match);
        }
        this.allFileEntries = allFileEntries;
    }
    
    /**
     * 
     * @return A list of all the unique file entries
     */
    public List<String> getAllFileEntries() {
        return allFileEntries;
    }
    
    /**
     * 
     * @return A list of all the container signature match objects.
     */
    public List<ContainerSignatureMatch> getContainerSignatureMatches() {
        return matches;
    }
    
}
