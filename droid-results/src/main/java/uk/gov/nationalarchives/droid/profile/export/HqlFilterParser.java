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
package uk.gov.nationalarchives.droid.profile.export;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;
import uk.gov.nationalarchives.droid.profile.SqlUtils;

/**
 * @author rflitcroft
 *
 */
public class HqlFilterParser {

    private final Log log = LogFactory.getLog(getClass());
    
    /**
     * Parses a filter into HQL.
     * @param filter the filter to parse
     * @param hibernateSession the hibernate session
     * @param baseQuery the base query
     * @return a hibernate query
     */
    public Query parse(Filter filter, Session hibernateSession, String baseQuery) {
        
        QueryBuilder queryBuilder = SqlUtils.getQueryBuilder(filter);
        
        String ejbFilter = queryBuilder.toEjbQl();
        String startQuery = baseQuery;
        if (ejbFilter.contains("format.")) {
            startQuery = baseQuery + " INNER JOIN profileResourceNode.formatIdentifications as format ";
        }
        
        final String queryString = startQuery + " WHERE " + ejbFilter;
        log.debug("Query = " + queryString);

        Query query = hibernateSession.createQuery(queryString);
        int i = 0;
        for (Object value : queryBuilder.getValues()) {
            query.setParameter(i++, value);
        }
        
        return query;
    }
    
}
