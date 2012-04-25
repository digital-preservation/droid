/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.util.Map;


/**
 * @author rflitcroft
 *
 */
public class ArchiveHandlerFactoryImpl implements ArchiveHandlerFactory {

    private Map<String, ArchiveHandler> handlers;
    
    /**
     * @param handlers the handlers to set
     */
    public void setHandlers(
            Map<String, ArchiveHandler> handlers) {
        this.handlers = handlers;
    }

    /**
     * @param format the archive format
     * @return an Archive handler specific to the format given
     */
    @Override
    public final ArchiveHandler getHandler(String format) {
        return handlers.get(format);
    }

}
