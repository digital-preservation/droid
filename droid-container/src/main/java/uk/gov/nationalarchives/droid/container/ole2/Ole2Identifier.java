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
package uk.gov.nationalarchives.droid.container.ole2;

import java.io.IOException;
import java.nio.file.Path;

import uk.gov.nationalarchives.droid.container.AbstractContainerIdentifier;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequestFactory;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.container.IdentifierEngine;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;

/**
 * @author rflitcroft
 *
 */
public class Ole2Identifier extends AbstractContainerIdentifier {

    /**
     * Constructs an Ole2Identifier.
     * Will default to creating temporary files in the default system location.
     */
    public Ole2Identifier() {
        this(null);
    }

    /**
     * Constructs an Ole2Identifier with the location where temporary files should be created during processing.
     * @param tempFileDir The location where temporary files should be created.  If null defaults to default system.
     */
    public Ole2Identifier(Path tempFileDir) {
        IdentifierEngine ole2Engine = new Ole2IdentifierEngine();
        ole2Engine.setRequestFactory(new ContainerFileIdentificationRequestFactory(tempFileDir));
        setIdentifierEngine(ole2Engine);
    }

    @Override
    public final void process(IdentificationRequest request,
                              ContainerSignatureMatchCollection matches) throws IOException {
        getIdentifierEngine().process(request, matches);
    }
}
