/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.filter.expressions;

/**
 * @author rflitcroft
 *
 */
public interface Criterion {

    /**
     * 
     * @return the parameter values
     */
    Object[] getValues();

    /**
     * @param parent the parent of this Criterion
     * @return a JPA-Ql string
     */
    String toEjbQl(QueryBuilder parent);


}
