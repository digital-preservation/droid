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
package uk.gov.nationalarchives.droid.submitter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.matchers.TypeSafeMatcher;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.DirectoryProfileResource;
import uk.gov.nationalarchives.droid.profile.FileProfileResource;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileSpec;
import uk.gov.nationalarchives.droid.results.handlers.ProgressMonitor;

/**
 * @author rflitcroft
 * 
 */
public class ProfileSpecWalkerImplTest {

    private static final File TEST_ROOT = new File("ProfileSpecWalkerImplTest");
    private static String[] files;

    @BeforeClass
    public static void setup() throws Exception {

        files = new String[] { 
            "dir1/file11.ext",
            "dir1/file12.ext",
            "dir1/file13.ext", 
            "dir1/file14.ext", 
            "dir1/file15.ext",

            "dir2/file21.ext", 
            "dir2/file22.ext", 
            "dir2/file23.ext",
            "dir2/file24.ext", 
            "dir2/file25.ext",

            "dir1/subdir1/file111.ext", 
            "dir1/subdir1/file112.ext",
            "dir1/subdir1/file113.ext", 
            "dir1/subdir1/file114.ext",
            "dir1/subdir1/file115.ext",

            "dir1/subdir2/file121.ext", 
            "dir1/subdir2/file122.ext",
            "dir1/subdir2/file123.ext", 
            "dir1/subdir2/file124.ext",
            "dir1/subdir2/file125.ext",

            "dir2/subdir1/file211.ext", 
            "dir2/subdir1/file212.ext",
            "dir2/subdir1/file213.ext", 
            "dir2/subdir1/file214.ext",
            "dir2/subdir1/file215.ext",

            "dir2/subdir2/file221.ext", 
            "dir2/subdir2/file222.ext",
            "dir2/subdir2/file223.ext", 
            "dir2/subdir2/file224.ext",
            "dir2/subdir2/file225.ext", };

        TEST_ROOT.mkdir();

        for (String filename : files) {
            File f = new File(TEST_ROOT, filename);
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FileUtils.forceDelete(TEST_ROOT);
    }

    @Test
    public void testIterateFileResources() throws Exception {

        String[] locations = new String[] {
            "dir1/file11.ext",
            "dir1/file12.ext", 
            "dir2/file13.ext",
        };

        ProfileSpec profileSpec = mock(ProfileSpec.class);
        List<AbstractProfileResource> resources = buildFileResources(locations);
        when(profileSpec.getResources()).thenReturn(resources);
        
        ProfileInstance profile = mock(ProfileInstance.class);
        when(profile.getProfileSpec()).thenReturn(profileSpec);

        ProfileSpecWalkerImpl walker = new ProfileSpecWalkerImpl();
        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        walker.setProgressMonitor(progressMonitor);

        FileEventHandler fileEventHandler = mock(FileEventHandler.class);
        walker.setFileEventHandler(fileEventHandler);

        walker.walk(profileSpec, new ProfileWalkState());

        verify(fileEventHandler).onEvent(
                argThat(newFileUriMatcher("dir1/file11.ext")), (ResourceId) any(),
                (ResourceId) isNull());
        verify(fileEventHandler).onEvent(
                argThat(newFileUriMatcher("dir1/file12.ext")), (ResourceId) any(),
                (ResourceId) isNull());
        verify(fileEventHandler).onEvent(
                argThat(newFileUriMatcher("dir2/file13.ext")), (ResourceId) any(),
                (ResourceId) isNull());
    }

    @Test
    public void testIterateDirectoryResources() throws Exception {

        File[] locations = new File[] {
            canonicalFile(TEST_ROOT, "dir1"),
            canonicalFile(TEST_ROOT, "dir2"),
        };

        ProfileSpec profileSpec = mock(ProfileSpec.class);
        List<AbstractProfileResource> resources = buildDirectoryResources(locations);
        when(profileSpec.getResources()).thenReturn(resources);

        ProfileInstance profile = mock(ProfileInstance.class);
        when(profile.getProfileSpec()).thenReturn(profileSpec);

        ProfileSpecWalkerImpl walker = new ProfileSpecWalkerImpl();
        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        walker.setProgressMonitor(progressMonitor);
        FileEventHandler fileEventHandler = mock(FileEventHandler.class);
        DirectoryEventHandler dirEventhandler = mock(DirectoryEventHandler.class);
        when(dirEventhandler.onEvent(locations[0],
                null, 0, false)).thenReturn(new ResourceId(1L, ""));
        when(dirEventhandler.onEvent(locations[1],
                null, 0, false)).thenReturn(new ResourceId(2L, ""));
        when(dirEventhandler.onEvent(canonicalFile(TEST_ROOT, "dir1/subdir1"), 
                new ResourceId(1L, ""), 1, false)).thenReturn(new ResourceId(11L, ""));
        when(dirEventhandler.onEvent(canonicalFile(TEST_ROOT, "dir1/subdir2"),
                new ResourceId(1L, ""), 1, false)).thenReturn(new ResourceId(12L, ""));
        when(dirEventhandler.onEvent(canonicalFile(TEST_ROOT, "dir2/subdir1"),
                new ResourceId(2L, ""), 1, false)).thenReturn(new ResourceId(21L, ""));
        when(dirEventhandler.onEvent(canonicalFile(TEST_ROOT, "dir2/subdir2"), 
                new ResourceId(2L, ""), 1, false)).thenReturn(new ResourceId(22L, ""));
        
        walker.setDirectoryEventHandler(dirEventhandler);
        walker.setFileEventHandler(fileEventHandler);

        walker.walk(profileSpec, new ProfileWalkState());

        verify(fileEventHandler).onEvent(
                canonicalFile(TEST_ROOT, "dir1/file11.ext"), new ResourceId(1L, ""), null);
        verify(fileEventHandler).onEvent(
                canonicalFile(TEST_ROOT, "dir1/file12.ext"), new ResourceId(1L, ""), null);
        verify(fileEventHandler).onEvent(
                canonicalFile(TEST_ROOT, "dir1/file13.ext"), new ResourceId(1L, ""), null);
        verify(fileEventHandler).onEvent(
                canonicalFile(TEST_ROOT, "dir1/file14.ext"), new ResourceId(1L, ""), null);
        verify(fileEventHandler).onEvent(
                canonicalFile(TEST_ROOT, "dir1/file15.ext"), new ResourceId(1L, ""), null);

        verify(fileEventHandler).onEvent(
                canonicalFile(TEST_ROOT, "dir2/file21.ext"), new ResourceId(2L, ""), null);
        verify(fileEventHandler).onEvent(
                canonicalFile(TEST_ROOT, "dir2/file21.ext"), new ResourceId(2L, ""), null);
        verify(fileEventHandler).onEvent(
                canonicalFile(TEST_ROOT, "dir2/file21.ext"), new ResourceId(2L, ""), null);
        verify(fileEventHandler).onEvent(
                canonicalFile(TEST_ROOT, "dir2/file21.ext"), new ResourceId(2L, ""), null);
        verify(fileEventHandler).onEvent(
                canonicalFile(TEST_ROOT, "dir2/file21.ext"), new ResourceId(2L, ""), null);

        verify(fileEventHandler, times(10)).onEvent(any(File.class), (ResourceId) any(),
                (ResourceId) isNull());

        verify(fileEventHandler, never()).onEvent(
                argThat(new TypeSafeMatcher<File>() {
                    @Override
                    public void describeTo(Description arg0) {
                        arg0
                                .appendText("A Node with a File containing the String 'sub'");

                    }

                    @Override
                    public boolean matchesSafely(File item) {
                        return item.toString().contains("sub");
                    }
                }), (ResourceId) any(), (ResourceId) isNull());
    }

    private List<AbstractProfileResource> buildFileResources(String[] locations) {
        List<AbstractProfileResource> resources = new ArrayList<AbstractProfileResource>();

        for (String location : locations) {
            FileProfileResource resource = new FileProfileResource(new File(
                    location));
            resources.add(resource);
        }

        return resources;
    }

    private List<AbstractProfileResource> buildDirectoryResources(
            File[] locations) {
        List<AbstractProfileResource> resources = new ArrayList<AbstractProfileResource>();

        for (File location : locations) {
            DirectoryProfileResource resource = new DirectoryProfileResource(
                    location, false);
            resources.add(resource);
        }

        return resources;
    }

    private List<AbstractProfileResource> buildRecursiveDirectoryResources(
            File[] locations) {
        List<AbstractProfileResource> resources = new ArrayList<AbstractProfileResource>();

        for (File location : locations) {
            DirectoryProfileResource resource = new DirectoryProfileResource(
                    location, true);
            resources.add(resource);
        }

        return resources;
    }
    
    /* This test now fails due to IOException thrown by 
     * crash recovery code.  It is no longer a good test
     * for access denied.

    @Test
    public void testWalkDirectoryWithAccessDenied() throws Exception {
        List<AbstractProfileResource> resources = new ArrayList<AbstractProfileResource>();
        
        File dirNoAccess = new File("tmp/no_access").getCanonicalFile();
        resources.add(new DirectoryProfileResource(dirNoAccess, false));
        
        ProfileSpec profileSpec = mock(ProfileSpec.class);
        when(profileSpec.getResources()).thenReturn(resources);
        
        DirectoryEventHandler directoryEventHandler = mock(DirectoryEventHandler.class);
        FileEventHandler fileEventHandler = mock(FileEventHandler.class);
        
        ProfileInstance profile = mock(ProfileInstance.class);
        when(profile.getProfileSpec()).thenReturn(profileSpec);
        ProfileSpecWalkerImpl profileSpecWalker = new ProfileSpecWalkerImpl();
        
        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        profileSpecWalker.setProgressMonitor(progressMonitor);
        profileSpecWalker.setDirectoryEventHandler(directoryEventHandler);
        profileSpecWalker.setFileEventHandler(fileEventHandler);
        profileSpecWalker.walk(profileSpec, new ProfileWalkState());
        
        verify(directoryEventHandler).onEvent(dirNoAccess, null, 0, true);
        verify(progressMonitor).startJob(dirNoAccess.toURI());
    }
    */

    @Test
    public void testIterateRecursiveDirectoryResources() throws Exception {
        File[] locations = new File[] {
            canonicalFile(TEST_ROOT, "dir1"),
            canonicalFile(TEST_ROOT, "dir2"),
        };

        ProfileSpec profileSpec = mock(ProfileSpec.class);
        List<AbstractProfileResource> resources = buildRecursiveDirectoryResources(locations);
        when(profileSpec.getResources()).thenReturn(resources);

        ProfileInstance profile = mock(ProfileInstance.class);
        when(profile.getProfileSpec()).thenReturn(profileSpec);
        ProfileSpecWalkerImpl walker = new ProfileSpecWalkerImpl();
        
        ProgressMonitor progressMonitor = mock(ProgressMonitor.class);
        walker.setProgressMonitor(progressMonitor);
        FileEventHandler fileEventHandler = mock(FileEventHandler.class);
        walker.setFileEventHandler(fileEventHandler);

        DirectoryEventHandler dirEventhandler = mock(DirectoryEventHandler.class);
        when(dirEventhandler.onEvent(canonicalFile(TEST_ROOT, "dir1"), 
                null, 0, false)).thenReturn(new ResourceId(1L, ""));
        when(dirEventhandler.onEvent(canonicalFile(TEST_ROOT, "dir2"), 
                null, 0, false)).thenReturn(new ResourceId(2L, ""));
        when(dirEventhandler.onEvent(canonicalFile(TEST_ROOT, "dir1/subdir1"), 
                new ResourceId(1L, ""), 1, false)).thenReturn(new ResourceId(11L, ""));
        when(dirEventhandler.onEvent(canonicalFile(TEST_ROOT, "dir1/subdir2"),
                new ResourceId(1L, ""), 1, false)).thenReturn(new ResourceId(12L, ""));
        when(dirEventhandler.onEvent(canonicalFile(TEST_ROOT, "dir2/subdir1"),
                new ResourceId(2L, ""), 1, false)).thenReturn(new ResourceId(21L, ""));
        when(dirEventhandler.onEvent(canonicalFile(TEST_ROOT, "dir2/subdir2"), 
                new ResourceId(2L, ""), 1, false)).thenReturn(new ResourceId(22L, ""));
        walker.setDirectoryEventHandler(dirEventhandler);

        walker.walk(profileSpec, new ProfileWalkState());

        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/file11.ext"), new ResourceId(1L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/file12.ext"), new ResourceId(1L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/file13.ext"), new ResourceId(1L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/file14.ext"), new ResourceId(1L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/file15.ext"), new ResourceId(1L, ""), null);

        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/subdir1/file111.ext"),
                new ResourceId(11L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/subdir1/file112.ext"), 
                new ResourceId(11L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/subdir1/file113.ext"),
                new ResourceId(11L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/subdir1/file114.ext"), 
                new ResourceId(11L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/subdir1/file115.ext"), 
                new ResourceId(11L, ""), null);

        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/subdir2/file121.ext"), 
                new ResourceId(12L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/subdir2/file122.ext"), 
                new ResourceId(12L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/subdir2/file123.ext"), 
                new ResourceId(12L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/subdir2/file124.ext"), 
                new ResourceId(12L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir1/subdir2/file125.ext"), 
                new ResourceId(12L, ""), null);

        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/file21.ext"), new ResourceId(2L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/file21.ext"), new ResourceId(2L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/file21.ext"), new ResourceId(2L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/file21.ext"), new ResourceId(2L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/file21.ext"), new ResourceId(2L, ""), null);

        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/subdir1/file211.ext"), 
                new ResourceId(21L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/subdir1/file212.ext"), 
                new ResourceId(21L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/subdir1/file213.ext"), 
                new ResourceId(21L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/subdir1/file214.ext"), 
                new ResourceId(21L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/subdir1/file215.ext"), 
                new ResourceId(21L, ""), null);

        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/subdir2/file221.ext"), 
                new ResourceId(22L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/subdir2/file222.ext"), 
                new ResourceId(22L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/subdir2/file223.ext"), 
                new ResourceId(22L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/subdir2/file224.ext"), 
                new ResourceId(22L, ""), null);
        verify(fileEventHandler).onEvent(canonicalFile(TEST_ROOT, "dir2/subdir2/file225.ext"), 
                new ResourceId(22L, ""), null);

        verify(fileEventHandler, times(30)).onEvent(any(File.class), (ResourceId) any(),
                (ResourceId) isNull());
    }

    private static Matcher<File> newFileUriMatcher(final String fileName) {
        return new TypeSafeMatcher<File>() {
            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("Matches " + new File(fileName));

            }

            @Override
            public boolean matchesSafely(File item) {
                try {
                    return item.equals(new File(fileName).getCanonicalFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
    
    private static File canonicalFile(File parent, String child) throws IOException {
        return new File(parent, child).getCanonicalFile();
    }


}
