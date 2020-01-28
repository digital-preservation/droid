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
package uk.gov.nationalarchives.droid.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.command.archive.ArchiveConfiguration;
import uk.gov.nationalarchives.droid.command.archive.Bzip2ArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.GZipArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.IsoArchiveContainerIdentifier;
import uk.gov.nationalarchives.droid.command.archive.RarArchiveContainerIdentifier;
import uk.gov.nationalarchives.droid.command.archive.SevenZipArchiveContainerIdentifier;
import uk.gov.nationalarchives.droid.command.archive.ZipArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.TarArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.ArcArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.WarcArchiveContentIdentifier;
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
 * File identification results printer.
 *
 * NB: This class is called recursively when archive files are opened 
 * 
 * @author rbrennan
 */
public class ResultPrinter {

    private static final String R_SLASH = "/";
    private static final String DOUBLE_SLASH = "\\";
    private static final String L_BRACKET = "(";
    private static final String R_BRACKET = ")";
    private static final String SPACE = " ";

    private static final String OLE2_CONTAINER = "OLE2";
    private static final String ZIP = "ZIP";
    private static final String ZIP_ARCHIVE = "x-fmt/263";
    private static final String RAR_ARCHIVE = "x-fmt/264";
    private static final String RAR_ARCHIVE_OTHER = "fmt/411";
    private static final String TAR_ARCHIVE = "x-fmt/265";
    private static final String GZIP_ARCHIVE = "x-fmt/266";
    private static final String ARC_ARCHIVE = "x-fmt/219";
    private static final String ARC_ARCHIVE_OTHER = "fmt/410";
    private static final String WARC_ARCHIVE = "fmt/289";
    private static final String ISO_9660 = "fmt/468";
    private static final String SEVEN_ZIP = "fmt/484";
    private static final String BZIP2_ARCHIVE = "x-fmt/267";
    private static final String BZIP2_ARCHIVE_OTHER = "x-fmt/268";


    private BinarySignatureIdentifier binarySignatureIdentifier;
    private ContainerSignatureDefinitions containerSignatureDefinitions;
    private List<TriggerPuid> triggerPuids;
    private IdentificationRequestFactory requestFactory;
    private String path;
    private String slash;
    private String slash1;
    private String wrongSlash;
    private ArchiveConfiguration archiveConfiguration;

    /**
     * Store signature files.
     *  @param binarySignatureIdentifier     binary signature identifier
     * @param containerSignatureDefinitions container signatures
     * @param path                          current file/container path
     * @param slash                         local path element delimiter
     * @param slash1                        local first container prefix delimiter
     * @param archiveConfiguration          configuration to expand archives and web archives
     */
    public ResultPrinter(final BinarySignatureIdentifier binarySignatureIdentifier,
                         final ContainerSignatureDefinitions containerSignatureDefinitions,
                         final String path, final String slash, final String slash1, ArchiveConfiguration archiveConfiguration) {

        this.binarySignatureIdentifier = binarySignatureIdentifier;
        this.containerSignatureDefinitions = containerSignatureDefinitions;
        this.path = path;
        this.slash = slash;
        this.slash1 = slash1;
        this.wrongSlash = this.slash.equals(R_SLASH) ? DOUBLE_SLASH : R_SLASH;
        this.archiveConfiguration = archiveConfiguration;
        if (containerSignatureDefinitions != null) {
            triggerPuids = containerSignatureDefinitions.getTiggerPuids();
        }
    }

