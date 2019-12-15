/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.signature;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.gov.nationalarchives.droid.core.IdentificationRequestByteReaderAdapter;
import uk.gov.nationalarchives.droid.core.SignatureFileParser;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.FileFormatCollection;
import uk.gov.nationalarchives.droid.core.signature.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;

/**
 * Implementation of DroidCore which uses the droid binary signatures and container signatures
 * to identify files, and can also match against file extensions.
 *
 * @author rflitcroft, mpalmer
 */
public class SignatureIdentifier implements DroidCore {

    private static final String FILE_SCHEME = "file://";
    /**
     * A class which can return the correct container signature identifier given its container type (e.g. ZIP or OLE2).
     */
    private ContainerIdentifierFactory containerIdentifierFactory;
    /**
     * A class which can return the correct container type given a PUID.
     */
    private ArchiveFormatResolver containerFormatResolver;

    /**
     * The binary signatures.
     */
    private FFSignatureFile sigFile;

    /**
     * The URI of the binary signature file to parse.
     * TODO: this class shouldn't be doing the parsing - split this out into another bean.
     */
    private URI signatureFile;
    
    /**
     * Default constructor.
     */
    public SignatureIdentifier() { }

    /**
     * Parameterized constructor.
     *
     * @param signatureFile The path to the binary signature file.
     * @param containerFormatResolver the class which sees if there is a container format identifier registered for identifications made.
     * @param containerIdentifierFactory The class which can provide a container identifier given the container type.
     * @throws SignatureParseException if there was a problem parsing the binary signatures.
     */
    public SignatureIdentifier(URI signatureFile,
                               ArchiveFormatResolver containerFormatResolver,
                               ContainerIdentifierFactory containerIdentifierFactory) throws SignatureParseException {
        setSignatureFile(signatureFile);
        setContainerFormatResolver(containerFormatResolver);
        setContainerIdentifierFactory(containerIdentifierFactory);
        init();
    }

    /**
     * Sets the container identifier factory.
     * @param containerIdentifierFactory The container identifier factory to set.
     */
    public void setContainerIdentifierFactory(ContainerIdentifierFactory containerIdentifierFactory) {
        this.containerIdentifierFactory = containerIdentifierFactory;
    }

    /**
     * Initialises this droid core with its signature file.
     * 
     * @throws SignatureParseException When a signature could not be parsed
     */
    public void init() throws SignatureParseException {
        if (sigFile == null) {
            try {
                SignatureFileParser sigFileParser = new SignatureFileParser();
                if (signatureFile.getScheme() == null) {
                    signatureFile = new URI(FILE_SCHEME + signatureFile.toASCIIString());
                }
                sigFile = sigFileParser.parseSigFile(Paths.get(signatureFile));
                sigFile.prepareForUse();
            } catch (IllegalArgumentException | URISyntaxException ex) {
                throw new SignatureParseException(ex.getMessage(), ex);
            }
        }
    }
    
    @Override
    public IdentificationResultCollection match(IdentificationRequest request, boolean allExtensions) throws IOException {
        IdentificationResultCollection matches = matchBinarySignatures(request);
        String containerFormat = getContainerFormat(matches);
        if (containerFormat != null) {
            IdentificationResultCollection containerMatches = matchContainerSignatures(request, containerFormat);
            if (containerMatches != null) {
                matches = containerMatches;
            }
        }
        removeLowerPriorityHits(matches);
        return processExtensions(request, matches, allExtensions);
    }

    @Override
    public IdentificationResultCollection matchBinarySignatures(IdentificationRequest request) {
        //BNO: Called once for each identification request
        IdentificationResultCollection results = new IdentificationResultCollection(request);
        results.setRequestMetaData(request.getRequestMetaData());
        ByteReader byteReader = new IdentificationRequestByteReaderAdapter(request);
        sigFile.runFileIdentification(byteReader);
        final int numHits = byteReader.getNumHits();
        for (int i = 0; i < numHits; i++) {
            FileFormatHit hit = byteReader.getHit(i);
            IdentificationResultImpl result = new IdentificationResultImpl();
            result.setMimeType(hit.getMimeType());
            result.setName(hit.getFileFormatName());
            result.setVersion(hit.getFileFormatVersion());
            result.setPuid(hit.getFileFormatPUID());
            result.setMethod(IdentificationMethod.BINARY_SIGNATURE);
            results.addResult(result);
        }
        results.setFileLength(request.size());
        results.setRequestMetaData(request.getRequestMetaData());
        return results;
    }

