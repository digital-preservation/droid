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
package uk.gov.nationalarchives.droid.command;

import java.io.IOException;
import java.util.List;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.command.archive.GZipArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.TarArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.ZipArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.container.Ole2ContainerContentIdentifier;
import uk.gov.nationalarchives.droid.command.container.ZipContainerContentIdentifier;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequestFactory;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.TriggerPuid;
import uk.gov.nationalarchives.droid.container.ole2.Ole2IdentifierEngine;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifierEngine;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;

/**
 * File identification results printer
 * 
 * @author rbrennan
 */
public class ResultPrinter {
    
    private BinarySignatureIdentifier binarySignatureIdentifier;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private List<TriggerPuid> triggerPuids;
    private IdentificationRequestFactory requestFactory;
    private String path;
    private String slash;
    private boolean processArchives = false; // next release
    
    /*
     * Store signature files
     */
    public ResultPrinter(BinarySignatureIdentifier binarySignatureIdentifier,
            ContainerSignatureDefinitions containerSignatureDefinitions,
            String path, String slash)
            throws CommandExecutionException {
    
        this.binarySignatureIdentifier = binarySignatureIdentifier;
        this.containerSignatureDefinitions = containerSignatureDefinitions;
        this.path = path;
        this.slash = slash;
        if (containerSignatureDefinitions != null) {
            triggerPuids = containerSignatureDefinitions.getTiggerPuids();
        }
    }
    
    /*
     * Attempt to identify this file
     */
    public void print(IdentificationResultCollection results, IdentificationRequest request)
            throws CommandExecutionException {
        try {
            IdentificationResultCollection containerResults =
                new IdentificationResultCollection(request);
                
            if (results.getResults().size() > 0 && containerSignatureDefinitions != null) {
                for (IdentificationResult identResult : results.getResults()) {
                    String filePuid = identResult.getPuid();
                    if (filePuid != null) {
                        TriggerPuid containerPuid = getTriggerPuidByPuid(filePuid);
                        if (containerPuid != null) {
                            requestFactory = new ContainerFileIdentificationRequestFactory();
                            String containerType = containerPuid.getContainerType();

                            if (containerType.equals("OLE2")) {
                                Ole2ContainerContentIdentifier ole2Identifier = new Ole2ContainerContentIdentifier();
                                ole2Identifier.init(containerSignatureDefinitions, containerType);
                                Ole2IdentifierEngine ole2IdentifierEngine = new Ole2IdentifierEngine();
                                ole2IdentifierEngine.setRequestFactory(requestFactory);
                                ole2Identifier.setIdentifierEngine(ole2IdentifierEngine);
                                containerResults =
                                    ole2Identifier.process(request.getSourceInputStream(), containerResults);

                            } else if (containerType.equals("ZIP")) {
                                ZipContainerContentIdentifier zipIdentifier = new ZipContainerContentIdentifier();
                                zipIdentifier.init(containerSignatureDefinitions, containerType);
                                ZipIdentifierEngine zipIdentifierEngine = new ZipIdentifierEngine();
                                zipIdentifierEngine.setRequestFactory(requestFactory);
                                zipIdentifier.setIdentifierEngine(zipIdentifierEngine);
                                containerResults =
                                    zipIdentifier.process(request.getSourceInputStream(), containerResults);

                            } else {
                                throw new CommandExecutionException("Unknown container type: " + containerPuid);
                            }
                        }
                    }
                }
            }
            IdentificationResultCollection finalResults = new IdentificationResultCollection(request);
            if (containerResults.getResults().size() > 0) {
                finalResults = containerResults;
            } else if (results.getResults().size() > 0) {
                finalResults = results;
            }
            if (finalResults.getResults().size() > 0) {
                binarySignatureIdentifier.removeLowerPriorityHits(finalResults);
            }
            String fileName = path + request.getFileName();
            if (finalResults.getResults().size() > 0) {
                for (IdentificationResult identResult : finalResults.getResults()) {
                    String puid = identResult.getPuid();
                    System.out.println(fileName + "," + puid);
                    if (processArchives) {
                        if (puid.equals("x-fmt/266")) {
                            GZipArchiveContentIdentifier gzipArchiveIdentifier = 
                                new GZipArchiveContentIdentifier(binarySignatureIdentifier,
                                    containerSignatureDefinitions, path, slash);
                            gzipArchiveIdentifier.identify(results.getUri(), request);
                        } else if (puid.equals("x-fmt/265")) {
                            TarArchiveContentIdentifier tarArchiveIdentifier = 
                                new TarArchiveContentIdentifier(binarySignatureIdentifier,
                                    containerSignatureDefinitions, path, slash);
                            tarArchiveIdentifier.identify(results.getUri(), request);
                        } else if (puid.equals("x-fmt/263") || puid.equals("x-fmt/412")) {
                            ZipArchiveContentIdentifier zipArchiveIdentifier = 
                                new ZipArchiveContentIdentifier(binarySignatureIdentifier,
                                    containerSignatureDefinitions, path, slash);
                            zipArchiveIdentifier.identify(results.getUri(), request);
                        }
                    }
                }   
            } else {
                System.out.println(fileName + ",Unknown");
            }
        } catch (IOException e) {
            throw new CommandExecutionException(e);
        }
    }
    
    private TriggerPuid getTriggerPuidByPuid(final String puid) {
        for (final TriggerPuid tp : triggerPuids) {
            if (tp.getPuid().equals(puid)) {
                return tp;
            }
        }
        return null;
    }
}
