/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.filter;

/**
 * @author rflitcroft
 *
 */
public enum CriterionOperator {
    
    /** Less than. */
    LT("less than", false),
    /** Less than or equal. */
    LTE("less than or equal to", false),
    /** Equals. */
    EQ("equal to", false),
    /** Greater than or equal. */
    GTE("greater than or equal to", false),
    /** Greater than. */
    GT("greater than", false),
    /** Not equal. */
    NE("not equal to", false),
    
    /** Any of. */
    ANY_OF("any of", true),
    /** None of. */
    NONE_OF("none of", true),
    
    /** String starts with. */
    STARTS_WITH("starts with", false),
    /** String ends with. */
    ENDS_WITH("ends with", false),
    /** String contains. */
    CONTAINS("contains", false),
    /** String does not start with. */
    NOT_STARTS_WITH("does not start with", false),
    /** String does not end with. */
    NOT_ENDS_WITH("does not end with", false),
    /** String does not contain. */
    NOT_CONTAINS("does not contain", false);

    
    private String name;
    private boolean setOperator;
    
    private CriterionOperator(String name, boolean setOperator) {
        this.name = name;
        this.setOperator = setOperator;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * @return true if the operator applies to a set of values, false otherwise
     */
    public boolean isSetOperator() {
        return setOperator;
    }
}
