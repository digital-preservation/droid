/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javax.sql.DataSource;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReader;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReaderCallback;
import uk.gov.nationalarchives.droid.export.interfaces.JobCancellationException;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.SqlUtils;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;
import uk.gov.nationalarchives.droid.results.handlers.JDBCBatchResultHandlerDao;

/**
 * @author Brian O'Reilly (based on SQLItemReader).
 * @param <T> The type of the item to read.
 */
public class JDBCSqlItemReader<T> implements ItemReader<T> {

    private static final String PUID = "PUID";
    private static final String NODE_ID = "NODE_ID";
    private static final String NAME = "NAME";

    private static final String SELECT_PROFILE_ALL_FIELDS = "select p.*, ";

    private ResultSet cursor;
    private PreparedStatement profileStatement;
    private int fetchSize;
    private int chunkSize;

    private DataSource datasource;

    private JDBCBatchResultHandlerDao resultHandlerDao;
    private IdentificationReader identificationReader;

    private final Log log = LogFactory.getLog(getClass());

    private final Class<T> typeParameterClass;

    //For use in determining filter parameter types so we can set these to the correct SQL type.
    private  enum ClassName {
        String,
        Date,
        Long,
        Integer,
        Boolean
    }

    /**
     * Default constructor.
     * BNO - see comment for read() method below.  As things stand, using the 2nd constructor would always
     * result in an error if the type parameter is not assignable to ProfileResourceNode
     */
    public JDBCSqlItemReader() {
        this.typeParameterClass = (Class<T>) ProfileResourceNode.class;
    }

    /**
     *
     * @param typeParameterClass The class to use for the type parameter - see comment in read().
     */
    public JDBCSqlItemReader(Class<T>  typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }


    /**
     * Get the JDBCBatchResultHandlerDao.
     * @return the class JDBCBatchResultHandlerDao.
     */
    public JDBCBatchResultHandlerDao getResultHandlerDao() {
        return resultHandlerDao;
    }

    /**
     * Set the JDBCBatchResultHandlerDao.
     * @param resultHandlerDao  The resultHandlerDao to set
     */
    public void setResultHandlerDao(JDBCBatchResultHandlerDao resultHandlerDao) {
        this.resultHandlerDao = resultHandlerDao;
        this.datasource = resultHandlerDao.getDatasource();
    }

    /**
     * Reads the next profileResourceNode from the cursor.
     * @return The ProfileResourceNode to read, or null if there are no further nodes.
     */
    //CHECKSTYLE:OFF  One statement too many ...
    @SuppressWarnings("unchecked")
    private ProfileResourceNode readNode() {

        try {
            if (cursor.next()) {

                NodeMetaData metaData = new NodeMetaData();
                Timestamp timestamp = cursor.getTimestamp("LAST_MODIFIED_DATE");
                if (timestamp != null) {
                    metaData.setLastModified(timestamp.getTime());
                }

                metaData.setName(cursor.getString(NAME));
                metaData.setExtension(cursor.getString("EXTENSION"));
                metaData.setIdentificationMethod(IdentificationMethod.getIdentifationMethodForOrdinal(
                        cursor.getInt("IDENTIFICATION_METHOD")));
                metaData.setResourceType(ResourceType.getResourceTypeForOrdinal(cursor.getInt("RESOURCE_TYPE")));

                if (metaData.getResourceType() != ResourceType.FOLDER) {
                    metaData.setSize(cursor.getLong("FILE_SIZE"));
                }

                metaData.setHash(cursor.getString("HASH"));
                metaData.setNodeStatus(NodeStatus.DONE);

                ProfileResourceNode profileResourceNode = new ProfileResourceNode(new URI(cursor.getString("URI")));
                Long currentNodeId = cursor.getLong(NODE_ID);
                profileResourceNode.setId(currentNodeId);
                profileResourceNode.setParentId(cursor.getLong("PARENT_ID"));
                profileResourceNode.setExtensionMismatch(cursor.getBoolean("EXTENSION_MISMATCH"));
                profileResourceNode.setMetaData(metaData);

                int numberOfIdentifications =  cursor.getInt("id_count");

                for (int i = numberOfIdentifications; i > 0; i--) {
                    String puid = cursor.getString(PUID);
                    if (cursor.getLong(NODE_ID) == currentNodeId) {
                        Format format = this.identificationReader.getFormatForPuid(puid);
                        profileResourceNode.addFormatIdentification(format);
                    } else {
                        throw new SQLDataException("Unexpected node ID during traversal of identification results!");
                    }

                    if (i > 1) {
                        if (!cursor.next()) {
                            break;
                        }
                    }
                }

                if (metaData.getResourceType() != ResourceType.FOLDER
                    && profileResourceNode.getIdentificationCount() == null) {
                    profileResourceNode.setZeroIdentifications();
                }

                return new ProfileResourceNode(profileResourceNode);
            }
        } catch (URISyntaxException ex) {
            log.error("Syntax error reading Profile resource Node in JDBCSqlItemReader class", ex);
        } catch (SQLException ex) {
            log.error("SQL Exception error reading Profile resource Node in JDBCSqlItemReader class", ex);
        }

        return null;
    }
    //CHECKSTYLE:ON
    /**
     *
     * @return The item read from the  cursor (which must be a ProfileResourceNode or subclass thereof
     * BNO: Not particularly elegant, but one way of working around the limitations of Java generics...
     */
    public T read() {

        if (this.typeParameterClass.isAssignableFrom(ProfileResourceNode.class)) {
            ProfileResourceNode node = readNode();
            return (T) node;
        } else {
            throw new NotImplementedException("Unsupported generic type for JDBCSqlItemReader!");
        }
    }

