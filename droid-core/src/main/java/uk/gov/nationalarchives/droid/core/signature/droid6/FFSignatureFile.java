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
/*
 * The National Archives 2005-2006.  All rights reserved.
 * See Licence.txt for full licence details.
 *
 * Developed by:
 * Tessella Support Services plc
 * 3 Vineyard Chambers
 * Abingdon, OX14 3PX
 * United Kingdom
 * http://www.tessella.com
 *
 * Tessella/NPD/4305
 * PRONOM 4
 *
 * $Id: FFSignatureFile.java,v 1.6 2006/03/13 15:15:29 linb Exp $
 *
 * $Log: FFSignatureFile.java,v $
 * Revision 1.6  2006/03/13 15:15:29  linb
 * Changed copyright holder from Crown Copyright to The National Archives.
 * Added reference to licence.txt
 * Changed dates to 2005-2006
 *
 * Revision 1.5  2006/02/07 17:16:22  linb
 * - Change fileReader to IdentificationResults in formal parameters of methods
 * - use new static constructors
 * - Add detection of if a filePath is a URL or not
 *
 * Revision 1.4  2006/02/07 12:34:57  gaur
 * Removed restriction on priority relationships so that they can
 * be applied between any combination of generic and specific signatures (second recommit because of missing logging)
 *
 *
 * $History: FFSignatureFile.java $
 * 
 * *****************  Version 7  *****************
 * User: Walm         Date: 19/04/05   Time: 18:24
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * Provide initial values for version and dateCreated
 * 
 * *****************  Version 6  *****************
 * User: Walm         Date: 18/03/05   Time: 12:39
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * add some more exception handling
 * 
 * *****************  Version 5  *****************
 * User: Walm         Date: 15/03/05   Time: 14:39
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * fileReader class now holds reference to identificationFile object
 * 
 * *****************  Version 4  *****************
 * User: Mals         Date: 14/03/05   Time: 15:08
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * Takes into account of IdentificationFile objects in checkExtension
 * 
 * *****************  Version 3  *****************
 * User: Mals         Date: 14/03/05   Time: 14:30
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * runFileIdentification accepts IdentificationFile parameter
 *
 */
package uk.gov.nationalarchives.droid.core.signature.droid6;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.FileFormatCollection;
import uk.gov.nationalarchives.droid.core.signature.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * Holds all the file formats and binary signatures used to 
 * match them.
 * 
 * <p/>Can match a target file against all the binary signatures,
 * returning which file formats were hit when matching.
 * 
 * @author Martin Waller
 * @author Matt Palmer
 * @version 6.0.0
 */
public class FFSignatureFile extends SimpleElement {

    /**
     * Default size of the tentative file format collection.
     */
    private static final int DEFAULT_TENTATIVE_EXTENSION_SIZE = 100;
    private static final int DEFAULT_ALL_EXTENSION_SIZE = 300;
    
    //private Log log = LogFactory.getLog(this.getClass());
    
    private String version = "";
    private String dateCreated = "";
    private FileFormatCollection formatCollection;
    private InternalSignatureCollection intSigs;
    private Map<String, List<FileFormat>> tentativeFormats =
        new HashMap<String, List<FileFormat>>(DEFAULT_TENTATIVE_EXTENSION_SIZE);
    private Map<String, List<FileFormat>> formatsForExtension =
        new HashMap<String, List<FileFormat>>(DEFAULT_ALL_EXTENSION_SIZE);
    
    private long maxBytesToScan = -1; // default to scanning all bytes.

    /* setters */
    /**
     * @param coll The file format collection for this signature file.
     */
    public final void setFileFormatCollection(final FileFormatCollection coll) {
        this.formatCollection = coll;
    }


    /**
     * 
     * @param col3  The internal signature collection for this signature file.
     */
    public final void setInternalSignatureCollection(final InternalSignatureCollection col3) {
        this.intSigs = col3;
    }

    private void setVersion(final String vers) {
        this.version = vers;
    }

    private void setDateCreated(final String created) {
        this.dateCreated = created;
    }

    @Override
    public final void setAttributeValue(final String name, final String value) {
        if ("Version".equals(name)) {
            setVersion(value.trim());
        } else if ("DateCreated".equals(name)) {
            setDateCreated(value);
        } else {
            unknownAttributeWarning(name, this.getElementName());
        }
    }

    /* getters */
    private int getNumInternalSignatures() {
        return this.intSigs.getInternalSignatures().size();
    }


    /**
     * 
     * @return The list of internal signatures in this signature file.
     */
    public List<InternalSignature> getSignatures() {
        return intSigs.getInternalSignatures();
    }

    private InternalSignature getInternalSignature(final int theIndex) {
        return intSigs.getInternalSignatures().get(theIndex);
    }

    /**
     * 
     * @return The number of file formats in the signature file.
     */
    public final int getNumFileFormats() {
        return this.formatCollection.getFileFormats().size();
    }

