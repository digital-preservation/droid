/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.FileFormatHit;

/**
 * Adapts an IdentificationRequest to the ByteReader interface.
 * @author rflitcroft
 *
 */
public class IdentificationRequestByteReaderAdapter implements ByteReader {

    private Log log = LogFactory.getLog(this.getClass());
    private IdentificationRequest request;
    private long fileMarker;
    
    private List<FileFormatHit> hits = new ArrayList<FileFormatHit>();
    
    /**
     * 
     * @param request the request to wrap.
     */
    public IdentificationRequestByteReaderAdapter(IdentificationRequest request) {
        this.request = request;
    }

    /**
     * @param theHit the hit to add
     */
    @Override
    public final void addHit(FileFormatHit theHit) {
        hits.add(theHit);
    }

    /* (non-Javadoc)
     * @see uk.gov.nationalarchives.droid.core.signature.droid6.bytereader.ByteReader#close()
     */
    @Override
    public final void close() {
        try {
            request.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Not supported.
     * @return Not Supported
     */
    @Override
    public final byte[] getbuffer() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the byte oit the index specified.
     * @param fileIndex the file index
     * @return the byte at index fileIndex
     * 
     */
    @Override
    public final byte getByte(long fileIndex) {
        return request.getByte(fileIndex);
    }

    /**
     * Not supported.
     * @return Not Supported
     */
    @Override
    public final int getClassification() {
        throw new UnsupportedOperationException();
    }


    /**
     * @return the File name of the request
     */
    @Override
    public final String getFileName() {
        return request.getFileName();
    }

    /**
     * @return Not Supported
     */
    @Override
    public final String getFilePath() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param theIndex the index of the hit
     * @return the hit at the specified index
     */
    @Override
    public final FileFormatHit getHit(int theIndex) {
        return hits.get(theIndex);
    }

    /**
     * @return Not supported
     */
    @Override
    public final String getIdentificationWarning() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the nuimber of bytes available from this resource.
     */
    @Override
    public final long getNumBytes() {
        return request.size();
    }

    /**
     * @return the number of hits
     */
    @Override
    public final int getNumHits() {
        return hits.size();
    }

    /**
     * @return Not supported
     */
    @Override
    public final boolean isClassified() {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a hit at thindex specified.
     * @param theIndex th index of th hit to remove.
     */
    @Override
    public final void removeHit(int theIndex) {
        hits.remove(theIndex);
    }

    /**
     * @Override
     */
    @Override
    public final void setErrorIdent() {
        
    }


    /**
     * Sets an identification warning.
     * @param theWarning the warning to set.
     */
    @Override
    public final void setIdentificationWarning(String theWarning) {
        throw new UnsupportedOperationException();
    }

    /**
     * @Override
     */
    @Override
    public final void setNoIdent() {
        
    }

    /**
     * @Override
     */
    @Override
    public final void setPositiveIdent() {
        
    }

    /**
     * @Override
     */
    @Override
    public final void setTentativeIdent() {
        
    }

    /**
     * @return the fileMarker
     */
    public final long getFileMarker() {
        return fileMarker;
    }

    /**
     * @param fileMarker the fileMarker to set
     */
    public final void setFileMarker(long fileMarker) {
        this.fileMarker = fileMarker;
    }

    /**
     * @see uk.gov.nationalarchives.droid.core.signature.ByteReader#getReader()
     * @return a ByteReader
     */
    @Override
    public final net.domesdaybook.reader.ByteReader getReader() {
        return request.getReader();
    }
    
    
    
}
