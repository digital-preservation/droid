/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.export.interfaces;

import java.util.List;

/**
 * Callback to be invoked when an item reader ancounters an item.
 * The itemChunk will contain the number of items the reader has been configure to 
 * fetch at once.
 * 
 * @param <T> the type of item to be handled.
 * @author rflitcroft
 *
 */
public interface ItemReaderCallback<T> {

    /**
     * Invoked when the underlying reader finds items.
     * @param itemChunk the items found
     * @throws JobCancellationException to cancel the job
     */
    void onItem(List<? extends T> itemChunk) throws JobCancellationException;
}
