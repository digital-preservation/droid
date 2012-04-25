/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.action;


/**
 * Callback for when actions have been done.
 * @author rflitcroft
 * @param <T> the action type
 */
public interface ActionDoneCallback<T> {

    /**
     * @param action The completed action
     */
    void done(T action);
}
