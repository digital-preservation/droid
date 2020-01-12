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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureFileReader;
import uk.gov.nationalarchives.droid.container.TriggerPuid;
import uk.gov.nationalarchives.droid.container.ole2.Ole2Identifier;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifier;
import uk.gov.nationalarchives.droid.core.IdentificationRequestByteReaderAdapter;
import uk.gov.nationalarchives.droid.core.SignatureFileParser;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.FileFormatCollection;
import uk.gov.nationalarchives.droid.core.signature.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignature;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignatureCollection;

//TODO: design for subclassing or make final?

/**
 * Implementation of DroidCore which uses the droid binary signatures and container signatures
 * to identify files, and can also match against file extensions.
 * <p>
 * This class must be initialised with the path to the binary signatures - it can do nothing without those,
 * not in the least because this is where all the file formats are defined, and the binary signatures we need
 * to identify container types.
 * <p>
 * Supplying container signatures is optional; if you supply them they will be used, if you do not, only
 * the binary signatures and extensions will be used for matching.
 *
 * @author rflitcroft, mpalmer
 */
public class SignatureIdentifier implements DroidCore {

    private static final String FILE_SCHEME = "file://";
    private static final String ZIP_CONTAINER_TYPE = "ZIP";
    private static final String OLE2_CONTAINER_TYPE = "OLE2";

    /**
     * A class which parses, then caches a container signature file.
     */
    private ContainerSignatureFileReader containerSignatureFileReader;

    /**
     * A class which processes zip container signatures.
     */
    private ContainerIdentifier zipIdentifier;

    /**
     * A collection of binary signatures that recognise zip files.
     */
    private InternalSignatureCollection zipBinarySigs;

    /**
     * A class which processes ole2 container signatures.
     */
    private ContainerIdentifier ole2Identifier;

    /**
     * A collection of binary signatures that recognise ole2 files.
     */
    private InternalSignatureCollection ole2BinarySigs;

    /**
     * The binary signatures.
     */
    private FFSignatureFile sigFile;

    /**
     * The URI of the binary signature file to parse.
     * TODO: this class shouldn't be doing the parsing - split this out into another bean, like the container signature file reader.
     */
    private URI signatureFile;

    /**
     * The max bytes to scan at the top and tail of a file or stream.  A negative number means unlimited.
     * //TODO: what should this default to?
     */
    private long maxBytesToScan = -1; // 65536; // default to 64K at top and tail of files to be identified.

    /**
     * Whether DROID should always match all extensions, or only match if no other signatures match.
     */
    private boolean matchAllExtensions;

    /**
     * Path to store temporary files created when processing internal streams of container signatures.
     */
    private Path tempDirLocation;

    /**
     * Default constructor.
     */
    public SignatureIdentifier() { }

    /**
     * Parameterized constructor.
     *
     * @param signatureFile The path to the binary signature file.
     * @param containerSignatureFileReader A class which can parse a container signature file and cache the parsed definitions.
     * @throws SignatureParseException if there was a problem parsing the binary signatures.
     */
    public SignatureIdentifier(URI signatureFile, ContainerSignatureFileReader containerSignatureFileReader)
            throws SignatureParseException {
        setSignatureFile(signatureFile);
        setContainerSignatureFileReader(containerSignatureFileReader);
        init();
    }

    //TODO: constructor for all parameters.

    /**
     * Initialises this droid core with its signature files.
     * 
     * @throws SignatureParseException When a signature could not be parsed
     */
    public void init() throws SignatureParseException {
        getBinarySignatureIdentifier().setMaxBytesToScan(maxBytesToScan);
        if (containerSignatureFileReader != null) {
            try {
                createZipIdentifier();
                createOle2Identifier();
                processContainerSignatureTriggerPuids(); // get the binary signatures that identify container formats.
            } catch (SignatureFileException e) {
                throw new SignatureParseException(e.getMessage(), e);
            }
        }
    }

