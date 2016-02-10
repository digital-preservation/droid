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
package uk.gov.nationalarchives.droid.profile;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Map;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.RestrictionFactory;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Junction;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Restrictions;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * @author a-mpalmer, boreilly
 *
 */
public final class SqlUtils {

    private static final int NODE_COL_INDEX = 1;
    private static final int EXTENSION_MISMATCH_COL_INDEX = 2;
    private static final int FINISHED_TIMESTAMP_COL_INDEX = 3;
    private static final int EXTENSION_COL_INDEX = 5;
    private static final int HASH_COL_INDEX = 6;
    private static final int IDINT_COL_INDEX = 7;
    private static final int LAST_MOD_COL_INDEX = 8;
    private static final int NAME_COL_INDEX = 9;
    private static final int NODE_STATUS_COL_INDEX = 10;
    private static final int RESOURCE_TYPE_COL_INDEX = 11;
    private static final int SIZE_COL_INDEX = 12;
    private static final int PARENT_ID_COL_INDEX = 13;
    private static final int PREFIX_COL_INDEX = 14;
    private static final int PREFIX_PLUS_ONE_COL_INDEX = 15;
    private static final int URI_COL_INDEX = 17;
    private static final int FILTER_STATUS_COL_INDEX = 20;

    private static final int FORMAT_VERSION_INDEX = 4;
    private static final int FORMAT_NAME_INDEX = 3;
    private static final int FORMAT_MIME_TYPE_INDEX = 2;
    private static final int FORMAT_PUID_INDEX = 1;


    private SqlUtils() {
    }

    /**
     * For use in determining filter parameter types so we can set these to the correct SQL type.
     */
    //CHECKSTYLE:OFF Fairly self explanatory, I would have thought...
    public enum ClassName {
        String,
        Date,
        Long,
        Integer,
        Boolean
    }
    //CHECKSTYLE:ON

    /**
     * Changes enumeration types to their ordinal value.
     * @param value The object to be transformed
     * @return Object the transformed object.
     */
    public static Object transformParameterToSQLValue(Object value) {
        return value instanceof Enum<?> ? ((Enum<?>) value).ordinal() : value;
    }
    
    /**
     * Transforms EJB queries into SQL syntax by replacing their
     * class names with table aliases.
     * 
     * @param ejbFragment The fragment of ejb to transform.
     * @param nodePrefix  The alias prefix of the profile resource node table.
     * @param formatPrefix The alias prefix of the format table.
     * @return String the EJB transformed to aliased tables and columns.
     */
    public static String transformEJBtoSQLFields(String ejbFragment,
            String nodePrefix, String formatPrefix) {
        return ejbFragment.replace("profileResourceNode.metaData.name", 
                        //nodePrefix + ".name ")
                        nodePrefix + ".u_name ")
                .replace("profileResourceNode.metaData.size", 
                        nodePrefix + ".file_size")
                .replace("profileResourceNode.metaData.extension", 
                        //nodePrefix + ".extension")
                        nodePrefix + ".u_extension")
                .replace("profileResourceNode.identificationCount",
                        nodePrefix + ".identification_count")
                .replace("profileResourceNode.metaData.lastModifiedDate",
                        nodePrefix + ".last_modified_date")
                .replace("profileResourceNode.metaData.resourceType",
                        nodePrefix + ".resource_type")        
                .replace("profileResourceNode.metaData.identificationMethod",
                        nodePrefix + ".identification_method")
                .replace("profileResourceNode.metaData.nodeStatus",
                        nodePrefix + ".node_status")
                .replace("profileResourceNode.metaData.extensionMismatch",
                        nodePrefix + ".extension_mismatch")
                .replace("format.mimeType",
                        formatPrefix + ".mime_type")
                .replace("format.name",
                        //formatPrefix + ".name")
                        formatPrefix + ".u_name")
                .replace("format.puid",
                        formatPrefix + ".puid")
                .replace("extensionMismatch", "extension_mismatch"); 
    }    
    
    /**
     * 
     * @param filter a filter to use to build the query.
     * @return QueryBuilder - the filter as an EJB QueryBuilder object.
     */
    public static QueryBuilder getQueryBuilder(Filter filter) {
        QueryBuilder queryBuilder = QueryBuilder
        .forAlias("profileResourceNode");
        queryBuilder.createAlias("format");
        
        if (filter.isNarrowed()) {
            for (FilterCriterion criterion : filter.getCriteria()) {
                queryBuilder.add(RestrictionFactory.forFilterCriterion(criterion));
            }
        } else {
            Junction disjunction = Restrictions.disjunction();
            for (FilterCriterion criterion : filter.getCriteria()) {
                disjunction.add(RestrictionFactory.forFilterCriterion(criterion));
            }
            queryBuilder.add(disjunction);
        }
        return queryBuilder;
    }



