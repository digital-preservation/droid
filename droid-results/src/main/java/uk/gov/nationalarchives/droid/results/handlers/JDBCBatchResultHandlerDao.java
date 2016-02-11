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
package uk.gov.nationalarchives.droid.results.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ResourceUtils;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.SqlUtils;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * An implementation of the ResultHandlerDao interface, using JDBC to access the profile database directly.
 *
 * @author Matt Palmer, boreilly
 */
public class JDBCBatchResultHandlerDao implements ResultHandlerDao {

    //CHECKSTYLE:OFF  Various formatting issues with SQL Statements.  E.g. some longer than 120 lines but
    // splitting them likely to hamper rather than assist readability here.

    // How many results in the batch before committing.
    public static final int BATCH_LIMIT = 100;

    // A "poison-pill" node info to signal to the writing thread that
    // it should terminate and commit any results so far.
    private static final NodeInfo COMMIT_SO_FAR = new NodeInfo(null, false);

    private static final String INSERT_PROFILE_RESOURCE_NODE =
            "INSERT INTO PROFILE_RESOURCE_NODE " +
                    "(NODE_ID,EXTENSION_MISMATCH,FINISHED_TIMESTAMP,IDENTIFICATION_COUNT," +
                    " EXTENSION,HASH,IDENTIFICATION_METHOD,LAST_MODIFIED_DATE,NAME,NODE_STATUS," +
                    " RESOURCE_TYPE,FILE_SIZE,PARENT_ID,PREFIX,PREFIX_PLUS_ONE,URI) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String INSERT_IDENTIFICATIONS       = "INSERT INTO IDENTIFICATION (NODE_ID,PUID) VALUES ";
    private static final String INSERT_ZERO_IDENTIFICATIONS  = INSERT_IDENTIFICATIONS + "(?,'')";
    private static final String INSERT_ONE_IDENTIFICATION    = INSERT_IDENTIFICATIONS + "(?,?)";
    private static final String INSERT_TWO_IDENTIFICATIONS   = INSERT_IDENTIFICATIONS + "(?,?),(?,?)";
    private static final String INSERT_THREE_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?)";
    private static final String INSERT_FOUR_IDENTIFICATIONS  = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?)";
    private static final String INSERT_FIVE_IDENTIFICATIONS  = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static final String INSERT_SIX_IDENTIFICATIONS   = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static final String INSERT_SEVEN_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static final String INSERT_EIGHT_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static final String INSERT_NINE_IDENTIFICATIONS  = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static final String INSERT_TEN_IDENTIFICATIONS   = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";

    private static final String[] INSERT_IDENTIFICATION      = {
        INSERT_ZERO_IDENTIFICATIONS, INSERT_ONE_IDENTIFICATION,
        INSERT_TWO_IDENTIFICATIONS, INSERT_THREE_IDENTIFICATIONS, INSERT_FOUR_IDENTIFICATIONS,
        INSERT_FIVE_IDENTIFICATIONS, INSERT_SIX_IDENTIFICATIONS, INSERT_SEVEN_IDENTIFICATIONS,
        INSERT_EIGHT_IDENTIFICATIONS, INSERT_NINE_IDENTIFICATIONS, INSERT_TEN_IDENTIFICATIONS, };

    private static final String UPDATE_NODE_STATUS = "UPDATE PROFILE_RESOURCE_NODE SET NODE_STATUS = ? WHERE NODE_ID = ?";
    private static final String DELETE_NODE = "DELETE FROM PROFILE_RESOURCE_NODE WHERE NODE_ID = ?";
    private static final String SELECT_FORMAT = "SELECT PUID, MIME_TYPE, NAME, VERSION FROM FORMAT WHERE PUID = ?";

    private static final String SELECT_FORMAT_COUNT = "SELECT COUNT('x') AS total FROM FORMAT";
    private static final String SELECT_FORMATS = "SELECT PUID, MIME_TYPE, NAME, VERSION FROM FORMAT";
    private static final String SELECT_PROFILE_RESOURCE_NODE =
            "SELECT NODE_ID, EXTENSION_MISMATCH, FINISHED_TIMESTAMP, IDENTIFICATION_COUNT, EXTENSION, HASH, "
            + "IDENTIFICATION_METHOD, LAST_MODIFIED_DATE, NAME, NODE_STATUS, RESOURCE_TYPE, FILE_SIZE, "
            + "PARENT_ID, PREFIX, PREFIX_PLUS_ONE, TEXT_ENCODING, URI FROM PROFILE_RESOURCE_NODE WHERE NODE_ID = ?";

    private static final String SELECT_IDENTIFICATIONS = "SELECT NODE_ID, PUID FROM IDENTIFICATION WHERE NODE_ID = ?";
    private static final String DELETE_IDENTIFICATIONS = "DELETE FROM IDENTIFICATION WHERE NODE_ID = ?";
    private static final String MAX_NODE_ID_QUERY = "SELECT MAX(NODE_ID) FROM PROFILE_RESOURCE_NODE";

    //DDL statements
    private static final String CREATE_TABLE_FORMAT =
            "CREATE TABLE FORMAT (PUID VARCHAR(255) NOT NULL, MIME_TYPE VARCHAR(255), NAME VARCHAR(255), "
            + "VERSION VARCHAR(255), U_NAME GENERATED ALWAYS AS (UPPER(NAME)), PRIMARY KEY (PUID))";
    private static final String CREATE_TABLE_IDENTIFICATION =
            "CREATE TABLE IDENTIFICATION (NODE_ID BIGINT NOT NULL, PUID VARCHAR(255) NOT NULL, "
            + "PRIMARY KEY(NODE_ID, PUID))";
    private static final String CREATE_TABLE_PRN = "CREATE TABLE PROFILE_RESOURCE_NODE (NODE_ID BIGINT NOT NULL, EXTENSION_MISMATCH BOOLEAN NOT NULL, "
                                                + "FINISHED_TIMESTAMP TIMESTAMP, IDENTIFICATION_COUNT INTEGER, EXTENSION VARCHAR(255), "
                                                + "HASH VARCHAR(64), IDENTIFICATION_METHOD INTEGER, LAST_MODIFIED_DATE TIMESTAMP, NAME VARCHAR(1000) NOT NULL, "
                                                + "NODE_STATUS INTEGER, RESOURCE_TYPE INTEGER NOT NULL, FILE_SIZE BIGINT, PARENT_ID BIGINT, PREFIX VARCHAR(255), "
                                                + "PREFIX_PLUS_ONE VARCHAR(255), TEXT_ENCODING INTEGER, URI VARCHAR(4000) NOT NULL, "
                                                + "U_EXTENSION GENERATED ALWAYS AS (UPPER(EXTENSION)), U_NAME GENERATED ALWAYS AS (UPPER(NAME)), "
                                                + "PRIMARY KEY (NODE_ID))";
    private static final String CREATE_IDX_MIME_TYPE_ON_FORMAT = "CREATE INDEX IDX_MIME_TYPE ON FORMAT (MIME_TYPE)";
    private static final String CREATE_IDX_FORMAT_NAME_ON_FORMAT = "CREATE INDEX IDX_FORMAT_NAME ON FORMAT (U_NAME)";

    private static final String CREATE_IDX_ID_COUNT_ON_PRN = "CREATE INDEX IDX_ID_COUNT ON PROFILE_RESOURCE_NODE (IDENTIFICATION_COUNT)";
    private static final String CREATE_IDX_PRN_EXT_ON_PRN = "CREATE INDEX IDX_PRN_EXTENSION ON PROFILE_RESOURCE_NODE (U_EXTENSION)";
    private static final String CREATE_IDX_PRN_ID_METHOD_ON_PRN = "CREATE INDEX IDX_PRN_ID_METHOD ON PROFILE_RESOURCE_NODE (IDENTIFICATION_METHOD)";
    private static final String CREATE_IDX_PRN_LAST_MODIFIED_ON_PRN = "CREATE INDEX IDX_PRN_LAST_MODIFIED ON PROFILE_RESOURCE_NODE (LAST_MODIFIED_DATE)";
    private static final String CREATE_IDX_PRN_NAME_ON_PRN = "CREATE INDEX IDX_PRN_NAME ON PROFILE_RESOURCE_NODE (NAME)";
    private static final String CREATE_IDX_PRN_NODE_STATUS_ON_PRN = "CREATE INDEX IDX_PRN_NODE_STATUS ON PROFILE_RESOURCE_NODE (NODE_STATUS)";
    private static final String CREATE_IDX_ID_RESOURCE_ON_PRN = "CREATE INDEX IDX_PRN_ID_RESOURCETYPE ON PROFILE_RESOURCE_NODE (RESOURCE_TYPE)";
    private static final String CREATE_IDX_PRN_FILE_SIZE_ON_PRN = "CREATE INDEX IDX_PRN_FILE_SIZE ON PROFILE_RESOURCE_NODE (FILE_SIZE)";
    private static final String CREATE_IDX_PARENT_ID_ON_PRN = "CREATE INDEX IDX_PARENT_ID ON PROFILE_RESOURCE_NODE (PARENT_ID)";
    private static final String CREATE_IDX_PREFIX_ON_PRN = "CREATE INDEX IDX_PREFIX ON PROFILE_RESOURCE_NODE (PREFIX)";
    private static final String CREATE_IDX_PREFIX_PLUS_ONE_ON_PRN = "CREATE INDEX IDX_PREFIX_PLUS_ONE ON PROFILE_RESOURCE_NODE (PREFIX_PLUS_ONE)";
    //private static String CREATE_IDX_TEXT_ENCODING_ON_PRN = "CREATE INDEX IDX_TEXT_ENCODING ON PROFILE_RESOURCE_NODE (TEXT_ENCODING)";
    //private static String CREATE_IDX_URI_ON_PRN = "CREATE INDEX IDX_URI ON PROFILE_RESOURCE_NODE (URI)";
    private static final String IDENTIFICATION_CONSTRAINT_1 = "ALTER TABLE IDENTIFICATION ADD CONSTRAINT FK_FH484CCWWL4E5W9QUQKE4N6RI FOREIGN KEY (PUID) REFERENCES FORMAT";
    private static final String IDENTIFICATION_CONSTRAINT_2 = "ALTER TABLE IDENTIFICATION ADD CONSTRAINT FK_TPXMO6PPUXECKDRELN5PT5E39 FOREIGN KEY (NODE_ID) REFERENCES PROFILE_RESOURCE_NODE";

    private static final String CREATE_UCASE_PRN_EXTN_COL = "ALTER TABLE PROFILE_RESOURCE_NODE ADD COLUMN U_EXTENSION GENERATED ALWAYS AS (UPPER(EXTENSION))";
    private static final String CREATE_UCASE_PRN_NAME_COL = "ALTER TABLE PROFILE_RESOURCE_NODE ADD COLUMN U_NAME GENERATED ALWAYS AS (UPPER(NAME))";
    private static final String CREATE_UCASE_FMT_NAME_COL = "ALTER TABLE FORMAT ADD COLUMN U_NAME GENERATED ALWAYS AS (UPPER(NAME))";

    private static final String ALTER_NAME_COLUMN_SIZE = "ALTER TABLE PROFILE_RESOURCE_NODE ALTER COLUMN NAME SET DATA TYPE VARCHAR(1000)";
    //CHECKSTYLE:ON
    private static final int PRN_COL_COUNT_SANS_UCASE_COLS = 17;
    private static final int PRN_COL_COUNT_WITH_UCASE_COLS = 19;

    private static boolean freshTemplate;
    private static final Object LOCKER = new Object();

    private static final int BLOCKING_QUEUE_SIZE = 256;
    private static final int MOST_RECENTLY_ADDED_NODE_CACHE_SIZE = 512;
    private static final int PUID_FORMAT_MAP_SIZE = 2500;

    private final Log log = LogFactory.getLog(getClass());

    private DataSource datasource;
    private AtomicLong nodeIds;

    private List<Format> formats;
    private Map<String, Format> puidFormatMap = new HashMap<String, Format>(PUID_FORMAT_MAP_SIZE);

    private BlockingQueue<NodeInfo> blockingQueue = new ArrayBlockingQueue<NodeInfo>(BLOCKING_QUEUE_SIZE);
    private MostRecentlyAddedNodeCache nodeCache  = new MostRecentlyAddedNodeCache(MOST_RECENTLY_ADDED_NODE_CACHE_SIZE);

    private Thread databaseWriterThread;
    private DatabaseWriter writer;

    @Override
    public void init() {

        // BNO: Need to allow for the possibility that we're dealing with a fresh DROID install (or first run
        // after a new signature?). In which case, the call to setUpFormatsAndDatabaseWriter() will fail since
        // the database schema objects (created by hibernate in previous versions of DROID) will not exist.  Moreover,
        // we can't just create the schema then call setUpFormatsAndDatabaseWriter() because the format data won't
        // have been populated. This is handled by ProfileContextLocator-generateNewDatabaseAndTemplates
        //  -profileManager.initProfile, after this class has been instantiated by Spring...
        synchronized (LOCKER) {
            if (!getIsFreshTemplate()) {
                checkCreateUpperCaseColumns();
                setUpFormatsAndDatabaseWriter();
            } else {
                createSchemaOnFreshTemplate();
            }
        }
    }

    @Override
    public void initialiseForNewTemplate() {

        if (JDBCBatchResultHandlerDao.getIsFreshTemplate()) {
            log.error("Cannot initialise the JDBCBatchResultHandlerDao because it is still in fresh template mode and "
                    + "the format reference data has not yet been populated");
            return;
        }

        synchronized (LOCKER) {
            // Check if already initialised - this will be the case if we;re starting from an existing template
            // since then the formats and database writer will have been set up in the init() method
            boolean alreadyInitialised = this.formats != null;
            if (!alreadyInitialised) {
                setUpFormatsAndDatabaseWriter();
            }
        }
    }

    //TODO: This method creates the calculated upper case columns required to support the case
    // insensitive filtering introduced in Release 6.1.6.  This is the first release with this feature,
    // and without this check users would get an SQL error if they already have DROID installed with
    // an existing template which does not have the columns.  However, the check
    // creates additional overhead in opening profiles - consider removing in a future
    // release when we can assume most people will have a template that already includes these columns.
    // In addition, we increase the size of the NAME column in the PROFILE_RESOURCE_NODE table to 1000
    // characters (it was 255 in the previous DROID version).  This is required to accommodate the long
    // extracted names in ARC and WARC files.
    private void checkCreateUpperCaseColumns() {

        Connection conn = null;
        PreparedStatement loadNode = null;
        PreparedStatement createColumn = null;
        ResultSet result = null;
        try {
            conn = datasource.getConnection();
            conn.setAutoCommit(false);
            loadNode = conn.prepareStatement("SELECT * FROM PROFILE_RESOURCE_NODE");
            result = loadNode.executeQuery();
            int numberOfColumnsInPrnTable = result.getMetaData().getColumnCount();
            result.close();
            int x;

            switch(numberOfColumnsInPrnTable) {
                case PRN_COL_COUNT_SANS_UCASE_COLS:
                    String[] statements =
                    {ALTER_NAME_COLUMN_SIZE, CREATE_UCASE_PRN_EXTN_COL, CREATE_UCASE_PRN_NAME_COL,
                        CREATE_UCASE_FMT_NAME_COL, };

                    for (String s : statements) {
                        try {
                            createColumn = conn.prepareStatement(s);
                            x = createColumn.executeUpdate();
                        } catch (SQLException ex) {
                            log.error(ex.getMessage());
                        } finally {
                            createColumn.close();
                        }
                    }
                    conn.commit();
                    break;
                case PRN_COL_COUNT_WITH_UCASE_COLS:
                    //Do nothing - the required columns already exist in the template
                    break;
                default:
                    throw new SQLException("Invalid number of columns in profile_resource_node table!");
            }
        } catch (SQLException e) {
            log.error(e);
        } finally {
            try {
                //Check for null references to avoid the exception overhead
                if (result != null) {
                    result.close();
                }
                if (result != null) {
                    loadNode.close();
                }
                if (conn != null) {
                    conn.rollback();
                    conn.close();
                }

            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private void setUpFormatsAndDatabaseWriter() {
        formats = loadAllFormats();
        for (final Format format : formats) {
            puidFormatMap.put(format.getPuid(), format);
        }
        nodeIds = new AtomicLong(getMaxNodeId() + 1);
        createAndRunDatabaseWriterThread();
    }

    //CHECKSTYLE:OFF        Nested tries and too many statements
    private void createSchemaOnFreshTemplate() {

        try {
            final Connection conn = datasource.getConnection();
            PreparedStatement createFormatTable = null;
            PreparedStatement createIdentificationTable = null;
            PreparedStatement createProfileResourceNodeTable = null;

            try {
                createFormatTable = conn.prepareStatement(CREATE_TABLE_FORMAT);
                createFormatTable.execute();

                createIdentificationTable = conn.prepareStatement(CREATE_TABLE_IDENTIFICATION);
                createIdentificationTable.execute();

                createProfileResourceNodeTable = conn.prepareStatement(CREATE_TABLE_PRN);
                createProfileResourceNodeTable.execute();

                List<String> createIndexesAndConstraints = new ArrayList<String>(19);
                createIndexesAndConstraints.add(CREATE_IDX_MIME_TYPE_ON_FORMAT);
                createIndexesAndConstraints.add(CREATE_IDX_FORMAT_NAME_ON_FORMAT);
                //createIndexesAndConstraints.add(CREATE_IDX_EXT_MISMATCH_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_ID_COUNT_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_EXT_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_ID_METHOD_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_LAST_MODIFIED_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_NAME_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_NODE_STATUS_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_ID_RESOURCE_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_FILE_SIZE_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PARENT_ID_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PREFIX_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PREFIX_PLUS_ONE_ON_PRN);
                //createIndexesAndConstraints.add(CREATE_IDX_TEXT_ENCODING_ON_PRN);
                //createIndexesAndConstraints.add(CREATE_IDX_URI_ON_PRN);
                createIndexesAndConstraints.add(IDENTIFICATION_CONSTRAINT_1);
                createIndexesAndConstraints.add(IDENTIFICATION_CONSTRAINT_2);

                for (String ddlSQL : createIndexesAndConstraints) {
                    final PreparedStatement ddlStatement = conn.prepareStatement(ddlSQL);
                    ddlStatement.execute();
                    ddlStatement.close();
                }

                conn.commit();

            } catch (SQLException e) {
                throw e;
            } finally {
                /*
                AutoCloseable[] autoclosables = new AutoCloseable[] {createFormatTable,
                        createIdentificationTable, createProfileResourceNodeTable, conn,};
                for(AutoCloseable a : autoclosables) {
                    if (a != null) {
                        try {
                            a.close();
                        //CHECKSTYLE:OFF    Have to catch generic exception from close()
                        } catch (Exception e) {
                            log.error(e);
                        }
                        //CHECKSTYLE:ON
                    }
                }
                */
                //AutoCloseable is new in java 7 - needs to compile for Java 6....
                //CHECKSTYLE:OFF    Have to catch generic exception from close()
                PreparedStatement[] statements = new PreparedStatement[] {createFormatTable,
                        createIdentificationTable, createProfileResourceNodeTable,};
                for (PreparedStatement a : statements) {
                    if (a != null) {
                        try {
                            a.close();

                        } catch (Exception e) {
                            log.error(e);
                        }
                    }
                }

                try {
                    conn.close();
                } catch (Exception e) {
                    log.error(e);
                }
                //CHECKSTYLE:ON
            }
        } catch (SQLException e) {
            log.error("A database exception occurred getting when creating the Derby database for a fresh install.", e);
        }
        //CHECKSTYLE:ON
    }

    @Override
    public void save(final ProfileResourceNode node, final ResourceId parentId) {

        final boolean insertNode = node.getId() == null;
        if (insertNode) {
            setNodeIds(node, parentId);
        }
        try {
            synchronized (nodeCache) { // different threads can add nodes.
                nodeCache.put(node.getId(), node);
            }
            blockingQueue.put(new NodeInfo(node, insertNode));
        } catch (InterruptedException e) {
            log.debug("Saving was interrupted while putting a new node into the queue.", e);
        }
    }

    @Override
    public void commit() {
        try {
            blockingQueue.put(COMMIT_SO_FAR);
        } catch (InterruptedException e) {
            log.debug("Interrupted while requesting a commit.", e);
        }
    }

    private void setNodeIds(ProfileResourceNode node, ResourceId parentId) {

        final Long nodeId = nodeIds.incrementAndGet();
        node.setId(nodeId);
        String parentsPrefixString = "";
        if (parentId != null) {
            parentsPrefixString = parentId.getPath();
            node.setParentId(parentId.getId());
        }

        int parentsPrefixStringLength = parentsPrefixString != null ? parentsPrefixString.length() : 0;
        final int buliderBaseSize = 5;
        final int nodeValueSize = 5;

        final StringBuilder builder = new StringBuilder(buliderBaseSize + parentsPrefixStringLength);
        builder.append(parentsPrefixString);
        final char[] nodeValue = new char[nodeValueSize];
        ResourceUtils.getBase128IntegerCharArray(nodeId, nodeValue);
        builder.append(nodeValue);
        node.setPrefix(builder.toString());
        //builder.setLength(parentsPrefixString.length());
        builder.setLength(parentsPrefixStringLength);
        ResourceUtils.getBase128IntegerCharArray(nodeId + 1, nodeValue);
        builder.append(nodeValue);
        node.setPrefixPlusOne(builder.toString());
    }


    @Override
    public Format loadFormat(final String puid) {
        //CHECKSTYLE:OFF   Nested tries
        Format format = null;
        try {
            final Connection conn = datasource.getConnection();
            try {
                final PreparedStatement loadFormat = conn.prepareStatement(SELECT_FORMAT);
                try {
                    loadFormat.setString(1, puid);
                    final ResultSet results = loadFormat.executeQuery();
                    try {
                        if (results.next()) {
                            format = SqlUtils.buildFormat(results);
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
            log.error("A database exception occurred loading a format with puid " + puid, e);
        }
        return format;
        //CHECKSTYLE:ON
    }

    @Override
    public List<Format> getAllFormats() {
        return formats;
    }

    @Override
    public Map<String, Format> getPUIDFormatMap() {
        return puidFormatMap;
    }

    @Override
    public ProfileResourceNode loadNode(Long nodeId) {
        //CHECKSTYLE:OFF   Nested tries and line length
        ProfileResourceNode node = null;
        synchronized (nodeCache) { // different threads can add nodes at any time.
            node = nodeCache.get(nodeId);
            if (node != null) {
                node = new ProfileResourceNode(node); // return a copy of the node so it can be safely modified by another thread.
            }
        }
        if (node == null) {
            try {
                final Connection conn = datasource.getConnection();
                try {
                    final PreparedStatement loadNode = conn.prepareStatement(SELECT_PROFILE_RESOURCE_NODE);
                    try {
                        loadNode.setLong(1, nodeId);
                        final ResultSet nodeResults = loadNode.executeQuery();
                        try {
                            if (nodeResults.next()) {
                                final PreparedStatement loadIdentifications =
                                        conn.prepareStatement(SELECT_IDENTIFICATIONS);
                                loadIdentifications.setLong(1, nodeId);
                                final ResultSet idResults = loadIdentifications.executeQuery();
                                node = SqlUtils.buildProfileResourceNode(nodeResults);
                                SqlUtils.addIdentifications(node, idResults, puidFormatMap);
                            }
                        } finally {
                            nodeResults.close();
                        }
                    } finally {
                        loadNode.close();
                    }
                } finally {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("A database exception occurred loading a node with id " + nodeId, e);
            }
            //CHECKSTYLE:ON
        }
        return node;
    }

    @Override
    public void deleteNode(Long nodeId) {
        //CHECKSTYLE:OFF     Nested tries.
        try {
            final Connection conn = datasource.getConnection();
            try {
                final PreparedStatement nodeStatement = conn.prepareStatement(DELETE_NODE);
                try {
                    nodeStatement.setLong(1, nodeId);
                    nodeStatement.execute();
                } finally {
                    nodeStatement.close();
                }
                final PreparedStatement idStatement = conn.prepareStatement(DELETE_IDENTIFICATIONS);
                try {
                    idStatement.setLong(1, nodeId);
                    idStatement.execute();
                } finally {
                    idStatement.close();
                }
                conn.commit();
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred deleting a node with id " + nodeId, e);
        }
        //CHECKSTYLE:ON
    }

    /**
     * To allow for the datasource to be referenced from JDBCSqlItemReader when called from ExportTask.
     * @return the datasource
     */
    public DataSource getDatasource() {
        return this.datasource;
    }

    /**
     * Sets the datasource. - to allow it to be referenced from JDBCSqlItemReader when called from ExportTask.
     * @param datasource datasource to set
     */
    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    private long getMaxNodeId() {
        long maxId = 0;
        //CHECKSTYLE:OFF - Nested tries
        try {
            final Connection conn = datasource.getConnection();
            try {
                final PreparedStatement maxNodes = conn.prepareStatement(MAX_NODE_ID_QUERY);
                try {
                    final ResultSet results = maxNodes.executeQuery();
                    try {
                        if (results.next()) {
                            maxId = results.getLong(1);
                        }
                    } finally {
                        results.close();
                    }
                } finally {
                    maxNodes.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred finding the maximum id in the database", e);
        }
        return maxId;
        //CHECKSTYLE:ON
    }

    private List<Format> loadAllFormats() {
        //CHECKSTYLE:OFF   Nested try depth..
        List<Format> formatsList = null;
        try {
            final Connection conn = datasource.getConnection();
            try {
                //BNO - Get the actual number of formats so we can initialise the list based on the current count.
                final PreparedStatement getFormatCount = conn.prepareStatement(SELECT_FORMAT_COUNT);
                final ResultSet rsFormatCount = getFormatCount.executeQuery();
                rsFormatCount.next();
                final int formatCount = rsFormatCount.getInt("total");
                formatsList = new ArrayList<Format>(formatCount);

                final PreparedStatement loadFormat = conn.prepareStatement(SELECT_FORMATS);
                try {
                    final ResultSet results = loadFormat.executeQuery();
                    try {
                        while (results.next()) {
                            formatsList.add(SqlUtils.buildFormat(results));
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
        return formatsList;
        //CHECKSTYLE:ON
    }

    private void createAndRunDatabaseWriterThread() {
        writer = new DatabaseWriter(blockingQueue, datasource, BATCH_LIMIT);
        try {
            writer.init();
        } catch (SQLException e) {
            //TODO: not a runtime exception - what to use here?
            throw new RuntimeException("Could not initialise the database writer - fatal error.", e);
        }
        databaseWriterThread = new Thread(writer);

        try {
            databaseWriterThread.join();
            databaseWriterThread.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleans up resources after profile processing completed.
     */
    public void cleanup() {
        //System.out.println("In cleanUp() not doing anything...");
        //System.out.println("Cleaning up JDBCBatchResultHandlerDao, calling closeResources()");
        this.writer.closeResources();
    }

    /**
     * Used to indicate whether DROID is running in "fresh temple" mode, e.g. after a new install.
     * @param isFreshTemplate Whether or not this instance is instantiated by DROID running with a fresh template.
     */
    public static synchronized void setIsFreshTemplate(boolean isFreshTemplate) {
        freshTemplate = isFreshTemplate;
    }

    /**
     * Used to indicate whether DROID is running in "fresh temple" mode, e.g. after a new install.
     * @return Whether or not this instance is instantiated by DROID running with a fresh template.
     */
    public static boolean getIsFreshTemplate() {
        return freshTemplate;
    }

    private static class NodeInfo {

        private ProfileResourceNode node;
        private boolean insertNode;

        public NodeInfo(ProfileResourceNode node, boolean insertNode) {
            this.node = node;
            this.insertNode = insertNode;
        }

        public ProfileResourceNode getNode() {
            return node;
        }

        public boolean isInsertNode() {
            return insertNode;
        }
    }

    /**
     * A simple Most Recently Used cache of ProfileResourceNodes, mapped against their node id.
     * <p>
     * This is to support loading a node (e.g. when handling an error), when the node itself may
     * not yet be committed to the database (due to batched commits).
     * <p>
     * Keeping a most recently added cache of nodes allows us to return a recent node before it
     * has actually been committed to the database.
     * <p>
     * <b>Thread Safety</b>  This is not thread-safe, and nodes may be added by different threads.
     *                       We will use synchronization in the calling class to provide thread-safety,
     *                       since there is no ConcurrentLinkedHashMap in the JDK by default.
     */
    private final class MostRecentlyAddedNodeCache extends LinkedHashMap<Long, ProfileResourceNode> {

        private static final float LOAD_FACTOR = 1.1f;
        private final int capacity;

        private MostRecentlyAddedNodeCache(int capacity) {
            super(capacity + 1, LOAD_FACTOR, false);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<Long, ProfileResourceNode> eldest) {
            return size() > capacity;
        }
    }

    /**
     * Class to run in a thread which takes from the blocking queue and batch commits
     * to the database.
     */
    private static class DatabaseWriter implements Runnable {

        private static final int INSERT_NODE_URI_INDEX = 16;
        private static final int INSERT_NODE_PREFIX_PLUS_ONE_INDEX = 15;
        private static final int INSERT_NODE_PREFIX_INDEX = 14;
        private static final int INSERT_NODE_PARENT_ID_INDEX = 13;
        private static final int INSERT_NODE_SIZE_INDEX = 12;
        private static final int INSERT_NODE_RESOURCE_TYPE_INDEX = 11;
        private static final int INSERT_NODE_STATUS_INDEX = 10;
        private static final int INSERT_NODE_NAME_INDEX = 9;
        private static final int INSERT_NODE_MOD_DATE_INDEX = 8;
        private static final int INSERT_NODE_METHOD_INDEX = 7;
        private static final int INSERT_NODE_HASH_INDEX = 6;
        private static final int INSERT_NODE_EXT_INDEX = 5;
        private static final int INSERT_NODE_NUM_IDENTS_INDEX = 4;
        private static final int INSERT_NODE_FINISHED_INDEX = 3;
        private static final int INSERT_NODE_MISMATCH_INDEX = 2;
        private static final int INSERT_NODE_ID_INDEX = 1;

        private final Log log = LogFactory.getLog(getClass());
        private BlockingQueue<NodeInfo> blockingQueue;
        private DataSource datasource;
        private Connection connection;
        private PreparedStatement insertNodeStatement;
        private PreparedStatement updateNodeStatement;
        private Map<Integer, PreparedStatement> insertIdentifications;
        private volatile int batchCount;
        private final int batchLimit;

        DatabaseWriter(final BlockingQueue<NodeInfo> blockingQueue,
                       final DataSource datasource,
                       final int batchLimit) {
            this.blockingQueue = blockingQueue;
            this.datasource    = datasource;
            this.batchLimit    = batchLimit;
        }

        /**
         * Initialise database prepared statements for the writer.
         * <p>
         * This must be called before running the writer.
         *
         * @throws SQLException
         */
        public void init() throws SQLException {
            connection = datasource.getConnection();
            insertNodeStatement = connection.prepareStatement(INSERT_PROFILE_RESOURCE_NODE);
            updateNodeStatement = connection.prepareStatement(UPDATE_NODE_STATUS);
            final int maxStatements = 64;

            insertIdentifications = new HashMap<Integer, PreparedStatement>(maxStatements);
            for (int i = 0; i < INSERT_IDENTIFICATION.length; i++) {
                final PreparedStatement insertStatement = connection.prepareStatement(INSERT_IDENTIFICATION[i]);
                insertIdentifications.put(i, insertStatement);
            }
        }

        @Override
        public void run() {
            try {
                // Loop until we're interrupted.
                while (true) {
                    final NodeInfo info = blockingQueue.take(); // this will block if there's nothing in the queue.
                    if (info == COMMIT_SO_FAR) {
                        commit();
                    } else {
                        try {
                            if (info.insertNode) { // are we inserting a node, or updating one already saved?
                                batchInsertNode(info.getNode());
                            } else {
                                updateNodeStatus(info.getNode());
                            }
                        } catch (SQLException e) {
                            log.error("A database problem occurred inserting a node: " + info.getNode(), e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.debug("The database writer thread was interrupted.", e);
            }
            //TODO: unless the thread is interrupted, how does it clean up resources?
            System.out.println("Calling closeResources() from  the run method");
            closeResources();
        }



        private void closeResources() {
            //System.out.println("In JDBCBatchResultHandlerDao - closeResources()");
            for (final PreparedStatement statement : insertIdentifications.values()) {
                try {
                    statement.close();
                } catch (SQLException s) {
                    log.error("A problem occurred closing an identification prepared statement.", s);
                }
            }
            try {
                insertNodeStatement.close();
            } catch (SQLException s) {
                log.error("A problem occurred closing a node insert prepared statement.", s);
            }
            try {
                connection.close();
            } catch (SQLException s) {
                log.error("A problem occurred closing a database connection", s);
            }
        }

        //CHECKSTYLE:OFF  Too many statements..
        private void batchInsertNode(final ProfileResourceNode node) throws SQLException {
            // insert main node:
            final long nodeId = node.getId();
            final NodeMetaData metadata = node.getMetaData();
            final String uri = node.getUri().toString();
            final java.sql.Date finished = new java.sql.Date(new java.util.Date().getTime());
            final boolean mismatch = node.getExtensionMismatch();
            final String name = metadata.getName();
            final String hash = metadata.getHash(); // nullable
            final Long size = metadata.getSize(); // nullable.
            final NodeStatus nodeStatus = metadata.getNodeStatus();
            final ResourceType resourceType = metadata.getResourceType();
            final String extension = metadata.getExtension();
            final Integer numIdentifications = node.getIdentificationCount();
            final Date modDate = metadata.getLastModifiedDate();
            final IdentificationMethod method = metadata.getIdentificationMethod();
            final Long nodeParentId = node.getParentId();
            final String nodePrefix = node.getPrefix();
            final String nodePrefixPlusOne = node.getPrefixPlusOne();
            final PreparedStatement insertNode = insertNodeStatement;
            insertNode.setLong(INSERT_NODE_ID_INDEX, nodeId);
            insertNode.setBoolean(INSERT_NODE_MISMATCH_INDEX, mismatch);
            SqlUtils.setNullableTimestamp(INSERT_NODE_FINISHED_INDEX, finished, insertNode);
            SqlUtils.setNullableInteger(INSERT_NODE_NUM_IDENTS_INDEX,  numIdentifications, insertNode);
            SqlUtils.setNullableString(INSERT_NODE_EXT_INDEX,  extension, insertNode);
            SqlUtils.setNullableString(INSERT_NODE_HASH_INDEX,  hash, insertNode);
            SqlUtils.setNullableEnumAsInt(INSERT_NODE_METHOD_INDEX, method, insertNode);
            SqlUtils.setNullableTimestamp(INSERT_NODE_MOD_DATE_INDEX, modDate, insertNode);
            insertNode.setString(INSERT_NODE_NAME_INDEX,  name);
            SqlUtils.setNullableEnumAsInt(INSERT_NODE_STATUS_INDEX, nodeStatus, insertNode);
            SqlUtils.setNullableEnumAsInt(INSERT_NODE_RESOURCE_TYPE_INDEX, resourceType, insertNode);
            SqlUtils.setNullableLong(INSERT_NODE_SIZE_INDEX, size, insertNode);
            SqlUtils.setNullableLong(INSERT_NODE_PARENT_ID_INDEX, nodeParentId, insertNode);
            SqlUtils.setNullableString(INSERT_NODE_PREFIX_INDEX, nodePrefix, insertNode);
            SqlUtils.setNullableString(INSERT_NODE_PREFIX_PLUS_ONE_INDEX, nodePrefixPlusOne, insertNode);
            insertNode.setString(INSERT_NODE_URI_INDEX, uri);
            insertNode.addBatch();

            // insert its identifications:
            //TODO: check for NULL format weirdness...

            final int identifications = numIdentifications == null ? 0 : numIdentifications;
            final PreparedStatement statement = getIdentificationStatement(identifications);
            if (identifications == 0) {
                statement.setLong(1, nodeId);
            } else {
                int parameterCount = 1;
                for (final Format format : node.getFormatIdentifications()) {
                    statement.setLong(parameterCount++, nodeId);
                    String p = format.getPuid();
                    statement.setString(parameterCount++, p == null ? "" : p);
                }
            }
            statement.addBatch();

            commitBatchIfLargeEnough();
        }
        //CHECKSTYLE:ON

        private void updateNodeStatus(final ProfileResourceNode node) throws SQLException {
            final Long nodeId = node.getId();
            if (nodeId != null) {
                NodeMetaData nm = node.getMetaData();
                if (nm != null) {
                    SqlUtils.setNullableEnumAsInt(1, nm.getNodeStatus(), updateNodeStatement);
                    updateNodeStatement.setLong(2, nodeId);
                    updateNodeStatement.addBatch();
                    commitBatchIfLargeEnough();
                } else {
                    log.error("A node was flagged for status update, but had no status metadata. Node id was: "
                            + nodeId);
                }
            } else {
                log.error("A node was flagged for status update, but it did not have an id already.  Parent id was: "
                        + node.getParentId());
            }
        }

        private void commitBatchIfLargeEnough() {
            // Commit if exceeded batch limit:
            if (batchCount++ >= batchLimit) {
                batchCount = 0;
                try {
                    // Insert new nodes:
                    insertNodeStatement.executeBatch();

                    // Update node status:
                    updateNodeStatement.executeBatch();

                    // Insert identifications of new nodes:
                    for (final PreparedStatement identifications : insertIdentifications.values()) {
                        identifications.executeBatch();
                        //TODO: optimise? what about identification statements not used in this run?
                    }
                    connection.commit();
                } catch (SQLException e) {
                    log.error("A problem occurred attempting to batch commit nodes into the database. ", e);
                }
            }
        }

        /**
         * Commits everything batched so far.
         */
        public void commit() {
            batchCount = BATCH_LIMIT;
            commitBatchIfLargeEnough();
        }

        private PreparedStatement getIdentificationStatement(final int numIdentifications) throws SQLException {
            PreparedStatement statement = insertIdentifications.get(numIdentifications);
            if (statement == null) {
                final String newIdentificationSQL = buildInsertIdentificationString(numIdentifications);
                statement = connection.prepareStatement(newIdentificationSQL);
                insertIdentifications.put(numIdentifications, statement);
            }
            return statement;
        }

        private String buildInsertIdentificationString(final int numIdentifications) {

            final int baseInsertStatementSize = 60;
            final int sizeForEachIdentification = 6;

            final StringBuilder builder =
                    new StringBuilder(baseInsertStatementSize + numIdentifications * sizeForEachIdentification);
            builder.append(INSERT_IDENTIFICATIONS);
            for (int i = 0; i < numIdentifications - 1; i++) {
                builder.append("(?,?),");
            }
            builder.append("(?,?)");
            return builder.toString();
        }
    }
}