    private void createZipIdentifier() throws SignatureFileException {
        ZipIdentifier zipId = new ZipIdentifier(tempDirLocation);
        zipId.setContainerType(ZIP_CONTAINER_TYPE);
        zipId.setMaxBytesToScan(maxBytesToScan);
        zipId.setSignatureReader(containerSignatureFileReader);
        zipId.setDroidCore(this);
        zipId.init();
        //TODO: zipId.setFormats();
        zipIdentifier = zipId;
    }

    private void createOle2Identifier() throws SignatureFileException {
        Ole2Identifier ole2Id = new Ole2Identifier(tempDirLocation);
        ole2Id.setContainerType(OLE2_CONTAINER_TYPE);
        ole2Id.setMaxBytesToScan(maxBytesToScan);
        ole2Id.setSignatureReader(containerSignatureFileReader);
        ole2Id.setDroidCore(this);
        ole2Id.init();
        ole2Identifier = ole2Id;
    }

    /**
     * Returns a binary signature identifier object.
     * @return a binary signature identifier object
     * @throws SignatureParseException If there was a problem parsing the signature file.
     */
    public FFSignatureFile getBinarySignatureIdentifier() throws SignatureParseException {
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
        return sigFile;
    }

    @Override
    public IdentificationResultCollection match(IdentificationRequest request) throws IOException {
        IdentificationResultCollection results = matchContainerSignatures(request);
        if (results == null  || results.getResults().isEmpty()) {
            results = matchBinarySignatures(request);
        }
        removeLowerPriorityHits(results);
        return processExtensions(request, results);
    }

    //TODO: split these methods so we can pass a single ByteReader into several rather than re-creating it each time.