    /**
     * Builds a Format from a SQL result set.
     *
     * @param results A result set containing one or more formats queried from the profile table.
     *
     * @return A Format object.
     * @throws SQLException if there is a problem getting results from the result set.
     */
    public static Format buildFormat(final ResultSet results) throws SQLException {
        final Format format = new Format();
        format.setPuid(results.getString(FORMAT_PUID_INDEX));
        format.setMimeType(getNullableString(FORMAT_MIME_TYPE_INDEX, results));
        format.setName(getNullableString(FORMAT_NAME_INDEX, results));
        format.setVersion(getNullableString(FORMAT_VERSION_INDEX, results));
        return format;
    }

    /**
     * Builds a profile resource node from a database query result sets for the profile resource
     * and its identifications.  Uses a map of puids to Formats to build the identifications.
     *
     * @param nodeResults ResultSet
     *
     * @return A ProfileResourceNode
     *
     * @throws SQLException if there is problem processing the SQL result sets.
     */
    //CHECKSTYLE:OFF Too many statements
    public static ProfileResourceNode buildProfileResourceNode(final ResultSet nodeResults) throws SQLException {

        // Get data from result set:
        final Long nodeId                   = nodeResults.getLong(NODE_COL_INDEX);
        final Boolean extensionMismatch     = nodeResults.getBoolean(EXTENSION_MISMATCH_COL_INDEX);
        final Date finishedTimestamp        = getNullableTimestamp(FINISHED_TIMESTAMP_COL_INDEX, nodeResults);
        final String extension               = getNullableString(EXTENSION_COL_INDEX, nodeResults);
        final String hash                    = getNullableString(HASH_COL_INDEX, nodeResults);
        final Integer idInt                  = getNullableInteger(IDINT_COL_INDEX, nodeResults);
        final IdentificationMethod idMethod  = idInt == null ? null : IdentificationMethod.values()[idInt];
        final Date lastModifiedDate                  = getNullableTimestamp(LAST_MOD_COL_INDEX, nodeResults);
        final Long lastModifiedTime             = lastModifiedDate == null ? null : lastModifiedDate.getTime();
        final String name                    = nodeResults.getString(NAME_COL_INDEX);
        final Integer nodeS                  = getNullableInteger(NODE_STATUS_COL_INDEX, nodeResults);
        final NodeStatus nodeStatus          = nodeS == null ? null : NodeStatus.values()[nodeS];
        final Integer rType                  = getNullableInteger(RESOURCE_TYPE_COL_INDEX, nodeResults);
        final ResourceType resourceType      = rType == null ? null : ResourceType.values()[rType];
        final Long size                      = getNullableLong(SIZE_COL_INDEX, nodeResults);
        final Long parentId                  = getNullableLong(PARENT_ID_COL_INDEX, nodeResults);
        final String prefix                  = getNullableString(PREFIX_COL_INDEX, nodeResults);
        final String prefixPlusOne           = getNullableString(PREFIX_PLUS_ONE_COL_INDEX, nodeResults);
        final String uriString               = nodeResults.getString(URI_COL_INDEX);
        final URI uri;

        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            throw new SQLException("The URI for the node obtained from the database: [" + uriString
                    + "] could not be converted into a URI", e);
        }
        int filterStatus = 1;
        if (getNumberOfColumns(nodeResults) > URI_COL_INDEX) {
            filterStatus = nodeResults.getInt(FILTER_STATUS_COL_INDEX);
        }

