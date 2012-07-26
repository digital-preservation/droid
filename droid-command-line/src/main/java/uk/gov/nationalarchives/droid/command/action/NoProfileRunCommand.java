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
package uk.gov.nationalarchives.droid.command.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;
import org.apache.commons.io.FileUtils;
import uk.gov.nationalarchives.droid.command.ResultPrinter;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * @author rbrennan
 *
 */
public class NoProfileRunCommand implements DroidCommand {

    private String fileSignaturesFileName;
    private String containerSignaturesFileName;
    private String[] resources;
    private String[] extensions;
    private int maxBytesToScan = -1;
    private boolean quietFlag = false;  // default quiet flag value
    private boolean recursive;

    /**
    * {@inheritDoc}
    */
    @Override
    public void execute() throws CommandExecutionException {
        
        if (!this.quietFlag)
            this.outputRuntimeInformation();
      
        File dirToSearch = new File(resources[0]);
        if (!dirToSearch.isDirectory())
            throw new CommandExecutionException("Resources directory not found");

        BinarySignatureIdentifier binarySignatureIdentifier = new BinarySignatureIdentifier();
        File fileSignaturesFile = new File(fileSignaturesFileName);
        if (!fileSignaturesFile.exists())
            throw new CommandExecutionException("Signature file not found");

        binarySignatureIdentifier.setSignatureFile(fileSignaturesFileName);
        try {
            binarySignatureIdentifier.init();
        } catch (SignatureParseException e) {
            throw new CommandExecutionException ("Can't parse signature file");
        }
        binarySignatureIdentifier.setMaxBytesToScan(maxBytesToScan);
        String path = fileSignaturesFile.getAbsolutePath();
        String slash = path.contains("/") ? "/" : "\\";
      
        ContainerSignatureDefinitions containerSignatureDefinitions = null;
        if (containerSignaturesFileName != null) {
            File containerSignaturesFile = new File(containerSignaturesFileName);
            if (!containerSignaturesFile.exists()) {
                throw new CommandExecutionException("Container signature file not found");
            }
            try {
                InputStream in = new FileInputStream(containerSignaturesFileName);
                ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
                containerSignatureDefinitions = parser.parse(in);
            } catch (SignatureParseException e) {
                throw new CommandExecutionException ("Can't parse container signature file");
            } catch (Exception e) {
                throw new CommandExecutionException(e);
            }
        }
        path = "";
 /*       try {
            path = dirToSearch.getCanonicalPath() + slash;
        } catch (IOException e) {
            throw new CommandExecutionException(e);
        } */
        ResultPrinter resultPrinter =
            new ResultPrinter(binarySignatureIdentifier, containerSignatureDefinitions,
                path, slash);
        
        Collection<File> matchedFiles =
                FileUtils.listFiles(dirToSearch, this.extensions, this.recursive);
        String fileName = null;
        for (File file : matchedFiles) {
            try {
                fileName = file.getCanonicalPath();
            } catch (IOException e) {
                throw new CommandExecutionException(e);
            }
            URI uri = file.toURI();
            RequestMetaData metaData =
                new RequestMetaData(file.length(), file.lastModified(), fileName);
            RequestIdentifier identifier = new RequestIdentifier(uri);
            identifier.setParentId(1L);
            
            IdentificationRequest request = null;
            InputStream in = null;
            request = new FileSystemIdentificationRequest(metaData, identifier);
            try {
                in = new FileInputStream(file);
                request.open(in);
                IdentificationResultCollection results =
                    binarySignatureIdentifier.matchBinarySignatures(request);
                
                resultPrinter.print(results, request);
            } catch (IOException e) {
                throw new CommandExecutionException(e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        throw new CommandExecutionException(e);
                    }
                }
            }
        }
    }

    private void outputRuntimeInformation() {
        String versionString = ResourceBundle.getBundle("options").getString("version_no");
        System.out.println("DROID " + versionString + " No Profile mode: Runtime Information");
        System.out.println("Binary signature file: " + this.fileSignaturesFileName);
       
        if (this.containerSignaturesFileName == null)
            System.out.println("Container signature file: None");
        else
            System.out.println("Container signature file: " + this.containerSignaturesFileName);
       
        if (this.extensions == null)
            System.out.println("Extension filter: No filter set");
        else
            System.out.println("Extension filter: " + Arrays.toString(this.extensions).replace("[", "").replace("]", "").trim());
        
        if (this.recursive == false)
           System.out.println("Recurse folders: False");
        else
           System.out.println("Recurse folders: True");
    }

    /**
     * @param resources the resources to set
     */
    public void setResources(String[] resources) {
        this.resources = resources;
    }
    
    public void setSignatureFile(String signatureFile) {
        this.fileSignaturesFileName = signatureFile;
    }

    public void setContainerSignatureFile(String signatureFile) {
        this.containerSignaturesFileName = signatureFile;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }
    
    public void setExtensionFilter(String[] extensions) {
        // No need to normalize extensions arr if empty, listFiles accepts null value 
        this.extensions = extensions;
    }
    
    public void setQuiet(boolean quiet) {
        this.quietFlag = quiet;
    }
}
