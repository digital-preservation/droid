/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReader;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReaderCallback;
import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;
import uk.gov.nationalarchives.droid.export.interfaces.JobCancellationException;
import uk.gov.nationalarchives.droid.profile.ProfileContextLocator;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileInstanceManager;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * @author rflitcroft, Brian O'Reilly
 *
 */
public class ExportTask implements Runnable {

    private static final String PROJECT_NOT_AVAILABLE_FOR_EXPORT = "Profile not available for export: %s";

    private final Log log = LogFactory.getLog(getClass());

    private final String destination;
    private final List<String> profileIds;
    private final Filter filterOverride;
    private final ExportOptions options;
    private final String outputEncoding;
    private final ItemWriter<ProfileResourceNode> itemWriter;
    private final ProfileContextLocator profileContextLocator;

    private volatile boolean cancelled;
    

    /**
     * @param destination Output file path
     * @param profileIds ids of the profiles to export
     * @param filterOverride the override filter
     * @param options options for the export file format
     * @param outputEncoding A charset encoding for the output file, or null indicates platform locale encoding
     * @param itemWriter The writer for writing the export items
     * @param profileContextLocator locator of the profile context
     */
    public ExportTask(final String destination, final List<String> profileIds,
            final Filter filterOverride, final ExportOptions options,
            final String outputEncoding, final ItemWriter<ProfileResourceNode> itemWriter,
            final ProfileContextLocator profileContextLocator) {
        this.destination = destination;
        this.profileIds = profileIds;
        this.filterOverride = filterOverride;
        this.options = options;
        this.outputEncoding = outputEncoding;
        this.itemWriter = itemWriter;
        this.profileContextLocator = profileContextLocator;
    }

    /**
     * Cancels the task.
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        final Writer writer;
        final String destinationDescription = destination == null ? "System.out" : destination;
        if (destination == null) {
            writer = new PrintWriter(System.out);
        } else {
            try {
                writer = newOutputFileWriter();
            } catch (IOException e) {
                String message = String.format("IO exception occurred trying to read from: %s",
                        destinationDescription);
                log.error(message, e);
                throw new RuntimeException(message, e);
            }
        }
        doExport(writer, destinationDescription);
    }

    /**
     * Creates a new writer for the output file.
     * 
     * @return The writer for the output file
     * @throws IOException if an IO error occurs during establishing
     *  a writer for the file
     */
    protected Writer newOutputFileWriter() throws IOException {
        final Writer writer;

        //set encoding of output file
        if (outputEncoding != null) {
            writer = new BufferedWriter(newOutputFileWriterEncoded(outputEncoding, new File(destination)));
        } else {
            //leave to platform to determine encoding
            writer = new BufferedWriter(new FileWriter(destination));
        }

        return writer;
    }

    /**
     * Creates a new writer for the output file using a specific encoding for
     * the file content.
     * 
     * @param encoding The character encoding for the output file content
     * @param f The file to create a writer for
     * 
     * @return The writer for the output file
     * @throws IOException if an IO error occurs during establishing
     *  a writer for the file
     */
    protected Writer newOutputFileWriterEncoded(final String encoding, final File f) throws IOException {
        return new OutputStreamWriter(new FileOutputStream(f), encoding);
    }


    private void doExport(final Writer writer, final String destinationDescription) {
        log.info(String.format("Exporting profiles to: [%s]", destinationDescription));

        //BNO - amended to add header customisations for different hash algorithms
        Map<String, String> headerCustomisations = getHeaderCustomisationsFromProfiles();
        itemWriter.setHeaders(headerCustomisations);
        itemWriter.setOptions(options);
        itemWriter.open(writer);
        
        StopWatch stopWatch = new StopWatch();
        try {
            for (String profileId : profileIds) {
                stopWatch.start();
                if (!profileContextLocator.hasProfileContext(profileId)) {
                    final String message = String.format(PROJECT_NOT_AVAILABLE_FOR_EXPORT, profileId);
                    log.warn(message);
                    throw new RuntimeException(message);
                }
                ProfileInstance profile = profileContextLocator.getProfileInstance(profileId);
                ProfileInstanceManager profileContext = profileContextLocator.openProfileInstanceManager(profile);
                ItemReader<ProfileResourceNode> reader = profileContext.getNodeItemReader();
                ItemReaderCallback<ProfileResourceNode> callback = new ItemReaderCallback<ProfileResourceNode>() {
                    @Override
                    public void onItem(List<? extends ProfileResourceNode> itemChunk)
                        throws JobCancellationException {
                        itemWriter.write(itemChunk);
                        if (cancelled) {
                            log.info("Export interrupted");
                            throw new JobCancellationException("Cancelled");
                        }
                    }
                };

                Filter filter = filterOverride != null ? filterOverride : profile.getFilter();
                reader.readAll(callback, filter);
                stopWatch.stop();
                log.info(String.format("Time for export [%s]: %s ms", profileId, stopWatch.getTime()));
                stopWatch.reset();
            }
        } catch (JobCancellationException e) {
            String message = String.format("Export cancelled - deleting export destination: %s",
                    destinationDescription);
            log.info(message);
            cancelled = true;
        } finally {
            log.info(String.format("Closing export file: %s", destinationDescription));
            itemWriter.close();
            if (cancelled && destination != null) {
                File toDelete = new File(destination);
                if (!toDelete.delete()  && toDelete.exists()) {
                    log.warn(String.format("Could not delete export file: %s. "
                            + "Will try to delete on exit.", destination));
                    toDelete.deleteOnExit();
                }
            }
        }
    }
    
    private Map<String, String> getHeaderCustomisationsFromProfiles() {

        // Brian O'Reilly, July 2014:
        // Customises the column headers for the export output.  Currently (July 2014), only the column header 
        // for the hash algorithm needs to be customised depending on whether or not the profile(s) selected
        // for export contain hash algorithms, and if so
        // whether all the selected profiles use the same algorithm.
        Map<String, String> map = new HashMap<String, String>();
        String hashAlgorithmHeader = "HASH";
        Set<String> algorithmsFound = new HashSet<String>();
 
        for (String profileId : this.profileIds) {
            if (!this.profileContextLocator.hasProfileContext(profileId)) {
                final String message = String.format(PROJECT_NOT_AVAILABLE_FOR_EXPORT, profileId);
                log.warn(message);
                throw new RuntimeException(message);
            }
            ProfileInstance profile = profileContextLocator.getProfileInstance(profileId);
            if (profile.getGenerateHash()) {
                algorithmsFound.add(profile.getHashAlgorithm().toUpperCase());
            }
        } 
        
        // If the count is 1, this means that at least one profile in the export cohort was run with hash generation,  
        // and that only a single hash algorithm was used for all the profiles in the export (e.g. not one with MD5 
        // and one with SHa256). If so, we can set the column header to reflect the specific hash - otherwise we use 
        //the generic "HASH" header.
        if (algorithmsFound.size() == 1) {
            hashAlgorithmHeader = algorithmsFound.iterator().next() + "_HASH";
        }
        
        map.put("hash", hashAlgorithmHeader);
        return map;
    }
}
