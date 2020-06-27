/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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

import java.nio.file.Path;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author rflitcroft
 * 
 */
@XmlRootElement(name = "Dir")
public class DirectoryProfileResource extends FileProfileResource {

    @XmlAttribute(name = "Recursive")
    private boolean recursive;

    /**
     * Default Constructor.
     */
    DirectoryProfileResource() {
    }

    /**
     * 
     * @param file
     *            the directory to represent.
     * @param recursive
     *            whether the resource should recurse into subdirectorie
     */
    public DirectoryProfileResource(final Path file, boolean recursive) {
        super(file);
        this.recursive = recursive;
    }

    /**
     * @return the recursive
     */
    @Override
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        return true;
    }

    /**
     * setting an arbitrary size of 0 for the folder resource.
     * @param filePath - ignored for the folder
     */
    @Override
    public void setSize(Path filePath) {
        setSize(-1L);
    }
}