    @Override
    public IdentificationResultCollection matchContainerSignatures(IdentificationRequest request) throws IOException {
        IdentificationResultCollection results = null;
        if (containerSignatureFileReader != null) {
            ByteReader byteReader = new IdentificationRequestByteReaderAdapter(request);
            if (!zipBinarySigs.getMatchingSignatures(byteReader, maxBytesToScan).isEmpty()) {
                IdentificationResultCollection containerResults = zipIdentifier.submit(request);
                containerResults.setFileLength(request.size());
                containerResults.setRequestMetaData(request.getRequestMetaData());
                results = containerResults;
            } else if (!ole2BinarySigs.getMatchingSignatures(byteReader, maxBytesToScan).isEmpty()) {
                IdentificationResultCollection containerResults = ole2Identifier.submit(request);
                containerResults.setFileLength(request.size());
                containerResults.setRequestMetaData(request.getRequestMetaData());
                results = containerResults;
            }
        }
        return results; //TODO: should return null or an empty collection?
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
    public IdentificationResultCollection matchExtensions(IdentificationRequest request) {
        IdentificationResultCollection results = new IdentificationResultCollection(request);
        results.setRequestMetaData(request.getRequestMetaData());
        String fileExtension = request.getExtension();
        if (fileExtension != null && !fileExtension.isEmpty()) {
            List<FileFormat> fileFormats;
            if (matchAllExtensions) {
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
        //TODO: deal with sig file not being loaded yet?  what calls this method, feels inside out being public.
        sigFile.puidHasOverridingSignatures(puid);
    }

    @Override
    public void removeLowerPriorityHits(IdentificationResultCollection results) {
        // Build a set of format ids the results have priority over:
        FileFormatCollection allFormats = sigFile.getFileFormatCollection();
        Set<Integer> lowerPriorityIDs = new HashSet<>();
        final List<IdentificationResult> theResults = results.getResults();
        int numResults = theResults.size();
        for (int i = 0; i < numResults; i++) {
            final IdentificationResult result = theResults.get(i);
            final String resultPUID = result.getPuid();
            final FileFormat format = allFormats.getFormatForPUID(resultPUID);
            lowerPriorityIDs.addAll(format.getFormatIdsHasPriorityOver());
        }
        
        // If a result has an id in this set, add it to the remove list;
        List<IdentificationResult> lowerPriorityResults = new ArrayList<>();
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
        this.maxBytesToScan = maxBytes;
    }

    /**
     * Sets whether we should identify all extensions, or only if there are no binary signatures defined for them.
     * @param matchAllExtensions whether we should identify all extensions, or only if there are no binary signatures
     *                          defined for them.
     */
    public void setMatchAllExtensions(boolean matchAllExtensions) {
        this.matchAllExtensions = matchAllExtensions;
    }

    /**
     * Sets the location for temporary files to be created while processing containers.
     * If null, DROID will use the system default location for temporary files.
     * @param tempDirLocation The location of the directory where temporary files should be created.
     */
    public void setTempDirLocation(Path tempDirLocation) {
        this.tempDirLocation = tempDirLocation;
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
     * Sets the class which can parse container signature files and cache the parsed definitions.
     * @param containerSignatureFileReader The container signature file reader to set.
     */
    public void setContainerSignatureFileReader(ContainerSignatureFileReader containerSignatureFileReader) {
        this.containerSignatureFileReader = containerSignatureFileReader;
    }

    /**
     * Sets the signature file URI to parse.
     * @param signatureFileURI the URI of the signature file to parse.
     */
    public void setSignatureFile(URI signatureFileURI) {
        this.signatureFile = signatureFileURI;
    }

    /**
     * Sets the zip identifier.
     * @param zipIdentifier the zip identifier.
     */
    public void setZipIdentifier(ContainerIdentifier zipIdentifier) {
        this.zipIdentifier = zipIdentifier;
    }

    /**
     * Sets the ol2 identifier.
     * @param ole2Identifier the ole2 identifier.
     */
    public void setOle2Identifier(ContainerIdentifier ole2Identifier) {
        this.ole2Identifier = ole2Identifier;
    }

    /**
     * Returns a set of results with extension signature information added.
     * If there are no existing results, it will match on extensions only.
     * If there are existing results, it will look for extension mismatches.
     * @param request The request
     * @param results The results
     * @return the results
     */
    protected IdentificationResultCollection processExtensions(IdentificationRequest<?> request,
                                                               IdentificationResultCollection results) {
        List<IdentificationResult> resultList = results.getResults();
        // If we have no results at all so far:
        if (resultList != null && resultList.isEmpty()) {
            IdentificationResultCollection extensionResults = matchExtensions(request);
            if (extensionResults != null) {
                return extensionResults;
            }
        } else { // check for extensions mismatches in the results we have.
            checkForExtensionsMismatches(results, request.getExtension());
        }
        return results;
    }

    /**
     * Find the binary signatures which match the base container type.
     *
     * @throws SignatureParseException If there was a problem parsing the container signatures.
     */
    protected void processContainerSignatureTriggerPuids() throws SignatureParseException {
        if (containerSignatureFileReader != null) {
            ContainerSignatureDefinitions definitions = containerSignatureFileReader.getDefinitions();
            for (TriggerPuid trigger : definitions.getTiggerPuids()) {
                String puid = trigger.getPuid();
                InternalSignatureCollection binarySignatures = new InternalSignatureCollection();
                for (InternalSignature sig : getBinarySignatureIdentifier().getSignaturesForPuid(puid)) {
                    binarySignatures.addInternalSignature(sig);
                }
                switch (trigger.getContainerType()) {
                    case ZIP_CONTAINER_TYPE : {
                        zipBinarySigs = binarySignatures;
                        break;
                    }
                    case OLE2_CONTAINER_TYPE: {
                        ole2BinarySigs = binarySignatures;
                        break;
                    }
                    default: throw new IllegalArgumentException("The container type is not supported: "
                                                                + trigger.getContainerType());
                }
            }
        }
    }

}
