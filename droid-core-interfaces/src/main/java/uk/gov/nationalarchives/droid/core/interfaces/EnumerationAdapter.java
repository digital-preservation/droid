/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adapts an enumeration to the Iterator interface.
 * @author rflitcroft
 *
 * @param <T>
 */
public class EnumerationAdapter<T> implements Iterator<T> {
    
    private Enumeration<T> en;
    
    /**
     * Wraps a enumeration in this iterator.
     * @param en the enumeration to wrap.
     */
    public EnumerationAdapter(Enumeration<T> en) {
        this.en = en;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return en.hasMoreElements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T next() {
        return en.nextElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported.");
    }
}
