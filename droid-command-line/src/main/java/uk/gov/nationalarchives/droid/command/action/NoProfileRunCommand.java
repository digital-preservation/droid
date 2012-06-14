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
import java.lang.reflect.Array;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;

import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.SignatureParseException;

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
   private BinarySignatureIdentifier binarySignatureIdentifier;
   private boolean quietFlag = false;  // default quiet flag value

   /**
   * {@inheritDoc}
   */
   @Override
   public void execute() throws CommandExecutionException {

      if(!this.quietFlag)
         this.outputRuntimeInformation();
      
      binarySignatureIdentifier = new BinarySignatureIdentifier();
      File sigFile = new File(signatureFile);

      if (!sigFile.exists())
         throw new CommandExecutionException("Signature file not found");

      File dirToSearch = new File(resources[0]);

      if (!dirToSearch.isDirectory()) {
         throw new CommandExecutionException("Resources directory not found");
      }
      
      binarySignatureIdentifier.setSignatureFile(signatureFile);
      try {
          binarySignatureIdentifier.init();
      } catch (SignatureParseException x) {
          throw new CommandExecutionException ("Can't parse signature file");
      }
      binarySignatureIdentifier.setMaxBytesToScan(-1);

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
    
    public void setQuiet()
    {
       this.quietFlag = true;
    }
    
    /**
     * @param locationResolver the locationResolver to set
     */
    public void setLocationResolver(LocationResolver locationResolver) {
        this.locationResolver = locationResolver;
    }
    
    private void outputRuntimeInformation()
    {
       String versionString = ResourceBundle.getBundle("options").getString("version_no");
       System.out.println("DROID " + versionString + " No Profile mode: Runtime Information");
       System.out.println("Binary signature file: " + this.signatureFile);
       if(this.extensions == null)
          System.out.println("Extension filter: No filter set");
       else
         System.out.println("Extension filter: " + Arrays.toString(this.extensions).replace("[", "").replace("]", "").trim());
    }
}