    @Override
    public IdentificationResultCollection matchContainerSignatures(IdentificationRequest request, String containerType) throws IOException {
        if (containerType != null) {
            ContainerIdentifier containerIdentifier = containerIdentifierFactory.getIdentifier(containerType);
            //containerIdentifier.setMaxBytesToScan(maxBytesToScan); //TODO: where should max bytes to scan be set?
            IdentificationResultCollection containerResults = containerIdentifier.submit(request);
            containerResults.setFileLength(request.size());
            containerResults.setRequestMetaData(request.getRequestMetaData());
            return containerResults;
        }
        return null;
    }

    @Override
    public IdentificationResultCollection matchExtensions(
            IdentificationRequest request, boolean allExtensions) {
        IdentificationResultCollection results = new IdentificationResultCollection(request);
        results.setRequestMetaData(request.getRequestMetaData());
        String fileExtension = request.getExtension();
        if (fileExtension != null && !fileExtension.isEmpty()) {
            List<FileFormat> fileFormats;
            if (allExtensions) {
                fileFormats = sigFile.getFileFormatsForExtension(fileExtension);
            } else {
                fileFormats = sigFile.getTentativeFormatsForExtension(fileExtension);
            }
            if (fileFormats != null) {
                final int numFormats = fileFormats.size();
                for (int i = 0; i < numFormats; i++) {
                    final FileFormat format = fileFormats.get(i);
                    IdentificationResultImpl result = new IdentificationResultImpl();
                    result.setName(format.getName());
                    result.setVersion(format.getVersion());
                    result.setPuid(format.getPUID());
                    result.setMimeType(format.getMimeType());
                    result.setMethod(IdentificationMethod.EXTENSION);
                    results.addResult(result);
                }
            }
        }
        results.setFileLength(request.size());
        results.setRequestMetaData(request.getRequestMetaData());
        return results;
    }    
    
    @Override
    public void removeSignatureForPuid(String puid) {
        sigFile.puidHasOverridingSignatures(puid);
    }

    @Override
    public void removeLowerPriorityHits(
            IdentificationResultCollection results) {
        // Build a set of format ids the results have priority over:
        FileFormatCollection allFormats = sigFile.getFileFormatCollection();
        Set<Integer> lowerPriorityIDs = new HashSet<Integer>();
        final List<IdentificationResult> theResults = results.getResults();
        int numResults = theResults.size();
        for (int i = 0; i < numResults; i++) {
            final IdentificationResult result = theResults.get(i);
            final String resultPUID = result.getPuid();
            final FileFormat format = allFormats.getFormatForPUID(resultPUID);
            lowerPriorityIDs.addAll(format.getFormatIdsHasPriorityOver());
        }
        
        // If a result has an id in this set, add it to the remove list;
        List<IdentificationResult> lowerPriorityResults = new ArrayList<IdentificationResult>();
        for (int i = 0; i < numResults; i++) {
            final IdentificationResult result = theResults.get(i);
            final String resultPUID = result.getPuid();
            final FileFormat format = allFormats.getFormatForPUID(resultPUID);
            if (lowerPriorityIDs.contains(format.getID())) {
                lowerPriorityResults.add(result);
            }
        }
         
        // Now remove any lower priority results from the collection:
        numResults = lowerPriorityResults.size();
        for (int i = 0; i < numResults; i++) {
            final IdentificationResult result = lowerPriorityResults.get(i);
            results.removeResult(result);
        }
    }

