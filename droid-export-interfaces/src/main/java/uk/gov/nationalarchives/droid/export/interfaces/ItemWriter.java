/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.export.interfaces;

import java.io.Writer;
import java.util.List;


/**
 * @author rflitcroft
 * @param <T> the type to write
 */
public interface ItemWriter<T> {

    /**
     * Writes the items.
     * @param items the items to write.
     */
    void write(List<? extends T> items);
    
    /**
     * Opens a writer for writing.
     * @param writer the writer to use
     */
    void open(Writer writer);
    
    /**
     * Closes the writer.
     */
    void close();
    
    /**
     * 
     * @param options Sets the options to use when writing out the export.
     */
    void setOptions(ExportOptions options);
}
