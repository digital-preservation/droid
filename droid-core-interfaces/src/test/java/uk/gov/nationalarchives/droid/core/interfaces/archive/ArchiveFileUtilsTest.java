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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author rflitcroft
 *
 */
public class ArchiveFileUtilsTest {

    @Test
    public void testBuildTarGzUri() throws URISyntaxException {
        URI parent = new URI("gz:http://anyhost/dir/mytar.tar.gz!/mytar.tar");
        String entryName = "path/in tar/README.txt";
        
        assertEquals("tar:gz:http://anyhost/dir/mytar.tar.gz!/mytar.tar!/path/in%20tar/README.txt",
                ArchiveFileUtils.toTarUri(parent, entryName).toString());
    }

    @Test
    public void testBuildTarGzUriWithSpacesInEntry() {
        URI parent = URI.create("gz:http://any%20host/dir/mytar.tar.gz!/mytar.tar");
        String entryName = "path/in/tar/READ ME.txt";
        
        assertEquals("tar:gz:http://any%20host/dir/mytar.tar.gz!/mytar.tar!/path/in/tar/READ%20ME.txt",
                ArchiveFileUtils.toTarUri(parent, entryName).toString());
    }

    @Test
    public void testBuildZipUri() {
        URI parent = URI.create("file:/C:/anyhost/dir/my%20zip.zip");
        String entryName = "my\\zip entry";
        
        assertEquals("zip:file:/C:/anyhost/dir/my%20zip.zip!/my/zip%20entry",
                ArchiveFileUtils.toZipUri(parent, entryName).toString());
        
    }

    @Test
    public void testBuildNestedZipUri() {
        URI parent = URI.create("zip:file:/C:/anyhost/dir/my%20zip.zip!/zip1");
        String entryName = "my\\zip entry";
        
        assertEquals("zip:zip:file:/C:/anyhost/dir/my%20zip.zip!/zip1!/my/zip%20entry",
                ArchiveFileUtils.toZipUri(parent, entryName).toString());
        
    }

    @Test
    public void testBuildGzipUriWithGzExtension() {
        URI parent = URI.create("file:/C:/anyhost/dir/mygz.tar.gz");
        
        assertEquals("gz:file:/C:/anyhost/dir/mygz.tar.gz!/mygz.tar",
                ArchiveFileUtils.toGZipUri(parent).toString());
    }

    @Test
    public void testBuildGzipUriWithNoGzExtension() {
        URI parent = URI.create("file:/C:/anyhost/dir/mygz");
        
        assertEquals("gz:file:/C:/anyhost/dir/mygz!/mygz",
            ArchiveFileUtils.toGZipUri(parent).toString());
    }
    
    @Test
    public void testToReplayUri() {
        URI uri = URI.create("zip:file:/C:/anyhost/dir/my%20zip.zip!/my/zip%20entry");
        assertEquals("file:/C:/anyhost/dir/my%20zip.zip", ArchiveFileUtils.toReplayUri(uri).toString());
        
        uri = URI.create("file:/C:/anyhost/dir/my%20zip.zip");
        assertEquals("file:/C:/anyhost/dir/my%20zip.zip", ArchiveFileUtils.toReplayUri(uri).toString());
        
        uri = URI.create("tar:gz:file:/C:/anyhost/dir/my%20zip.gz!/mytar.tar!/myfile");
        assertEquals("file:/C:/anyhost/dir/my%20zip.gz", ArchiveFileUtils.toReplayUri(uri).toString());
    }

}
