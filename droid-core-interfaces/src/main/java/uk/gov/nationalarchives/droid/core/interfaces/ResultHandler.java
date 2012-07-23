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
package uk.gov.nationalarchives.droid.core.interfaces;






/**
 * Handles identification results.
 * @author rflitcroft
 *
 */
public interface ResultHandler {

    /**
     * Invoked whenever DROID completes an identification.
     * @param result the result of the identification.
     * @return the alloacted ID of the handled result.
     */
    ResourceId handle(IdentificationResultCollection result);

    /**
     * Invoked whenever DROID completes an identification.
     * @param result the result of the identification.
     * @param parentId the dirtectory's parent's ID
     * @param restricted if access to directory was restricted
     * @return the alloacted ID of the handled result.
     */
    ResourceId handleDirectory(IdentificationResult result, ResourceId parentId, boolean restricted);

    /**
     * Invoked whenever DROID cannot complete an identification.
     * @param e the error which prevented the job from completing
     */
    void handleError(IdentificationException e);


    /**
     * Deletes a node an all its children.
     * @param nodeId the node to delete.
     */
    void deleteCascade(Long nodeId);
    
    
    /**
     * Commits any uncommitted results.
     */
    void commit();
    
    
    /**
     * Ensures that the result handler is initialised properly.
     */
    void init();

}
