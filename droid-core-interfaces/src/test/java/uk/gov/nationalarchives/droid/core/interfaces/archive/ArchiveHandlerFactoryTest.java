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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


/**
 * @author rflitcroft
 *
 */
public class ArchiveHandlerFactoryTest {

    private static ArchiveHandlerFactoryImpl factory;
    private static ArchiveHandler zipHandler;
    private static ArchiveHandler tarHandler;
    private static ArchiveHandler gzHandler;
    private static ArchiveHandler arcHandler;
    private static ArchiveHandler bzHandler;

    private static ArchiveHandler sevenZipHandler;

    @BeforeAll
    public static void setup() {
        factory = new ArchiveHandlerFactoryImpl();

        zipHandler = mock(ArchiveHandler.class);
        tarHandler = mock(ArchiveHandler.class);
        gzHandler = mock(ArchiveHandler.class);
        arcHandler = mock(ArchiveHandler.class);
        bzHandler = mock(ArchiveHandler.class);

        sevenZipHandler = mock(ArchiveHandler.class);

        Map<String, ArchiveHandler> handlers = new HashMap<String, ArchiveHandler>();

        handlers.put("ZIP", zipHandler);
        handlers.put("TAR", tarHandler);
        handlers.put("GZIP", gzHandler);
        handlers.put("ARC", arcHandler);
        handlers.put("BZ", bzHandler);

        handlers.put("7Z", sevenZipHandler);

        factory.setHandlers(handlers);

    }
    
    @Test
    public void testGetEachTypeOfHandler() {
     
        assertEquals(zipHandler, factory.getHandler("ZIP"));
        assertEquals(tarHandler, factory.getHandler("TAR"));
        assertEquals(gzHandler, factory.getHandler("GZIP"));
        assertEquals(arcHandler, factory.getHandler("ARC"));
        assertEquals(bzHandler, factory.getHandler("BZ"));
        assertEquals(sevenZipHandler, factory.getHandler("7Z"));
    }
    
}
