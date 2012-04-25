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
import uk.gov.nationalarchives.droid.export.interfaces.ItemReader;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReaderCallback;
import uk.gov.nationalarchives.droid.export.interfaces.JobCancellationException;

/**
 * @author rflitcroft
 * @param <T>
 *
 */
public class HibernateItemReader<T> implements ItemReader<T> {
    
//    /** Query String job option. */
//    public static final String QUERY_STRING = "query.string";
    
    private SessionFactory sessionFactory;
    private Session session;
    private ScrollableResults cursor;
    private int fetchSize;
    private int chunkSize;
//    private Map<String, Object> parameterValues;
    
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
     * @throws JobCancellationException 
     */
    @Override
    //@Transactional(propagation = Propagation.REQUIRED)
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
     * @param filter an optional filter
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
        
        String queryString = " from ProfileResourceNode profileResourceNode ";

        
//        String queryString = " from FormatIdentification fis " 
//            + " inner join fetch fis.node profileResourceNode " 
//            + " left join profileResourceNode.systemMessages sm " 
//            + " inner join fetch fis.format format";

        Query query;
        if (filter != null && filter.isEnabled()) {
            HqlFilterParser filterParser = new HqlFilterParser();
            query = filterParser.parse(filter, session, queryString);
        } else {
            query = session.createQuery(queryString);
        }
        
        return query.setFetchSize(fetchSize).scroll(ScrollMode.FORWARD_ONLY);
    }

    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * Fetch size used internally by Hibernate to limit amount of data fetched
     * from database per round trip.
     * 
     * @param fetchSize the fetch size to pass down to Hibernate
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }
    
    /**
     * @param chunkSize the chunkSize to set
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }


}
