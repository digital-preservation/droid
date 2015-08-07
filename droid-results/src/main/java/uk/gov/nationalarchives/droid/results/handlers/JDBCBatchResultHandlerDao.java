/**
 * Copyright (c) 2015, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;


/**
 * An implementation of the ResultHandlerDao interface, using JDBC to access the profile database directly.
 *
 * @author Matt Palmer
 */
public class JDBCBatchResultHandlerDao implements ResultHandlerDao {

    // How many results in the batch before committing.
    public static final int BATCH_LIMIT = 100;

    // A "poison-pill" node info to signal to the writing thread that
    // it should terminate and commit any results so far.
    private static NodeInfo COMMIT_SO_FAR = new NodeInfo(null, false);

    private static String INSERT_PROFILE_RESOURCE_NODE =
            "INSERT INTO PROFILE_RESOURCE_NODE " +
                    "(NODE_ID,EXTENSION_MISMATCH,FINISHED_TIMESTAMP,IDENTIFICATION_COUNT," +
                    " EXTENSION,HASH,IDENTIFICATION_METHOD,LAST_MODIFIED_DATE,NAME,NODE_STATUS," +
                    " RESOURCE_TYPE,FILE_SIZE,PARENT_ID,PREFIX,PREFIX_PLUS_ONE,URI) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static String INSERT_IDENTIFICATIONS       = "INSERT INTO IDENTIFICATION (NODE_ID,PUID) VALUES ";
    private static String INSERT_ZERO_IDENTIFICATIONS  = INSERT_IDENTIFICATIONS + "(?,'')";
    private static String INSERT_ONE_IDENTIFICATION    = INSERT_IDENTIFICATIONS + "(?,?)";
    private static String INSERT_TWO_IDENTIFICATIONS   = INSERT_IDENTIFICATIONS + "(?,?),(?,?)";
    private static String INSERT_THREE_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?)";
    private static String INSERT_FOUR_IDENTIFICATIONS  = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_FIVE_IDENTIFICATIONS  = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_SIX_IDENTIFICATIONS   = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_SEVEN_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_EIGHT_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_NINE_IDENTIFICATIONS  = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_TEN_IDENTIFICATIONS   = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";

    private static String[] INSERT_IDENTIFICATION      = {
            INSERT_ZERO_IDENTIFICATIONS, INSERT_ONE_IDENTIFICATION,
            INSERT_TWO_IDENTIFICATIONS, INSERT_THREE_IDENTIFICATIONS, INSERT_FOUR_IDENTIFICATIONS,
            INSERT_FIVE_IDENTIFICATIONS, INSERT_SIX_IDENTIFICATIONS, INSERT_SEVEN_IDENTIFICATIONS,
            INSERT_EIGHT_IDENTIFICATIONS, INSERT_NINE_IDENTIFICATIONS, INSERT_TEN_IDENTIFICATIONS};

    private static String UPDATE_NODE_STATUS           = "UPDATE PROFILE_RESOURCE_NODE SET NODE_STATUS = ? WHERE NODE_ID = ?";
    private static String DELETE_NODE                  = "DELETE FROM PROFILE_RESOURCE_NODE WHERE NODE_ID = ?";
    private static String SELECT_FORMAT                = "SELECT * FROM FORMAT WHERE PUID = ?";
    //BNO
    private static String SELECT_FORMAT_COUNT          = "SELECT COUNT('x') AS total FROM FORMAT";
    private static String SELECT_FORMATS               = "SELECT * FROM FORMAT";
    private static String SELECT_PROFILE_RESOURCE_NODE = "SELECT * FROM PROFILE_RESOURCE_NODE WHERE NODE_ID = ?";
    private static String SELECT_IDENTIFICATIONS       = "SELECT * FROM IDENTIFICATION WHERE NODE_ID = ?";
    private static String DELETE_IDENTIFICATIONS       = "DELETE FROM IDENTIFICATION WHERE NODE_ID = ?";
    private static String MAX_NODE_ID_QUERY            = "SELECT MAX(NODE_ID) FROM PROFILE_RESOURCE_NODE";

    //DDL statements
    private static String CREATE_TABLE_FORMAT = "create table format (puid varchar(255) not null, mime_type varchar(255), name varchar(255), version varchar(255), primary key (puid))";
    private static String CREATE_TABLE_IDENTIFICATION = "create table identification (node_id bigint not null, puid varchar(255) not null)";
    private static String CREATE_TABLE_PRN = "create table profile_resource_node (node_id bigint not null, extension_mismatch boolean not null, finished_timestamp timestamp, identification_count integer, extension varchar(255), hash varchar(64), identification_method integer, last_modified_date timestamp, name varchar(255) not null, node_status integer, resource_type integer not null, file_size bigint, parent_id bigint, prefix varchar(255), prefix_plus_one varchar(255), text_encoding integer, uri varchar(4000) not null, primary key (node_id))";
    private static String CREATE_IDX_MIME_TYPE_ON_FORMAT = "create index idx_mime_type on format (mime_type)";
    private static String CREATE_IDX_FORMAT_NAME_ON_FORMAT = "create index idx_format_name on format (name)";
    private static String CREATE_IDX_EXT_MISMATCH_ON_PRN = "create index idx_extension_mismatch on profile_resource_node (extension_mismatch)";
    private static String CREATE_IDX_ID_COUNT_ON_PRN = "create index idx_id_count on profile_resource_node (identification_count)";
    private static String CREATE_IDX_PRN_EXT_ON_PRN = "create index idx_prn_extension on profile_resource_node (extension)";
    private static String CREATE_IDX_PRN_HASH_TYPE_ON_PRN = "create index idx_prn_hash on profile_resource_node (hash)";
    private static String CREATE_IDX_PRN_ID_METHOD_ON_PRN = "create index idx_prn_id_method on profile_resource_node (identification_method)";
    private static String CREATE_IDX_PRN_LAST_MODIFIED_ON_PRN = "create index idx_prn_last_modified on profile_resource_node (last_modified_date)";
    private static String CREATE_IDX_PRN_NAME_ON_PRN = "create index idx_prn_name on profile_resource_node (name)";
    private static String CREATE_IDX_PRN_NODE_STATUS_ON_PRN = "create index idx_prn_node_status on profile_resource_node (node_status)";
    private static String CREATE_IDX_ID_RESOURCE_ON_PRN = "create index idx_prn_id_resourceType on profile_resource_node (resource_type)";
    private static String CREATE_IDX_PRN_FILE_SIZE_ON_PRN = "create index idx_prn_file_size on profile_resource_node (file_size)";
    private static String CREATE_IDX_PARENT_ID_ON_PRN = "create index idx_parent_id on profile_resource_node (parent_id)";
    private static String CREATE_IDX_PREFIX_ON_PRN = "create index idx_prefix on profile_resource_node (prefix)";
    private static String CREATE_IDX_PREFIX_PLUS_ONE_ON_PRN = "create index idx_prefix_plus_one on profile_resource_node (prefix_plus_one)";
    private static String CREATE_IDX_TEXT_ENCODING_ON_PRN = "create index idx_text_encoding on profile_resource_node (text_encoding)";
    private static String CREATE_IDX_URI_ON_PRN = "create index idx_uri on profile_resource_node (uri)";
    private static String IDENTIFICATION_CONSTRAINT_1 = "alter table identification add constraint FK_fh484ccwwl4e5w9quqke4n6ri foreign key (puid) references format";
    private static String IDENTIFICATION_CONSTRAINT_2 = "alter table identification add constraint FK_tpxmo6ppuxeckdreln5pt5e39 foreign key (node_id) references profile_resource_node";

    //TODO: May not need these mnow we're not using Hibernate?
    private static String CREATE_UNIQUE_KEY = "create table hibernate_unique_key ( next_hi integer )";
    private static String INSERT_UNIQUE_KEY = "insert into hibernate_unique_key values ( 0 )";

    private static boolean freshTemplate;
    private static Object locker = new Object();

    private final Log log = LogFactory.getLog(getClass());

    private DataSource datasource;
    private AtomicLong nodeIds;

    private List<Format> formats;
    private Map<String, Format> puidFormatMap = new HashMap<String,Format>(2500);

    private BlockingQueue<NodeInfo> blockingQueue = new ArrayBlockingQueue<NodeInfo>(128);
    private MostRecentlyAddedNodeCache nodeCache  = new MostRecentlyAddedNodeCache(256);

    private Thread databaseWriterThread;
    private DatabaseWriter writer;

    @Override
    public void init() {

        //TODO: If we're running DROID for the first time, the database will exist but won't have the DROID_USER
        // BNO: Need to allow for the possibility that we're dealing with a fresh DROID install (or first run
        // after a new signature?). In which case, the call to setUpFormatsAndDatabaseWriter() will fail since
        // the database schema objects (previously created by hibedrnate) will not exist.  It gets worse.  We can't just
        // create the schema then call setUpFormatsAndDatabaseWriter() beacuse the format data won't have been popiulated.
        // This is handled by ProfileContextLocator-generateNewDatabaseAndTemplates after this class has been instantiated
        // byy Spring...
        synchronized (locker) {
            if(!getIsFreshTemplate()) {
                setUpFormatsAndDatabaseWriter();
            } else {
                createSchemaOnFreshTemplate();
            }
        }
    }

    public void setUpFormatsAndDatabaseWriterForNewTemplate () {
        synchronized (locker) {
            setUpFormatsAndDatabaseWriter();
        }
    }

    private void setUpFormatsAndDatabaseWriter () {
        formats = loadAllFormats();
        for (final Format format : formats) {
            puidFormatMap.put(format.getPuid(), format);
        }
        nodeIds = new AtomicLong(getMaxNodeId() + 1);
        createAndRunDatabaseWriterThread();

    }


    private void createSchemaOnFreshTemplate() {

        try {
            final Connection conn = datasource.getConnection();

            try {
                final PreparedStatement createFormatTable = conn.prepareStatement(CREATE_TABLE_FORMAT);
                createFormatTable.execute();

                final PreparedStatement createIdentificationTable = conn.prepareStatement(CREATE_TABLE_IDENTIFICATION);
                createIdentificationTable.execute();

                final PreparedStatement createProfileResourceNodeTable = conn.prepareStatement(CREATE_TABLE_PRN);
                createProfileResourceNodeTable.execute();

                List<String> createIndexesAndConstraints = new ArrayList<String>(19);
                createIndexesAndConstraints.add(CREATE_IDX_MIME_TYPE_ON_FORMAT);
                createIndexesAndConstraints.add(CREATE_IDX_FORMAT_NAME_ON_FORMAT);
                createIndexesAndConstraints.add(CREATE_IDX_EXT_MISMATCH_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_ID_COUNT_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_EXT_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_HASH_TYPE_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_ID_METHOD_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_LAST_MODIFIED_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_NAME_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_NODE_STATUS_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_ID_RESOURCE_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PRN_FILE_SIZE_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PARENT_ID_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PREFIX_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_PREFIX_PLUS_ONE_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_TEXT_ENCODING_ON_PRN);
                createIndexesAndConstraints.add(CREATE_IDX_URI_ON_PRN);
                createIndexesAndConstraints.add(IDENTIFICATION_CONSTRAINT_1);
                createIndexesAndConstraints.add(IDENTIFICATION_CONSTRAINT_2);

                //BNO - possibly don't need these nw we're not using Hibernate?
                //createIndexesAndConstraints.add(CREATE_UNIQUE_KEY);
                //createIndexesAndConstraints.add(INSERT_UNIQUE_KEY);

                for (String ddlSQL : createIndexesAndConstraints) {
                    final PreparedStatement ddlStatement = conn.prepareStatement(ddlSQL);
                    ddlStatement.execute();
                }

                conn.commit();

            } catch (SQLException e) {
                throw e;
            } finally {
                conn.close();
                //Does the table exist?
                //loadAllFormats();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred getting when creating the Derby database for a fresh install.", e);
        }
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
        final String nodePrefix = parentsPrefixString + ResourceUtils.getBase128Integer(nodeId);
        final String nodePrefixPlusOne =  parentsPrefixString + ResourceUtils.getBase128Integer(nodeId + 1);
        node.setPrefix(nodePrefix);
        node.setPrefixPlusOne(nodePrefixPlusOne);
    }


    @Override
    public Format loadFormat(final String puid) {
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
            } finally{
                conn.close();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred loading a format with puid " + puid, e);
        }
        return format;
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
                                final PreparedStatement loadIdentifications = conn.prepareStatement(SELECT_IDENTIFICATIONS);
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
        }
        return node;
    }

    @Override
    public void deleteNode(Long nodeId) {
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
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    private long getMaxNodeId() {
        long maxId = 0;
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
            } finally{
                conn.close();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred finding the maximum id in the database", e);
        }
        return maxId;
    }

    private List<Format> loadAllFormats() {
        //final List<Format> formats = new ArrayList<Format>(2000); // about 1500 formats as of 2015.

        List<Format> formats = null;
        try {
            final Connection conn = datasource.getConnection();
            log.error("Connection: " + conn.toString());
            try {
                //BNO - Get the actual number of formats so we can initialise the list based on the current count.
                final PreparedStatement getFormatCount = conn.prepareStatement(SELECT_FORMAT_COUNT);
                final ResultSet rsFormatCount = getFormatCount.executeQuery();
                rsFormatCount.next();
                final int formatCount = rsFormatCount.getInt("total");
                formats = new ArrayList<Format>(formatCount);

                final PreparedStatement loadFormat = conn.prepareStatement(SELECT_FORMATS);
                try {
                    final ResultSet results = loadFormat.executeQuery();
                    try {
                        while (results.next()) {
                            formats.add(SqlUtils.buildFormat(results));
                        }
                    } finally {
                        results.close();
                    }
                } finally {
                    loadFormat.close();
                }
            } finally{
                conn.close();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred getting all formats.", e);
        }
        return formats;
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
        databaseWriterThread.start();
    }


    public synchronized static void setIsFreshTemplate(boolean isFreshTemplate) {
        freshTemplate = isFreshTemplate;
    }

    public static boolean getIsFreshTemplate() {
        return freshTemplate;
    }

    private static class NodeInfo {
        public ProfileResourceNode node;
        public boolean             insertNode;

        public NodeInfo(ProfileResourceNode node, boolean insertNode) {
            this.node = node;
            this.insertNode = insertNode;
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
    private class MostRecentlyAddedNodeCache extends LinkedHashMap<Long, ProfileResourceNode> {

        private final int capacity;

        private MostRecentlyAddedNodeCache(int capacity) {
            super(capacity + 1, 1.1f, false);
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
            insertIdentifications = new HashMap<Integer, PreparedStatement>(64);
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
                                batchInsertNode(info.node);
                            } else {
                                updateNodeStatus(info.node);
                            }
                        } catch (SQLException e) {
                            log.error("A database problem occurred inserting a node: " + info.node, e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.debug("The database writer thread was interrupted.", e);
            }
            //TODO: unless the thread is interrupted, how does it clean up resources?
            closeResources();
        }



        private void closeResources() {
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
            insertNode.setLong(            1,  nodeId);
            insertNode.setBoolean(         2,  mismatch);
            insertNode.setDate(            3,  finished);
            SqlUtils.setNullableInteger(   4,  numIdentifications, insertNode);
            SqlUtils.setNullableString(    5,  extension, insertNode);
            SqlUtils.setNullableString(    6,  hash, insertNode);
            SqlUtils.setNullableEnumAsInt( 7, method, insertNode);
            SqlUtils.setNullableTimestamp( 8, modDate, insertNode);
            insertNode.setString(          9,  name);
            SqlUtils.setNullableEnumAsInt( 10, nodeStatus, insertNode);
            SqlUtils.setNullableEnumAsInt( 11, resourceType, insertNode);
            SqlUtils.setNullableLong(      12, size, insertNode);
            SqlUtils.setNullableLong(      13, nodeParentId, insertNode);
            SqlUtils.setNullableString(    14, nodePrefix, insertNode);
            SqlUtils.setNullableString(    15, nodePrefixPlusOne, insertNode);
            insertNode.setString(          16, uri);
            insertNode.addBatch();

            // insert its identifications:
            //TODO: check for NULL format weirdness...
            final int identifications = numIdentifications == null? 0 : numIdentifications;
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

        private void updateNodeStatus(final ProfileResourceNode node) throws SQLException {
            final Long nodeId = node.getId();
            if (nodeId != null) {
                NodeMetaData nm = node.getMetaData();
                if (nm != null) {
                    SqlUtils.setNullableEnumAsInt(1, nm.getNodeStatus(), updateNodeStatement);
                    updateNodeStatement.setLong(  2, nodeId);
                    updateNodeStatement.addBatch();
                    commitBatchIfLargeEnough();
                } else {
                    log.error("A node was flagged for status update, but had no status metadata. Node id was: " + nodeId);
                }
            } else {
                log.error("A node was flagged for status update, but it did not have an id already.  Parent id was: " + node.getParentId());
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
                        identifications.executeBatch(); //TODO: optimise? what about identification statements not used in this run?
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
            final StringBuilder builder = new StringBuilder(60 + numIdentifications * 6);
            builder.append(INSERT_IDENTIFICATIONS);
            for (int i = 0; i < numIdentifications - 1; i++) {
                builder.append("(?,?),");
            }
            builder.append("(?,?)");
            return builder.toString();
        }

    }

}
