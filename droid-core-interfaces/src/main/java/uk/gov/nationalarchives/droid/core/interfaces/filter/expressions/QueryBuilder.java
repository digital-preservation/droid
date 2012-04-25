/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.filter.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Builds JPAQl queriy framgments. Do not mix these fragments with JPA queries which use named parameters!!.
 * @author rflitcroft
 *
 */
public final class QueryBuilder implements Criterion {

    private String alias;
    private List<Criterion> criteria = new ArrayList<Criterion>();
    private Set<String> aliases = new HashSet<String>();
    
    private QueryBuilder(String alias) {
        this.alias = alias;
    }
    
    /**
     * Cinstructs a new QueryBuilder for the given class and alias.
     * @param alias the base entity's alias
     * @return a new QueryBuilder
     */
    public static QueryBuilder forAlias(String alias) {
        return new QueryBuilder(alias);
    }
    
    /**
     * Adds an expression to an aliassed entity.
     * @param exp the expression to add
     * @return this builder
     */
    public QueryBuilder add(Criterion exp) {
        criteria.add(exp);
        return this;
    }

    /**
     * Adds an alias for an entity.
     * @param newAlias the alias
     * @return a new QueryBuilder for the alias
     */
    public QueryBuilder createAlias(String newAlias) {
        aliases.add(newAlias);
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getValues() {
        List<Object> params = new ArrayList<Object>();
        
        for (Criterion criterion : criteria) {
            if (criterion != null) {
                params.addAll(Arrays.asList(criterion.getValues()));
            }
        }
        
        return params.toArray();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toEjbQl(QueryBuilder parent) {
        StringBuilder sb = new StringBuilder();
        
        for (Iterator<Criterion> it = criteria.iterator(); it.hasNext();) {
            Criterion criterion = it.next();
            if (criterion != null) {
                sb.append(criterion.toEjbQl(this));
                if (it.hasNext()) {
                    sb.append(" AND ");
                }
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 
     * @return JPA-QL for this query.
     */
    public String toEjbQl() {
        return toEjbQl(null);
    }
    
    /**
     * @return the alias
     */
    String getAlias() {
        return alias;
    }
    
    /**
     * @return the aliases
     */
    public Set<String> getAliases() {
        return aliases;
    }
    
}
