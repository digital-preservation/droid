/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author rbrennan
 *
 */
public class NoProfileRunCommand implements DroidCommand {

   private String signatureFile;
   private String[] resources;
   private boolean recursive;
   private String[] extensions;

   private LocationResolver locationResolver;

   /**
   * {@inheritDoc}
   */
   @Override
   public void execute() throws CommandExecutionException {

      BinarySignatureIdentifier binarySignatureIdentifier = new BinarySignatureIdentifier();
      File sigFile = new File(signatureFile);

      if (!sigFile.exists())
         throw new CommandExecutionException("Signature file not found");

      binarySignatureIdentifier.setSignatureFile(signatureFile);
      binarySignatureIdentifier.init();

      binarySignatureIdentifier.setMaxBytesToScan(-1);
      binarySignatureIdentifier.init();

      File dirToSearch = new File(resources[0]);

      if (!dirToSearch.isDirectory()) {
         throw new CommandExecutionException("Resources directory not found");
      }
   
      Collection<File> matchedFiles = FileUtils.listFiles(dirToSearch,
                  this.extensions, true);

      for (File file : matchedFiles) {
         URI resourceUri = file.toURI();

         RequestMetaData metaData = new RequestMetaData(file.length(),
                     file.lastModified(), file.getName());
         RequestIdentifier identifier = new RequestIdentifier(resourceUri);
         identifier.setParentId(1L);

         IdentificationRequest request = null;
         InputStream in = null;
         try {
         in = new FileInputStream(file);
                  request = new FileSystemIdentificationRequest(metaData, identifier);

         request.open(in);

         IdentificationResultCollection results =
                     binarySignatureIdentifier.matchBinarySignatures(request);

         if (results.getResults().size() > 0) {
            for (IdentificationResult identResult : results.getResults()) {
            if (identResult.getPuid() != null) {
               System.out.println(file.getAbsolutePath() + "," + identResult.getPuid());
                           }
            }
         } else {
            System.out.println(file.getAbsolutePath() + ",Unknown");
         }
         } catch (IOException e) {
                  throw new CommandExecutionException(e);
         } finally {
                  if (request != null) {
                     try {
                           request.close();
                     } catch (IOException e) {
                           throw new CommandExecutionException(e);
                     }
                  }

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

    /**
     * @param resources the resources to set
     */
    public void setResources(String[] resources) {
        this.resources = resources;
    }
    
    /**
     * @param signatureFile the signatureFile to set
     */
    public void setSignatureFile(String signatureFile) {
        this.signatureFile = signatureFile;
    }

    /**
     * @param recursive the recursive to set
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }
    
    public void setExtensionFilter(String[] extensions){
       // No need to normalize extensions arr if empty, listFiles accepts null value 
       this.extensions = extensions;
    }
            
    
    /**
     * @param locationResolver the locationResolver to set
     */
    public void setLocationResolver(LocationResolver locationResolver) {
        this.locationResolver = locationResolver;
    }
}
