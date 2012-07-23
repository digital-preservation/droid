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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.QueryBuilder;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * JPA implementation of ProfileDao.
 * 
 * @author rflitcroft
 */
public class JpaProfileDaoImpl implements ProfileDao {

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
    @SuppressWarnings("unchecked")
    @Override
    public List<Format> getAllFormats() {
        String query = "from Format order by name";
        Query q = entityManager.createQuery(query);
        return q.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveFormat(Format format) {
        entityManager.persist(format);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    //@Transactional(propagation = Propagation.REQUIRED)
    public List<ProfileResourceNode> findProfileResourceNodes(Long parentId) {

        //String query = "select n from ProfileResourceNode n"
        //        + " where n.parent.id  " + (parentId == null ? " is null" : " = ? ");

        String query = "select * from profile_resource_node n"
            + " where n.parent_id " + getParentIdQuery(parentId);
        
        //log.debug("query = " + query);

        //Query q = entityManager.createQuery(query);
        Query q = entityManager.createNativeQuery(query, ProfileResourceNode.class);
        
        //log.debug("parent Id  = " + parentId);
        if (parentId != null) {
            q.setParameter(1, parentId);
        }

        //long start = System.currentTimeMillis();
        List<ProfileResourceNode> results = q.getResultList();
        //log.debug("Query time (ms) =  " + (System.currentTimeMillis() - start));

        return results;
    }


    /**
     * {@inheritDoc}
     */
    
    @Override
    @SuppressWarnings("unchecked")
    //@Transactional(propagation = Propagation.REQUIRED)
    public List<ProfileResourceNode> findProfileResourceNodes(Long parentId,
            Filter filter) {
        QueryBuilder queryBuilder = SqlUtils.getQueryBuilder(filter);
        String ejbFilter = queryBuilder.toEjbQl();
        String query = getSQLQueryString(ejbFilter, parentId);
        Query q = entityManager.createNativeQuery(query, "ResourceNodeWithFilterStatus");
        
        // Set the parameters:
        int i = 1;
        Object[] values = queryBuilder.getValues();
        for (int j = 0; j < values.length; j++) {
            Object value = values[j];
            
            // FIXME: ugly hack to transform certain enumerations back into
            // their ordinal values.  For some reason, if putting the enum
            // into the parameter list directly, some work, and some die with
            // an exception trying to set SQLInteger to a byte[] in the derby
            // prepared statement.  Wierdly, NodeStatus and PUID work, but the
            // other enumerations don't.
            value = SqlUtils.transformParameterToSQLValue(value);
            q.setParameter(i++, value);
        }
        for (int j = 0; j < values.length; j++) {
            Object value = values[j];
            
            // FIXME: ugly hack to transform certain enumerations back into
            // their ordinal values.  For some reason, if putting the enum
            // into the parameter list directly, some work, and some die with
            // an exception trying to set SQLInteger to a byte[] in the derby
            // prepared statement.  Wierdly, NodeStatus and PUID work, but the
            // other enumerations don't.
            value = SqlUtils.transformParameterToSQLValue(value);
            q.setParameter(i++, value);
        }
        if (parentId != null) {
            q.setParameter(i++, parentId);
        }
        
        // Get the results:
        long start = System.currentTimeMillis();
        List<Object[]> queryResults = q.getResultList();
        log.debug("Query time (ms) = " + (System.currentTimeMillis() - start));

        // get the profile resource nodes and set their filter status:
        List<ProfileResourceNode> result = new ArrayList<ProfileResourceNode>();
        for (Object[] objects : queryResults) {
            Integer status = (Integer) objects[1];
            if (status > 0) {
                ProfileResourceNode node = (ProfileResourceNode) objects[0];
                node.setFilterStatus(status);
                result.add(node);
            }
        }

        return result;
    }    

   
    private String getSQLQueryString(final String ejbFilter, final Long parentId) {
        boolean formatCriteriaExist = formatCriteriaExist(ejbFilter);
        boolean formatMetadataExist = formatCriteriaExist ? formatMetadataExist(ejbFilter) : false;
        String filterCriteriaDirect = SqlUtils.transformEJBtoSQLFields(ejbFilter,
                "profile", "form");
        String filterCriteriaChild = SqlUtils.transformEJBtoSQLFields(ejbFilter,
                "children", "child_form");
        
        String query = formatCriteriaExist ? "select distinct profile.*," : "select profile.*,";
        query += " case when (" + filterCriteriaDirect + ") then 1"
            + " else case when profile.resource_type <> 2 and exists ("
            + " select children.node_id from profile_resource_node as children";
        
        if (formatCriteriaExist) { // if we have filter conditions on the file formats:
            if (formatMetadataExist) { // join to the format table through identifications.
                query = query + " inner join identification as child_ident on child_ident.node_id = children.node_id"
                    + " inner join format as child_form on child_form.puid = child_ident.puid";
            } else { // a puid-only format query only needs to join to identifications.
                query = query + " inner join identification as child_form on child_form.node_id = children.node_id";
            }
        }

        query = query + " where children.prefix > profile.prefix and children.prefix < profile.prefix_plus_one"
            + " and (" + filterCriteriaChild + ")) then 2 else 0 end end as FilterStatus"
            + " from profile_resource_node as profile";

        if (formatCriteriaExist) { // if we have filter conditions on the file formats:
            if (formatMetadataExist) { // join to the format table through identifications.
                query = query + " inner join identification as ident on ident.node_id = profile.node_id"
                    + " inner join format as form on form.puid = ident.puid";
            } else { // // a puid-only format query only needs to join to identifications.
                query = query + " inner join identification as form on form.node_id = profile.node_id";
            }
        }
            
        return query + " where profile.parent_id " + getParentIdQuery(parentId);
    }
    
    private boolean formatCriteriaExist(final String filter) {
        return filter.contains("format.");
    }

    private boolean formatMetadataExist(final String filter) {
        return filter.contains("format.mimeType") || filter.contains("format.name");
    }
    
    
    private String getParentIdQuery(Long parentId) {
        return parentId == null ? "is null" : " = ?";
    }
    
}