    /**
     * 
     * @param theIndex The index of the file format.
     * @return The file format at the given index in this signature file.
     */
    public final FileFormat getFileFormat(final int theIndex) {
        return formatCollection.getFileFormats().get(theIndex);
    }
    
    
    /**
     * 
     * @param puid The puid to get a file format for.
     * @return The file format for this puid.
     */
    public final FileFormat getFileFormat(final String puid) {
        return formatCollection.getFormatForPUID(puid);
    }
    
    /**
     * 
     * @return A file format collection.
     */
    public FileFormatCollection getFileFormatCollection() {
        return formatCollection;
    }

    /**
     * 
     * @return The version of this signature file.
     */
    public final String getVersion() {
        return version;
    }

    /**
     * 
     * @return The date this signature file was created.
     */
    public final String getDateCreated() {
        return dateCreated;
    }


    /**
     * This method must be run after the signature file data has been read
     * and before the FFSignatureFile class is used.
      */
    public final void prepareForUse() {
        this.prepareInternalSignatures();
    }

    
    /**
     * Informs the signature file that we should remove any
     * internal binary signatures for a given puid.
     * 
     * For example, if there are container signatures for a puid,
     * we should call this method, to ensure that we don't run  
     * the binary signatures as well as the container signatures.
     * 
     * This will remove any internal signatures that exist
     * for that puid, and adjust the tentative extension maps.
     * 
     * Note that internal signatures can be mapped to more than
     * one file format, and file formats can have more than one
     * signature attached to them.
     * 
     * @param puid The puid.
     */
    public final void puidHasOverridingSignatures(String puid) {
        FileFormat format = getFileFormat(puid);
        if (format != null) {
            // 1. remove all the internal signature ids from the file format:
            List<Integer> removedSignatureIDs = format.clearSignatures();

            // 2. For each signature removed from the file format,
            //    also remove the file format from the signature:
            for (Integer id : removedSignatureIDs) {
                InternalSignature signature = intSigs.getInternalSignature(id);
                if (signature != null) {
                    signature.removeFileFormat(format);
                    // 3. If the signature no longer points at any
                    //    file formats, remove the signature entirely:
                    if (signature.getNumFileFormats() == 0) {
                        intSigs.removeInternalSignature(signature);
                    }
                }
            }

            // 4. The file format no longer has any internal signatures.
            //    It is possible that it never had any, and was a 
            //    tentative format.  We can't tell at this point, 
            //    as this method may have been called before.
            //    However, it is definitely not a tentative format now,
            //    as it has an overriding signature (probably a container
            //    signature at the time of writing for DROID 6).
            //    Therefore, remove it from the tentative extensions lists,
            //    if it ever existed there.  
            for (String extension : format.getExtensions()) {
                List<FileFormat> tentativeFormatsForExtension = tentativeFormats.get(extension);
                if (tentativeFormatsForExtension != null) {
                    tentativeFormatsForExtension.remove(format);
                    // 5.  If there are no more file formats defined
                    //     for this extension, remove the entry entirely
                    //     so it is consistent with how it was built.
                    if (tentativeFormatsForExtension.size() == 0) {
                        tentativeFormats.remove(extension);
                    }
                }
            }
        }
    }

    
    /*
     * Ensures that each internal signature does whatever it needs to do
     * to ensure its own best performance.
     */
    private void prepareInternalSignatures() {
        this.setAllSignatureFileFormats();
        this.intSigs.prepareForUse();
        intSigs.sortSignatures(new InternalSignatureComparator());
        buildFileExtensions();
    }
    


    private void debugWriteOutInternalSignatures() {
        OutputStream out = null;
        Writer writer = null;
        try {
            // debug: write out signatures
            File outputFile = new File(System.getProperty("user.home") 
                + File.separator + "DROID4 Signature Sequences.csv");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            try {
                outputFile.createNewFile();
                out = new FileOutputStream(outputFile);
                writer = new OutputStreamWriter(out);
                final int stop = this.getNumInternalSignatures();
                for (int signatureIndex = 0; signatureIndex < stop; signatureIndex++) {
                    final InternalSignature sig = getInternalSignature(signatureIndex);
                    sig.debugWriteOutSignatureSequences(writer);
                }
            } catch (IOException ex) {
                getLog().error(ex.getMessage());
            }

        } finally {
            try {
                if (writer != null) { writer.close(); }
                if (out != null) { out.close(); }
            } catch (IOException ex) {
                getLog().error(ex.getMessage());
            }
        }
    }


