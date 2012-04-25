/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.treemodel;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * 
 * @author rflitcroft
 *
 * @param <T> the source comparable
 */
public abstract class DirectoryComparableObject<T extends Comparable<T>> implements DirectoryComparable<T> {

    // The data value
    private T source;
    
    // The resource node the value is associated with.
    private ProfileResourceNode node;
    

    
    
    /**
     * 
     * @param source the source object
     * @param node the tree node from which we can compare various properties for sorting.
     */
    public DirectoryComparableObject(T source, ProfileResourceNode node) {
        this.source = source;
        this.node = node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(DirectoryComparable<T> o) {
        return nullSafeCompareTo(o);
    }

    private int nullSafeCompareTo(DirectoryComparable<T> o) {
        int compare;
        
        if (source == null && o == null) {
            compare = 0;
        } else if (source == null) {
            compare = -1;
        } else if (o == null || o.getSource() == null) {
            compare = 1;
        } else {
            compare = sourceCompareTo(o);
        }

        return compare;
    }
    
    /**
     * 
     * @param o The object to compare.
     * @return int the result of the source object comparison
     */
    protected int sourceCompareTo(DirectoryComparable<T> o) {
        return source.compareTo(o.getSource());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return source == null ? "" : source.toString();
    }
    
    /**
     * {@inheritDoc}
     * @see uk.gov.nationalarchives.droid.gui.treemodel.DirectoryComparable#getSource()
     */
    @Override
    public T getSource() {
        return source;
    }
    
    
    @Override
    public int getFilterStatus() {
        return node.getFilterStatus();
    }    
    
    
    /**
     * 
     * @return whether the object has a file extension mismatch.
     */
    public boolean getExtensionMismatch() {
        return node.getExtensionMismatch();
    }

    /**
     * {@inheritDoc}
     */
    //@Override
    //public Boolean isFile() {
    //    return file;
    //}
    @Override
    public ResourceType getResourceType() {
        return node.getMetaData().getResourceType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(source).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        DirectoryComparableObject<T> other = (DirectoryComparableObject<T>) obj;
        return new EqualsBuilder().append(source, other.source).isEquals();
    }

}
