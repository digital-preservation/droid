/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