    /**
     *
     * @param callback the callback with items read
     * @param filter an optional filter
     * @throws JobCancellationException If the caller cancels the operation
     */
    public void readAll(ItemReaderCallback<T> callback, Filter filter) throws JobCancellationException {
        open(filter);

        this.identificationReader = new IdentificationReader();

        try {
            List<T> chunk = new ArrayList<T>();

            T item;
            while ((item = read()) != null) {
                chunk.add(item);
                if (chunk.size() == chunkSize) {
                    callback.onItem(chunk);
                    chunk = new ArrayList<T>();
                }
            }

            if (!chunk.isEmpty()) {
                callback.onItem(chunk);
                chunk = new ArrayList<T>();
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
    //@Override
    public void open(Filter filter) {
        this.cursor = getProfileCursor(filter);
    }

    /**
     * Close the open session.
     */
    //@Override
    public void close() {

        try {
            if (this.cursor != null) {
                this.cursor.close();
            }

            if (this.profileStatement != null) {
                this.profileStatement.close();
            }

            this.identificationReader.closeResources();
        } catch (SQLException e) {
            log.error("Error cleaning up JDBSCSqlItemReader", e);
        }
    }

    /**
     * Get a cursor over all of the results, with the forward-only flag set.
     * 
     * @return a forward-only {@link ResultSet}
     */
    //CHECKSTYLE:OFF Too mant statements and a few other minor issues, revisit when time allows..
    private ResultSet getProfileCursor(Filter filter)  {

        ResultSet profileResultSet = null;

        try {
            final Connection conn = datasource.getConnection();

            String queryString = "";
            boolean filterExists = filter != null && filter.isEnabled();
            if (filterExists) {
                QueryBuilder queryBuilder = SqlUtils.getQueryBuilder(filter);
                String ejbFragment = queryBuilder.toEjbQl();
                boolean formatCriteriaExist = ejbFragment.contains("format.");
                String sqlFilter = SqlUtils.transformEJBtoSQLFields(ejbFragment, "p", "f");
                queryString = SELECT_PROFILE_ALL_FIELDS;

                queryString += "(select count('x') from identification i1 where i1.node_id = p.node_id) AS id_count, i.PUID ";

                queryString += "from profile_resource_node p inner join identification i on p.node_id = i.node_id ";

                if (formatCriteriaExist) {
                    queryString += " inner join format f on f.puid = i.puid ";
                }

                //(1) To get only rows including the filter value e.g. if there are multiple PUIDs but only one is
                // listed in the filter, only the matching one will be returned:
                // queryString += "where " + sqlFilter;

                //(2) TO get all identifications where any of the identifications matches a filter condition.
                // E.g. if there are 2 PUIDs but only one is listed in the filter, both will be returned.
                // This is the current behaviour with DROID 6.1.5
                queryString += "where p.node_id IN (SELECT p2.node_id FROM profile_resource_node p2 ";
                queryString += " INNER JOIN identification i2 ON p2.node_id = i2.node_id ";
                queryString += "INNER JOIN format f2 on i2.puid = f2.puid ";
                queryString += "WHERE " + sqlFilter.replace("f.", "f2.") + ") order by p.node_id";

                int i = 0;

                this.profileStatement = conn.prepareStatement(queryString);

                for (Object value : queryBuilder.getValues()) {
                    Object value2 = SqlUtils.transformParameterToSQLValue(value);

                    String className = value2.getClass().getSimpleName();

                    //Java 6 doesn't support switch on string!!
                    switch(ClassName.valueOf(className)) {
                        case String:
                            this.profileStatement.setString(++i, (String) value2);
                            break;
                        case Date:
                            java.util.Date d = (java.util.Date) value2;
                            this.profileStatement.setDate(++i, new java.sql.Date(d.getTime()));
                            break;
                        case Long:
                            this.profileStatement.setLong(++i, (Long) value2);
                            break;
                        case Integer:
                            this.profileStatement.setInt(++i, (Integer) value2);
                            break;
                        default:
                            log.error("Invalid filter parameter type in JDBCSQLItemReader");
                            break;
                    }
                }
            } else {
                queryString = SELECT_PROFILE_ALL_FIELDS;
                queryString += "(select count('x') from identification i1 where i1.node_id = p.node_id) AS id_count, i.PUID ";
                queryString += "from profile_resource_node p  inner join identification i on p.node_id = i.node_id order by p.node_id";
                profileStatement = conn.prepareStatement(queryString);
            }
            profileResultSet = profileStatement.executeQuery();

        } catch (SQLException ex) {
            log.error("A database exception occurred retrieving nodes " + ex);
        }

        return profileResultSet;

    }
    //CHECKSTYLE:ON
    /**
     * Set the cursor fetch size.
     * @param fetchSize  The number of records to fetch each time.
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

    private class IdentificationReader {

        //CHECKSTYLE:OFF  1 char over line limit.  Let's have some common sense, please...
        private static final String FORMAT_QUERY = "SELECT T1.NODE_ID, T1.PUID, T2.MIME_TYPE, T2.NAME, T2.VERSION "
                + "FROM IDENTIFICATION AS T1 INNER JOIN FORMAT AS T2 ON T1.PUID = T2.PUID "
                + "WHERE T1.NODE_ID = ?";

        private static final String FORMAT_QUERY_RANGE = "SELECT T1.NODE_ID, T1.PUID, T2.MIME_TYPE, T2.NAME, T2.VERSION "
                + "FROM IDENTIFICATION AS T1 INNER JOIN FORMAT AS T2 ON T1.PUID = T2.PUID "
                + "WHERE T1.NODE_ID BETWEEN ? AND ?";
        //CHECKSTYLE:ON
        private static final String SELECT_FORMATS               = "SELECT * FROM FORMAT";
        private static final String SELECT_FORMAT_COUNT          = "SELECT COUNT('x') AS total FROM FORMAT";

        private Map<String, Format> formats;
        private Connection connection;
        private PreparedStatement formatsStatement;

        IdentificationReader()  {
            try {
                this.connection  = JDBCSqlItemReader.this.datasource.getConnection();
                this.formatsStatement = this.connection.prepareStatement(FORMAT_QUERY);
                this.formats = loadAllFormats();
            } catch (SQLException ex) {
                log.error("Error retrieving SQL connection for format identifications", ex);
            }
        }

        List<Format> getFormatsForResourceNode(long nodeId) {
        /*
        BNO.  Another possible implementation is to retrieve all rows ordered by node id
        into the ResultSet.  We would then iterate the ResultSet to retrieve identifications for
        nodeId.  Since the nodes are also retrieved in ascending order, we should always be at the
        correct point - if the current node id is < nodeId, we would iterate until the current node id
        was > nodeId (ideally we would fetch beyond here but see below re.fetch size.
        However, given the lack of Derby support for forward only cursors and that it
        only appears to support a fetch size of 1, it's unclear if this would be more performant than
        the current approach.
         */
            try {
                this.formatsStatement.setLong(1, nodeId);
                ResultSet rs = formatsStatement.executeQuery();
                List formatsList = new ArrayList<Format>();

                while (rs.next()) {
                    Format format = new Format();
                    format.setPuid(rs.getString(PUID));
                    format.setMimeType(rs.getString("MIME_TYPE"));
                    format.setName(rs.getString(NAME));
                    format.setVersion(rs.getString("VERSION"));
                    formatsList.add(format);
                }
                rs.close();
                return formatsList;

            } catch (SQLException ex) {
                log.error("Error retrieving format identifications", ex);
            }
            return Collections.EMPTY_LIST;
        }

        private Map<String, Format> loadAllFormats() {
            //CHECKSTYLE:OFF Too many nested tries - ToDO review
            Map<String, Format> formatsToLoad = null;
            try {
                final Connection conn = datasource.getConnection();
                try {
                    //BNO - Get the actual number of formats so we can initialise the list based on the current count.
                    //final PreparedStatement getFormatCount = conn.prepareStatement(SELECT_FORMAT_COUNT);
                    //final ResultSet rsFormatCount = getFormatCount.executeQuery();
                    //rsFormatCount.next();
                    //final int formatCount = rsFormatCount.getInt("total");
                    formatsToLoad = new HashMap<String, Format>();

                    final PreparedStatement loadFormat = conn.prepareStatement(SELECT_FORMATS);
                    try {
                        final ResultSet results = loadFormat.executeQuery();
                        try {
                            while (results.next()) {
                                String puid = results.getString(PUID);
                                formatsToLoad.put(puid, SqlUtils.buildFormat(results));
                            }
                        } finally {
                            results.close();
                        }
                    } finally {
                        loadFormat.close();
                    }
                } finally {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("A database exception occurred getting all formats.", e);
            }
            return formatsToLoad;
            //CHECKSTYLE:ON
        }

        public Format getFormatForPuid(String puid) {
            Format format = this.formats.get(puid);
            return format;
        }


        private void closeResources() {
            try {
                if (this.formatsStatement != null) {
                    this.formatsStatement.close();
                }
/*
            BNO: Note that we do not close the connection - since it was not opened in this class
*/
            } catch (SQLException e) {
                log.error("Error cleaning up resources for IdentificationReader", e);
            }
        }
    }

}
