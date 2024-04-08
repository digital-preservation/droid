/*
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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.export.interfaces.ExportDetails;
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
    private String destination;
    private Filter filter;
    private ExportOptions options;
    private boolean bom;
    private boolean quoteAllFields = true;
    private String columnsToWrite;

    private String exportTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws CommandExecutionException {
        List<String> profileIds = new ArrayList<String>();

        // load each profile
        for (String profileLocation : profiles) {
            ProfileInstance profile;
            try {
                profile = profileManager.open(Paths.get(profileLocation), new ProgressObserver() {
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
            //default to UTF-8
            final Future<?> fProfiles = exportManager.exportProfiles(profileIds, destination, filter, getExportDetails());
            fProfiles.get();
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
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * @return the filter used for the export, or null if no filter.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     *
     * @return status of bom.
     */
    public boolean isBom() {
        return bom;
    }

    /**
     * Set to true to export with BOM in begining of file.
     * @param bom Byte order mark.
     */
    public void setBom(boolean bom) {
        this.bom = bom;
    }

    /**
      * @param quoteAllFields whether all fields should be quoted when exporting.
     */
    public void setQuoteAllFields(boolean quoteAllFields) {
        this.quoteAllFields = quoteAllFields;
    }

    /**
     * @return whether all fields are quoted, or just those that contain field separators (commas).
     */
    public boolean getQuoteAllFields() {
        return quoteAllFields;
    }

    /**
     * @param columnNames A space delimited list of columns to write for export, or null or empty for all columns.
     */
    public void setColumnsToWrite(String columnNames) {
        this.columnsToWrite = columnNames;
    }

    /**
     * @return A list of the columns to write, or null if all columns.
     */
    public String getColumnsToWrite() {
        return columnsToWrite;
    }

    /**
     * @return Absolute path of export template.
     */
    public String getExportTemplate() {
        return exportTemplate;
    }

    /**
     * @param exportTemplate Absolute path of export template.
     */
    public void setExportTemplate(String exportTemplate) {
        this.exportTemplate = exportTemplate;
    }


    /**
     *
     * @return the export details for this export command.
     * For an export from CLI,
     * OutputEncoding is always defaulted to UTF-8
     * exportTemplate is always null until we implement support for the template in CLI
     */
    private ExportDetails getExportDetails() {
        ExportDetails.ExportDetailsBuilder builder = new ExportDetails.ExportDetailsBuilder();

        return builder.withExportOptions(getExportOptions())
                .withOutputEncoding("UTF-8") //default
                .withBomFlag(isBom())
                .withQuotingAllFields(getQuoteAllFields())
                .withColumnsToWrite(getColumnsToWrite())
                .withExportTemplatePath(getExportTemplate())
                .build();
    }
}
