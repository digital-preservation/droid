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
package uk.gov.nationalarchives.droid.core.interfaces.filter.expressions;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author rflitcroft
 *
 */
public final class Restrictions {

    /**
     * 
     */
    private static final String PERIOD = ".";
    private static final char SPACE = ' ';
    private static final String AND = "AND";
    private static final String OR = "OR";

    private Restrictions() { }
    
    /**
     * An equals expression.
     * @param propertyName the property name
     * @param value the value
     * @return an equals expression
     */
    public static Criterion eq(String propertyName, Object value) {
        return new SimpleExpression(propertyName, value, "=");
    }
    
    /**
     * A not-equals expression.
     * @param propertyName the property name
     * @param value the value
     * @return a not equals expression
     */
    public static Criterion neq(String propertyName, Object value) {
        return new SimpleExpression(propertyName, value, "!=");
    }

    /**
     * A greater than expression.
     * @param propertyName the property name
     * @param value the value
     * @return a greater than expression
     */
    public static Criterion gt(String propertyName, Object value) {
        return new SimpleExpression(propertyName, value, ">");
    }

    /**
     * A less than expression.
     * @param propertyName the property name
     * @param value the value
     * @return a less than expression
     */
    public static Criterion lt(String propertyName, Object value) {
        return new SimpleExpression(propertyName, value, "<");
    }

    /**
     * A greater than or equals expression.
     * @param propertyName the property name
     * @param value the value
     * @return a greater than or equals expression
     */
    public static Criterion gte(String propertyName, Object value) {
        return new SimpleExpression(propertyName, value, ">=");
    }

    /**
     * A less than or equals expression.
     * @param propertyName the property name
     * @param value the value
     * @return an less than or equals expression
     */
    public static Criterion lte(String propertyName, Object value) {
        return new SimpleExpression(propertyName, value, "<=");
    }

    /**
     * An like expression.
     * @param propertyName the property name
     * @param value the value
     * @return a like expression
     */
    public static Criterion like(String propertyName, String value) {
        return new SimpleExpression(propertyName, value, "LIKE");
    }
    
    /**
     * A not like expression.
     * @param propertyName the property name
     * @param value the value
     * @return a not like expression
     */
    public static Criterion notLike(String propertyName, String value) {
        return new SimpleExpression(propertyName, value, "NOT LIKE");
    }

    /**
     * An IN expression.
     * @param propertyName the property name
     * @param values the values
     * @return an in expression
     */
    public static Criterion in(String propertyName, Object[] values) {
        return new InExpression(propertyName, values, false);
    }

    /**
     * A NOT IN expression.
     * @param propertyName the property name
     * @param values the values
     * @return a not in expression
     */
    public static Criterion notIn(String propertyName, Object[] values) {
        return new InExpression(propertyName, values, true);
    }
    
    /**
     * An OR expression.
     * @param lhs the left hand side
     * @param rhs the right hand side
     * @return an OR expression
     */
    public static Criterion or(Criterion lhs, Criterion rhs) {
        return new LogicalExpression(lhs, rhs, OR);
    }

    /**
     * An AND expression.
     * @param lhs the left hand side
     * @param rhs the right hand side
     * @return an AND expression
     */
    public static Criterion and(Criterion lhs, Criterion rhs) {
        return new LogicalExpression(lhs, rhs, AND);
    }
    
    /**
     * An expression for boolean properties where some of them must be true.
     * @param propertyNames the names of the properties any of which can be true.
     * @return Criterion object representing the anyAreTrue condition
     */
    public static Criterion anyAreTrue(List<String> propertyNames) {
        return new BooleanExpression(propertyNames, true, OR, false);
    }
    
    
    /** 
     * An expression for boolean properties where all of them must be true.
     * @param propertyNames the names of the properties all of which must be true.
     * @return Criterion object representing the allAreTrue condition
     */
    public static Criterion allAreTrue(List<String> propertyNames) {
        return new BooleanExpression(propertyNames, true, AND, false);
    }
    
    
    /**
     * An expression for boolean properties where none of them can be true.
     * @param propertyNames the names of the properties none of which must be true.
     * @return Criterion object representing the noneAreTrue condition
     */
    public static Criterion noneAreTrue(List<String> propertyNames) {
        return new BooleanExpression(propertyNames, true, OR, true);
    }
    
