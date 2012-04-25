/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.export.interfaces;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;

/**
 * Reads an item.
 * @author rflitcroft
 * @param <T> the type to read
 */
public interface ItemReader<T> {

    /**
     * Reads an item.
     * @return the item that was read
     */
    T read();
    
    /**
     * Closes the reader.
     */
    void close();
    
    /**
     * Opens a reader.
     * @param filter an optional filter
     */
    void open(Filter filter);
    
    /**
     * Reads all items and invokes the callback.
     * @param callback the callback with items read
     * @param filter an optional filter
     * @throws JobCancellationException if the job was cancelled
     */
    void readAll(ItemReaderCallback<T> callback, Filter filter) throws JobCancellationException;

}
