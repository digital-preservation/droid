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
package uk.gov.nationalarchives.droid.planet.xml.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;
import uk.gov.nationalarchives.droid.profile.SqlUtils;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;
/**
 * JDBC implementation of JpaPlanetsXMLDaoImpl.
 * 
 * @author Brian O'Reilly
 *  N.B. This code can be reviewed once we don't need to support Java versions prior to 8.
 *  Many of the methods are very similar and it should be possible to reduce the code significantly
 *  via the use of lambdas/delegates.  But at this point in tme (Oct 2015) we still need to support
 *  Versions 6 nd 7, and it's tricky to avoid the proliferation of similar methods!
 */
public class JdbcPlanetsXMLDaoImpl implements PlanetsXMLDao {

    private static final int THREE = 3;
    private static final String ZERO = "0";
    private static final int FOLDER_TYPE = 0;

    private static final String SELECT_FORMAT_COUNT          = "SELECT COUNT('x') AS TOTAL FROM FORMAT";
    private static final String SELECT_FORMATS               = "SELECT PUID, MIME_TYPE, NAME, VERSION FROM FORMAT";
    private static final String SELECT_PRN_COUNT             = "SELECT COUNT(*) FROM PROFILE_RESOURCE_NODE p ";

    private static final int FORMAT_MAP_SIZE = 2500;

    private static final String FILTER_FORMAT_SQL_JOIN =
            " inner join identification i on p.node_id = i.node_id inner join format f on f.puid = i.puid ";

    private static final String TOTAL = "total";
    private static final String AND_RESOURCE_TYPE = "and p.resource_type = ? ";
    private static final String FROM_PROFILE_RESOURCE_NODE = "from profile_resource_node  p ";
    private static final String FILE_COUNT = "file_count";
    private static final String FILE_SIZE = "file_size";
    private static final String WHERE_NODE_STATUS_OR = " where (p.node_status = ? or p.node_status = ?) ";

    private String filterQueryString = "";

    private boolean filterEnabled;
    private boolean formatCriteriaExist;
    private QueryBuilder queryBuilder;

    private final Log log = LogFactory.getLog(getClass());

    private DataSource datasource;
    private Connection connection;

    private Map<String, Format> puidFormatMap;

    /**
     * Default constructor.
     */
    public JdbcPlanetsXMLDaoImpl() {

    }

