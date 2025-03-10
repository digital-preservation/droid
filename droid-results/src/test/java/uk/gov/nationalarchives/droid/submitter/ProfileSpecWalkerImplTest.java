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
package uk.gov.nationalarchives.droid.submitter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.ArgumentMatcher;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.profile.*;
import uk.gov.nationalarchives.droid.results.handlers.ProgressMonitor;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rflitcroft
 * 
 */
public class ProfileSpecWalkerImplTest {

    private static final Path TEST_ROOT = Paths.get("ProfileSpecWalkerImplTest");
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

        Files.createDirectories(TEST_ROOT);

        for (final String filename : files) {
            Path f = TEST_ROOT.resolve(filename);
            Files.createDirectories(f.getParent());
            Files.createFile(f);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FileUtil.deleteQuietly(TEST_ROOT);
    }

    @Test
    public void testIterateResources() throws Exception {

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
        S3EventHandler s3EventHandler = getS3EventHandler(resources);
        HttpEventHandler httpEventHandler = getHttpEventHandler();
        ResultHandler resultHandler = mock(ResultHandler.class);
        when(resultHandler.handleDirectory(any(IdentificationResult.class), any(ResourceId.class), anyBoolean())).thenReturn(new ResourceId(1, "/"));

        walker.setFileEventHandler(fileEventHandler);
        walker.setResultHandler(resultHandler);
        walker.setS3EventHandler(s3EventHandler);
        walker.setHttpEventHandler(httpEventHandler);

        walker.walk(profileSpec, new ProfileWalkState());

        verify(s3EventHandler).onS3Event(argThat(newNameMatcher("dir1/file11.ext")), any());
        verify(s3EventHandler).onS3Event(argThat(newNameMatcher("dir1/file12.ext")), any());
        verify(s3EventHandler).onS3Event(argThat(newNameMatcher("dir2/file13.ext")), any());

        verify(httpEventHandler).onHttpEvent(argThat(newNameMatcher("dir1/file11.ext")));
        verify(httpEventHandler).onHttpEvent(argThat(newNameMatcher("dir1/file12.ext")));
        verify(httpEventHandler).onHttpEvent(argThat(newNameMatcher("dir2/file13.ext")));

        verify(fileEventHandler).onEvent(
                argThat(newFileUriMatcher("dir1/file11.ext")), (ResourceId) any(),
                isNull());
        verify(fileEventHandler).onEvent(
                argThat(newFileUriMatcher("dir1/file12.ext")), (ResourceId) any(),
                isNull());
        verify(fileEventHandler).onEvent(
                argThat(newFileUriMatcher("dir2/file13.ext")), (ResourceId) any(),
                isNull());
    }

    private static S3EventHandler getS3EventHandler(List<AbstractProfileResource> resources) {
        S3EventHandler s3EventHandler = mock(S3EventHandler.class);

        S3Client s3Client = mock(S3Client.class);
        List<AbstractProfileResource> s3Resources = resources.stream().filter(AbstractProfileResource::isS3Object).toList();

        for (AbstractProfileResource resource : s3Resources) {
            String key = resource.getName().startsWith("/") ? resource.getName().substring(1) : resource.getName();
            S3Object s3Object = S3Object.builder().key(key).build();
            ArgumentMatcher<ListObjectsV2Request> requestArgumentMatcher = argument ->
                    argument != null && resource.getName().equals(argument.prefix());
            ListObjectsV2Response response = ListObjectsV2Response.builder().contents(List.of(s3Object)).build();
            when(s3Client.listObjectsV2(argThat(requestArgumentMatcher))).thenReturn(response);
            ListObjectsV2Iterable listObjectsV2Iterable = new ListObjectsV2Iterable(s3Client, ListObjectsV2Request.builder().prefix(resource.getName()).build());
            when(s3Client.listObjectsV2Paginator(argThat(requestArgumentMatcher))).thenReturn(listObjectsV2Iterable);
        }

        when(s3EventHandler.getS3Client(any(AbstractProfileResource.class))).thenReturn(s3Client);
        doNothing().when(s3EventHandler).onS3Event(any(AbstractProfileResource.class), any(ResourceId.class));

        return s3EventHandler;
    }

    private static HttpEventHandler getHttpEventHandler() {
        HttpEventHandler httpEventHandler = mock(HttpEventHandler.class);
        doNothing().when(httpEventHandler).onHttpEvent(any(AbstractProfileResource.class));
        return httpEventHandler;
    }

    @Test
    public void testIterateDirectoryResources() throws Exception {

        final Path[] locations = {
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

        verify(fileEventHandler, times(10)).onEvent(any(Path.class), (ResourceId) any(),
                (ResourceId) isNull());

        verify(fileEventHandler, never()).onEvent(
                argThat(new ArgumentMatcher<Path>() {
                    @Override
                    public boolean matches(Path argument) {
                        return argument.toString().contains("sub");
                    }
                    @Override
                    public String toString() {
                        return "A Node with a File containing the String 'sub'";
                    }

                }), (ResourceId) any(), (ResourceId) isNull());
    }

    private List<AbstractProfileResource> buildFileResources(String[] locations) {
        List<AbstractProfileResource> resources = new ArrayList<AbstractProfileResource>();

        for (String location : locations) {
            Path locationPath = Paths.get(location);
            FileProfileResource resource = new FileProfileResource(locationPath);
            resources.add(resource);
            URI s3Uri = URI.create("s3://bucket/" + location);
            S3ProfileResource s3ProfileResource = new S3ProfileResource(s3Uri.toString());
            resources.add(s3ProfileResource);
            URI httpUri = URI.create("https://example.com/" + location);
            HttpProfileResource httpProfileResource = new HttpProfileResource(httpUri.toString());
            resources.add(httpProfileResource);
        }

        return resources;
    }

    private List<AbstractProfileResource> buildDirectoryResources(
            Path[] locations) {
        List<AbstractProfileResource> resources = new ArrayList<AbstractProfileResource>();

        for (Path location : locations) {
            DirectoryProfileResource resource = new DirectoryProfileResource(
                    location, false);
            resources.add(resource);
        }

        return resources;
    }

    private List<AbstractProfileResource> buildRecursiveDirectoryResources(
            Path[] locations) {
        List<AbstractProfileResource> resources = new ArrayList<AbstractProfileResource>();

        for (Path location : locations) {
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
        Path[] locations =  {
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

        verify(fileEventHandler, times(30)).onEvent(any(Path.class), (ResourceId) any(),
                (ResourceId) isNull());
    }

    private static ArgumentMatcher<Path> newFileUriMatcher(final String fileName) {
        return new ArgumentMatcher<Path>() {
            @Override
            public boolean matches(Path argument) {
                return argument.equals(Paths.get(fileName).toAbsolutePath());
            }

            @Override
            public String toString() {
                return "Matches" + ((Paths.get(fileName)).toString());
            }
        };
    }

    private static ArgumentMatcher<AbstractProfileResource> newNameMatcher(final String fileName) {
        return argument ->
                argument.getName().equals(fileName);
    }
    
    private static Path canonicalFile(final Path parent, String child) throws IOException {
        return parent.resolve(child).toAbsolutePath();
    }


}
