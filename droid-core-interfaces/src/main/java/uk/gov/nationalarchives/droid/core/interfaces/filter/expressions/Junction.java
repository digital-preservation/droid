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
import java.util.Iterator;
import java.util.List;

/**
 * @author rflitcroft
 *
 */
public abstract class Junction implements Criterion {

    private static final String SPACE = " ";
    private List<Criterion> criteria = new ArrayList<Criterion>();
    private String op;
    
    /**
     * 
     * @param op the operation
     */
    protected Junction(String op) {
        this.op = op;
    }
    
    /**
     * Adds a criterion to this Junction.
     * @param criterion the criterion to add
     * @return this Junction
     */
    public Junction add(Criterion criterion) {
        criteria.add(criterion);
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getValues() {
        List<Object> params = new ArrayList<Object>();
        
        for (Criterion criterion : criteria) {
            params.addAll(Arrays.asList(criterion.getValues()));
        }
        
        return params.toArray();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toEjbQl(QueryBuilder parent) {
        if (criteria.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Iterator<Criterion> it = criteria.iterator(); it.hasNext();) {
            Criterion criterion = it.next();
            sb.append(criterion.toEjbQl(parent));
            if (it.hasNext()) {
                sb.append(SPACE + op + SPACE);
            }
        }
        sb.append(")");
        
        return sb.toString();
    }
}
