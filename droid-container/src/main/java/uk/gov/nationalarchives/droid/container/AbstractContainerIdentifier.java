/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.gov.nationalarchives.droid.core.IdentificationRequestByteReaderAdapter;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolver;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ErrorCode;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;

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
    private IdentificationRequestFactory requestFactory;

    private List<ContainerSignature> containerSignatures = new ArrayList<ContainerSignature>();
    private Map<Integer, List<FileFormatMapping>> formats = new HashMap<Integer, List<FileFormatMapping>>(); 
    private List<String> uniqueFileEntries;
    
    private long maxBytesToScan = -1;
    
    /**
     * {@inheritDoc}
     * @throws IOException 
     */
    @Override
    public final IdentificationResultCollection submit(IdentificationRequest request) throws IOException {
        ContainerSignatureMatchCollection matches =
            new ContainerSignatureMatchCollection(getContainerSignatures(), uniqueFileEntries, maxBytesToScan);

        process(request, matches);
        
        IdentificationResultCollection results = new IdentificationResultCollection(request);
        for (ContainerSignatureMatch match : matches.getContainerSignatureMatches()) {
            if (match.isMatch()) {
                List<FileFormatMapping> mappings = formats.get(match.getSignature().getId());
                for (FileFormatMapping mapping : mappings) {
                    IdentificationResultImpl result = new IdentificationResultImpl();
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
     * @param containerSignature the containerSignature to add.
     */
    public void addContainerSignature(ContainerSignature containerSignature) {
        containerSignatures.add(containerSignature);
    }
    
    /**
     * Returns a ByteReader for the input stream supplied.
     * @param in an input stream
     * @return a Byte reader
     * @throws IOException if the input stream could not be read
     */
    protected ByteReader newByteReader(InputStream in) throws IOException {
        IdentificationRequest request = getRequestFactory().newRequest(null, null);
        request.open(in);
        return new IdentificationRequestByteReaderAdapter(request);
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
        return containerSignatures;
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
    
    /**
     * @param requestFactory the requestFactory to set
     */
    public void setRequestFactory(IdentificationRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }
    
    /**
     * @return the requestFactory
     */
    protected IdentificationRequestFactory getRequestFactory() {
        return requestFactory;
    }
  

    @Override
    public void setMaxBytesToScan(long maxBytesToScan) {
        this.maxBytesToScan = maxBytesToScan;
    }
}
