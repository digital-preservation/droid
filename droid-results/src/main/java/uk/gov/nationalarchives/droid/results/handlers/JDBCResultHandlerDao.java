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
//CHECKSTYLE:OFF  Class Not currently used.
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of the ResultHandlerDao interface, using JDBC to access the profile database directly.
 *
 * @author Matt Palmer
 */

public class JDBCResultHandlerDao implements ResultHandlerDao {

    private static String INSERT_PROFILE_RESOURCE_NODE =
            "INSERT INTO PROFILE_RESOURCE_NODE " +
                    "(NODE_ID,EXTENSION_MISMATCH,FINISHED_TIMESTAMP,IDENTIFICATION_COUNT," +
                    " EXTENSION,HASH,IDENTIFICATION_METHOD,LAST_MODIFIED_DATE,NAME,NODE_STATUS," +
                    " RESOURCE_TYPE,FILE_SIZE,PARENT_ID,PREFIX,PREFIX_PLUS_ONE,URI) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static String INSERT_IDENTIFICATIONS = "INSERT INTO IDENTIFICATION (NODE_ID,PUID) VALUES ";
    private static String INSERT_ZERO_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,'')";
    private static String INSERT_ONE_IDENTIFICATION = INSERT_IDENTIFICATIONS + "(?,?)";
    private static String INSERT_TWO_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?)";
    private static String INSERT_THREE_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?)";
    private static String INSERT_FOUR_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_FIVE_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_SIX_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_SEVEN_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_EIGHT_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_NINE_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String INSERT_TEN_IDENTIFICATIONS = INSERT_IDENTIFICATIONS + "(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?),(?,?)";
    private static String[] INSERT_IDENTIFICATION = {INSERT_ZERO_IDENTIFICATIONS, INSERT_ONE_IDENTIFICATION,
            INSERT_TWO_IDENTIFICATIONS, INSERT_THREE_IDENTIFICATIONS, INSERT_FOUR_IDENTIFICATIONS,
            INSERT_FIVE_IDENTIFICATIONS, INSERT_SIX_IDENTIFICATIONS, INSERT_SEVEN_IDENTIFICATIONS,
            INSERT_EIGHT_IDENTIFICATIONS, INSERT_NINE_IDENTIFICATIONS, INSERT_TEN_IDENTIFICATIONS};

    private static String UPDATE_NODE_STATUS           = "UPDATE PROFILE_RESOURCE_NODE SET NODE_STATUS = ? WHERE NODE_ID = ?";
    private static String DELETE_NODE                  = "DELETE FROM PROFILE_RESOURCE_NODE WHERE NODE_ID = ?";
    private static String SELECT_FORMAT                = "SELECT * FROM FORMAT WHERE PUID = ?";
    private static String SELECT_FORMATS               = "SELECT * FROM FORMAT";
    private static String SELECT_PROFILE_RESOURCE_NODE = "SELECT * FROM PROFILE_RESOURCE_NODE WHERE NODE_ID = ?";
    private static String SELECT_IDENTIFICATIONS       = "SELECT * FROM IDENTIFICATION WHERE NODE_ID = ?";
    private static String DELETE_IDENTIFICATIONS       = "DELETE FROM IDENTIFICATION WHERE NODE_ID = ?";
    private static String MAX_NODE_ID_QUERY            = "SELECT MAX(NODE_ID) FROM PROFILE_RESOURCE_NODE";

    private final Log log = LogFactory.getLog(getClass());

    private DataSource datasource;
    private AtomicLong nodeIds;

    private List<Format> formats;
    private Map<String, Format> puidFormatMap = new HashMap<String,Format>(2500);

    @Override
    public void init() {
        formats = loadAllFormats();
        for (final Format format : formats) {
            puidFormatMap.put(format.getPuid(), format);
        }
        nodeIds = new AtomicLong(getMaxNodeId() + 1);
    }

    @Override
    public void save(final ProfileResourceNode node, final ResourceId parentId) {
        try {
            final Connection conn = datasource.getConnection();
            try {
                Long nodeId = node.getId();
                if (nodeId != null) {
                    final PreparedStatement updateNode = getUpdateNodeStatus(conn, node.getId(), node.getMetaData().getNodeStatus());
                    try {
                        updateNode.execute();
                        conn.commit();
                    } finally {
                        updateNode.close();
                    }
                } else {
                    nodeId = nodeIds.incrementAndGet();
                    node.setId(nodeId);
                    final PreparedStatement insertNode = getNodeInsertStatement(conn, node, parentId);
                    try {
                        insertNode.execute();
                        final PreparedStatement insertIdentifications = getIdentificationStatement(conn, nodeId, node.getFormatIdentifications());
                        insertIdentifications.execute();
                        conn.commit();
                    } finally {
                        insertNode.close();
                    }
                }
            } finally{
                conn.close();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred inserting a node " + node +
                      " with parent id " + parentId, e);
        }
    }

    @Override
    public void commit() {
        // nothing to do - results are committed on each save.
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
            } finally{
                conn.close();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred loading a node with id " + nodeId, e);
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

    @Override
    public void initialiseForNewTemplate() {
        //No need to do anything - all required initalisation will have been completed by the time we reach here.
        //For a new template, the database schema will have ben created in Hibernate.
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

    private PreparedStatement getUpdateNodeStatus(final Connection conn,
                                                  final long nodeId,
                                                  final NodeStatus nodeStatus) throws SQLException {
        final PreparedStatement updateNodeStatus = conn.prepareStatement(UPDATE_NODE_STATUS);
        SqlUtils.setNullableEnumAsInt(1, nodeStatus, updateNodeStatus);
        updateNodeStatus.setLong(     2, nodeId);
        return updateNodeStatus;
    }

    private PreparedStatement getNodeInsertStatement(final Connection conn,
                                                     final ProfileResourceNode node,
                                                     final ResourceId parentId) throws SQLException {
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
        final Long nodeParentId = parentId == null? null : parentId.getId();
        String parentsPrefixString = "";
        if (parentId != null) {
            parentsPrefixString = parentId.getPath();
            node.setParentId(parentId.getId());
        }
        final String nodePrefix = parentsPrefixString + ResourceUtils.getBase128Integer(nodeId);
        final String nodePrefixPlusOne =  parentsPrefixString + ResourceUtils.getBase128Integer(nodeId + 1);
        node.setPrefix(nodePrefix);
        node.setPrefixPlusOne(nodePrefixPlusOne);

        final PreparedStatement insertNode = conn.prepareStatement(INSERT_PROFILE_RESOURCE_NODE);

        insertNode.setLong(            1,  nodeId);
        insertNode.setBoolean(         2,  mismatch);
        insertNode.setDate(            3,  finished);
        SqlUtils.setNullableInteger(   4,  numIdentifications, insertNode);
        SqlUtils.setNullableString(    5,  extension, insertNode);
        SqlUtils.setNullableString(    6,  hash, insertNode);
        SqlUtils.setNullableEnumAsInt(7, method, insertNode);
        SqlUtils.setNullableTimestamp(8, modDate, insertNode);
        insertNode.setString(          9,  name);
        SqlUtils.setNullableEnumAsInt( 10, nodeStatus, insertNode);
        SqlUtils.setNullableEnumAsInt( 11, resourceType, insertNode);
        SqlUtils.setNullableLong(      12, size, insertNode);
        SqlUtils.setNullableLong(      13, nodeParentId, insertNode);
        SqlUtils.setNullableString(    14, nodePrefix, insertNode);
        SqlUtils.setNullableString(    15, nodePrefixPlusOne, insertNode);
        insertNode.setString(          16, uri);
        return insertNode;
    }

    private PreparedStatement getIdentificationStatement(final Connection conn,
                                                         final long nodeId,
                                                         final List<Format> identifications) throws SQLException {
        final PreparedStatement insertIdentifications;
        final int size = identifications.size();
        if (size == 0) {
            insertIdentifications = conn.prepareStatement(INSERT_ZERO_IDENTIFICATIONS);
            insertIdentifications.setLong(1, nodeId);
        } else if (size < 11) {
            insertIdentifications = conn.prepareStatement(INSERT_IDENTIFICATION[size]);
            setIdentificationParameters(insertIdentifications, nodeId, identifications);
        } else {
            insertIdentifications = conn.prepareStatement(buildInsertIdentificationString(identifications));
            setIdentificationParameters(insertIdentifications, nodeId, identifications);
        }
        return insertIdentifications;
    }

    private String buildInsertIdentificationString(final List<Format> identifications) {
        final StringBuilder builder = new StringBuilder(60 + identifications.size() * 6);
        builder.append(INSERT_IDENTIFICATIONS);
        for (int i = 0; i < identifications.size() - 1; i++) {
            builder.append("(?,?),");
        }
        builder.append("(?,?)");
        return builder.toString();
    }

    private void setIdentificationParameters(final PreparedStatement insert, final long nodeId,
                                             final List<Format> identifications) throws SQLException {
        int parameterCount = 1;
        for (final Format format : identifications) {
            insert.setLong(parameterCount++, nodeId);
            String p = format.getPuid();
            insert.setString(parameterCount++, p == null? "" : p);
        }
    }

    private List<Format> loadAllFormats() {
        final List<Format> formats = new ArrayList<Format>(2000);
        PreparedStatement loadFormat = null;
        ResultSet results = null;

        try {
            final Connection conn = datasource.getConnection();
            try {
                loadFormat = conn.prepareStatement(SELECT_FORMATS);
                results = loadFormat.executeQuery();

                while (results.next()) {
                    formats.add(SqlUtils.buildFormat(results));
                }
            } finally {
                if ( loadFormat != null) {
                    loadFormat.close();
                }

                if (results != null) {
                    results.close();
                }

                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            log.error("A database exception occurred getting all formats.", e);
        }
        return formats;
    }
}
//CHECKSTYLE:ON
