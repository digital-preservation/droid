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
package uk.gov.nationalarchives.droid.command.container;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.*;
import uk.gov.nationalarchives.droid.container.ole2.Ole2IdentifierEngine;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 *
 * @author rbrennan
 */
public class Ole2ContainerContentIdentifierTest {
    
    private Ole2ContainerContentIdentifier ole2ContainerContentIdentifier;
    private ContainerFileIdentificationRequestFactory requestFactory;
    private Ole2IdentifierEngine ole2IdentifierEngine;
    private ContainerIdentifierInit containerIdentifierInit;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private Map<Integer, List<FileFormatMapping>> formats; 
    private Path containerSignatures =
            Paths.get("../droid-container/workDir/container-sigs/container-signature.xml");
    private String ole2File =
            "../droid-container/src/test/resources/word97.doc";
    
    @Before
    public void setUp() {
        ole2ContainerContentIdentifier = new Ole2ContainerContentIdentifier();
        requestFactory = new ContainerFileIdentificationRequestFactory();
        ole2IdentifierEngine = new Ole2IdentifierEngine();
        containerIdentifierInit = new ContainerIdentifierInit();
        formats = new HashMap<>();
    }
    
    @After
    public void tearDown() {
        ole2ContainerContentIdentifier = null;
        requestFactory = null;
        ole2IdentifierEngine = null;
        containerIdentifierInit = null;
        formats = null;
    }
    
    @Test
    public void identifyOle2FileTest() throws CommandExecutionException {
        ole2IdentifierEngine.setRequestFactory(requestFactory);
        ole2ContainerContentIdentifier.setIdentifierEngine(ole2IdentifierEngine);
        Object object = ole2ContainerContentIdentifier.getIdentifierEngine();
        assertEquals(object.getClass(), ole2IdentifierEngine.getClass());
        
        try (final InputStream in = Files.newInputStream(containerSignatures);) {
            ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
            containerSignatureDefinitions = parser.parse(in);
        } catch (SignatureParseException e) {
            throw new CommandExecutionException ("Can't parse container signature file");
        } catch (Exception e) {
            throw new CommandExecutionException(e);
        }
        
        ole2ContainerContentIdentifier.init(containerSignatureDefinitions, "OLE2");
        object = ole2ContainerContentIdentifier.getContainerIdentifierInit();
        assertEquals(object.getClass(), containerIdentifierInit.getClass());
        
        object = ole2ContainerContentIdentifier.getFormats();
        assertEquals(object.getClass(), formats.getClass());

        String fileName;
        final Path file = Paths.get(ole2File);
        if (!Files.exists(file)) {
            fail("OLE2 test file not found");
        }
        URI uri = file.toUri();
        RequestIdentifier identifier = new RequestIdentifier(uri);
        identifier.setParentId(1L);
        try {
            fileName = file.toAbsolutePath().toString();
            RequestMetaData metaData = new RequestMetaData(
                    Files.size(file), Files.getLastModifiedTime(file).toMillis(), fileName);
            FileSystemIdentificationRequest request =
                new FileSystemIdentificationRequest(metaData, identifier);
            IdentificationResultCollection results =
                new IdentificationResultCollection(request);
            request.open(file);
            results = ole2ContainerContentIdentifier.process(request.getSourceInputStream(), results);
            if (results.getResults().isEmpty()) {
                fail("OLE2 file not identified");
            }
            String fmtExpected = "";
            for (IdentificationResult identResult : results.getResults()) {
                if (identResult.getPuid().equals("fmt/40")) {
                    fmtExpected = identResult.getPuid();
                }
            }
            assertEquals(fmtExpected, "fmt/40");
        } catch (IOException e) {
            throw new CommandExecutionException(e);
        }
    }
}