    //CHECKSTYLE:OFF  Too complex
    /**
     * {@inheritDoc}
     */
    @Override
    public PlanetsXMLData getDataForPlanetsXML(Filter filter) {

        if (this.puidFormatMap == null) {
            List<Format> formats = null;
            try {
                this.puidFormatMap = new HashMap<String, Format>(FORMAT_MAP_SIZE);
                formats = loadAllFormats();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            for (final Format format : formats) {
                puidFormatMap.put(format.getPuid(), format);
            }
        }

        filterQueryString = "";
        if (filter != null) {
            this.filterEnabled = filter.isEnabled() && !filter.getCriteria().isEmpty();

            this.queryBuilder = SqlUtils.getQueryBuilder(filter);
            String ejbFragment = queryBuilder.toEjbQl();
            String sqlFilter = " and " + SqlUtils.transformEJBtoSQLFields(ejbFragment, "p", "f");

            this.filterQueryString = sqlFilter;
            this.formatCriteriaExist = ejbFragment.contains("format.");
        }

        PlanetsXMLData planetXMLData = null;

        try {

            if (this.puidFormatMap == null) {

                List<Format> formats = loadAllFormats();
                this.puidFormatMap = new HashMap<String, Format>(formats.size());

                for (final Format format : formats) {
                    puidFormatMap.put(format.getPuid(), format);
                }
            }

            this.connection = this.datasource.getConnection();
            planetXMLData = new PlanetsXMLData();
            planetXMLData.setProfileStat(getProfileStat());
            planetXMLData.setGroupByPuid(getGroupByPuid());
            planetXMLData.setGroupByYear(getGroupByYear());

        } catch (SQLException ex) {
            log.error(ex.getMessage(),  ex);
        } finally {
            try {
                if (this.connection != null) {
                    this.connection.close();
                }
            } catch (SQLException ex) {
                log.error(ex.getMessage(), ex);
            }

        }
        return planetXMLData;
    }
//CHECKSTYLE:ON

    private void setFilterParameters(QueryBuilder builder, PreparedStatement profileStatement, int startPosition)
        throws SQLException {

        int pos = startPosition;

        for (Object value : builder.getValues()) {
            Object value2 = SqlUtils.transformParameterToSQLValue(value);

            String className = value2.getClass().getSimpleName();
            //Java 6 doesn't support switch on string!!
            switch(SqlUtils.ClassName.valueOf(className)) {
                case String:
                    profileStatement.setString(pos++, (String) value2);
                    break;
                case Date:
                    java.util.Date d = (java.util.Date) value2;
                    profileStatement.setDate(pos++, new java.sql.Date(d.getTime()));
                    break;
                case Long:
                    profileStatement.setLong(pos++, (Long) value2);
                    break;
                case Integer:
                    profileStatement.setInt(pos++, (Integer) value2);
                    break;
                default:
                    log.error("Invalid filter parameter type in JDBCPlanetsXmlDaoImpl");
                    break;
            }
        }
    }
    //CHECKSTYLE:OFF
    private ProfileStat getProfileStat() throws  SQLException {
        ProfileStat profileStat = new ProfileStat();

        StringBuilder sb = new StringBuilder("SELECT   min(p.file_size) as smallest, max(p.file_size) as largest, ");
        sb.append("avg(p.file_size) as mean, sum(p.file_size) as total ");
        sb.append(" from  profile_resource_node p ");

        PreparedStatement statement = null;
        ResultSet resultset = null;


        try {
            int paramIndex = 1;

            String whereClause = " where p.resource_type != ?  ";

            if (filterEnabled) {
                if (this.filterQueryString.equals("")) {
                    log.error(" JDBC Planets XML - Expected to find a filter but it was not found!");
                }

                if (this.formatCriteriaExist) {
                    sb.append(FILTER_FORMAT_SQL_JOIN);
                }
                sb.append(whereClause);
                sb.append(this.filterQueryString);

                statement = this.connection.prepareStatement(sb.toString());
                statement.setInt(paramIndex++, FOLDER_TYPE);

                setFilterParameters(this.queryBuilder, statement, paramIndex);
            } else {
                sb.append(whereClause);
                statement = this.connection.prepareStatement(sb.toString());
                statement.setInt(paramIndex, FOLDER_TYPE);
            }

            resultset = statement.executeQuery();

            if (resultset.next()) {
                profileStat.setProfileSmallestSize(BigInteger.valueOf(resultset.getInt("smallest")));
                profileStat.setProfileLargestSize(BigInteger.valueOf(resultset.getInt("largest")));
                profileStat.setProfileMeanSize(resultset.getBigDecimal("mean").setScale(1));
                profileStat.setProfileTotalSize(BigInteger.valueOf(resultset.getInt(TOTAL)));
            }

            setTotalUnreadableFiles(profileStat);
            setTotalUnreadableFolders(profileStat);
            setTotalReadableFiles(profileStat);

        } catch (SQLException e) {
            throw e;
        } finally {
            try {
                if (resultset != null) { resultset.close(); }
                if (statement != null) { statement.close(); }
            } catch (SQLException e) {
                log.error(e);
            }
        }

        return profileStat;
    }
//CHECKSTYLE:ON
    /**
     * @param profileStat
     */
    private void setTotalReadableFiles(ProfileStat profileStat) throws SQLException {
        /*
        Query queryForTotalReadableFiles = entityManager
                .createQuery("SELECT   count(*) "
                        + "from ProfileResourceNode profileResourceNode  "
                        + "where metaData.nodeStatus not in (?, ?) "
                        + "and (metaData.resourceType = 0 or metaData.resourceType != ?) "
                        + filterQueryString);

        int index = 1;
        queryForTotalReadableFiles.setParameter(index++, NodeStatus.ACCESS_DENIED);
        queryForTotalReadableFiles.setParameter(index++, NodeStatus.NOT_FOUND);
        queryForTotalReadableFiles.setParameter(index++, ResourceType.FOLDER);

        setFilterParameters(queryForTotalReadableFiles, index);

        List<?> results = queryForTotalReadableFiles.getResultList();
        if (results != null && !results.isEmpty()) {
            for (Object res : results) {

                profileStat.setProfileTotalReadableFiles(new BigInteger(res
                        .toString()));
            }
        }
        */

        StringBuilder sb = new StringBuilder(SELECT_PRN_COUNT);
        if (this.formatCriteriaExist) {
            sb.append(FILTER_FORMAT_SQL_JOIN);
        }
        sb.append(" where p.node_status NOT IN (?, ?) ");
        //BNO: this is the equivalent  of previous Hibernate version which would amount to:
        // and p.resource_type = 0 OR p.resource_type != 0. So presumably not what was intended !
        //sb.append("and p.resource_type = 0 OR p.resource_type != ? ");
        sb.append("and  p.resource_type != ? ");

        if (this.filterEnabled && !this.filterQueryString.equals("")) {
            sb.append(this.filterQueryString);
        }

        int index = 1;

        ResultSet resultset = null;
        PreparedStatement statement = null;

        try {
            statement = this.connection.prepareStatement(sb.toString());
            statement.setInt(index++, NodeStatus.ACCESS_DENIED.ordinal());
            statement.setInt(index++, NodeStatus.NOT_FOUND.ordinal());
            statement.setInt(index++, ResourceType.FOLDER.ordinal());
            if (this.filterEnabled && !this.filterQueryString.equals("")) {
                setFilterParameters(this.queryBuilder, statement, index);
            }
            resultset = statement.executeQuery();

            if (resultset.next()) {
                profileStat.setProfileTotalReadableFiles(BigInteger.valueOf(resultset.getInt(1)));
            }
        } finally {
            if (resultset != null) { resultset.close(); }
            if (statement != null) { statement.close(); }
        }
    }

    /**
     * @param profileStat
     */
    private void setTotalUnreadableFolders(ProfileStat profileStat) throws SQLException {

        StringBuilder sb = new StringBuilder(SELECT_PRN_COUNT);
        if (this.formatCriteriaExist) {
            sb.append(FILTER_FORMAT_SQL_JOIN);
        }
        sb.append(WHERE_NODE_STATUS_OR);
        sb.append(AND_RESOURCE_TYPE);

        if (this.filterEnabled && !this.filterQueryString.equals("")) {
            sb.append(this.filterQueryString);
        }

        int index = 1;

        ResultSet resultset = null;
        PreparedStatement statement = null;

        try {
            statement = this.connection.prepareStatement(sb.toString());
            statement.setInt(index++, NodeStatus.ACCESS_DENIED.ordinal());
            statement.setInt(index++, NodeStatus.NOT_FOUND.ordinal());
            statement.setInt(index++, ResourceType.FOLDER.ordinal());
            if (this.filterEnabled && !this.filterQueryString.equals("")) {
                setFilterParameters(this.queryBuilder, statement, index);
            }
            resultset = statement.executeQuery();

            if (resultset.next()) {
                profileStat.setProfileTotalUnReadableFolders(BigInteger.valueOf(resultset.getInt(1)));
            }
        } finally {
            if (resultset != null) { resultset.close(); }
            if (statement != null) { statement.close(); }
        }
    }

    /**
     * @param profileStat
     */
    private void setTotalUnreadableFiles(ProfileStat profileStat) throws  SQLException {
        /*
            Query queryForTotalUnreadableFiles = entityManager
                .createQuery("SELECT count(*) "
                        + "from ProfileResourceNode profileResourceNode   "
                        + "where (metaData.nodeStatus = ? or metaData.nodeStatus = ?) "
                        + "and metaData.resourceType != ? "
                        + filterQueryString);

        int index = 1;
        queryForTotalUnreadableFiles.setParameter(index++, NodeStatus.ACCESS_DENIED);
        queryForTotalUnreadableFiles.setParameter(index++, NodeStatus.NOT_FOUND);
        queryForTotalUnreadableFiles.setParameter(index++, ResourceType.FOLDER);

        setFilterParameters(queryForTotalUnreadableFiles, index);
        List<?> results = queryForTotalUnreadableFiles.getResultList();
        if (results != null && !results.isEmpty()) {
            for (Object res : results) {

                profileStat.setProfileTotalUnReadableFiles(new BigInteger(res
                        .toString()));
            }
        }
        */

        StringBuilder sb = new StringBuilder(SELECT_PRN_COUNT);
        if (this.formatCriteriaExist) {
            sb.append(FILTER_FORMAT_SQL_JOIN);
        }
        sb.append(WHERE_NODE_STATUS_OR);
        sb.append("and p.resource_type != ? ");

        if (this.filterEnabled && !this.filterQueryString.equals("")) {
            sb.append(this.filterQueryString);
        }

        int index = 1;

        ResultSet resultset = null;
        PreparedStatement statement = null;

        try {
            statement = this.connection.prepareStatement(sb.toString());
            statement.setInt(index++, NodeStatus.ACCESS_DENIED.ordinal());
            statement.setInt(index++, NodeStatus.NOT_FOUND.ordinal());
            statement.setInt(index++, ResourceType.FOLDER.ordinal());
            if (this.filterEnabled && !this.filterQueryString.equals("")) {
                setFilterParameters(this.queryBuilder, statement, index);
            }
            resultset = statement.executeQuery();

            if (resultset.next()) {
                profileStat.setProfileTotalUnReadableFiles(BigInteger.valueOf(resultset.getInt(1)));
            }
        } finally {
            if (resultset != null) { resultset.close(); }
            if (statement != null) { statement.close(); }
        }
    }

    private List<GroupByYearSizeAndCountRow> getGroupByYear() throws SQLException {

        List<GroupByYearSizeAndCountRow> dataList = new ArrayList<GroupByYearSizeAndCountRow>();
        GroupByYearSizeAndCountRow groupByYearSizeAndCountRow = null;

        StringBuilder sb = new StringBuilder("SELECT year(p.last_modified_date) as file_year, ");
        sb.append("count(*) as file_count, sum(p.file_size) as file_size ");
        sb.append(FROM_PROFILE_RESOURCE_NODE);

        if (this.formatCriteriaExist) {
            sb.append(FILTER_FORMAT_SQL_JOIN);
        }
        sb.append("where p.resource_type != ? ");

        if (this.filterEnabled && !this.filterQueryString.equals("")) {
            sb.append(this.filterQueryString);
        }
        sb.append(" group by year(p.last_modified_date)");

        int index = 1;

        ResultSet resultset = null;
        PreparedStatement statement = null;

        try {
            statement = this.connection.prepareStatement(sb.toString());
            statement.setInt(index++, ResourceType.FOLDER.ordinal());
            if (this.filterEnabled && !this.filterQueryString.equals("")) {
                setFilterParameters(this.queryBuilder, statement, index);
            }
            resultset = statement.executeQuery();

            while (resultset.next()) {
                groupByYearSizeAndCountRow = new GroupByYearSizeAndCountRow();
                groupByYearSizeAndCountRow.setYear(resultset.getInt("file_year"));
                groupByYearSizeAndCountRow.setCount(BigInteger.valueOf(resultset.getLong(FILE_COUNT)));
                groupByYearSizeAndCountRow.setSize(resultset.getBigDecimal(FILE_SIZE).setScale(1));
                dataList.add(groupByYearSizeAndCountRow);
            }
        } finally {
            if (resultset != null) { resultset.close(); }
            if (statement != null) { statement.close(); }
        }

        return dataList;
    }
    //CHECKSTYLE:OFF  Too many statements...
    private List<GroupByPuidSizeAndCountRow> getGroupByPuid() throws SQLException {
        List<GroupByPuidSizeAndCountRow> dataList = new ArrayList<GroupByPuidSizeAndCountRow>();
        GroupByPuidSizeAndCountRow groupByPuidSizeAndCountRow = null;

        StringBuilder sb = new StringBuilder("SELECT f.puid, count(*) as file_count, sum(p.file_size) as file_size ");
        sb.append(FROM_PROFILE_RESOURCE_NODE);
        sb.append(FILTER_FORMAT_SQL_JOIN);
        sb.append(" where p.resource_type != ? ");

        if (this.filterEnabled && !this.filterQueryString.equals("")) {
            sb.append(this.filterQueryString);
        }
        sb.append(" group by f.puid ");

        int index = 1;

        ResultSet resultset = null;
        PreparedStatement statement = null;

        try {
            statement = this.connection.prepareStatement(sb.toString());
            statement.setInt(index++, ResourceType.FOLDER.ordinal());
            if (this.filterEnabled && !this.filterQueryString.equals("")) {
                setFilterParameters(this.queryBuilder, statement, index);
            }
            resultset = statement.executeQuery();

            while (resultset.next()) {
                groupByPuidSizeAndCountRow = new GroupByPuidSizeAndCountRow();
                String puid = resultset.getString("puid");
                BigInteger fileCount = BigInteger.valueOf(resultset.getLong(FILE_COUNT));
                groupByPuidSizeAndCountRow.setCount(fileCount);
                BigDecimal fileSize = resultset.getBigDecimal(FILE_SIZE).setScale(1);
                groupByPuidSizeAndCountRow.setSize(fileSize);

                Format format = puidFormatMap.get(puid);

                groupByPuidSizeAndCountRow.setFormatName(format.getName());
                groupByPuidSizeAndCountRow.setFormatVersion(format.getVersion());
                groupByPuidSizeAndCountRow.setMimeType(format.getMimeType());

                dataList.add(groupByPuidSizeAndCountRow);
            }
        } finally {
            if (resultset != null) { resultset.close(); }
            if (statement != null) { statement.close(); }
        }

        return dataList;
    }
//CHECKSTYLE:ON

    /**
     * Set the data source.
     * @param datasource The data source to set.
     */
    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    /**
     * Get the data source.
     * @return The data source
     */
    public DataSource getDatasource() {
        return this.datasource;
    }

    /**
     * Load all the formats.
     */
    private List<Format> loadAllFormats() throws SQLException {
        //CHECKSTYLE:OFF
        List<Format> formats = null;
        try {
            final Connection conn = datasource.getConnection();
            try {
                final PreparedStatement getFormatCount = conn.prepareStatement(SELECT_FORMAT_COUNT);
                final ResultSet rsFormatCount = getFormatCount.executeQuery();
                rsFormatCount.next();
                final int formatCount = rsFormatCount.getInt(TOTAL);
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
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            throw e;
        }
        return formats;
        //CHECKSTYLE:ON
    }
}