    /**
     * Points all internal signatures to the fileFormat objects they identify.
     */
    private void setAllSignatureFileFormats() {
        final int numFormats = this.getNumFileFormats();
        for (int iFormat = 0; iFormat < numFormats; iFormat++) {  //loop through file formats
            final int numFormatInternalSignatures = this.getFileFormat(iFormat).getNumInternalSignatures(); 
            for (int iFileSig = 0; 
                iFileSig < numFormatInternalSignatures;
                iFileSig++) {  //loop through internal signatures for each file format
                final int iFileSigID = this.getFileFormat(iFormat).getInternalSignatureID(iFileSig);
                //loop through all internal signatures to find one with a matching ID
                final int numTotalInternalSignatures = this.getNumInternalSignatures(); 
                for (int iIntSig = 0; iIntSig < numTotalInternalSignatures; iIntSig++) {
                    if (this.getInternalSignature(iIntSig).getID() == iFileSigID) {
                        this.getInternalSignature(iIntSig).addFileFormat(this.getFileFormat(iFormat));
                        break;
                    }
                }
            }
        }
    }

    
    // Builds a mapped list of file formats with no binary signatures, indexed against their file extensions.
    private void buildFileExtensions() {
        final int numFileFormats = this.getNumFileFormats();
        for (int iFormat = 0; iFormat < numFileFormats; iFormat++) {
            final FileFormat theFormat = this.getFileFormat(iFormat);
            if (theFormat.getNumInternalSignatures() == 0) {
                addTentativeFormat(theFormat);
            }
            addExtensions(theFormat);
        }
    }
    
    

    /**
     * Maps a format against its extensions, if it doesn't
     * have any other signature defined for it.
     * 
     * The original meaning of a Tentative format in earlier
     * versions of DROID was precisely a format which only had
     * file extensions defined, and no other signatures.
     * 
     * @param tentativeFormat
     */
    private void addTentativeFormat(final FileFormat tentativeFormat) {
        final int numExtensions = tentativeFormat.getNumExtensions();
        for (int iExtension = 0; iExtension < numExtensions; iExtension++) {
            final String extension = tentativeFormat.getExtension(iExtension).toUpperCase();
            List<FileFormat> formatList = tentativeFormats.get(extension);
            if (formatList == null) {
                formatList = new ArrayList<FileFormat>();
                tentativeFormats.put(extension, formatList);
            }
            formatList.add(tentativeFormat);
        }
    }
    
    
    /**
     * Maps a format against all the extensions it defines.
     *  
     * @param format The format to add its extensions for.
     */
    private void addExtensions(final FileFormat format) {
        final int numExtensions = format.getNumExtensions();
        for (int iExtension = 0; iExtension < numExtensions; iExtension++) {
            final String extension = format.getExtension(iExtension).toUpperCase();
            List<FileFormat> formatList = formatsForExtension.get(extension);
            if (formatList == null) {
                formatList = new ArrayList<FileFormat>();
                formatsForExtension.put(extension, formatList);
            }
            formatList.add(format);
        }
    }

    /**
     * Gets the file formats for an extension with no other signature defined.
     * 
     * @param extension The file extension to check for.
     * @return A list of file formats for this extension with no other signature defined.
     */
    public List<FileFormat> getTentativeFormatsForExtension(final String extension) {
        return tentativeFormats.get(extension.toUpperCase());
    }

    /**
     * Gets the file formats for a file extension.
     * 
     * @param extension The file extension to check for.
     * @return A list of file formats for this extension.
     */
    public List<FileFormat> getFileFormatsForExtension(final String extension) {
        return formatsForExtension.get(extension.toUpperCase());
    }
   

    /**
     * 
     * @return The maximum number of bytes to scan from each end of the file.  
     *         If the number is less than zero, then the full file can be scanned.
     */
    public final long getMaxBytesToScan() {
        return maxBytesToScan;
    }


    /**
     *
     * @param maxBytesToScan The maximum number of bytes to scan from each end of the file.
     *         If the number is less than zero, then the full file can be scanned.
     */
    public void setMaxBytesToScan(final long maxBytesToScan) {
        this.maxBytesToScan = maxBytesToScan;
    }

    
    /**
     * Identify the target file using the signatures defined in this signature file.
     *
     * @param targetFile The binary file to be identified
     */
    public final void runFileIdentification(final ByteReader targetFile) {
        List<InternalSignature> matchingSigs = intSigs.getMatchingSignatures(targetFile, maxBytesToScan);
        for (InternalSignature internalSig : matchingSigs) {
            targetFile.setPositiveIdent();
            final int numFileFormats = internalSig.getNumFileFormats();
            for (int fileFormatIndex = 0; fileFormatIndex < numFileFormats; fileFormatIndex++) {
                final FileFormatHit fileHit = 
                    new FileFormatHit(internalSig.getFileFormat(fileFormatIndex), 
                                      FileFormatHit.HIT_TYPE_POSITIVE_GENERIC_OR_SPECIFIC,
                                      internalSig.isSpecific(), "");
                targetFile.addHit(fileHit);
            }
        }
    }

}