        // Create the node
        final ProfileResourceNode node = new ProfileResourceNode(uri);
        final NodeMetaData metadata    = new NodeMetaData();
        node.setMetaData(metadata);
        node.setId(nodeId);
        node.setExtensionMismatch(extensionMismatch);
        node.setFinished(finishedTimestamp);
        metadata.setExtension(extension);
        metadata.setHash(hash);
        metadata.setIdentificationMethod(idMethod);
        metadata.setLastModified(lastModifiedTime);
        metadata.setName(name);
        metadata.setNodeStatus(nodeStatus);
        metadata.setResourceType(resourceType);
        metadata.setSize(size);
        node.setParentId(parentId);
        node.setPrefix(prefix);
        node.setPrefixPlusOne(prefixPlusOne);
        node.setFilterStatus(filterStatus);
        return node;
        //CHECKSTYLE:ON
    }

    /**
     * Returns the number of columns in the specified ResultSet.
     * @param resultSet  The ResultSet from which to retrieve the column count
     * @return The number of columns in the ResultSet
     * @throws SQLException SQL Exception
     */
    public static int getNumberOfColumns(final ResultSet resultSet) throws SQLException {
        final ResultSetMetaData rsmd = resultSet.getMetaData();
        return rsmd.getColumnCount();
    }

    /**
     * Retrieves an integer value (or null) from a given position in a ResultSet.
     * @param position Column index within ResultSet to look for an integer value
     * @param results The ResultSet from which to retrieve the integer value
     * @return An integer value or null
     * @throws SQLException SQL Exception
     */
    public static Integer getNullableInteger(final int position,
                                                  final ResultSet results) throws SQLException {
        final int value = results.getInt(position);
        return results.wasNull() ? null : value;
    }

    /**
     * Retrieves a string value (or null) from a given position in a ResultSet.
     * @param position Column index within ResultSet to look for a string
     * @param results The ResultSet from which to retrieve the string
     * @return A string or null
     * @throws SQLException SQL Exception
     */
    public static String getNullableString(final int position,
                                           final ResultSet results) throws SQLException {
        final String value = results.getString(position);
        return results.wasNull() ? null : value;
    }

    /**
     * Retrieves a long value (or null) from a given position in a ResultSet.
     * @param position Column index within ResultSet to look for a long value
     * @param results The ResultSet from which to retrieve the long value
     * @return A long value or null
     * @throws SQLException SQL Exception
     */
    public static Long getNullableLong(final int position,
                                         final ResultSet results) throws SQLException {
        final Long value = results.getLong(position);
        return results.wasNull() ? null : value;
    }

    /**
     * Retrieves a timestamp value (or null) from a given position in a ResultSet.
     * @param position Column index within ResultSet to look for a timestamp
     * @param results The ResultSet from which to retrieve the timestamp
     * @return A timestamp value or null
     * @throws SQLException SQL Exception
     */
    public static Date getNullableTimestamp(final int position,
                                            final ResultSet results) throws SQLException {
        final Date value = results.getTimestamp(position);
        return results.wasNull() ? null : value;
    }

    /**
     * Adds identifications to a Resource Node.
     * @param node the ProfileResourceNode to which identifications will be added
     * @param identifications   A ResultSet of identifications
     * @param puidFormatMap a map of PUIDs to their identifier strings
     * @throws SQLException  SQL Exception
     */
    public static void addIdentifications(final ProfileResourceNode node,
                                          final ResultSet identifications,
                                          final Map<String, Format> puidFormatMap) throws SQLException {
        //TODO: check for null formats - don't add this as an identification.
        while (identifications.next()) {
            node.addFormatIdentification(puidFormatMap.get(identifications.getString(2)));
        }
    }

    /**
     * Sets a string value in a PreparedStatement.
     * @param position The index of the parameter within the statement to set
     * @param value The parameter value
     * @param statement The statement in which to set the parameters
     * @throws SQLException SQL Exception
     */
    public static void setNullableString(final int position, final String value, final PreparedStatement statement)
        throws SQLException {
        if (value == null) {
            statement.setNull(position, Types.VARCHAR);
        } else {
            statement.setString(position, value);
        }
    }
    /**
     * Sets an integer value in a PreparedStatement.
     * @param position The index of the parameter within the statement to set
     * @param value The parameter value
     * @param statement The statement in which to set the parameters
     * @throws SQLException SQL Exception
     */
    public static void setNullableInteger(final int position, final Integer value, final PreparedStatement statement)
        throws SQLException {
        if (value == null) {
            statement.setNull(position, Types.INTEGER);
        } else {
            statement.setInt(position, value);
        }
    }

    /**
     * Sets a long value in a PreparedStatement.
     * @param position The index of the parameter within the statement to set
     * @param value The parameter value
     * @param statement The statement in which to set the parameters
     * @throws SQLException SQL Exception
     */
    public static void setNullableLong(final int position, final Long value, final PreparedStatement statement)
        throws SQLException {
        if (value == null) {
            statement.setNull(position, Types.BIGINT);
        } else {
            statement.setLong(position, value);
        }
    }

    /**
     * Sets timestamp value in a PreparedStatement.
     * @param position The index of the parameter within the statement to set
     * @param value The parameter value
     * @param statement The statement in which to set the parameters
     * @throws SQLException SQL Exception
     */
    public static void setNullableTimestamp(final int position, final Date value, final PreparedStatement statement)
        throws SQLException {
        if (value == null) {
            statement.setNull(position, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(position, new java.sql.Timestamp(value.getTime()));
        }
    }

    /**
     * Sets parameter values in a PreparedStatement.
     * @param position The index of the parameter within the statement to set
     * @param value The parameter value
     * @param statement The statement in which to set the parameters
     * @throws SQLException SQL Exception
     */
    public static void setNullableEnumAsInt(final int position, final Enum value, final PreparedStatement statement)
        throws SQLException {
        if (value == null) {
            statement.setNull(position, Types.INTEGER);
        } else {
            statement.setInt(position, value.ordinal());
        }
    }

    /**
     * Sets parameter values in a PreparedStatement.
     * @param position  The index of the parameter within the statement to set
     * @param parameter The parameter value
     * @param statement The statement in which to set the parameters
     * @throws SQLException SQL Exception
     */
    public static void setNonNullableParameter(int position, Object parameter, PreparedStatement statement)
        throws SQLException {
        if (parameter instanceof Integer) {
            statement.setInt(position, ((Integer) parameter).intValue());
        } else if (parameter instanceof Long) {
            statement.setLong(position, ((Long) parameter).longValue());
        } else if (parameter instanceof Enum) {
            statement.setInt(position, ((Enum) parameter).ordinal());
        } else if (parameter instanceof Date) {
            statement.setDate(position, new java.sql.Date(((Date) parameter).getTime()));
        } else if (parameter instanceof String) {
            statement.setString(position, (String) parameter);
        } else if (parameter instanceof Boolean) {
            statement.setBoolean(position, (Boolean) parameter);
        }
    }
}

