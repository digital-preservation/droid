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
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileManagerException;
import uk.gov.nationalarchives.droid.profile.ProfileState;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author rbrennan
 *
 */
public class NoProfileRunCommand implements DroidCommand {
    
    private String destination;
    private String[] resources;
    private boolean recursive;
    
    private ProfileManager profileManager;
    private SignatureManager signatureManager;
    private LocationResolver locationResolver;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws CommandExecutionException {
        try {
            Map<SignatureType, SignatureFileInfo> sigs = signatureManager.getDefaultSignatures();
            ProfileInstance profile = profileManager.createProfile(sigs);
            profile.changeState(ProfileState.VIRGIN);

            for (String resource : resources) {
                profile.addResource(locationResolver.getResource(resource, recursive));
            }
            
            Future<?> future = profileManager.start(profile.getUuid());
            future.get();
            
            ProgressObserver progressCallback = new ProgressObserver() {
                @Override
                public void onProgress(Integer progress) {
                }
            };
            
            profileManager.save(profile.getUuid(), new File(destination), progressCallback);
            profileManager.closeProfile(profile.getUuid());
        } catch (ProfileManagerException e) {
            throw new CommandExecutionException(e);
        } catch (InterruptedException e) {
            throw new CommandExecutionException(e);
        } catch (ExecutionException e) {
            throw new CommandExecutionException(e.getCause());
        } catch (IOException e) {
            throw new CommandExecutionException(e);
        } catch (SignatureFileException e) {
            throw new CommandExecutionException(e);
        }
        
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    /**
     * @param resources the resources to set
     */
    public void setResources(String[] resources) {
        this.resources = resources;
    }
    
    /**
     * @param profileManager the profileManager to set
     */
    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }
    
    /**
     * @param signatureManager the signatureManager to set
     */
    public void setSignatureManager(SignatureManager signatureManager) {
        this.signatureManager = signatureManager;
    }

    /**
     * @param recursive the recursive to set
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }
    
    /**
     * @param locationResolver the locationResolver to set
     */
    public void setLocationResolver(LocationResolver locationResolver) {
        this.locationResolver = locationResolver;
    }
}
