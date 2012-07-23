/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.container;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;

/**
 *
 * @author rbrennan
 */
public class ContainerIdentifierInit {
    
    private List<String> uniqueFileEntries;
    private List<ContainerSignature> containerSignatures = new ArrayList<ContainerSignature>();
    
    /**
     * @param defs The definitions from the container signature
     * @param containerType The type of the container
     * @param formats The formats to use for identification
     * @param droidCore if not null, then removeSignatureForPuid will be called for each container signature match
     */
    public void init(final ContainerSignatureDefinitions defs, final String containerType,
            final Map<Integer, List<FileFormatMapping>> formats, final DroidCore droidCore) {
        
        final Set<String> uniqueFileSet = new HashSet<String>();
        
        for (final ContainerSignature sig : defs.getContainerSignatures()) {
            if (sig.getContainerType().equals(containerType)) {
                addContainerSignature(sig);
                uniqueFileSet.addAll(sig.getFiles().keySet());
            }
        }
        uniqueFileEntries = new ArrayList<String>(uniqueFileSet); 
        
        for (final FileFormatMapping fmt : defs.getFormats()) {
            List<FileFormatMapping> mappings = formats.get(fmt.getSignatureId());
            if (mappings == null) {
                mappings = new ArrayList<FileFormatMapping>();
                formats.put(fmt.getSignatureId(), mappings);
            }
            mappings.add(fmt);

            if (droidCore != null) {
                droidCore.removeSignatureForPuid(fmt.getPuid());
            }
        }
    }
    
    /**
     * Add a container signature to use for identification.
     * 
     * @param containerSignature The container signature
     */
    public void addContainerSignature(final ContainerSignature containerSignature) {
        containerSignatures.add(containerSignature);
    }

    /**
     * Get all container signatures used for identification.
     * 
     * @return all container signatures
     */
    public List<ContainerSignature> getContainerSignatures() {
        return containerSignatures;
    }
    
    /**
     * Get the unique file entries.
     * 
     * @return the unique file entries
     */
    public List<String> getUniqueFileEntries() {
        return uniqueFileEntries;
    }   
}
