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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationErrorType;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.NodeStatus;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ResourceUtils;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * @author matt
 *
 */
public class BatchResultHandler implements ResultHandler {

    /**
     * 
     */
    private static final int DEFAULT_BATCH_SIZE = 50;

    private Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;
    
    private EntityManagerFactory entityManagerFactory;
    
    private ProgressMonitor progressMonitor;

    private AtomicLong nodeIdValue;
    private List<ProfileResourceNode> batchedNodes;
    private int batchSize = DEFAULT_BATCH_SIZE;
    
    /**
     * Constructs a batch result handler.
     */
    public BatchResultHandler() {
        //Long maxNodeId = getMaxNodeId();
        //nodeIdValue = new AtomicLong(maxNodeId + 1);
        batchedNodes = Collections.synchronizedList(new ArrayList<ProfileResourceNode>());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        Long maxNodeId = getMaxNodeId();
        nodeIdValue = maxNodeId == null ? new AtomicLong(0L) : new AtomicLong(maxNodeId + 1);        
    }    
    
    
    @Transactional
    private Long getMaxNodeId() {
        String maxNodeQuery = "select max(node_id) from profile_resource_node";
        Query q = entityManager.createNativeQuery(maxNodeQuery);
        Object result = q.getSingleResult();
        return (Long) result;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceId handle(IdentificationResultCollection results) {
        
        //log.debug(String.format("handling result for job [%s]", results.getUri()));
        
        ProfileResourceNode node = new ProfileResourceNode(results.getUri());
        
        RequestMetaData requestMetaData = results.getRequestMetaData();
        
        NodeMetaData metaData = new NodeMetaData();
        metaData.setLastModified(requestMetaData.getTime());
        metaData.setSize(results.getFileLength());
        metaData.setName(requestMetaData.getName());
        metaData.setExtension(ResourceUtils.getExtension(requestMetaData.getName()));
        metaData.setResourceType(results.isArchive() ? ResourceType.CONTAINER : ResourceType.FILE);
        metaData.setHash(requestMetaData.getHash());
        
        metaData.setNodeStatus(NodeStatus.DONE);

        node.setMetaData(metaData);
        node.setExtensionMismatch(results.getExtensionMismatch());
        node.setFinished(new Date());
        ResourceId parentId = results.getCorrelationId();
        setNodeIds(node, parentId);
        if (results.getResults().isEmpty()) {
            node.addFormatIdentification(Format.NULL);
            node.setZeroIdentifications();
        } else {
            for (IdentificationResult result : results.getResults()) {
                node.getMetaData().setIdentificationMethod(result.getMethod());
                //log.debug(String.format("Handling ID puid[%s]; uri[%s]", result.getPuid(), results.getUri()));
                Format format = loadFormat(result.getPuid());
                node.addFormatIdentification(format);
            }
        }
        progressMonitor.stopJob(node);
        batchNode(node);
        return new ResourceId(node.getId(), node.getPrefix());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceId handleDirectory(IdentificationResult result, ResourceId parentId, boolean restricted) {
        final URI uri = result.getIdentifier().getUri();
        //log.debug(String.format("handling directory [%s]", uri));
        ProfileResourceNode node = new ProfileResourceNode(uri);

        RequestMetaData requestMetaData = result.getMetaData();
        
        NodeMetaData metaData = new NodeMetaData();
        metaData.setName(requestMetaData.getName());
        metaData.setSize(null);
        metaData.setLastModified(requestMetaData.getTime());
        metaData.setIdentificationMethod(IdentificationMethod.NULL);
        metaData.setNodeStatus(restricted ? NodeStatus.ACCESS_DENIED : NodeStatus.DONE);
        metaData.setResourceType(ResourceType.FOLDER);
        node.setMetaData(metaData);
        setNodeIds(node, parentId);
        node.setFinished(new Date());
        node.addFormatIdentification(Format.NULL);

        progressMonitor.stopJob(node);
        batchNode(node);
        return new ResourceId(node.getId(), node.getPrefix());
    }

    
    private NodeStatus getNodeStatus(IdentificationErrorType error) {
        NodeStatus status;
        switch(error) {
            case ACCESS_DENIED:
                status = NodeStatus.ACCESS_DENIED;
                break;
            case FILE_NOT_FOUND:
                status = NodeStatus.NOT_FOUND;
                break;
            default:
                status = NodeStatus.ERROR;
        }
        return status;
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void handleError(IdentificationException e) {
        final IdentificationRequest request = e.getRequest();
        final RequestIdentifier identifier = request.getIdentifier();
        URI uri = identifier.getUri();
        //log.debug(String.format("handling error for job [%s]", uri));
        
        final Long nodeId = identifier.getNodeId();
        ProfileResourceNode node;
        if (nodeId != null) {
            node = loadNode(nodeId);
            node.getMetaData().setNodeStatus(NodeStatus.ERROR);
            // Need to initialise the collection eagerly...
            node.getFormatIdentifications().size();
        } else {
            node = new ProfileResourceNode(uri);
            node.setFinished(new Date());
            final NodeMetaData metaData = node.getMetaData();
            
            metaData.setNodeStatus(getNodeStatus(e.getErrorType()));
            metaData.setResourceType(ResourceType.FILE);
            node.setNoFormatsIdentified();
            
            RequestMetaData requestMetaData = request.getRequestMetaData();
            
            metaData.setName(requestMetaData.getName());
            metaData.setSize(requestMetaData.getSize());
            metaData.setExtension(request.getExtension());
            metaData.setLastModified(request.getRequestMetaData().getTime());
            metaData.setHash(requestMetaData.getHash());
            
            node.addFormatIdentification(Format.NULL);
            setNodeIds(node, identifier.getParentResourceId());
        }
        batchNode(node);
        progressMonitor.stopJob(node);
    }

    
    private void setNodeIds(ProfileResourceNode node, ResourceId parentId) {
        node.setId(nodeIdValue.getAndIncrement());
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
    
    private void batchNode(ProfileResourceNode node) {
        //entityManager.persist(node);
        batchedNodes.add(node);
        if (nodeIdValue.get() % batchSize == 0) {
            commit();
        }
    }
    
    /**
     * 
     * @param nodeId the id of the node to load.
     * @return The loaded node.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public ProfileResourceNode loadNode(Long nodeId) {
        return entityManager.find(ProfileResourceNode.class, nodeId);
    }
    
    /**
     * 
     * @param puid The puid of the format to load.
     * @return The format
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Format loadFormat(String puid) {
        return entityManager.find(Format.class, puid);
    }    
    
  
    /**
     * @param progressMonitor
     *            the progressMonitor to set
     */
    public void setProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }
    

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void deleteCascade(Long nodeId) {
        deleteNode(nodeId);
    }
    
    /**
     * 
     * @param factory the factory to use to create entity managers.
     */
    public void setEntityManagerFactory(EntityManagerFactory factory) {
        this.entityManagerFactory = factory;
    }
    
    /**
     * 
     * @param nodeId The id of the node to delete.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteNode(Long nodeId) {
        ProfileResourceNode node = entityManager.getReference(ProfileResourceNode.class, nodeId);
        log.warn(String.format("Deleting Node [%s]", node.getUri()));
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
            
    
    
    /**
     * Commits any batched-up nodes to the database using extended entity manager.
     */
    /*
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public synchronized void commit() {
        try {
            try {
                transaction.begin();
            } catch (NotSupportedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SystemException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            entityManager.joinTransaction();
            try {
                transaction.commit();
            } catch (RollbackException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (HeuristicMixedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (HeuristicRollbackException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SystemException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        //CHECKSTYLE:OFF
        } catch (RuntimeException ex) {
            log.error(ex);
        }
        //CHECKSTYLE:ON
    }
    */
    
    /**
     * Commits any batched-up nodes to the database using local entity manager.
     * 
     * We use a local entity manager in a synchronized method, 
     * because using the @Transactional annotation seems to produce threading issues.
     * This method can be called by several threads at the same time.
     * 
     * It is unclear why this method isn't amenable to using the annotation, as all the
     * previous implementations, including ResultHandlerImpl used the annotation without
     * issue.  This produces a slightly sub-optimal result, as threads get blocked
     * when calling the commit method.  However, the net result of batching up the 
     * database inserts is about a third faster than not doing so.
     * 
     */
    @Override
    public synchronized void commit() {
        try {
            EntityManager em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            synchronized (batchedNodes) {
                for (ProfileResourceNode node : batchedNodes) {
                    em.persist(node);
                }
                batchedNodes.clear();
            }
            em.getTransaction().commit();
            em.close();
        //CHECKSTYLE:OFF
        } catch (RuntimeException ex) {
            log.error(ex);
        }
        //CHECKSTYLE:ON
    }

}

