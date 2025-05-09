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
package uk.gov.nationalarchives.droid.command.action;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class CleanCommandTest {

    private Path droidHome;

    @Before
    public void setUp() throws IOException {
        droidHome = Files.createTempDirectory("clean");
    }

    @Test
    public void testCleanDeletesDirectories() throws CommandExecutionException, IOException {
        Stream.of("container_sigs", "signature_files", "profiles", "profile_templates", "tmp", "not-included-directory").forEach(this::createDirectory);
        Stream.of("droid.properties", "log4j2.properties", "not-included-file").forEach(this::createFile);
        new CleanCommand(droidHome).execute();
        Assert.assertEquals(2, Files.list(droidHome).count());
        String[] fileNames = Files.list(droidHome).map(Path::getFileName).map(Path::toString).toArray(String[]::new);
        Assert.assertArrayEquals(fileNames, new String[]{"not-included-directory", "not-included-file"});
    }

    private void createFile(String filename) {
        try {
            Files.createFile(droidHome.resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDirectory(String directoryName) {
        try {
            Path directory = Files.createDirectory(droidHome.resolve(directoryName));
            Files.createFile(directory.resolve("test"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
