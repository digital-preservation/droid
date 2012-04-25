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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import uk.gov.nationalarchives.droid.command.filter.CommandLineFilter;
import uk.gov.nationalarchives.droid.command.filter.DqlFilterParser;
import uk.gov.nationalarchives.droid.command.filter.SimpleFilter;
import uk.gov.nationalarchives.droid.export.interfaces.ExportManager;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author rflitcroft
 *
 */
public class ExportCommand implements DroidCommand {

    private String[] profiles;
    private ExportManager exportManager;
    private ProfileManager profileManager;
    private DqlFilterParser dqlFilterParser;
    private String destination;
    private CommandLineFilter cliFilter;
    private ExportOptions options;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws CommandExecutionException {
        List<String> profileIds = new ArrayList<String>();
        
        SimpleFilter filter = null;
        if (cliFilter != null) {
            filter = new SimpleFilter(cliFilter.getFilterType());
            
            for (String dql : cliFilter.getFilters()) {
                filter.add(dqlFilterParser.parse(dql));
            }
        }
        
        // load each profile
        for (String profileLocation : profiles) {
            ProfileInstance profile;
            try {
                profile = profileManager.open(new File(profileLocation), new ProgressObserver() {
                    @Override
                    public void onProgress(Integer progress) {
                    }
                });
                profileIds.add(profile.getUuid());
            } catch (IOException e) {
                throw new CommandExecutionException(e);
            }
        }
        
        // Run the export
        try {
            exportManager.exportProfiles(profileIds, destination, filter, options).get();
        } catch (InterruptedException e) {
            throw new CommandExecutionException(e);
        } catch (ExecutionException e) {
            throw new CommandExecutionException(e.getCause());
        } finally {
            // close all profiles
            for (String profileId : profileIds) {
                profileManager.closeProfile(profileId);
            }
        }
    }

    /**
     * @param profileList the list of profiles to export.
     */
    public void setProfiles(String[] profileList) {
        this.profiles = profileList;
    }

    /**
     * @param exportManager the export manager to set
     */
    public void setExportManager(ExportManager exportManager) {
        this.exportManager = exportManager;
    }

    /**
     * @return the profiles
     */
    String[] getProfiles() {
        return profiles;
    }
    
    /**
     * 
     * @return The export options.
     */
    public ExportOptions getExportOptions() {
        return options;
    }
    
    /**
     *
     * @param opt The export options to use for this command.
     */
    public void setExportOptions(ExportOptions opt) {
        this.options = opt;
    }
    
    /**
     * @param profileManager the profileManager to set
     */
    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }
    
    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    /**
     * @return the destination
     */
    String getDestination() {
        return destination;
    }

    /**
     * Sets the filter.
     * @param filter the filter to set
     */
    public void setFilter(CommandLineFilter filter) {
        this.cliFilter = filter;
    }
    
    /**
     * @param dqlFilterParser the dqlFilterParser to set
     */
    public void setDqlFilterParser(DqlFilterParser dqlFilterParser) {
        this.dqlFilterParser = dqlFilterParser;
    }

}
