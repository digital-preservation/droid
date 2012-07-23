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
package uk.gov.nationalarchives.droid.results.handlers;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ResourceUtils;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * @author rflitcroft
 * 
 */
public class JpaResultHandlerDao implements ResultHandlerDao {
 
    private final Log log = LogFactory.getLog(getClass());
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    // Making this explicitly transactional is unnecessary, 
    // as it is wrapped in a transaction when called from 
    // the resulthandlerimpl.  All this does is force two 
    // commits for the same resource node.
    //@Transactional(propagation = Propagation.MANDATORY)
    public void save(ProfileResourceNode node, ResourceId parentId) {

        entityManager.persist(node);
        Long nodeId = node.getId();
        String nodePrefix = ResourceUtils.getBase128Integer(nodeId);
        String nodePrefixPlusOne = ResourceUtils.getBase128Integer(nodeId + 1);

        String parentsPrefixString = "";
        if (parentId != null) {
            parentsPrefixString = parentId.getPath();
            node.setParentId(parentId.getId());
        }

        node.setPrefix(parentsPrefixString + nodePrefix);
        node.setPrefixPlusOne(parentsPrefixString + nodePrefixPlusOne);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Format loadFormat(String puid) {
        return entityManager.find(Format.class, puid);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ProfileResourceNode loadNode(Long nodeId) {
        return entityManager.find(ProfileResourceNode.class, nodeId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteNode(Long nodeId) {
        ProfileResourceNode node = entityManager.getReference(ProfileResourceNode.class, nodeId);
        log.debug(String.format("Deleting Node [%s]", node.getUri()));
        
        String nodesToRemove = "from ProfileResourceNode n " 
                + " where n.prefix >= ? " 
                + " and n.prefix < ? ";
        
        Query q = entityManager.createQuery(nodesToRemove);
        q.setParameter(1, node.getPrefix());
        q.setParameter(2, node.getPrefixPlusOne());
        for (Object o : q.getResultList()) {
            entityManager.remove(o);
        }
       
    }

}
