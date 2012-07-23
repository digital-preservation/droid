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
package uk.gov.nationalarchives.droid.planet.xml.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
 * JPA implementation of JpaPlanetsXMLDaoImpl.
 * 
 * @author Alok Kumar Dash.
 */
public class JpaPlanetsXMLDaoImpl implements PlanetsXMLDao {

    private static final int THREE = 3;
    private static final String ZERO = "0";

    private String filterQueryString = "";
    //private Map<String, Object> filterQueryParameterMap;
    private Object[] filterParams;
    private boolean filterEnabled;

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Flushes the DROID entity manager.
     */
    void flush() {
        entityManager.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PlanetsXMLData getDataForPlanetsXML(Filter filter) {

        filterQueryString = "";
        if (filter != null) {
            filterEnabled = filter.isEnabled() && !filter.getCriteria().isEmpty();
        }
        
        if (filterEnabled) {
            
            QueryBuilder queryBuilder = QueryBuilder.forAlias("profileResourceNode");
            if (filter.isNarrowed()) {
                for (FilterCriterion criterion : filter.getCriteria()) {
                    queryBuilder.add(RestrictionFactory.forFilterCriterion(criterion));
                }
            } else {
                Junction orJunction = Restrictions.disjunction();
                for (FilterCriterion criterion : filter.getCriteria()) {
                    orJunction.add(RestrictionFactory.forFilterCriterion(criterion));
                }
                queryBuilder.add(orJunction);
            }
            
            filterQueryString = " AND " + queryBuilder.toEjbQl();
            filterParams = queryBuilder.getValues();
            
//            FilterQueryStringGenerator filterQueryGenerator = new FilterQueryStringGenerator();
//            FilterQueryStringGenerator.FilterQueryStringAndNamedParameter queryAndParameter = filterQueryGenerator
//                    .getFilterQueryString(filter);
//            filterQueryString = " AND " + queryAndParameter.getFilterQueryString();
//            filterQueryParameterMap = queryAndParameter.getNamesParameterMap();
        } 

        PlanetsXMLData planetXMLData = new PlanetsXMLData();
        planetXMLData.setProfileStat(getProfileStat());
        planetXMLData.setGroupByPuid(getGroupByPuid());
        planetXMLData.setGroupByYear(getGroupByYear());
        return planetXMLData;
    }

    /**
     * 
     */
    private void setFilterParameters(Query q, int startPosition) {
        if (filterEnabled) {
            int pos = startPosition;
            for (Object param : filterParams) {
                q.setParameter(pos++, param);
            }
//            
//            
//            for (String parameterKey : filterQueryParameterMap.keySet()) {
//                log.info("The Parameter is : " + parameterKey);
//
//                if (filterQueryParameterMap.get(parameterKey) instanceof String) {
//                    q.setParameter(parameterKey, filterQueryParameterMap.get(parameterKey));
//                    log.info("The Parameter  Value is : "
//                            + filterQueryParameterMap.get(parameterKey));
//                } else if (filterQueryParameterMap.get(parameterKey) instanceof Long) {
//                    q.setParameter(parameterKey, filterQueryParameterMap.get(parameterKey));
//                    log.info("The Parameter Value is : "
//                            + filterQueryParameterMap.get(parameterKey));
//                }
//            }
        }
    }

    private ProfileStat getProfileStat() {

        ProfileStat profileStat = new ProfileStat();
        Query q = entityManager
                .createQuery("SELECT   min(profileResourceNode.metaData.size), max(profileResourceNode.metaData.size), "
                        + "avg(profileResourceNode.metaData.size), sum(profileResourceNode.metaData.size) "
                        + " from  ProfileResourceNode profileResourceNode "
                        + "where profileResourceNode.metaData.resourceType != ?  "
                        + filterQueryString);

        int paramIndex = 1;
        q.setParameter(paramIndex++, ResourceType.FOLDER);

        setFilterParameters(q, paramIndex);

        List<?> results = q.getResultList();
        if (results != null && !results.isEmpty()) {
            for (Object res : results) {

                Object[] aaa = (Object[]) res;

                BigInteger bigInt = (aaa[0] == null) ? new BigInteger(ZERO)
                        : new BigInteger(aaa[0].toString());
                profileStat.setProfileSmallestSize(bigInt);

                bigInt = (aaa[1] == null) ? new BigInteger(ZERO)
                        : new BigInteger(aaa[1].toString());
                profileStat.setProfileLargestSize(bigInt);

                BigDecimal bigDec = (aaa[2] == null) ? new BigDecimal(ZERO)
                        : new BigDecimal(aaa[2].toString());
                profileStat.setProfileMeanSize(bigDec);

                bigInt = (aaa[THREE] == null) ? new BigInteger(ZERO)
                        : new BigInteger(aaa[THREE].toString());
                profileStat.setProfileTotalSize(bigInt);

            }
        }

        setTotalUnreadableFiles(profileStat);
        setTotalUnreadableFolders(profileStat);
        setTotalReadableFiles(profileStat);

        return profileStat;
    }

    /**
     * @param profileStat
     */
    private void setTotalReadableFiles(ProfileStat profileStat) {
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
    }

    /**
     * @param profileStat
     */
    private void setTotalUnreadableFolders(ProfileStat profileStat) {
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
    }

    /**
     * @param profileStat
     */
    private void setTotalUnreadableFiles(ProfileStat profileStat) {
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
    }

    private List<GroupByYearSizeAndCountRow> getGroupByYear() {

        List<GroupByYearSizeAndCountRow> dataList = new ArrayList<GroupByYearSizeAndCountRow>();
        GroupByYearSizeAndCountRow groupByYearSizeAndCountRow = null;

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
        return dataList;
    }

    private List<GroupByPuidSizeAndCountRow> getGroupByPuid() {
        List<GroupByPuidSizeAndCountRow> dataList = new ArrayList<GroupByPuidSizeAndCountRow>();
        GroupByPuidSizeAndCountRow groupByPuidSizeAndCountRow = null;

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

        return dataList;
    }

}
