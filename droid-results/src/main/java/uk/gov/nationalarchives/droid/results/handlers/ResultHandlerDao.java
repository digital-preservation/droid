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
package uk.gov.nationalarchives.droid.results.handlers;

import java.util.List;
import java.util.Map;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * Operations for persistence.
 * @author rflitcroft
 *
 */
public interface ResultHandlerDao {

    /**
     * Do anything required to start up the result handler.
     */
    void init();

    /**
     * Saves a new identification to the database.
     *
     * @param node the result to save
     * @param parentId the node's parent ID
     */
    void save(ProfileResourceNode node, ResourceId parentId);

    /**
     * Ensure that all results so far are committed.
     * <p>
     * Some implementations may commit each result as they are saved, in which case this will do nothing.
     * Other implementations may batch up results to improve performance.  In the event that a profile is paused
     * or finishes naturally, commit() should be called to flush out any un-committed results to the database.
     */
    void commit();

    /**.
     * Loads a Format. 
     * @param puid - the unique id of the format
     * @return the format.
     */
    Format loadFormat(String puid);

    /**
     * Gets a list of all formats which can be recognised.
     *
     * @return a list of all formats.
     */
    List<Format> getAllFormats();

    /**
     * Gets a map of all formats against their PUID values,
     * allowing the fast look up of a Format object from the PUID.
     *
     * @return A map of all formats against their PUID values.
     */
    Map<String, Format> getPUIDFormatMap();

    /**
     * @param nodeId the Id of the node to load
     * @return the refernce to the node
     */
    ProfileResourceNode loadNode(Long nodeId);

    /**
     * Deletes a node and all its children.
     * @param nodeId the noe to remove
     */
    void deleteNode(Long nodeId);

    /**
     * BNO: Added for new method in JDBCBatchResulthandlerDao, for customising behaviour for new vs existing
     * installations.  Haven't previously published this interface via an API etc. ASAIK so shouldn't break anything...
     */
    void initialiseForNewTemplate();

}