    /**
     * If there is no extension, then issue a mismatch warning if
     * any of the file formats have an extension defined.
     * 
     * If there is an extension, then issue a mismatch warning if
     * any of the result formats do not match the given extension,
     * 
     * If there are no identified file formats at all, then do not 
     * issue a format mismatch warning no matter what the extension.
     * 
     * {@inheritDoc}   
     */
    @Override
    public void checkForExtensionsMismatches(
            IdentificationResultCollection results, String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            FileFormatCollection allFormats = sigFile.getFileFormatCollection();
            final List<IdentificationResult> theResults = results.getResults();
            // garbage reduction: use indexed loop instead of allocating iterator.
            final int numResults = theResults.size();
            for (int i = 0; i < numResults; i++) {
                final IdentificationResult result = theResults.get(i);
                final String resultPUID = result.getPuid();
                final FileFormat format = allFormats.getFormatForPUID(resultPUID);
                if (format.getNumExtensions() > 0) {
                    results.setExtensionMismatch(true);
                    break;
                }
            }
        } else {
            FileFormatCollection allFormats = sigFile.getFileFormatCollection();
            final List<IdentificationResult> theResults = results.getResults();
            // garbage reduction: use indexed loop instead of allocating iterator.
            final int numResults = theResults.size();
            for (int i = 0; i < numResults; i++) {
                final IdentificationResult result = theResults.get(i);
                final String resultPUID = result.getPuid();
                final FileFormat format = allFormats.getFormatForPUID(resultPUID);
                if (format.hasExtensionMismatch(fileExtension)) {
                    results.setExtensionMismatch(true);
                    break;
                }
            }
        }
    }

    @Override
    public void setMaxBytesToScan(long maxBytes) {
        sigFile.setMaxBytesToScan(maxBytes);
        //TODO: set on container identifiers too?
    }

    /**
     * Sets the signature file path as a URI.
     * @param signatureFile the signature file to set
     */
    @Override
    public void setSignatureFile(final String signatureFile) {
        this.signatureFile = Paths.get(signatureFile).toUri();
    }

    /**
     * @return the sigFile, null if not initialized.
     */
    public FFSignatureFile getSigFile() {
        return sigFile;
    }

    /**
     * Sets the container format resolver to use.
     * @param containerFormatResolver the container format resolver to use.
     */
    public void setContainerFormatResolver(ArchiveFormatResolver containerFormatResolver) {
        this.containerFormatResolver = containerFormatResolver;
    }

    /**
     * Sets the signature file URI to parse.
     * @param signatureFileURI the URI of the signature file to parse.
     */
    public void setSignatureFile(URI signatureFileURI) {
        this.signatureFile = signatureFileURI;
    }

    /**
     * Returns a set of results with extension signature information added.
     * If there are no existing results, it will match on extensions only.
     * If there are existing results, it will look for extension mismatches.
     * @param request The request
     * @param results The results
     * @param allExtensions Whether to match extensions only for formats with no other signatures defined,
     *                      or whether to match extensions on all known formats, even if they have other signatures.
     * @return the results
     */
    protected IdentificationResultCollection processExtensions(IdentificationRequest request,
                                                               IdentificationResultCollection results,
                                                               boolean allExtensions) {
        List<IdentificationResult> resultList = results.getResults();
        // If we have no results at all so far:
        if (resultList != null && resultList.isEmpty()) {
            IdentificationResultCollection extensionResults = matchExtensions(request, allExtensions);
            if (extensionResults != null) {
                return extensionResults;
            }
        } else { // check for extensions mismatches in the results we have.
            checkForExtensionsMismatches(results, request.getExtension());
        }
        return results;
    }

    /**
     * Returns a container format identifier (e.g. ZIP or OLE2) given a set of identification results which matches
     * one of the trigger PUIDs for that type of container, or null if there is no container format match in the results.
     * @param results Identification results obtained so far.
     * @return A container format if one of them matches a container trigger PUID, or null if they don't.
     */
    protected String getContainerFormat(IdentificationResultCollection results) {
        if (containerFormatResolver != null) {
            final List<IdentificationResult> theResults = results.getResults();
            final int numResults = theResults.size(); // use an indexed loop to reduce garbage, don't allocate an iterator.
            for (int i = 0; i < numResults; i++) {
                final IdentificationResult result = theResults.get(i);
                final String format = containerFormatResolver.forPuid(result.getPuid());
                if (format != null) {
                    return format;
                }
            }
        }
        return null;
    }

}
