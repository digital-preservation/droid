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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReader;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReaderCallback;
import uk.gov.nationalarchives.droid.export.interfaces.JobCancellationException;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.SqlUtils;

/**
 * @author a-mpalmer
 * @param <T>
 */
public class SqlItemReader<T> implements ItemReader<T> {

    // /** Query String job option. */

    private SessionFactory sessionFactory;
    private Session session;
    private ScrollableResults cursor;
    private int fetchSize;
    private int chunkSize;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public T read() {

        if (cursor.next()) {
            Object[] data = cursor.get();

            // Assume if there is only one item that it is the data the user
            // wants.
            // If there is only one item this is going to be a nasty shock
            // if T is an array type but there's not much else we can do...
            T item = (T) data[0];
            return item;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws JobCancellationException
     */
    @Override
    // @Transactional(propagation = Propagation.REQUIRED)
    public void readAll(ItemReaderCallback<T> callback, Filter filter) throws JobCancellationException {
        open(filter);

        try {
            List<T> chunk = new ArrayList<T>();

            T item;
            while ((item = read()) != null) {
                chunk.add(item);
                if (chunk.size() == chunkSize) {
                    callback.onItem(chunk);
                    chunk = new ArrayList<T>();
                    session.clear();
                }
            }

            if (!chunk.isEmpty()) {
                callback.onItem(chunk);
                chunk = new ArrayList<T>();
                session.clear();
            }
        } finally {
            close();
        }
    }

    /**
     * Opens this item reader for reading.
     * 
     * @param filter
     *            an optional filter
     */
    @Override
    public void open(Filter filter) {
        cursor = getForwardOnlyCursor(filter);
    }

    /**
     * Close the open session.
     */
    @Override
    public void close() {
        if (session != null) {
            try {
                session.close();
            } finally {
                session = null;
            }
        }
    }

    /**
     * Get a cursor over all of the results, with the forward-only flag set.
     * 
     * @return a forward-only {@link ScrollableResults}
     */
    private ScrollableResults getForwardOnlyCursor(Filter filter) {
        if (session == null) {
            session = sessionFactory.openSession();
            session.setFlushMode(FlushMode.MANUAL);
        }
        Query query;
        String queryString = "";
        boolean filterExists = filter != null && filter.isEnabled();
        if (filterExists) {
            QueryBuilder queryBuilder = SqlUtils.getQueryBuilder(filter);
            String ejbFragment = queryBuilder.toEjbQl();
            boolean formatCriteriaExist = ejbFragment.contains("format.");
            String sqlFilter = SqlUtils.transformEJBtoSQLFields(ejbFragment, "profile", "form");
            queryString = formatCriteriaExist ? "select distinct profile.* " : "select profile.* ";
            queryString += "from profile_resource_node as profile ";
            if (formatCriteriaExist) {
                queryString += "inner join identification as ident on ident.node_id = profile.node_id"
                    + " inner join format as form on form.puid = ident.puid ";
            }
            queryString += "where " + sqlFilter;
            query = session.createSQLQuery(queryString).addEntity(ProfileResourceNode.class);
            int i = 0;
            for (Object value : queryBuilder.getValues()) {
                Object value2 = SqlUtils.transformParameterToSQLValue(value);
                query.setParameter(i++, value2);
            }        
        } else {
            queryString = "select * from profile_resource_node";
            query = session.createSQLQuery(queryString).addEntity(ProfileResourceNode.class);
        }
        return query.setFetchSize(fetchSize).scroll(ScrollMode.FORWARD_ONLY);
    }

    
    /**
     * @param sessionFactory
     *            the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Fetch size used internally by Hibernate to limit amount of data fetched
     * from database per round trip.
     * 
     * @param fetchSize
     *            the fetch size to pass down to Hibernate
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    /**
     * @param chunkSize
     *            the chunkSize to set
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

}
