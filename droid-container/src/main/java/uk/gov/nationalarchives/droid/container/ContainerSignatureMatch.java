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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignatureCollection;

/**
 * Class which tracks matching against a container signature.
 * @author rflitcroft
 *
 */
public class ContainerSignatureMatch {

    private ContainerSignature signature;
    private long maxBytesToScan = -1;
    
    private Set<String> unmatchedFiles = new HashSet<String>();
    
    /**
     * Constructs a new Container signature match.
     * @param sig the signature to match against
     * @param maxBytesToScan - the max bytes to binary match on, or negative meaning unlimited.
     */
    public ContainerSignatureMatch(ContainerSignature sig, long maxBytesToScan) {
        unmatchedFiles.addAll(sig.getFiles().keySet());
        this.signature = sig;
        this.maxBytesToScan = maxBytesToScan;
    }
    
    /**
     * 
     * @return The set of unmatched files.
     */
    public Set<String> getUnmatchedFiles() {
        return unmatchedFiles;
    }
    
    /**
     * 
     * @return true if the signature has matched completely; false otherwise
     */
    public boolean isMatch() {
        return unmatchedFiles.isEmpty();
    }
    
    /**
     * Matches a file entry name against the signature.
     * If there are no signatures defined, just having the
     * filename is enough to match it.
     * @param entryName the name of the container file entry
     */
    public void matchFileEntry(String entryName) {
        if (unmatchedFiles.contains(entryName)) {
            //String textSig = signature.getFiles().get(entryName).getTextSignature();
            InternalSignatureCollection binSigs = signature.getFiles().get(entryName).getCompiledBinarySignatures();
            //if (textSig == null && binSig == null) {
            if (binSigs == null) {
                unmatchedFiles.remove(entryName);
            }
        }
    }
    
    /**
     * Determines if an entry requires a text signature match.
     * @param entryName the name of the container file path
     * @return true if this file is subject to a text signature; false otherwise
     */
    /*
    public boolean needsTextMatch(String entryName) {
        boolean needsMatch = false;
        if (unmatchedFiles.contains(entryName)) {
            String textSig = signature.getFiles().get(entryName).getTextSignature();
            needsMatch = textSig != null;
        }
        
        return needsMatch;
    }
    */
    
    /**
     * Determines if an entry requires a text signature match.
     * @param entryName the name of the container file path
     * @return true if this file is subject to a text signature; false otherwise
     */
    public boolean needsBinaryMatch(String entryName) {
        boolean needsMatch = false;
        if (unmatchedFiles.contains(entryName)) {
            InternalSignatureCollection binarySigs = signature.getFiles().get(entryName).getCompiledBinarySignatures();
            needsMatch = binarySigs != null;
        }
        return needsMatch;
    }

    /**
     * Matches some text against a text signature of a container file.
     * @param entryName the name of a container entry
     * @param content the content to me matched against a text signature
     */
    /*
    public void matchTextContent(String entryName, String content) {
        boolean matched = false;
        if (unmatchedFiles.contains(entryName)) {
            matched = true;
            String textSig = signature.getFiles().get(entryName).getTextSignature();
            if (textSig == null) {
                throw new NullPointerException(
                    String.format("No text signature for file entry [%s]. "
                        + "Use needsTextMatch(String) before calling this method.", entryName));
            }
            matched = TextSignatureMatcher.matches(textSig, content);
        }
        
        if (matched) {
            unmatchedFiles.remove(entryName);
        }
    }
    */
    
    /**
     * Matches some a binary files against a binary signature.
     * If there is no binary signature defined for the file,
     * then merely matching the name will cause a match,
     * otherwise, the match depends on whether the binary 
     * signature matche
     * @param entryName the name of a container entry
     * @param content the content to me matched against a text signature
     */
    public void matchBinaryContent(String entryName, ByteReader content) {
        boolean matched = true;
        if (unmatchedFiles.contains(entryName)) {
            Map<String, ContainerFile> sigFiles = signature.getFiles();
            InternalSignatureCollection binSigs = sigFiles.get(entryName).getCompiledBinarySignatures();
            if (binSigs != null) {
                matched = binSigs.getMatchingSignatures(content, maxBytesToScan).size() > 0;
            }
            if (matched) {
                unmatchedFiles.remove(entryName);
            }
        }
    }

    
    
    /**
     * @return the signature
     */
    public ContainerSignature getSignature() {
        return signature;
    }
    
}
