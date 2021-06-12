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
package uk.gov.nationalarchives.droid.command.filter.Creator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;


public class DirectoryStreamFilterCreatorTest {

    @Rule
    public TemporaryFolder tempRoot = new TemporaryFolder();
    private File root_flying_gif;
    private File root_indiaNa_jpeg;
    private File root_rocket_gif;
    private File root_tna_icon_png;
    private File root_tna_jpeg;
    private File root_oneMore_flying_gif;
    private File root_oneMore_tna_icon_png;
    private File root_oneMore_tna_jpeg;
    private File oneMoreFolder;

    /**
     * The structure being used for these tests is
     *    rootFolder
     *       |
     *       + flying.gif
     *       |
     *       + india-na.jpeg
     *       |
     *       + rocket.gif
     *       |
     *       + the_national_archives_icon.png
     *       |
     *       + tna.jpeg
     *       |
     *       +--------~ one-more
     *                     |
     *                     + flying.gif
     *                     |
     *                     + the_national_archives_icon.png
     *                     |
     *                     + tna.jpeg
     *
     */
    @Before
    public void setupFoldersAndFiles() throws IOException {
        oneMoreFolder = tempRoot.newFolder("one-more");
        oneMoreFolder.mkdir();

        root_flying_gif = tempRoot.newFile("flying.gif");
        root_indiaNa_jpeg = tempRoot.newFile("india-na.jpeg");
        root_rocket_gif = tempRoot.newFile("rocket.gif");
        root_tna_icon_png =  tempRoot.newFile("the_national_archives_icon.png");
        root_tna_jpeg = tempRoot.newFile("tna.jpeg");
        root_oneMore_flying_gif = tempRoot.newFile("one-more/flying.gif");
        root_oneMore_tna_icon_png = tempRoot.newFile("one-more/the_national_archives_icon.png");
        root_oneMore_tna_jpeg = tempRoot.newFile("one-more/tna.jpeg");
    }

    @Test
    public void shouldFilterAllFilesInCurrentFolderWhenThereIsNoExtensionFilterAndRecursiveIsFalse() throws IOException {
        List<Path> paths = getPathList(false, null);
        assertEquals(5, paths.size());

        List<String> expectedPaths = Arrays.asList(root_tna_jpeg.getPath(), root_indiaNa_jpeg.getPath(),
                root_rocket_gif.getPath(), root_tna_icon_png.getPath(), root_flying_gif.getPath());

        List<String> actualPaths = paths.stream().map(a -> a.toString()).collect(Collectors.toList());
        assertThat(actualPaths, containsInAnyOrder(expectedPaths.toArray()));
    }

    @Test
    public void shouldFilterAllFilesAndFolderWhenRecursingAndNoFilterOnExtension() throws IOException {
        List<Path> paths = getPathList(true, null);
        assertEquals(6, paths.size());

        List<String> expectedPaths = Arrays.asList(root_tna_jpeg.getPath(), root_indiaNa_jpeg.getPath(),
                root_rocket_gif.getPath(), root_tna_icon_png.getPath(), root_flying_gif.getPath(), oneMoreFolder.getPath());

        List<String> actualPaths = paths.stream().map(a -> a.toString()).collect(Collectors.toList());
        assertThat(actualPaths, containsInAnyOrder(expectedPaths.toArray()));
    }

    @Test
    public void shouldFilterOnlyTheFilesMatchingExtensionWhenNotRecursing() throws IOException {
        List<Path> paths = getPathList(false, new String[] {"gif"});
        assertEquals(2, paths.size());

        List<String> expectedPaths = Arrays.asList(root_rocket_gif.getPath(), root_flying_gif.getPath());

        List<String> actualPaths = paths.stream().map(a -> a.toString()).collect(Collectors.toList());
        assertThat(actualPaths, containsInAnyOrder(expectedPaths.toArray()));
    }

    @Test
    public void shouldFilterOnlyTheFilesMatchingExtensionAndFoldersWhenRecursing() throws IOException {
        List<Path> paths = getPathList(true, new String[] {"gif"});
        assertEquals(3, paths.size());

        List<String> expectedPaths = Arrays.asList(root_rocket_gif.getPath(), root_flying_gif.getPath(),
                oneMoreFolder.getPath());

        List<String> actualPaths = paths.stream().map(a -> a.toString()).collect(Collectors.toList());
        assertThat(actualPaths, containsInAnyOrder(expectedPaths.toArray()));
    }

    @Test
    public void shouldFilterAllTheFilesMatchingMultipleExtensionAndFoldersWhenRecursing() throws IOException {
        List<Path> paths = getPathList(true, new String[] {"gif", "jpeg"});
        assertEquals(5, paths.size());

        List<String> expectedPaths = Arrays.asList(root_rocket_gif.getPath(), root_flying_gif.getPath(),
                oneMoreFolder.getPath(), root_tna_jpeg.getPath(), root_indiaNa_jpeg.getPath());

        List<String> actualPaths = paths.stream().map(a -> a.toString()).collect(Collectors.toList());
        assertThat(actualPaths, containsInAnyOrder(expectedPaths.toArray()));
    }

    /**
     * Private helper method for manipulating the iterator into a testable list.
     * @param recursive whether the filter is recursive
     * @param extensions whether the filter has extensions
     * @return filtered Paths as a List
     * @throws IOException
     */
    private List<Path> getPathList(boolean recursive, String[] extensions) throws IOException {
        DirectoryStreamFilterCreator creator = new DirectoryStreamFilterCreator(recursive, extensions);
        DirectoryStream.Filter<Path> dirStreamFilter = creator.create();

        DirectoryStream<Path> filteredStream = Files.newDirectoryStream(tempRoot.getRoot().toPath(), dirStreamFilter);
        return StreamSupport.stream(filteredStream.spliterator(), false).collect(Collectors.toList());
    }
}