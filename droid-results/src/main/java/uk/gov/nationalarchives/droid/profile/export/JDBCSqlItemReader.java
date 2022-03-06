/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReader;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReaderCallback;
import uk.gov.nationalarchives.droid.export.interfaces.JobCancellationException;
import uk.gov.nationalarchives.droid.profile.JDBCProfileDao;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNodeFilter;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;
import uk.gov.nationalarchives.droid.results.handlers.JDBCBatchResultHandlerDao;

/**
 * @author Brian O'Reilly (based on SQLItemReader).
 */
public class JDBCSqlItemReader implements ItemReader<ProfileResourceNode> {

    private static final String PUID = "PUID";
    private static final String NODE_ID = "NODE_ID";
    private static final String NAME = "NAME";
    private static final String MIME_TYPE = "MIME_TYPE";
    private static final String VERSION = "VERSION";

    private static final String EMPTY_FOLTER_SUBSELECT = " CASE \n"
            + "\t\t  WHEN p.RESOURCE_TYPE = 0 THEN \n"
            + "\t\t  \tCASE\n"
            + "\t\t  \t\twhen NOT EXISTS(SELECT NODE2.PARENT_ID FROM PROFILE_RESOURCE_NODE NODE2 WHERE NODE2.PARENT_ID = p.NODE_ID) then true\n"
            + "\t\t  \t\telse false\n"
            + "\t\t  \tEND\n"
            + "\t\t  ELSE false\n"
            + "\t\tEND as EMPTY_DIR,  ";


    private static final String SELECT_PROFILE_ALL_FIELDS = "select p.*, ";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ResultSet cursor;
    private PreparedStatement profileStatement;
    private int fetchSize;
    private int chunkSize;
    private DataSource datasource;
    private JDBCBatchResultHandlerDao resultHandlerDao;
    private IdentificationReader identificationReader;

    /**
     * Default constructor.
     * BNO - see comment for read() method below.  As things stand, using the 2nd constructor would always
     * result in an error if the type parameter is not assignable to ProfileResourceNode
     */
    public JDBCSqlItemReader() {
    }

    /**
     * Parameterized constructor.
     * @param resultHandlerDao Sets the resulthandlerdao to use.
     */
    public JDBCSqlItemReader(JDBCBatchResultHandlerDao resultHandlerDao) {
        setResultHandlerDao(resultHandlerDao);
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
     private ProfileResourceNode readNode() {
        try {
            if (cursor.next()) {
                ProfileResourceNode profileResourceNode;
                profileResourceNode = JDBCProfileDao.PROFILE_RESOURCE_NODE_ROW_MAPPER_WITH_EMPTY_FOLDER.mapRow(cursor, 0);
                NodeMetaData metaData = profileResourceNode.getMetaData();

                int numberOfIdentifications =  cursor.getInt("id_count");

                for (int i = numberOfIdentifications; i > 0; i--) {
                    String puid = cursor.getString(PUID);
                    if (cursor.getLong(NODE_ID) == profileResourceNode.getId()) {
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
        } catch (SQLException ex) {
            log.error("SQL Exception error reading Profile resource Node in JDBCSqlItemReader class", ex);
        }

        return null;
    }

    /**
     *
     * @return The item read from the  cursor (which must be a ProfileResourceNode or subclass thereof
     * BNO: Not particularly elegant, but one way of working around the limitations of Java generics...
     */
    public ProfileResourceNode read() {
        return readNode();
    }

    /**
     *
     * @param callback the callback with items read
     * @param itemFilter an optional filter
     * @throws JobCancellationException If the caller cancels the operation
     */
    public void readAll(ItemReaderCallback<ProfileResourceNode> callback, Filter itemFilter) throws JobCancellationException {
        open(itemFilter);

        this.identificationReader = new IdentificationReader();
        boolean filterExists = itemFilter != null && itemFilter.isEnabled();
        ProfileResourceNodeFilter nodeFilter = new ProfileResourceNodeFilter(filterExists ? itemFilter : null);
        try {
            List<ProfileResourceNode> chunk = new ArrayList<>();

            ProfileResourceNode item;
            while ((item = read()) != null) {
                if (nodeFilter.passesFilter(item)) {
                    chunk.add(item);
                    if (chunk.size() == chunkSize) {
                        callback.onItem(chunk);
                        chunk = new ArrayList<>();
                    }
                }
            }

            if (!chunk.isEmpty()) {
                callback.onItem(chunk);
                chunk = new ArrayList<>();
            }
        } finally {
            close();
        }
    }

    /**
     * Opens this item reader for reading.
     * 
     * @param itemFilter
     *            an optional filter
     */
    //@Override
    public void open(Filter itemFilter) {
        this.cursor = getProfileCursor();
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
        } catch (SQLException e) {
            log.error("Error cleaning up JDBSCSqlItemReader", e);
        }
    }

    /**
     * Get a cursor over all of the results, with the forward-only flag set.
     *
     * @return a forward-only {@link ResultSet}
     */
    private ResultSet getProfileCursor()  {
        ResultSet profileResultSet = null;
        try {
            final Connection conn = datasource.getConnection();
            String queryString = SELECT_PROFILE_ALL_FIELDS;
            queryString += EMPTY_FOLTER_SUBSELECT;
            queryString += "(select count('x') from identification i1 where i1.node_id = p.node_id) AS id_count, i.PUID ";
            queryString += "from profile_resource_node p  inner join identification i on p.node_id = i.node_id order by p.uri,p.node_id";
            profileStatement = conn.prepareStatement(queryString);
            profileResultSet = profileStatement.executeQuery();
        } catch (SQLException ex) {
            log.error("A database exception occurred retrieving nodes ", ex);
        }
        return profileResultSet;
    }

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

        private static final String FORMAT_QUERY_RANGE = "SELECT T1.NODE_ID, T1.PUID, T2.MIME_TYPE, T2.NAME, T2.VERSION "
                + "FROM IDENTIFICATION AS T1 INNER JOIN FORMAT AS T2 ON T1.PUID = T2.PUID "
                + "WHERE T1.NODE_ID BETWEEN ? AND ?";
        //CHECKSTYLE:ON
        private static final String SELECT_FORMATS               = "SELECT * FROM FORMAT";
        private Map<String, Format> formats;

        IdentificationReader()  {
            this.formats = loadAllFormats();
        }

        private Map<String, Format> loadAllFormats() {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
            List<Format> formatsFromDatabase = jdbcTemplate.query(
                    SELECT_FORMATS,
                    (rs, rowNum) -> {
                        Format format = new Format();
                        format.setPuid(rs.getString(PUID));
                        format.setMimeType(rs.getString(MIME_TYPE));
                        format.setName(rs.getString(NAME));
                        format.setVersion(rs.getString(VERSION));
                        return format;
                    });


            Map<String, Format> theformats =  formatsFromDatabase.stream().filter(format -> format.getPuid() != null).collect(
                    Collectors.toMap(Format::getPuid, x -> x));

            // special handling for null format key
            theformats.putAll(formatsFromDatabase.stream().filter(format -> format.getPuid() == null).collect(
                    Collectors.toMap(x -> "", x -> x)));

            return theformats;
        }

        public Format getFormatForPuid(String puid) {
            return this.formats.get(puid);
        }
    }

}
