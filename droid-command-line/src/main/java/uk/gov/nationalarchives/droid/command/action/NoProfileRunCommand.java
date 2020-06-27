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
package uk.gov.nationalarchives.droid.command.action;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.command.ResultPrinter;
import uk.gov.nationalarchives.droid.command.archive.ArchiveConfiguration;
import uk.gov.nationalarchives.droid.command.filter.Creator.DirectoryStreamFilterCreator;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rbrennan
 *
 */
public class NoProfileRunCommand implements DroidCommand {
    
    private static final String EXTENSION_FILTER_NOT_APPLICABLE = 
            "Ignoring extension filter option since it is not applicable when the selected resource is a file.";
    private static final String RECURSE_NOT_APPLICABLE = 
            "Ignoring recurse folders option since it is not applicable when the selected resource is a file.";
    private static final String MULTIPLE_RESOURCES_SPECIFIED = 
            "You specified more than one folder/file. Only the first item specified (%s) will be processed";
    private static final String FORWARD_SLASH = "/";
    private static final String BACKWARD_SLASH = "\\";
    private static final String PRINTABLE_TRUE = "True";
    private static final String PRINTABLE_FALSE = "False";
    private static final String PRINTABLE_ALL = "All";
    private static final String PRINTABLE_NONE = "None";

    private String fileSignaturesFileName;
    private String containerSignaturesFileName;
    private String[] resources;
    private String[] extensions;
    private int maxBytesToScan = -1;
    private boolean quietFlag;
    private boolean recursive;
    private boolean expandAllArchives;
    private String[] expandArchiveTypes;
    private boolean expandAllWebArchives;
    private String[] expandWebArchiveTypes;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    //CHECKSTYLE:OFF
    /**
    * {@inheritDoc}
    */
    @Override
    public void execute() throws CommandExecutionException {
        
        //BNO This is why only the first file or folder specified is processed, and any additional ones are ignored..
        final Path targetDirectoryOrFile = Paths.get(resources[0]);
    	
        if (!this.quietFlag) {
            this.outputRuntimeInformation(targetDirectoryOrFile);
        }
      

       //BNO - allow processing of a single file as well as directory
        final Collection<Path> matchedFiles;
        if (Files.isDirectory(targetDirectoryOrFile)) {
            final DirectoryStream.Filter<Path> filter = new DirectoryStreamFilterCreator(recursive, extensions).create();
            try {
                matchedFiles = FileUtil.listFiles(targetDirectoryOrFile, this.recursive, filter);
            } catch (final IOException e) {
                throw new CommandExecutionException("Can't find input files");
            }

        } else  if (Files.isRegularFile(targetDirectoryOrFile)) {
        	matchedFiles = new ArrayList<>();
        	matchedFiles.add(targetDirectoryOrFile);
        } else {
            throw new CommandExecutionException(String.format("The specified input %s was not found", targetDirectoryOrFile));       	
        }

        BinarySignatureIdentifier binarySignatureIdentifier = new BinarySignatureIdentifier();
        final Path fileSignaturesFile = Paths.get(fileSignaturesFileName);
        if (!Files.exists(fileSignaturesFile)) {
            throw new CommandExecutionException("Signature file not found");
        }

        binarySignatureIdentifier.setSignatureFile(fileSignaturesFileName);
        try {
            binarySignatureIdentifier.init();
        } catch (SignatureParseException e) {
            throw new CommandExecutionException("Can't parse signature file");
        }
        binarySignatureIdentifier.setMaxBytesToScan(maxBytesToScan);
        String path = fileSignaturesFile.toAbsolutePath().toString();
        String slash = path.contains(FORWARD_SLASH) ? FORWARD_SLASH : BACKWARD_SLASH;
      
        ContainerSignatureDefinitions containerSignatureDefinitions = null;
        if (containerSignaturesFileName != null) {
            final Path containerSignaturesFile = Paths.get(containerSignaturesFileName);
            if (!Files.exists(containerSignaturesFile)) {
                throw new CommandExecutionException("Container signature file not found");
            }
            try(final InputStream in = new BufferedInputStream(Files.newInputStream(containerSignaturesFile))) {
                final ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
                containerSignatureDefinitions = parser.parse(in);
            } catch (final SignatureParseException e) {
                throw new CommandExecutionException("Can't parse container signature file");
            } catch (final IOException | JAXBException ioe) {
                throw new CommandExecutionException(ioe);
            }
        }
        
        path = "";
        ResultPrinter resultPrinter =
            new ResultPrinter(binarySignatureIdentifier, containerSignatureDefinitions,
                path, slash, slash, new ArchiveConfiguration(expandAllArchives, expandArchiveTypes, expandAllWebArchives, expandWebArchiveTypes));

        for (final Path file : matchedFiles) {
            final String fileName = file.toAbsolutePath().toString();
            final URI uri = file.toUri();
            RequestMetaData metaData = new RequestMetaData(
                    FileUtil.sizeQuietly(file), FileUtil.lastModifiedQuietly(file).toMillis(), fileName);
            RequestIdentifier identifier = new RequestIdentifier(uri);
            identifier.setParentId(1L);

            try(final IdentificationRequest<Path> request = new FileSystemIdentificationRequest(metaData, identifier)) {
                request.open(file);
                IdentificationResultCollection results =
                    binarySignatureIdentifier.matchBinarySignatures(request);
                
                resultPrinter.print(results, request);
            } catch (FileNotFoundException fnfe) {
            	log.error("error processing files", fnfe);
            	throw new CommandExecutionException(fnfe);
            } catch (IOException e) {
                throw new CommandExecutionException(e);
            }
        }
    }

