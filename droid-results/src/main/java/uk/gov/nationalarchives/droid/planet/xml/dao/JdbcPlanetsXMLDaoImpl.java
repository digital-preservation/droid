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
package uk.gov.nationalarchives.droid.planet.xml.dao;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.RestrictionFactory;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Junction;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Restrictions;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

import javax.persistence.Query;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.profile.SqlUtils;

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

    private String filterQueryString = "";

    private static String SELECT_FORMAT_COUNT          = "SELECT COUNT('x') AS total FROM FORMAT";
    private static String SELECT_FORMATS               = "SELECT * FROM FORMAT";

    private static final String FILTER_FORMAT_SQL_JOIN = " inner join identification i on p.node_id = i.node_id inner join format f on f.puid = i.puid ";
    private Object[] filterParams;
    private boolean filterEnabled;
    private Boolean formatCriteriaExist;
    private QueryBuilder queryBuilder;

    private final Log log = LogFactory.getLog(getClass());

    private DataSource datasource;
    private Connection connection;

    private Map<String, Format> puidFormatMap;

    //For use in determining filter parameter types so we can set these to the correct SQL type.
    //TODO: This is used elsewhere so move to shared location e.g. Utils
    private  enum ClassName {
        String,
        Date,
        Long,
        Integer,
        Boolean
    }

    public JdbcPlanetsXMLDaoImpl() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
   // @Transactional(propagation = Propagation.REQUIRED)
    public PlanetsXMLData getDataForPlanetsXML(Filter filter) {

        if (this.puidFormatMap == null) {
            List<Format> formats = null;
            try {
                this.puidFormatMap = new HashMap<String,Format>(2500);
                formats = loadAllFormats();
            } catch (SQLException e) {
                log.error(e.getMessage(),e);
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
                this.puidFormatMap = new HashMap<String,Format>(formats.size());

                for (final Format format : formats) {
                    puidFormatMap.put(format.getPuid(), format);
                }
            }

            this.connection =  this.datasource.getConnection();;
            planetXMLData = new PlanetsXMLData();
            planetXMLData.setProfileStat(getProfileStat());
            planetXMLData.setGroupByPuid(getGroupByPuid());
            planetXMLData.setGroupByYear(getGroupByYear());

        } catch (SQLException ex) {
            log.error(ex.getMessage(),  ex);
        } finally {
            try {
                if(this.connection != null) {this.connection.close();}
            } catch(SQLException ex) {
                log.error(ex.getMessage(), ex);
            }

        }
        return planetXMLData;
    }


    private void setFilterParameters(QueryBuilder queryBuilder, PreparedStatement profileStatement, int startPosition) throws SQLException {

        int pos = startPosition;

        for (Object value : queryBuilder.getValues()) {
            Object value2 = SqlUtils.transformParameterToSQLValue(value);

            String className = value2.getClass().getSimpleName();
            //Java 6 doesn't support switch on string!!
            switch(ClassName.valueOf(className)) {
                case String:
                    profileStatement.setString(pos++, (String) value2);
                    break;
                case Date:
                    java.util.Date d = (java.util.Date)value2;
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

    private ProfileStat getProfileStat() throws  SQLException{
        ProfileStat profileStat = new ProfileStat();

        StringBuilder sb = new StringBuilder("SELECT   min(p.file_size) as smallest, max(p.file_size) as largest, ");
        sb.append("avg(p.file_size) as mean, sum(p.file_size) as total ");
        sb.append(" from  profile_resource_node p ");

        PreparedStatement statement = null;
        ResultSet resultset = null;
        QueryBuilder queryBuilder = null;

        try {
            int paramIndex = 1;

            if(filterEnabled) {
                if(this.filterQueryString == "" ) {
                    log.error(" JDBC Planets XML - Expected to find a filter but it was not found!");
                }

                if (this.formatCriteriaExist) {
                    sb.append(FILTER_FORMAT_SQL_JOIN);
                }
                sb.append(" where p.resource_type != ?  ");
                sb.append(this.filterQueryString);

                statement = this.connection.prepareStatement(sb.toString());
                statement.setInt(paramIndex++, FOLDER_TYPE);

                setFilterParameters(this.queryBuilder, statement, paramIndex);
            } else {
                sb.append(" where p.resource_type != ?  ");
                statement = this.connection.prepareStatement(sb.toString());
                statement.setInt(paramIndex, FOLDER_TYPE);
            }

            resultset = statement.executeQuery();

            if(resultset.next()) {
                profileStat.setProfileSmallestSize(BigInteger.valueOf(resultset.getInt("smallest")));
                profileStat.setProfileLargestSize(BigInteger.valueOf(resultset.getInt("largest")));
                profileStat.setProfileMeanSize(resultset.getBigDecimal("mean").setScale(1));
                profileStat.setProfileTotalSize(BigInteger.valueOf(resultset.getInt("total")));
            }

            setTotalUnreadableFiles(profileStat);
            setTotalUnreadableFolders(profileStat);
            setTotalReadableFiles(profileStat);

        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            log.error(e);
        } finally {

            try {
                if(resultset != null) { resultset.close();}
                if(statement != null) {statement.close();}
            } catch (SQLException e) {
                log.error(e);
            }
        }



        return profileStat;
    }

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

        StringBuilder sb = new StringBuilder("SELECT count(*) from profile_resource_node p ");
        if (this.formatCriteriaExist) {
            sb.append(FILTER_FORMAT_SQL_JOIN);
        }
        sb.append(" where p.node_status NOT IN (?, ?) ");
        //BNO: this is the equivalent  of previous Hibernate version which would amount to and p.resource_type = 0 OR p.resource_type != 0
        //So presumably not what was intended !
        //sb.append("and p.resource_type = 0 OR p.resource_type != ? ");
        sb.append("and  p.resource_type != ? ");

        if(this.filterEnabled && this.filterQueryString != "") {
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
            if(this.filterEnabled && this.filterQueryString != "") {
                setFilterParameters(this.queryBuilder, statement, index);
            }
            resultset = statement.executeQuery();

            if(resultset.next()) {
                profileStat.setProfileTotalReadableFiles(BigInteger.valueOf(resultset.getInt(1)));
            }
        } finally {
            if(resultset != null){resultset.close();}
            if(statement != null){statement.close();}
        }
    }

    /**
     * @param profileStat
     */
    private void setTotalUnreadableFolders(ProfileStat profileStat) throws SQLException {
       /*
        Query queryForTotalUnreadableFolders = entityManager
                .createQuery("SELECT  count(*) "
                        + "from  ProfileResourceNode profileResourceNode "
                        + "where (metaData.nodeStatus = ? or metaData.nodeStatus = ?)  "
                        + "and metaData.resourceType = ? "
                        + filterQueryString);

        int index = 1;
        queryForTotalUnreadableFolders.setParameter(index++, NodeStatus.ACCESS_DENIED);
        queryForTotalUnreadableFolders.setParameter(index++, NodeStatus.NOT_FOUND);
        queryForTotalUnreadableFolders.setParameter(index++, ResourceType.FOLDER);

        setFilterParameters(queryForTotalUnreadableFolders, index);
        List<?> results = queryForTotalUnreadableFolders.getResultList();
        if (results != null && !results.isEmpty()) {
            for (Object res : results) {

                profileStat.setProfileTotalUnReadableFolders(new BigInteger(res
                        .toString()));
            }
        }
        */

        StringBuilder sb = new StringBuilder("SELECT count(*) from profile_resource_node p ");
        if (this.formatCriteriaExist) {
            sb.append(FILTER_FORMAT_SQL_JOIN);
        }
        sb.append(" where (p.node_status = ? or p.node_status = ?) ");
        sb.append("and p.resource_type = ? ");

        if(this.filterEnabled && this.filterQueryString != "") {
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
            if(this.filterEnabled && this.filterQueryString != "") {
                setFilterParameters(this.queryBuilder, statement, index);
            }
            resultset = statement.executeQuery();

            if(resultset.next()) {
                profileStat.setProfileTotalUnReadableFolders(BigInteger.valueOf(resultset.getInt(1)));
            }
        } finally {
            if(resultset != null){resultset.close();}
            if(statement != null){statement.close();}
        }
    }

    /**
     * @param profileStat
     */
    private void setTotalUnreadableFiles(ProfileStat profileStat) throws  SQLException{
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

        StringBuilder sb = new StringBuilder("SELECT count(*) from profile_resource_node p ");
        if (this.formatCriteriaExist) {
            sb.append(FILTER_FORMAT_SQL_JOIN);
        }
        sb.append(" where (p.node_status = ? or p.node_status = ?) ");
        sb.append("and p.resource_type != ? ");

        if(this.filterEnabled && this.filterQueryString != "") {
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
            if(this.filterEnabled && this.filterQueryString != "") {
                setFilterParameters(this.queryBuilder, statement, index);
            }
            resultset = statement.executeQuery();

            if(resultset.next()) {
                profileStat.setProfileTotalUnReadableFiles(BigInteger.valueOf(resultset.getInt(1)));
            }
        } finally {
            if(resultset != null){resultset.close();}
            if(statement != null){statement.close();}
        }
    }

    private List<GroupByYearSizeAndCountRow> getGroupByYear() throws SQLException {

        List<GroupByYearSizeAndCountRow> dataList = new ArrayList<GroupByYearSizeAndCountRow>();
        GroupByYearSizeAndCountRow groupByYearSizeAndCountRow = null;
        /*
        Query q = entityManager
                .createQuery("SELECT   year(profileResourceNode.metaData.lastModifiedDate), "
                        + "count(*), sum(profileResourceNode.metaData.size) "
                        + "from ProfileResourceNode  profileResourceNode "
                        + "where profileResourceNode.metaData.resourceType != ? "
                        + filterQueryString
                        + " group by year(profileResourceNode.metaData.lastModifiedDate) ");

        int index = 1;
        q.setParameter(index++, ResourceType.FOLDER);
        
        setFilterParameters(q, index);
        List<?> results = q.getResultList();
        if (results != null && !results.isEmpty()) {
            for (Object res : results) {
                groupByYearSizeAndCountRow = new GroupByYearSizeAndCountRow();
                Object[] aaa = (Object[]) res;
                if (aaa[0] != null) {
                    groupByYearSizeAndCountRow.setYear(new Integer(aaa[0]
                            .toString()));
                }
                if (aaa[1] != null) {
                    groupByYearSizeAndCountRow.setCount(new BigInteger(aaa[1]
                            .toString()));
                }
                if (aaa[2] != null) {
                    groupByYearSizeAndCountRow.setSize(new BigDecimal(aaa[2]
                            .toString()));
                }
                dataList.add(groupByYearSizeAndCountRow);

            }

        }
        */
        StringBuilder sb = new StringBuilder("SELECT year(p.last_modified_date) as file_year, ");
        sb.append("count(*) as file_count, sum(p.file_size) as file_size ");
        sb.append("from profile_resource_node  p ");

        if (this.formatCriteriaExist) {
            sb.append(FILTER_FORMAT_SQL_JOIN);
        }
        sb.append("where p.resource_type != ? ");

        if(this.filterEnabled && this.filterQueryString != "") {
            sb.append(this.filterQueryString);
        }
        sb.append(" group by year(p.last_modified_date)");

        int index = 1;

        ResultSet resultset = null;
        PreparedStatement statement = null;

        try {
            statement = this.connection.prepareStatement(sb.toString());
            statement.setInt(index++, ResourceType.FOLDER.ordinal());
            if(this.filterEnabled && this.filterQueryString != "") {
                setFilterParameters(this.queryBuilder, statement, index);
            }
            resultset = statement.executeQuery();

            while(resultset.next()) {
                groupByYearSizeAndCountRow = new GroupByYearSizeAndCountRow();
                groupByYearSizeAndCountRow.setYear(resultset.getInt("file_year"));
                groupByYearSizeAndCountRow.setCount(BigInteger.valueOf(resultset.getLong("file_count")));
                groupByYearSizeAndCountRow.setSize(resultset.getBigDecimal("file_size").setScale(1));
                dataList.add(groupByYearSizeAndCountRow);
            }
        } finally {
            if(resultset != null){resultset.close();}
            if(statement != null){statement.close();}
        }

        return dataList;
    }

    private List<GroupByPuidSizeAndCountRow> getGroupByPuid() throws SQLException {
        List<GroupByPuidSizeAndCountRow> dataList = new ArrayList<GroupByPuidSizeAndCountRow>();
        GroupByPuidSizeAndCountRow groupByPuidSizeAndCountRow = null;
        /*
        Query q = entityManager
                .createQuery("SELECT  f.puid, count(*), sum(profileResourceNode.metaData.size) "
                        + "from ProfileResourceNode profileResourceNode "
                        + "inner join profileResourceNode.formatIdentifications f "
                        + "where  profileResourceNode.metaData.resourceType != ? "
                        + filterQueryString + " group by f.puid ");

        int index = 1;
        q.setParameter(index++, ResourceType.FOLDER);

        setFilterParameters(q, index);
        List<?> results = q.getResultList();
        if (results != null && !results.isEmpty()) {
            for (Object res : results) {
                groupByPuidSizeAndCountRow = new GroupByPuidSizeAndCountRow();

                Object[] aaa = (Object[]) res;
                String puid = "";
                if (aaa[0] != null) {
                    puid = new String(aaa[0].toString());
                }
                groupByPuidSizeAndCountRow.setPuid(puid);
                if (aaa[1] != null) {
                    groupByPuidSizeAndCountRow.setCount(new BigInteger(aaa[1]
                            .toString()));
                }
                if (aaa[2] != null) {
                    groupByPuidSizeAndCountRow.setSize(new BigDecimal(aaa[2]
                            .toString()));
                }
                
                // FIXME: N + 1 SELECTS ??????? WHY???
                String query = "from Format  where puid =:puid";

                Query detailsQuery = entityManager.createQuery(query);

                detailsQuery.setParameter("puid", puid);

                Format format = (Format) detailsQuery.getSingleResult();
                groupByPuidSizeAndCountRow.setFormatName(format.getName());
                groupByPuidSizeAndCountRow
                        .setFormatVersion(format.getVersion());
                groupByPuidSizeAndCountRow.setMimeType(format.getMimeType());

                dataList.add(groupByPuidSizeAndCountRow);
            }

        }

                        .createQuery("SELECT  f.puid, count(*), sum(profileResourceNode.metaData.size) "
                        + "from ProfileResourceNode profileResourceNode "
                        + "inner join profileResourceNode.formatIdentifications f "
                        + "where  profileResourceNode.metaData.resourceType != ? "
                        + filterQueryString + " group by f.puid ");
        */

        StringBuilder sb = new StringBuilder("SELECT f.puid, count(*) as file_count, sum(p.file_size) as file_size ");
        sb.append("from profile_resource_node  p ");
        sb.append(FILTER_FORMAT_SQL_JOIN);
        sb.append(" where p.resource_type != ? ");

        if(this.filterEnabled && this.filterQueryString != "") {
            sb.append(this.filterQueryString);
        }
        sb.append(" group by f.puid ");

        int index = 1;

        ResultSet resultset = null;
        PreparedStatement statement = null;

        try {
            statement = this.connection.prepareStatement(sb.toString());
            statement.setInt(index++, ResourceType.FOLDER.ordinal());
            if(this.filterEnabled && this.filterQueryString != "") {
                setFilterParameters(this.queryBuilder, statement, index);
            }
            resultset = statement.executeQuery();
/*
            groupByPuidSizeAndCountRow.setFormatName(format.getName());
            groupByPuidSizeAndCountRow
                    .setFormatVersion(format.getVersion());
            groupByPuidSizeAndCountRow.setMimeType(format.getMimeType());
   */
            while(resultset.next()) {
                groupByPuidSizeAndCountRow = new GroupByPuidSizeAndCountRow();
                String puid = resultset.getString("puid");
                BigInteger fileCount = BigInteger.valueOf(resultset.getLong("file_count"));
                groupByPuidSizeAndCountRow.setCount(fileCount);
                BigDecimal fileSize = resultset.getBigDecimal("file_size").setScale(1);
                groupByPuidSizeAndCountRow.setSize(fileSize);

                Format format = puidFormatMap.get(puid);

                groupByPuidSizeAndCountRow.setFormatName(format.getName());
                groupByPuidSizeAndCountRow.setFormatVersion(format.getVersion());
                groupByPuidSizeAndCountRow.setMimeType(format.getMimeType());

                dataList.add(groupByPuidSizeAndCountRow);
            }
        } finally {
            if(resultset != null){resultset.close();}
            if(statement != null){statement.close();}
        }

        return dataList;
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    public DataSource getDatasource() {
        return this.datasource;
    }

    private List<Format> loadAllFormats() throws SQLException{

        List<Format> formats = null;
        try {
            final Connection conn = datasource.getConnection();
            try {
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
            throw e;
        }
        return formats;
    }
}
