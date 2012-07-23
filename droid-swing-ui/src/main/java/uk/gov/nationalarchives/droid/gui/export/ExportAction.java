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
package uk.gov.nationalarchives.droid.gui.export;

import java.io.File;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.export.interfaces.ExportManager;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.gui.action.ActionDoneCallback;

/**
 * @author rflitcroft
 *
 */
public class ExportAction extends SwingWorker<Void, Integer> {

    private final Log log = LogFactory.getLog(getClass());
    
    private ExportManager exportManager;
    private File destination;
    private ExportOptions options;
    private List<String> profileIds;
    private ActionDoneCallback<ExportAction> callback;
    
    private Future<?> exportTask;
    
    /**
     * Default Constructor. 
     */
    public ExportAction() {
    }

    /**
     * Initialsies the export action.
     */
    public void init() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void done() {
        callback.done(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void process(List<Integer> chunks) {
        super.process(chunks);
    }
    
    /**
     * @return nothing.
     */
    @Override
    protected Void doInBackground() {
        exportTask = exportManager.exportProfiles(profileIds, destination.getPath(), null, options);
        try {
            exportTask.get();
        } catch (InterruptedException e) {
            log.debug(e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            log.error(e.getCause(), e);
            throw new RuntimeException(e.getCause());
        } catch (CancellationException e) {
            cancel(false);
            log.info("Export task was cancelled");
        }
        return null;
    }
    
    /**
     * @param exportManager the exportManager to set
     */
    public void setExportManager(ExportManager exportManager) {
        this.exportManager = exportManager;
    }
    
    /**
     * @param destination the destination to set
     */
    public void setDestination(File destination) {
        this.destination = destination;
    }
    
    
    /**
     * 
     * @param opt The export options to use.
     */
    public void setExportOptions(ExportOptions opt) {
        this.options = opt;
    }
    
    /**
     * @param profileIds the profileIds to set
     */
    public void setProfileIds(List<String> profileIds) {
        this.profileIds = profileIds;
    }
    
    /**
     * @param callback the callback to set
     */
    public void setCallback(ActionDoneCallback<ExportAction> callback) {
        this.callback = callback;
    }

    /**
     * Cancels this export action.
     */
    public void cancel() {
        exportTask.cancel(true);
    }
    
}