    private void outputRuntimeInformation(final Path targetDirectoryOrFile) {
    	
    	// BNO: updated the parameter sanitisation and output messages to account for the fact that the input
    	// can now be either a folder or a single file.
    	
    	// BNO Currently if the user specifies more than one folder/file with the -Nr switch, only the first 
    	// item is processed, but no message is output. Therefore added code to inform the user accordingly.
    	if (resources.length > 1) {
    		System.out.println(String.format(MULTIPLE_RESOURCES_SPECIFIED, targetDirectoryOrFile));
    	}
    	
        String versionString = ResourceBundle.getBundle("options").getString("version_no");
        System.out.println("DROID " + versionString + " No Profile mode: Runtime Information");
        System.out.println("Selected folder or file: " + this.resources[0]);
        System.out.println("Binary signature file: " + this.fileSignaturesFileName);
       
        System.out.println("Container signature file: " 
            + (this.containerSignaturesFileName == null ? " None" : this.containerSignaturesFileName));
        
        
        if (Files.isDirectory(targetDirectoryOrFile)) {
            System.out.println("Recurse folders: " + (this.recursive ? PRINTABLE_TRUE : PRINTABLE_FALSE));
            if (this.extensions == null) {
                System.out.println("Extension filter: No filter set");
            } else {
                System.out.println("Extension filter: "
                    + Arrays.toString(this.extensions).replace("[", "").replace("]", "").trim());
            }
        }
        
        if (!Files.isDirectory(targetDirectoryOrFile)) {
            if (this.recursive) {
                System.out.println(RECURSE_NOT_APPLICABLE);
            }
            if (this.extensions != null) {
                System.out.println(EXTENSION_FILTER_NOT_APPLICABLE);
            }
        }
        
        System.out.println("Open archives: " + (this.expandAllArchives ? PRINTABLE_ALL :
                (this.expandArchiveTypes!=null && this.expandArchiveTypes.length>0 ? String.join(", ", this.expandArchiveTypes) : PRINTABLE_NONE)));
        System.out.println("Open web archives: " + (this.expandAllWebArchives ? PRINTABLE_ALL :
                (this.expandWebArchiveTypes !=null && this.expandWebArchiveTypes.length>0 ? String.join(", ", this.expandWebArchiveTypes) : PRINTABLE_NONE)));
    }
    //CHECKSTYLE:ON
    /**
     * Set the resources.
     * 
     * @param resources the resources to set
     */
    public void setResources(final String[] resources) {
        this.resources = resources;
    }
    
    /**
     * Set the signature file.
     * 
     * @param signatureFile The signature file
     */
    public void setSignatureFile(final String signatureFile) {
        this.fileSignaturesFileName = signatureFile;
    }

    /**
     * Set the container signature file.
     * 
     * @param containerSignatureFile The Container Signature file
     */
    public void setContainerSignatureFile(final String containerSignatureFile) {
        this.containerSignaturesFileName = containerSignatureFile;
    }

    /**
     * Set whether this examines Web Archives.
     *
     * @param expandAllWebArchives true if we should examine web archives, false otherwise
     */
    public void setExpandAllWebArchives(boolean expandAllWebArchives) {
        this.expandAllWebArchives = expandAllWebArchives;
    }

    /**
     * Set recursive.
     * 
     * @param recursive Should we recurse through folders
     */
    public void setRecursive(final boolean recursive) {
        this.recursive = recursive;
    }
    
    /**
     * Set the Extension Filter to use.
     * 
     * @param extensionsFilter The list of extensions to filter
     */
    public void setExtensionFilter(final String[] extensionsFilter) {
        // No need to normalize extensions arr if empty, listFiles accepts null value 
        this.extensions = extensionsFilter;
    }
    
    /**
     * Set Quiet flag.
     * 
     * @param quiet The quiet flag
     */
    public void setQuiet(final boolean quiet) {
        this.quietFlag = quiet;
    }

    /**
     *
     * @param expandWebArchiveTypes       list of web archive types to expand
     */
    public void setExpandWebArchiveTypes(String[] expandWebArchiveTypes) {
        this.expandWebArchiveTypes = expandWebArchiveTypes;
    }

    /**
     * Set whether this examines Archives.
     *
     * @param expandAllArchives true if we should examine archives, false otherwise
     */
    public void setExpandAllArchives(boolean expandAllArchives) {
        this.expandAllArchives = expandAllArchives;
    }

    /**
     *
     * @param expandArchiveTypes       list of web archive types to expand
     */
    public void setExpandArchiveTypes(String[] expandArchiveTypes) {
        this.expandArchiveTypes = expandArchiveTypes;
    }

}