    /**
     * A simple expression.
     * @author rflitcroft
     *
     */
    private static final class SimpleExpression implements Criterion {

        private String op;
        private String propertyName;
        private Object[] values;
        
        /**
         * 
         * @param propertyName the property name
         * @param value the property value
         * @param op the operation
         */
        SimpleExpression(String propertyName, Object value, String op) {
            this.propertyName = propertyName;
            this.values = new Object[] {value};
            this.op = op;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toEjbQl(QueryBuilder parent) {
            return getAliasedQualifier(propertyName, parent) + SPACE + op + " ?"; 
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getValues() {
            return values;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return propertyName + op + values;
        }
        
    }
    
    private static String getAliasedQualifier(String propertyName, QueryBuilder parent) {
        
        if (propertyName.indexOf('.') > 0) {
            String root = StringUtils.substringBefore(propertyName, PERIOD);
            String name = StringUtils.substringAfter(propertyName, PERIOD);
            if (parent.getAliases().contains(root)) {
                return root + '.' + name;
            }
        }
        return parent.getAlias() + '.' + propertyName;
        
    }
    
    private static final class InExpression implements Criterion {
        
        private String propertyName;
        private Object[] values;
        private boolean inverse;
        
        public InExpression(String propertyName, Object[] values, boolean inverse) {
            this.propertyName = propertyName;
            this.values = values;
            this.inverse = inverse;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getValues() {
            return values;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toEjbQl(QueryBuilder parent) {
            return getAliasedQualifier(propertyName, parent) + (inverse ? " NOT " : SPACE) + "IN ("
                    + repeat("?, ", values.length - 1) + "?)";
        }
        
        private static String repeat(String string, int times) {
            StringBuilder buf = new StringBuilder(string.length() * times);
            for (int i = 0; i < times; i++) {
                buf.append(string);
            }
            return buf.toString();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return propertyName + " IN (" + values + ')';
        }

    }
    
    private static final class LogicalExpression implements Criterion {
        
        private Criterion lhs;
        private Criterion rhs;
        private String op;
        
        LogicalExpression(Criterion lhs, Criterion rhs, String op) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.op = op;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getValues() {
            return ArrayUtils.addAll(lhs.getValues(), rhs.getValues());
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toEjbQl(QueryBuilder parent) {
            return '(' + lhs.toEjbQl(parent) + ' ' + op + ' ' + rhs.toEjbQl(parent) + ')';
        }
    }
    
    /**
     * @return a conjunction (AND)
     */
    public static Junction conjunction() {
        return new Conjunction();
    }

    /**
     * @return a disjunction (OR)
     */
    public static Junction disjunction() {
        return new Disjunction();
    }
    
    
    private static final class BooleanExpression implements Criterion {
        private List<String> propertyNames;
        private Object[] values;
        private String op;
        private boolean inverse;
        
        
        BooleanExpression(List<String> propertyNames, boolean value, String op, boolean inverse) {
            this.propertyNames = propertyNames;
            this.values = new Boolean[propertyNames.size()];
            Arrays.fill(this.values, new Boolean(value));
            this.op = op;
            this.inverse = inverse;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getValues() {
            return values;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toEjbQl(QueryBuilder parent) {
            StringBuilder builder = new StringBuilder();
            builder.append(inverse ? " NOT (" : " (");
            boolean firstProperty = true;
            for (String name : propertyNames) {
                if (!firstProperty) {
                    builder.append(op + SPACE);
                }
                builder.append(name + " = ? ");
                firstProperty = false;
            }
            builder.append(") ");
            return builder.toString();
        }
    }
  
}
