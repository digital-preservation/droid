/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.domesdaybook.reader.ByteReader;

/**
 * An interface for objects which can cache bytes from an
 * input stream.  It the extends {@link ByteReader} interface
 * to provide access to the bytes cached in it. 
 * 
 * @author Matt Palmer
 *
 */
public interface CachedBytes extends ByteReader {

    
    /**
     * Sets the optional Random Access File for the whole binary.
     * @param sourceFile the binary data source.
     * @throws IOException if the source file was not found or could not close previous file.
     */
    void setSourceFile(File sourceFile) throws IOException;

    /**
     * Closes the internal Random Access File.
     * @throws IOException if the file could not be closed.
     */
    void close() throws IOException;

    /**
     * @return the source input stream
     * @throws IOException if there was an exception reading the source
     */
    InputStream getSourceInputStream() throws IOException;

    /**
     * Returns a source file (if any) for this cached binary.
     * If the file size is less than the size of a single cache block, 
     * the source file may not be set, and this method will return null.
     *  
     * @return The source file, or null if not set.
     */
    File getSourceFile();

}
