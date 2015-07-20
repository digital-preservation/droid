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
package uk.gov.nationalarchives.droid.profile;

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

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author a-mpalmer
 *
 */
public final class SqlUtils {

    private SqlUtils() {
    }
    
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
                        nodePrefix + ".name ")
                .replace("profileResourceNode.metaData.size", 
                        nodePrefix + ".file_size")
                .replace("profileResourceNode.metaData.extension", 
                        nodePrefix + ".extension")
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
                        formatPrefix + ".name")
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
        format.setPuid(results.getString(1));
        format.setMimeType(results.getString(2));
        format.setName(results.getString(3));
        format.setVersion(results.getString(4));
        return format;
    }

    /**
     * Builds a profile resource node from a database query result sets for the profile resource
     * and its identifications.  Uses a map of puids to Formats to build the identifications.
     *
     * @param nodeResults
     *
     * @return A ProfileResourceNode
     *
     * @throws SQLException if there is problem processing the SQL result sets.
     */
    public static ProfileResourceNode buildProfileResourceNode(final ResultSet nodeResults) throws SQLException {
        // Get data from result set:
        final Long node_id                   = nodeResults.getLong(1);
        final Boolean extension_mismatch     = nodeResults.getBoolean(2);
        final Date finished_timestamp        = getNullableTimestamp(3, nodeResults);
        //final Integer identification_count   = nodeResults.getInt(4); this is set on the node by adding identifications.
        final String extension               = getNullableString(5, nodeResults);
        final String hash                    = getNullableString(6, nodeResults);
        final Integer idInt                  = getNullableInteger(7, nodeResults);
        final IdentificationMethod idMethod  = idInt == null? null : IdentificationMethod.values()[idInt];
        final Date last_mod                  = getNullableTimestamp(8, nodeResults);
        final Long last_modified             = last_mod == null? null : last_mod.getTime();
        final String name                    = nodeResults.getString(9);
        final Integer nodeS                  = getNullableInteger(10, nodeResults);
        final NodeStatus nodeStatus          = nodeS == null? null : NodeStatus.values()[nodeS];
        final Integer rType                  = getNullableInteger(11, nodeResults);
        final ResourceType resourceType      = rType == null? null : ResourceType.values()[rType];
        final Long size                      = getNullableLong(12, nodeResults);
        final Long parentId                  = getNullableLong(13, nodeResults);
        final String prefix                  = getNullableString(14, nodeResults);
        final String prefixPlusOne           = getNullableString(15, nodeResults);
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
        return node;
    }

    public static Integer getNullableInteger(final int position,
                                                  final ResultSet results) throws SQLException {
        final int value = results.getInt(position);
        return results.wasNull()? null : value;
    }

    public static String getNullableString(final int position,
                                           final ResultSet results) throws SQLException {
        final String value = results.getString(position);
        return results.wasNull()? null : value;
    }

    public static Long getNullableLong(final int position,
                                         final ResultSet results) throws SQLException {
        final Long value = results.getLong(position);
        return results.wasNull()? null : value;
    }

    public static Date getNullableTimestamp(final int position,
                                            final ResultSet results) throws SQLException {
        final Date value = results.getTimestamp(position);
        return results.wasNull() ? null : value;
    }

    public static void addIdentifications(final ProfileResourceNode node,
                                          final ResultSet identifications,
                                          final Map<String, Format> puidFormatMap) throws SQLException {
        //TODO: check for null formats - don't add this as an identification.
        while (identifications.next()) {
            node.addFormatIdentification(puidFormatMap.get(identifications.getString(2)));
        }
    }

    public static void setNullableString(final int position, final String value, final PreparedStatement statement) throws SQLException {
        if (value == null) {
            statement.setNull(position, Types.VARCHAR);
        } else {
            statement.setString(position, value);
        }
    }

    public static void setNullableInteger(final int position, final Integer value, final PreparedStatement statement ) throws SQLException {
        if (value == null) {
            statement.setNull(position, Types.INTEGER);
        } else {
            statement.setInt(position, value);
        }
    }

    public static void setNullableLong(final int position, final Long value, final PreparedStatement statement) throws SQLException {
        if (value == null) {
            statement.setNull(position, Types.BIGINT);
        } else {
            statement.setLong(position, value);
        }
    }

    public static void setNullableTimestamp(final int position, final Date value, final PreparedStatement statement) throws SQLException {
        if (value == null) {
            statement.setNull(position, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(position, new java.sql.Timestamp(value.getTime()));
        }
    }

    public static void setNullableEnumAsInt(final int position, final Enum value, final PreparedStatement statement) throws SQLException {
        if (value == null) {
            statement.setNull(position, Types.INTEGER);
        } else {
            statement.setInt(position, value.ordinal());
        }
    }


    public static void setNonNullableParameter(int position, Object parameter, PreparedStatement statement) throws SQLException {
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
