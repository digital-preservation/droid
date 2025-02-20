/*
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
package uk.gov.nationalarchives.droid.container;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author rflitcroft
 *
 */
@XmlRootElement(name = "ContainerSignatureMapping")
public class ContainerSignatureDefinitions {

    @XmlElementWrapper(name = "ContainerSignatures")
    @XmlElement(name = "ContainerSignature")
    private List<ContainerSignature> containerSignatures;
    
    @XmlElementWrapper(name = "FileFormatMappings")
    @XmlElement(name = "FileFormatMapping")
    private List<FileFormatMapping> formats;

    @XmlElementWrapper(name = "TriggerPuids")
    @XmlElement(name = "TriggerPuid")
    private List<TriggerPuid> tiggerPuids;
    
    /**
     * @return the containerSignatures
     */
    public List<ContainerSignature> getContainerSignatures() {
        return containerSignatures;
    }
    
    /**
     * @return the formats
     */
    public List<FileFormatMapping> getFormats() {
        return formats;
    }
    
    /**
     * @return the tiggerPuid
     */
    public List<TriggerPuid> getTiggerPuids() {
        return tiggerPuids;
    }
    
}
