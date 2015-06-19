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
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by matt on 19/06/15.
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
    private static String DELETE_NODE = "DELETE FROM PROFILE_RESOURCE_NODE WHERE NODE_ID = ?";
    private static String SELECT_FORMAT = "SELECT * FROM FORMAT WHERE PUID = ?";
    private static String SELECT_FORMATS = "SELECT * FROM FORMAT";
    private static String SELECT_PROFILE_RESOURCE_NODE = "SELECT * FROM PROFILE_RESOURCE_NODE WHERE NODE_ID = ?";
    private static String SELECT_IDENTIFICATIONS = "SELECT * FROM IDENTIFICATION WHERE NODE_ID = ?";
    private static String MAX_NODE_ID_QUERY = "SELECT MAX(NODE_ID) FROM PROFILE_RESOURCE_NODE";

    private final Log log = LogFactory.getLog(getClass());

    private DataSource datasource;
    private AtomicLong nodeIds;

    private List<Format> formats;
    private Map<String, Format> puidFormatMap = new HashMap<String,Format>(2500);

    //TODO: check auto commit status of data source.

    @Override
    public void init() {
        formats = getAllFormats();
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
                final Long nodeId = nodeIds.incrementAndGet();
                node.setId(nodeId);
                final PreparedStatement insertNode = getNodeInsertStatement(conn, node, parentId);
                insertNode.execute();
                final PreparedStatement insertIdentifications = getIdentificationStatement(conn, nodeId, node.getFormatIdentifications());
                insertIdentifications.execute();
                conn.commit();
            } finally{
                conn.close();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred inserting a node " + node +
                      " with parent id " + parentId, e);
        }
    }


    @Override
    public Format loadFormat(final String puid) {
        Format format = null;
        try {
            final Connection conn = datasource.getConnection();
            try {
                final PreparedStatement loadFormat = conn.prepareStatement(SELECT_FORMAT);
                loadFormat.setString(1, puid);
                final ResultSet results = loadFormat.executeQuery();
                if (results.next()) {
                    format = buildFormat(results);
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
        final List<Format> formats = new ArrayList<Format>(2000);
        try {
            final Connection conn = datasource.getConnection();
            try {
                final PreparedStatement loadFormat = conn.prepareStatement(SELECT_FORMATS);
                final ResultSet results = loadFormat.executeQuery();
                while (results.next()) {
                    formats.add(buildFormat(results));
                }
            } finally{
                conn.close();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred getting all formats.", e);
        }
        return formats;
    }

    @Override
    public ProfileResourceNode loadNode(Long nodeId) {
        ProfileResourceNode node = null;
        try {
            final Connection conn = datasource.getConnection();
            try {
                final PreparedStatement loadNode = conn.prepareStatement(SELECT_PROFILE_RESOURCE_NODE);
                loadNode.setLong(1, nodeId);
                final ResultSet nodeResults = loadNode.executeQuery();
                if (nodeResults.next()) {
                    final PreparedStatement loadIdentifications = conn.prepareStatement(SELECT_IDENTIFICATIONS);
                    loadIdentifications.setLong(1, nodeId);
                    final ResultSet idResults = loadIdentifications.executeQuery();
                    node = buildNode(nodeResults, idResults);
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
            final Connection conn = datasource.getConnection(); //TODO: check auto commit status.
            final PreparedStatement statement = conn.prepareStatement(DELETE_NODE);
            statement.setLong(1, nodeId);
            statement.execute();
            conn.commit();
            //TODO: do we need to explicitly delete formats associated with this node, or does it do cascade?
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
                final ResultSet results = maxNodes.executeQuery();
                if (results.next()) {
                    maxId = results.getLong(1);
                }
            } finally{
                conn.close();
            }
        } catch (SQLException e) {
            log.error("A database exception occurred finding the maximum id in the database", e);
        }
        return maxId;
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

        insertNode.setLong(    1, nodeId);
        insertNode.setBoolean( 2, mismatch);
        insertNode.setDate(    3, finished);

        if (numIdentifications == null) {
            insertNode.setNull(4, Types.INTEGER);
        } else {
            insertNode.setInt( 4, numIdentifications);
        }

        if (extension == null) {
            insertNode.setNull(5, Types.VARCHAR);
        } else {
            insertNode.setString(5, extension);
        }

        if (hash == null)  {
            insertNode.setNull(6, Types.VARCHAR);
        } else {
            insertNode.setString(  6, hash);
        }

        if (method == null) {
            insertNode.setNull(7, Types.INTEGER);
        } else {
            insertNode.setInt(7, method.ordinal()); // having one method per resource seems wrong with multiple identifications.
        }

        if (modDate == null) {
            insertNode.setNull(8, Types.TIMESTAMP);
        } else {
            insertNode.setDate(8, new java.sql.Date(modDate.getTime()));
        }

        insertNode.setString(  9, name);

        if (nodeStatus == null) {
            insertNode.setNull(10, Types.INTEGER);
        } else {
            insertNode.setInt(10, nodeStatus.ordinal());
        }

        if (resourceType == null) {
            insertNode.setNull(11, Types.INTEGER);
        } else {
            insertNode.setInt(11, resourceType.ordinal());
        }

        if (size == null) {
            insertNode.setNull(12, Types.BIGINT);
        } else {
            insertNode.setLong(12, size);
        }

        if (nodeParentId == null) {
            insertNode.setNull(13, Types.BIGINT);
        } else {
            insertNode.setLong(13, nodeParentId);
        }

        if (nodePrefix == null) {
            insertNode.setNull(14, Types.VARCHAR);
        } else {
            insertNode.setString(14, nodePrefix);
        }

        if (nodePrefixPlusOne == null) {
            insertNode.setNull(15, Types.VARCHAR);
        } else {
            insertNode.setString(15, nodePrefixPlusOne);
        }

        insertNode.setString( 16, uri);
        return insertNode;
    }

    private PreparedStatement getIdentificationStatement(final Connection conn,
                                                         final long nodeId,
                                                         final List<Format> identifications) throws SQLException {
        final PreparedStatement insertIdentifications;
        switch (identifications.size()) {
            case 0: {
                insertIdentifications = conn.prepareStatement(INSERT_ZERO_IDENTIFICATIONS);
                insertIdentifications.setLong(1, nodeId);
                break;
            }
            case 1: {
                insertIdentifications = conn.prepareStatement(INSERT_ONE_IDENTIFICATION);
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
            }
            case 2: {
                insertIdentifications = conn.prepareStatement(INSERT_TWO_IDENTIFICATIONS);
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
            }
            case 3: {
                insertIdentifications = conn.prepareStatement(INSERT_THREE_IDENTIFICATIONS);
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
            }
            case 4: {
                insertIdentifications = conn.prepareStatement(INSERT_FOUR_IDENTIFICATIONS);
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
            }
            case 5: {
                insertIdentifications = conn.prepareStatement(INSERT_FIVE_IDENTIFICATIONS);
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
            }
            case 6: {
                insertIdentifications = conn.prepareStatement(INSERT_SIX_IDENTIFICATIONS);
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
            }
            case 7: {
                insertIdentifications = conn.prepareStatement(INSERT_SEVEN_IDENTIFICATIONS);
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
            }
            case 8: {
                insertIdentifications = conn.prepareStatement(INSERT_EIGHT_IDENTIFICATIONS);
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
            }
            case 9: {
                insertIdentifications = conn.prepareStatement(INSERT_NINE_IDENTIFICATIONS);
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
            }
            case 10: {
                insertIdentifications = conn.prepareStatement(INSERT_TEN_IDENTIFICATIONS);
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
            }
            default:
                insertIdentifications = conn.prepareStatement(buildInsertIdentificationString(identifications));
                setIdentificationParameters(insertIdentifications, nodeId, identifications);
                break;
        }
        return insertIdentifications;
    }

    private String buildInsertIdentificationString(final List<Format> identifications) {
        final StringBuilder builder = new StringBuilder(50 + identifications.size() * 6);
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

    private Format buildFormat(final ResultSet results) throws SQLException {
        final Format format = new Format();
        format.setPuid(results.getString(1));
        format.setMimeType(results.getString(2));
        format.setName(results.getString(3));
        format.setVersion(results.getString(4));
        return format;
    }

    private ProfileResourceNode buildNode(final ResultSet nodeResults, final ResultSet idResults) throws SQLException {
        // Get data from result set:
        final long node_id                   = nodeResults.getLong(1);
        final boolean extension_mismatch     = nodeResults.getBoolean(2);
        final Date finished_timestamp        = nodeResults.getTimestamp(3);
        //final int identification_count     = nodeResults.getInt(4); this is set on the node by adding identifications.
        final String extension               = nodeResults.getString(5);
        final String hash                    = nodeResults.getString(6);
        final IdentificationMethod idMethod  = IdentificationMethod.values()[nodeResults.getInt(7)];
        final long last_modified             = nodeResults.getTimestamp(8).getTime();
        final String name                    = nodeResults.getString(9);
        final NodeStatus nodeStatus          = NodeStatus.values()[nodeResults.getInt(10)];
        final ResourceType resourceType      = ResourceType.values()[nodeResults.getInt(11)];
        final long size                      = nodeResults.getLong(12);
        final long parentId                  = nodeResults.getLong(13);
        final String prefix                  = nodeResults.getString(14);
        final String prefixPlusOne           = nodeResults.getString(15);
        //final int textEncoding               = nodeResults.getInt(16); This field is currently unused.
        final String uriString               = nodeResults.getString(17);
        final URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            throw new SQLException("The URI for the node obtained from the database: [" + uriString + "] could not be converted into a URI", e);
        }

        // Create the node
        final ProfileResourceNode node = new ProfileResourceNode(uri);
        final NodeMetaData metadata    = new NodeMetaData();
        node.setMetaData(metadata);
        node.setId(node_id);
        node.setExtensionMismatch(extension_mismatch);
        node.setFinished(finished_timestamp);
        metadata.setExtension(extension);
        metadata.setHash(hash);
        metadata.setIdentificationMethod(idMethod);
        metadata.setLastModified(last_modified);
        metadata.setName(name);
        metadata.setNodeStatus(nodeStatus);
        metadata.setResourceType(resourceType);
        metadata.setSize(size);
        node.setParentId(parentId);
        node.setPrefix(prefix);
        node.setPrefixPlusOne(prefixPlusOne);

        while (idResults.next()) {
            node.addFormatIdentification(puidFormatMap.get(idResults.getString(2)));
        }

        return node;
    }


}
