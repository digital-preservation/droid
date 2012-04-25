/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
