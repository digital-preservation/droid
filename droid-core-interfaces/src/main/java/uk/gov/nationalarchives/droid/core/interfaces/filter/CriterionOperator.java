/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
