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
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.File;
import java.net.URI;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
/**
 * @author rflitcroft
 * @Author: Brian O'Reilly
 * Attempting to incorporate RequestIdentifier into this class to avoid passing around
 * an instance of each that may or may not relate to the same resource, when consumers
 * clearly expect that they do...
 * TODO: Decide on the best home for this - shouldn't really be in "interfaces"
 */
public class RequestMetaData2 extends RequestMetaData {

    private String hash;
    // TODO: Probably simplest just to reuse the existing class as a member...
    // Possibly make it an inner class??
    private RequestIdentifier identifier;
    
    /*
     *         URI uri = file.toURI();
        RequestMetaData metaData = new RequestMetaData(file.length(), file
                .lastModified(), file.getName());
     */

    public RequestMetaData2(File file) {
    	super(file.length(), file.lastModified(), file.getName()); 
    	URI uri = file.toURI();
    	this.setIdentifier(new RequestIdentifier(uri));
    }

	public RequestIdentifier getIdentifier() {
		return identifier;
	}

	private void setIdentifier(RequestIdentifier identifier) {
		this.identifier = identifier;
	}
   
    public void setParentResourceId(ResourceId id) {
        this.identifier.setParentResourceId(id);
    }
    
    public void setResourceId(ResourceId id) {
    	this.identifier.setResourceId(id);
    }
}
