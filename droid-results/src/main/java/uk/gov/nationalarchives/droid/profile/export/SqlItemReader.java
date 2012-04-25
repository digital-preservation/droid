/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
