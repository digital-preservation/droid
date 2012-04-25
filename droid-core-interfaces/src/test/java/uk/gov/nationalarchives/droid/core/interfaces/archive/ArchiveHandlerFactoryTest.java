/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;


/**
 * @author rflitcroft
 *
 */
public class ArchiveHandlerFactoryTest {

    private ArchiveHandlerFactoryImpl factory;
    private ArchiveHandler zipHandler;
    private ArchiveHandler tarHandler;
    private ArchiveHandler gzHandler;
    
    @Before
    public void setup() {
        factory = new ArchiveHandlerFactoryImpl();
        
        zipHandler = mock(ArchiveHandler.class);
        tarHandler = mock(ArchiveHandler.class);
        gzHandler = mock(ArchiveHandler.class);
        
        Map<String, ArchiveHandler> handlers = new HashMap<String, ArchiveHandler>();
        
        handlers.put("ZIP", zipHandler);
        handlers.put("TAR", tarHandler);
        handlers.put("GZ", gzHandler);
        
        factory.setHandlers(handlers);
        
    }
    
    @Test
    public void testGetEachTypeOfHandler() {
     
        assertEquals(zipHandler, factory.getHandler("ZIP"));
        assertEquals(tarHandler, factory.getHandler("TAR"));
        assertEquals(gzHandler, factory.getHandler("GZ"));
    }
    
}
