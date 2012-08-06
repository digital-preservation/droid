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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ErrorCode;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;

/**
 * @author rflitcroft
 *
 */
//CHECKSTYLE:OFF - fan out complexity slightly too high.
public abstract class AbstractContainerIdentifier implements ContainerIdentifier {
//CHECKSTYLE:ON

    /**
     * 
     */
    private static final String ERROR_READING_SIGNATURE_FILE = "Error reading signature file";
    private ContainerSignatureSaxParser signatureFileParser;
    private ContainerIdentifierFactory containerIdentifierFactory;
    private String containerType;
    private ArchiveFormatResolver containerFormatResolver;
    private DroidCore droidCore;
    private String signatureFilePath;
    
    private ContainerIdentifierInit init;

    //private List<ContainerSignature> containerSignatures = new ArrayList<ContainerSignature>();
    private Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>(); 
    //private List<String> uniqueFileEntries;
    
    private long maxBytesToScan = -1;
    private IdentifierEngine identifierEngine;
    
    /**
     * {@inheritDoc}
     * @throws IOException 
     */
    @Override
    public final IdentificationResultCollection submit(IdentificationRequest request) throws IOException {
        final ContainerSignatureMatchCollection matches = new ContainerSignatureMatchCollection(
            getContainerSignatures(),
            init.getUniqueFileEntries(),
            maxBytesToScan);

        process(request, matches);
        
        final IdentificationResultCollection results = new IdentificationResultCollection(request);
        for (final ContainerSignatureMatch match : matches.getContainerSignatureMatches()) {
            if (match.isMatch()) {
                List<FileFormatMapping> mappings = formats.get(match.getSignature().getId());
                for (final FileFormatMapping mapping : mappings) {
                    final IdentificationResultImpl result = new IdentificationResultImpl();
                    result.setMethod(IdentificationMethod.CONTAINER);
                    result.setRequestMetaData(request.getRequestMetaData());
                    result.setPuid(mapping.getPuid());
                    results.addResult(result);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Subclasses should override this to process the container in its specific way.
     * @param request the identification request to process.
     * @param matches a Liost of container signature potential matches.
     * @throws IOException if the input stream could not be read
     */
    protected abstract void process(IdentificationRequest request, 
            ContainerSignatureMatchCollection matches) throws IOException;
    
    /**
     * Get the Identifier Engine.
     * 
     * @return The Identifier Engine
     */
    public IdentifierEngine getIdentifierEngine() {
        return identifierEngine;
    }
    
    /**
     * Set the Identifier Engine.
     * 
     * @param identifierEngine The Identifier Engine
     */
    public void setIdentifierEngine(final IdentifierEngine identifierEngine) {
        this.identifierEngine = identifierEngine;
    }
    
    /**
     * @param containerSignature the containerSignature to add.
     */
    public void addContainerSignature(ContainerSignature containerSignature) {
        init.addContainerSignature(containerSignature);
    }
    
    /**
     * @param formats the formats to set
     */
    public void setFormats(Map<Integer, List<FileFormatMapping>> formats) {
        this.formats = formats;
    }

    /**
     * @return all container signatures.
     */
    public List<ContainerSignature> getContainerSignatures() {
        return init.getContainerSignatures();
    }
    
    /**
     * @return the formats
     */
    protected Map<Integer, List<FileFormatMapping>> getFormats() {
        return formats;
    }
    
    /**
     * Initialises the Zip identifier using the XML parser configured.
     * @throws SignatureFileException if the Signature file could not be initialised
     */
    public void init() throws SignatureFileException {
        try {
            FileInputStream sigFile = new FileInputStream(signatureFilePath);
            ContainerSignatureDefinitions defs = signatureFileParser.parse(sigFile);
            
            init = new ContainerIdentifierInit();
            init.init(defs, containerType, formats, droidCore);
            
            /*
            Set<String> uniqueFileSet = new HashSet<String>();
            
            for (ContainerSignature sig : defs.getContainerSignatures()) {
                if (sig.getContainerType().equals(containerType)) {
                    addContainerSignature(sig);
                    uniqueFileSet.addAll(sig.getFiles().keySet());
                }
            }
            uniqueFileEntries = new ArrayList<String>(uniqueFileSet); 
            
            for (FileFormatMapping fmt : defs.getFormats()) {
                List<FileFormatMapping> mappings = formats.get(fmt.getSignatureId());
                if (mappings == null) {
                    mappings = new ArrayList<FileFormatMapping>();
                    formats.put(fmt.getSignatureId(), mappings);
                }
                mappings.add(fmt);
                droidCore.removeSignatureForPuid(fmt.getPuid());
            }
            */
            
            
            for (TriggerPuid triggerPuid : defs.getTiggerPuids()) {
                if (triggerPuid.getContainerType().equals(containerType)) {
                    containerIdentifierFactory.addContainerIdentifier(containerType, this);
                    final String puid = triggerPuid.getPuid();
                    containerFormatResolver.registerPuid(puid, containerType);
                }
            }
        } catch (IOException e) {
            throw new SignatureFileException(ERROR_READING_SIGNATURE_FILE, e, ErrorCode.FILE_NOT_FOUND);
        } catch (SignatureParseException e) {
            throw new SignatureFileException(ERROR_READING_SIGNATURE_FILE, e, ErrorCode.INVALID_SIGNATURE_FILE);
        }
    }
    
    /**
     * @param signatureFileParser the signatureFileParser to set
     */
    public void setSignatureFileParser(ContainerSignatureSaxParser signatureFileParser) {
        this.signatureFileParser = signatureFileParser;
    }
    
    /**
     * @param containerIdentifierFactory the containerIdentifierFactory to set
     */
    public void setContainerIdentifierFactory(ContainerIdentifierFactory containerIdentifierFactory) {
        this.containerIdentifierFactory = containerIdentifierFactory;
    }
    
    /**
     * @param containerType the containerType to set
     */
    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }
    
    /**
     * @param containerFormatResolver the containerFormatResolver to set
     */
    public void setContainerFormatResolver(ArchiveFormatResolver containerFormatResolver) {
        this.containerFormatResolver = containerFormatResolver;
    }
    
    /**
     * @param droidCore the droidCore to set
     */
    public void setDroidCore(DroidCore droidCore) {
        this.droidCore = droidCore;
    }
    
    /**
     * @param signatureFilePath the signatureFilePath to set
     */
    public void setSignatureFilePath(String signatureFilePath) {
        this.signatureFilePath = signatureFilePath;
    }

    @Override
    public void setMaxBytesToScan(long maxBytesToScan) {
        this.maxBytesToScan = maxBytesToScan;
    }
}
