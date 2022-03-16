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
package uk.gov.nationalarchives.droid.gui;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.openide.util.BaseUtilities.isWindows;

public class FileSystemSelectionValidatorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void windowsOnly() {
        org.junit.Assume.assumeTrue(isWindows());
    }

    @Test
    public void shouldReturnTrueForAFileFromFileSystemAndReturnEmptyErrorMessage() throws IOException {
        File file = temporaryFolder.newFile("someFile");
        FileSystemSelectionValidator validator = new FileSystemSelectionValidator(Arrays.asList(file));
        Assert.assertTrue(validator.isSelectionValid());
        Assert.assertEquals("", validator.getErrorMessage());
    }

/*      Unable to mock this. ShellFolder is private JDK API.
        If we mock ShellFolder, it doesn't work with FileSystemView

    @Test
    public void shouldReturnFalseForAFileFromNonFileSystem() throws IOException {
        ShellFolder mockfolder = Mockito.mock(ShellFolder.class);
        when(mockfolder.isFileSystem()).thenReturn(false);
        FileSystemSelectionValidator validator = new FileSystemSelectionValidator(Arrays.asList(mockfolder));
        Assert.assertFalse(validator.isSelectionValid());
    }

    @Test
    public void shouldReturnFalseWhenAnyOneFolderIsNonFileSystem() throws IOException {
        ShellFolder mockfolder = Mockito.mock(ShellFolder.class);
        when(mockfolder.isFileSystem()).thenReturn(false);
        File file = temporaryFolder.newFile("someFile");
        FileSystemSelectionValidator validator = new FileSystemSelectionValidator(Arrays.asList(mockfolder, file));
        Assert.assertFalse(validator.isSelectionValid());
    }

    @Test
    public void shouldReturnErrorMessageIndicatingWhichFolderIsNotAllowed() throws IOException {
        ShellFolder mockfolder = Mockito.mock(ShellFolder.class);
        when(mockfolder.isFileSystem()).thenReturn(false);
        when(mockfolder.getDisplayName()).thenReturn("Mock Libraries");
        File file = temporaryFolder.newFile("someFile");
        FileSystemSelectionValidator validator = new FileSystemSelectionValidator(Arrays.asList(mockfolder, file));
        Assert.assertFalse(validator.isSelectionValid());
        Assert.assertEquals("The following folders cannot be added as they are not from the file system: Mock Libraries",
                validator.getErrorMessage());
    }
 */
}