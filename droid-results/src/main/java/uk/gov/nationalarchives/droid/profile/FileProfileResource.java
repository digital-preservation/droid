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

import java.io.File;
import java.util.Date;


import javax.xml.bind.annotation.XmlRootElement;

//import org.apache.commons.io.FilenameUtils;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ResourceUtils;

/**
 * Defines a specification for a profile. This can be a single file, a
 * directory, a recursed directory or a URL.
 * 
 * @author rflitcroft
 * 
 */
@XmlRootElement(name = "File")
public class FileProfileResource extends AbstractProfileResource {

    /**
     * Default Constructor.
     */
    FileProfileResource() {
    }

    /**
     * 
     * @param file
     *            the file to represent.
     */
    public FileProfileResource(File file) {
        setUri(file.toURI());
        setName(file.getName());
        setSize(file.length());
        setLastModifiedDate(new Date(file.lastModified()));
        //setExtension(FilenameUtils.getExtension(file.getName()));
        setExtension(ResourceUtils.getExtension(file.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        return false;
    }
}
