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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.export.interfaces.ExportManager;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;
import uk.gov.nationalarchives.droid.profile.ProfileContextLocator;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * @author rflitcroft
 *
 */
public class ExportManagerImpl implements ExportManager {
    
    private ProfileContextLocator profileContextLocator;
    private ItemWriter<ProfileResourceNode> itemWriter;
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Future<?> exportProfiles(final List<String> profileIds, final String destination, 
        final Filter filter, final ExportOptions options, final String outputEncoding) {
        final ExportTask exportTask = new ExportTask(destination,
                profileIds, filter, options, outputEncoding, itemWriter, profileContextLocator);
        final FutureTask<?> task = new FutureTask<Object>(exportTask, null) {
            @Override
            public boolean cancel(final boolean mayInterruptIfRunning) {
                if (mayInterruptIfRunning) {
                    exportTask.cancel();
                }
                return super.cancel(false);
            }
        };
        executor.execute(task);
        return task;
    }
    
    /**
     * @param profileContextLocator the profileContextLocator to set
     */
    public void setProfileContextLocator(
            final ProfileContextLocator profileContextLocator) {
        this.profileContextLocator = profileContextLocator;
    }
    
    /**
     * @param itemWriter the itemWriter to set
     */
    public void setItemWriter(final ItemWriter<ProfileResourceNode> itemWriter) {
        this.itemWriter = itemWriter;
    }

    /**
     * Shuts down the executor service.
     */
    public void destroy() {
        executor.shutdownNow();
    }
    
}