    /**
     * Output identification for this file.
     * 
     * @param results                       identification Results
     * @param request                       identification Request
     * 
     * @throws CommandExecutionException    if unexpected container type encountered
     */
    //CHECKSTYLE:OFF
    public void print(final IdentificationResultCollection results,
            final IdentificationRequest request) throws CommandExecutionException {
        
        final String fileName = (path + request.getFileName()).replace(wrongSlash, slash);
        final IdentificationResultCollection containerResults =
                getContainerResults(results, request, fileName);

        IdentificationResultCollection finalResults = new IdentificationResultCollection(request);
        boolean container = false;
        if (containerResults.getResults().size() > 0) {
            container = true;
            finalResults = containerResults;
        } else if (results.getResults().size() > 0) {
            finalResults = results;
        }
        if (finalResults.getResults().size() > 0) {
            binarySignatureIdentifier.removeLowerPriorityHits(finalResults);
        }
        if (finalResults.getResults().size() > 0) {
            for (IdentificationResult identResult : finalResults.getResults()) {
                String puid = identResult.getPuid();
                System.out.println(fileName + "," + puid);
                if (!container) {
                    switch (puid){
                        case GZIP_ARCHIVE:
                            if ((archiveConfiguration.getExpandAllArchives() || containsCaseInsensitive("GZIP", archiveConfiguration.getExpandArchiveTypes()))) {
                                GZipArchiveContentIdentifier gzipArchiveIdentifier =
                                        new GZipArchiveContentIdentifier(binarySignatureIdentifier,
                                                containerSignatureDefinitions, path, slash, slash1, archiveConfiguration);
                                gzipArchiveIdentifier.identify(results.getUri(), request);
                            }
                            break;
                        case TAR_ARCHIVE:
                            if ((archiveConfiguration.getExpandAllArchives() || containsCaseInsensitive("TAR", archiveConfiguration.getExpandArchiveTypes()))) {
                                TarArchiveContentIdentifier tarArchiveIdentifier =
                                        new TarArchiveContentIdentifier(binarySignatureIdentifier,
                                                containerSignatureDefinitions, path, slash, slash1, archiveConfiguration);
                                tarArchiveIdentifier.identify(results.getUri(), request);
                            }
                            break;
                        case ZIP_ARCHIVE:
                            if ((archiveConfiguration.getExpandAllArchives() || containsCaseInsensitive(ZIP, archiveConfiguration.getExpandArchiveTypes()))) {
                                ZipArchiveContentIdentifier zipArchiveIdentifier =
                                        new ZipArchiveContentIdentifier(binarySignatureIdentifier,
                                                containerSignatureDefinitions, path, slash, slash1, archiveConfiguration);
                                zipArchiveIdentifier.identify(results.getUri(), request);
                            }
                            break;
                        case ISO_9660:
                            if ((archiveConfiguration.getExpandAllArchives() || containsCaseInsensitive("ISO", archiveConfiguration.getExpandArchiveTypes()))) {
                                IsoArchiveContainerIdentifier isoArchiveContainerIdentifier =
                                        new IsoArchiveContainerIdentifier(binarySignatureIdentifier,
                                                containerSignatureDefinitions, path, slash, slash1, archiveConfiguration);
                                isoArchiveContainerIdentifier.identify(results.getUri(), request);
                            }
                            break;
                        case SEVEN_ZIP:
                            if ((archiveConfiguration.getExpandAllArchives() || containsCaseInsensitive("7ZIP", archiveConfiguration.getExpandArchiveTypes()))) {
                                SevenZipArchiveContainerIdentifier sevenZipArchiveContainerIdentifier =
                                        new SevenZipArchiveContainerIdentifier(binarySignatureIdentifier,
                                                containerSignatureDefinitions, path, slash, slash1, archiveConfiguration);
                                sevenZipArchiveContainerIdentifier.identify(results.getUri(), request);
                            }
                            break;
                        case BZIP2_ARCHIVE:
                        case BZIP2_ARCHIVE_OTHER:
                            if ((archiveConfiguration.getExpandAllArchives() || containsCaseInsensitive("BZIP2", archiveConfiguration.getExpandArchiveTypes()))) {
                                Bzip2ArchiveContentIdentifier bzip2ArchiveContentIdentifier =
                                        new Bzip2ArchiveContentIdentifier(binarySignatureIdentifier,
                                                containerSignatureDefinitions, path, slash, slash1, archiveConfiguration);
                                bzip2ArchiveContentIdentifier.identify(results.getUri(), request);
                            }
                            break;
                        case RAR_ARCHIVE:
                        case RAR_ARCHIVE_OTHER:
                            if ((archiveConfiguration.getExpandAllArchives() || containsCaseInsensitive("RAR", archiveConfiguration.getExpandArchiveTypes()))) {
                                RarArchiveContainerIdentifier rarArchiveContentIdentifier =
                                        new RarArchiveContainerIdentifier(binarySignatureIdentifier,
                                                containerSignatureDefinitions, path, slash, slash1, archiveConfiguration);
                                rarArchiveContentIdentifier.identify(results.getUri(), request);
                            }
                            break;

                        case ARC_ARCHIVE:
                        case ARC_ARCHIVE_OTHER:
                            if ((archiveConfiguration.getExpandAllWebArchives() || containsCaseInsensitive("ARC", archiveConfiguration.getExpandWebArchiveTypes()))) {
                                ArcArchiveContentIdentifier arcArchiveIdentifier =
                                        new ArcArchiveContentIdentifier(binarySignatureIdentifier,
                                                containerSignatureDefinitions, path, slash, slash1, archiveConfiguration);
                                arcArchiveIdentifier.identify(results.getUri(), request);
                            }
                            break;
                        case WARC_ARCHIVE:
                            if ((archiveConfiguration.getExpandAllWebArchives() || containsCaseInsensitive("WARC", archiveConfiguration.getExpandWebArchiveTypes()))) {
                                WarcArchiveContentIdentifier warcArchiveIdentifier =
                                        new WarcArchiveContentIdentifier(binarySignatureIdentifier,
                                                containerSignatureDefinitions, path, slash, slash1, archiveConfiguration);
                                warcArchiveIdentifier.identify(results.getUri(), request);
                            }
                            break;
                    }

                }
            }
        } else {
            System.out.println(fileName + ",Unknown");
        }
    }

