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

import de.waldheinz.fs.FileSystem;
import de.waldheinz.fs.FileSystemFactory;
import de.waldheinz.fs.FsDirectory;
import de.waldheinz.fs.FsDirectoryEntry;
import de.waldheinz.fs.util.FileDisk;
import net.byteseek.io.reader.FileReader;
import net.byteseek.io.reader.WindowReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

/**
 * @author rflitcroft
 *
 */
public class ArchiveFileUtilsTest {

    @Test
    public void copyToBufferByteArray() throws Exception {
        testBufferCopy(0, 0, 1024, 1024);
        testBufferCopy(123, 0, 1024, 1024);
        testBufferCopy(1023, 0, 1024, 1024);
        testBufferCopy(2, 9, 32, 1024);
        testBufferCopy(7, 968, 1024, 2048);
        testBufferCopy(190000, 0, 1024, 1024);
        testBufferCopy(126, 98, 2048, 1024);
    }

    @Test
    public void copyToBufferByteBuffer() throws Exception {
        testByteBufferCopy(0, 1024);
        testByteBufferCopy(123, 1024);
        testByteBufferCopy(1023, 1024);
        testByteBufferCopy(2, 32);
        testByteBufferCopy(7, 968);
        testByteBufferCopy(190000, 1024);
        testByteBufferCopy(126, 2048);
    }

    private void testBufferCopy(long position, int offset, int length, int bufferSize) throws Exception {
        String resource = "saved.zip";

        // Copy from a file reader
        byte[] buffer = new byte[bufferSize];
        try (WindowReader reader = getFileReader(resource)) {
            ArchiveFileUtils.copyToBuffer(reader, position, buffer, offset, length);
        }

        // Copy from a random access file:
        byte[] expected = new byte[bufferSize];
        try (RandomAccessFile raf = getRAF(resource)) {
            if (position < raf.length()) {
                raf.seek(position);
                final int maxBytesToRead = Math.min(expected.length - offset, length);
                raf.read(expected, offset, maxBytesToRead);
            }
        }
        assertArrayEquals(buffer, expected);
    }

    private void testByteBufferCopy(long position, int bufferSize) throws Exception {
        String resource = "saved.zip";

        // Copy from a file reader
        byte[] buffer = new byte[bufferSize];
        try (WindowReader reader = getFileReader(resource)) {
            ByteBuffer byteBuf = ByteBuffer.wrap(buffer);
            ArchiveFileUtils.copyToBuffer(reader, position, byteBuf);
        }

        // Copy from a random access file:
        byte[] expected = new byte[bufferSize];
        try (RandomAccessFile raf = getRAF(resource)) {
            if (position < raf.length()) {
                raf.seek(position);
                raf.read(expected, 0, bufferSize);
            }
        }
        assertArrayEquals(buffer, expected);
    }

    private WindowReader getFileReader(String resourceName) throws IOException {
        Path p = Paths.get("./src/test/resources/" + resourceName);
        return new FileReader(p.toFile(), 127); // use a small odd window size so we cross window boundaries.
    }

    private RandomAccessFile getRAF(String resourceName) throws IOException {
        Path p = Paths.get("./src/test/resources/" + resourceName);
        return new RandomAccessFile(p.toFile(), "r");
    }

    @Test
    public void testIsoImageUri() throws URISyntaxException {
        URI parent = new URI("file://isoImage.iso");
        String entryName = "sample.txt";

        assertEquals("iso:file://isoImage.iso!/sample.txt", ArchiveFileUtils.toIsoImageUri(parent, entryName).toString());
    }


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
        
        assertEquals("gzip:file:/C:/anyhost/dir/mygz.tar.gz!/mygz.tar",
                ArchiveFileUtils.toGZipUri(parent).toString());
    }

    @Test
    public void testBuildGzipUriWithNoGzExtension() {
        URI parent = URI.create("file:/C:/anyhost/dir/mygz");
        
        assertEquals("gzip:file:/C:/anyhost/dir/mygz!/mygz",
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

    @Test
    public void testFatImageUri() throws URISyntaxException {
        URI parent = new URI("file://fatImage.img");
        String entryName = "sample.txt";

        assertEquals("fat:file://fatImage.img!/sample.txt", ArchiveFileUtils.toFatImageUri(parent, entryName).toString());
    }


    @Test
    public void writeFsFileToTemp() throws IOException {
        Path p = Paths.get("./src/test/resources/fat12.img");
        FileDisk fileDisk = new FileDisk(p.toFile(), true);

        FileSystem fatSystem = FileSystemFactory.create(fileDisk, true);
        Map<FsDirectoryEntry, Path> tempFilePathMap = new HashMap<>();
        FsDirectory root = fatSystem.getRoot();
        for (FsDirectoryEntry de : root) {
            if (de.isFile()) {
                tempFilePathMap.put(de, ArchiveFileUtils.writeFsFileToTemp(de,
                        new File(System.getProperty("java.io.tmpdir")).toPath()));
            }
        }

        for (Map.Entry<FsDirectoryEntry, Path> savedFile : tempFilePathMap.entrySet()) {
            assertThat("Saved file same as length:" + savedFile.getKey().getName(), savedFile.getKey().getFile().getLength(),
                    equalTo(savedFile.getValue().toFile().length()));
            assertThat(savedFile.getKey().getName()+": deleted", savedFile.getValue().toFile().delete(), equalTo(true));
        }
        assertThat("All files from FsDirectory written to temp file", tempFilePathMap.size(), equalTo(5));
    }

    @Test
    public void should_generate_BZipUri_with_correct_bZipPrefix_For_TarDotBz2() {
        URI bZip2Parent = URI.create("file:///home/some-user/test-data/droid/archives/one/samples.tar.bz2");
        URI bZip2TarURI = ArchiveFileUtils.toBZipUri(bZip2Parent);

        assertThat(bZip2TarURI.toString(), equalTo("bzip2:file:///home/some-user/test-data/droid/archives/one/samples.tar.bz2!/samples.tar"));
    }
}