    public boolean containsCaseInsensitive(String needle, String[] haystack) {
        if(haystack==null){
            return false;
        }
        return Arrays.stream(haystack).anyMatch(x -> x.equalsIgnoreCase(needle));
    }

    //CHECKSTYLE:ON
    private IdentificationResultCollection getContainerResults(
        final IdentificationResultCollection results,
        final IdentificationRequest request, final String fileName) throws CommandExecutionException {
    
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

                        if (OLE2_CONTAINER.equals(containerType)) {
                            try {
                                Ole2ContainerContentIdentifier ole2Identifier =
                                        new Ole2ContainerContentIdentifier();
                                ole2Identifier.init(containerSignatureDefinitions, containerType);
                                Ole2IdentifierEngine ole2IdentifierEngine = new Ole2IdentifierEngine();
                                ole2IdentifierEngine.setRequestFactory(requestFactory);
                                ole2Identifier.setIdentifierEngine(ole2IdentifierEngine);
                                containerResults = ole2Identifier.process(
                                    request.getSourceInputStream(), containerResults);
                            } catch (IOException e) {   // carry on after container i/o problems
                                System.err.println(e + SPACE + L_BRACKET + fileName + R_BRACKET);
                            }
                        } else if (ZIP.equals(containerType)) {
                            try {
                                ZipContainerContentIdentifier zipIdentifier =
                                            new ZipContainerContentIdentifier();
                                zipIdentifier.init(containerSignatureDefinitions, containerType);
                                ZipIdentifierEngine zipIdentifierEngine = new ZipIdentifierEngine();
                                zipIdentifierEngine.setRequestFactory(requestFactory);
                                zipIdentifier.setIdentifierEngine(zipIdentifierEngine);
                                containerResults = zipIdentifier.process(
                                    request.getSourceInputStream(), containerResults);
                            } catch (IOException e) {   // carry on after container i/o problems
                                System.err.println(e + SPACE + L_BRACKET + fileName + R_BRACKET);
                            }
                        } else {
                            throw new CommandExecutionException("Unknown container type: " + containerPuid);
                        }
                    }
                }
            }
        }
        return containerResults;
    }
    
    private TriggerPuid getTriggerPuidByPuid(final String puid) {
        for (final TriggerPuid tp : triggerPuids) {
            if (tp.getPuid().equals(puid)) {
                return tp;
            }
        }
        return null;
    }

    /**
     *
     * @return configuration to expand web archives and archives
     */
    public ArchiveConfiguration getArchiveConfiguration() {
        return archiveConfiguration;
    }

    /**
     *
     * @param archiveConfiguration configuration to expand web archives and archives
     */
    public void setArchiveConfiguration(ArchiveConfiguration archiveConfiguration) {
        this.archiveConfiguration = archiveConfiguration;
    }
}
