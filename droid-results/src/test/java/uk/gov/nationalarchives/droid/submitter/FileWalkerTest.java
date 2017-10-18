/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.submitter.FileWalker.ProgressEntry;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rflitcroft
 *
 */
public class FileWalkerTest {

   private static List<Path> files = new ArrayList<>();
   private static final Path TEST_ROOT = Paths.get("tmpFileWalker");

   @Before
   public void setup() throws Exception {
      final String[] fileNames = {
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
            "dir2/subdir2/file225.ext",};

      Files.createDirectories(TEST_ROOT);

      for (final String fileName : fileNames) {
         final Path path = TEST_ROOT.resolve(fileName).toAbsolutePath();
         Files.createDirectories(path.getParent());
         Files.createFile(path);
         files.add(path);
      }
   }

   @After
   public void tearDown() throws Exception {
      FileUtil.deleteQuietly(TEST_ROOT);
   }

   /**
    * testFastForwardFromUnprocessedFile. Tests to see if DROID pauses and then
    * resumes successfully from unprocessed file. We ask DROID to stop at an
    * arbitrary file and then return. Platform dependent behaviour removed by
    * use of a hash set. 
    *
    * @throws Exception
    */
   @Test
   public void testFastForwardFromUnprocessedFile() throws Exception {

      final int FILES_TO_WALK = 23;
      final Set<Path> fileSet = new HashSet<>(files);

      final AtomicLong nextId = new AtomicLong(0);
      FileWalker fileWalker = new FileWalker(TEST_ROOT.toUri(), true);

      FileWalkerHandler directoryHandler = mock(FileWalkerHandler.class);
      FileWalkerHandler fileHandler = mock(FileWalkerHandler.class);

      fileWalker.setDirectoryHandler(directoryHandler);
      fileWalker.setFileHandler(fileHandler);

      when(fileHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path breakFile = files.get(FILES_TO_WALK);
            final Path thisFile = (Path) invocation.getArguments()[0];
            int depth = (Integer) invocation.getArguments()[1];
            if (thisFile.equals(breakFile)) {
               throw new DirectoryWalker.CancelException(thisFile.toFile(), depth);
            }
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });

      when(directoryHandler.handle(any(Path.class), anyInt(), nullable(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });

      try {
         fileWalker.walk();
         fail("Expected file walker to throw exception");
      } catch (DirectoryWalker.CancelException e) {
         assertEquals(files.get(FILES_TO_WALK), e.getFile().toPath());
      }

      FileWalkerHandler resumeHandler = mock(FileWalkerHandler.class);
      
      when(resumeHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {
         
         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path thisFile = (Path) invocation.getArguments()[0];
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });   
      
      fileWalker.setFileHandler(resumeHandler);
      fileWalker.walk();
      
      assertEquals(fileSet.isEmpty(), true);

   }

   /**
    * testFastForwardFromMissingUnprocessedFile. Tests to see if DROID can 
    * be paused and recover from an unprocessed file being deleted while paused.
    *
    * @throws Exception
    */
   @Test
   public void testFastForwardFromMissingUnprocessedFile() throws Exception {

      final int FILES_TO_WALK = 23;
      final int NEXT_FILE_AFTER_MISSING = 24;

      final Set<Path> fileSet = new HashSet<>(files);
      
      final AtomicLong nextId = new AtomicLong(0);
      FileWalker fileWalker = new FileWalker(TEST_ROOT.toUri(), true);

      FileWalkerHandler directoryHandler = mock(FileWalkerHandler.class);
      FileWalkerHandler fileHandler = mock(FileWalkerHandler.class);

      fileWalker.setDirectoryHandler(directoryHandler);
      fileWalker.setFileHandler(fileHandler);

      when(fileHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path breakFile = files.get(FILES_TO_WALK);
            final Path thisFile = (Path) invocation.getArguments()[0];
            int depth = (Integer) invocation.getArguments()[1];
            if (thisFile.equals(breakFile)) {
               throw new DirectoryWalker.CancelException(thisFile.toFile(), depth);
            }
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });
      when(directoryHandler.handle(any(Path.class), anyInt(), nullable(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });

      try {
         fileWalker.walk();
         fail("Expected file walker to throw exception");
      } catch (DirectoryWalker.CancelException e) {
         assertEquals(files.get(FILES_TO_WALK), e.getFile().toPath());
      }
      
      if(FileUtil.deleteQuietly(files.get(FILES_TO_WALK))) {
         if(fileSet.contains(files.get(FILES_TO_WALK)))
            fileSet.remove(files.get(FILES_TO_WALK));
         else
            fail("File not found within set.");
      }
      else
         fail("Error deleting test file: " + files.get(FILES_TO_WALK).toUri());
      
      FileWalkerHandler resumeHandler = mock(FileWalkerHandler.class);
      when(resumeHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {
         
         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path thisFile = (Path) invocation.getArguments()[0];
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });   
      
      fileWalker.setFileHandler(resumeHandler);
      fileWalker.walk();

      assertEquals(fileSet.isEmpty(), true);
   }

   /**
    * testFastForwardFromMissingButPartiallyProcessedDirectory. test the handling
    * of resume functionality from the point where a directory is only partially
    * processed and then deleted. File counts that DROID expects are the main
    * point of interest here. 
    *
    * @throws Exception
    */
   @Test
   public void testFastForwardFromMissingButPartiallyProcessedDirectory() throws Exception {

      final int FILES_TO_WALK = 23;

      final Set<Path> fileSet = new HashSet<>(files);
      
      final AtomicLong nextId = new AtomicLong(0);
      FileWalker fileWalker = new FileWalker(TEST_ROOT.toUri(), true);

      FileWalkerHandler directoryHandler = mock(FileWalkerHandler.class);
      FileWalkerHandler fileHandler = mock(FileWalkerHandler.class);

      fileWalker.setDirectoryHandler(directoryHandler);
      fileWalker.setFileHandler(fileHandler);

      when(fileHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path breakFile = files.get(FILES_TO_WALK);
            final Path thisFile = (Path) invocation.getArguments()[0];
            int depth = (Integer) invocation.getArguments()[1];
            if (thisFile.equals(breakFile)) {
               throw new DirectoryWalker.CancelException(thisFile.toFile(), depth);
            }
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });
      
      when(directoryHandler.handle(any(Path.class), anyInt(), nullable(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });

      try {
         fileWalker.walk();
         fail("Expected file walker to throw exception");
      } catch (DirectoryWalker.CancelException e) {
         assertEquals(files.get(FILES_TO_WALK), e.getFile().toPath());
      }

      final Path parentDir = files.get(FILES_TO_WALK).getParent();
            
      // List files in directory that still exist to remove from the set
      // Simulating deletion of the files via the next deleteDirectory() call
      final List<Path> dirList = new ArrayList(FileUtil.listFiles(parentDir, true, (DirectoryStream.Filter)null));
            
      Iterator dirListIterator = dirList.iterator();

      while(dirListIterator.hasNext())
      {
         // remove any files we haven't scanned yet from hashset
         Path tmpFile = (Path) dirListIterator.next();
         if(fileSet.contains(tmpFile))
            fileSet.remove(tmpFile);
      }
      
      FileUtil.deleteQuietly(parentDir);
      
      FileWalkerHandler resumeHandler = mock(FileWalkerHandler.class);
      when(resumeHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {
         
         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path thisFile = (Path) invocation.getArguments()[0];
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });  
      
      fileWalker.setFileHandler(resumeHandler);
      fileWalker.walk();
      
      assertEquals(fileSet.isEmpty(), true);
   }

   /**
    * testFastForwardFromEmptyButPartiallyProcessedDirectory. Tests whether 
    * DROID correctly handles the scanning of a directory it is partially through
    * but then all the files are deleted from it. 
    *
    * @throws Exception
    */
   @Test
   public void testFastForwardFromEmptyButPartiallyProcessedDirectory() throws Exception {

      final int FILES_TO_WALK = 23;

      final Set<Path> fileSet = new HashSet<>(files);
      
      final AtomicLong nextId = new AtomicLong(0);
      FileWalker fileWalker = new FileWalker(TEST_ROOT.toUri(), true);

      FileWalkerHandler directoryHandler = mock(FileWalkerHandler.class);
      FileWalkerHandler fileHandler = mock(FileWalkerHandler.class);

      fileWalker.setDirectoryHandler(directoryHandler);
      fileWalker.setFileHandler(fileHandler);

      when(fileHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path breakFile = files.get(FILES_TO_WALK);
            final Path thisFile = (Path) invocation.getArguments()[0];
            int depth = (Integer) invocation.getArguments()[1];
            if (thisFile.equals(breakFile)) {
               throw new DirectoryWalker.CancelException(thisFile.toFile(), depth);
            }
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });
      when(directoryHandler.handle(any(Path.class), anyInt(), nullable(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });

      try {
         fileWalker.walk();
         fail("Expected file walker to throw exception");
      } catch (DirectoryWalker.CancelException e) {
         assertEquals(files.get(FILES_TO_WALK), e.getFile().toPath());
      }

      final Path directoryToEmpty = files.get(FILES_TO_WALK).getParent();

      for (final Path f : FileUtil.listFiles(directoryToEmpty, false, (DirectoryStream.Filter<Path>) null)) {
         if (fileSet.contains(f)) {
            fileSet.remove(f);
         }
         FileUtil.deleteQuietly(f);
      }
      assertEquals(0, FileUtil.listFiles(directoryToEmpty, true, (DirectoryStream.Filter<Path>) null).size());

      FileWalkerHandler resumeHandler = mock(FileWalkerHandler.class);
      when(resumeHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {
         
         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path thisFile = (Path) invocation.getArguments()[0];
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      }); 
      
      fileWalker.setFileHandler(resumeHandler);
      fileWalker.walk();

      assertEquals(fileSet.isEmpty(), true);
   }

   /**
    * testFastForwardFromPartiallyProcessedDirectoryWithANewFile. Ensure DROID
    * can handle the addition of a file mid scan. Note: This test is written 
    * specifically so it is added to the directory after the pause point and 
    * so the file is expected to be processed. It is not clear how effective 
    * this test is for DROIDs mode of operation and so will need amending in 
    * time. 
    *
    * @throws Exception
    */
   
   /**
    * TEST IGNORED: We need to understand why this is showing inconsistent
    * behaviour across platforms. Potentially the files to walk / position
    * for new file are incorrect however don't want to simply change for a
    * single platform to get this correct. 
    */
   
   @Ignore
   @Test
   public void testFastForwardFromPartiallyProcessedDirectoryWithANewFile() throws Exception {

      final int FILES_TO_WALK = 23;
      final int POSITION_FOR_NEW_FILE = 24;

      final Set<Path> fileSet = new HashSet<>(files);
      
      final AtomicLong nextId = new AtomicLong(0);
      FileWalker fileWalker = new FileWalker(TEST_ROOT.toUri(), true);

      FileWalkerHandler directoryHandler = mock(FileWalkerHandler.class);
      FileWalkerHandler fileHandler = mock(FileWalkerHandler.class);

      fileWalker.setDirectoryHandler(directoryHandler);
      fileWalker.setFileHandler(fileHandler);

      when(fileHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path breakFile = files.get(FILES_TO_WALK);
            final Path thisFile = (Path) invocation.getArguments()[0];
            int depth = (Integer) invocation.getArguments()[1];
            if (thisFile.equals(breakFile)) {
               throw new DirectoryWalker.CancelException(thisFile.toFile(), depth);
            }
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });
      when(directoryHandler.handle(any(Path.class), anyInt(), nullable(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });

      try {
         fileWalker.walk();
         fail("Expected file walker to throw exception");
      } catch (DirectoryWalker.CancelException e) {
         assertEquals(files.get(FILES_TO_WALK), e.getFile());
      }

      final Path file = files.get(POSITION_FOR_NEW_FILE);
      final Path newFile = file.resolveSibling(file.getFileName().toString() + "a");
      assertTrue(Files.exists(Files.createFile(newFile)));
      fileSet.add(newFile);   // represent the new file in the HashSet

      FileWalkerHandler resumeHandler = mock(FileWalkerHandler.class);
      when(resumeHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {
         
         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path thisFile = (Path) invocation.getArguments()[0];
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      }); 
      
      fileWalker.setFileHandler(resumeHandler);
      fileWalker.walk();
      
      assertEquals(fileSet.isEmpty(), true);
   }

   /**
    * testFastForwardFromPartiallyProcessedDirectoryWithANewDirectory. Ensure
    * DROID can handle the addition of a directory mid-scan without falling over
    * and that it continues to process all files as expected. Note: Again, need 
    * to revisit the quality of this test.
    *
    * @throws Exception
    */
   @Test
   public void testFastForwardFromPartiallyProcessedDirectoryWithANewDirectory() throws Exception {

      final int FILES_TO_WALK = 23;
      final int POSITION_FOR_NEW_FILE = 24;

      final Set<Path> fileSet = new HashSet<>(files);
      
      final AtomicLong nextId = new AtomicLong(0);
      FileWalker fileWalker = new FileWalker(TEST_ROOT.toUri(), true);

      FileWalkerHandler directoryHandler = mock(FileWalkerHandler.class);
      FileWalkerHandler fileHandler = mock(FileWalkerHandler.class);

      fileWalker.setDirectoryHandler(directoryHandler);
      fileWalker.setFileHandler(fileHandler);

      when(fileHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path breakFile = files.get(FILES_TO_WALK);
            final Path thisFile = (Path) invocation.getArguments()[0];
            int depth = (Integer) invocation.getArguments()[1];
            if (thisFile.equals(breakFile)) {
               throw new DirectoryWalker.CancelException(thisFile.toFile(), depth);
            }
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");            
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });
      when(directoryHandler.handle(any(Path.class), anyInt(), nullable(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {

         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      });


      try {
         fileWalker.walk();
         fail("Expected file walker to throw exception");
      } catch (DirectoryWalker.CancelException e) {
         assertEquals(files.get(FILES_TO_WALK), e.getFile().toPath());
      }

      /* Note: simply adds a new directory with nothing in it. We don't need
       * to add to the fileSet, DROID should simply look in the directory and
       * do nothing. It will then continue to scan the other files as normal.
       */
      final Path file = files.get(POSITION_FOR_NEW_FILE);
      final Path newFile = file.resolveSibling(file.getFileName().toString() + "a");
      assertTrue(FileUtil.mkdirsQuietly(newFile));

      FileWalkerHandler resumeHandler = mock(FileWalkerHandler.class);
      when(resumeHandler.handle(any(Path.class), anyInt(), any(ProgressEntry.class))).thenAnswer(new Answer<ResourceId>() {
         
         @Override
         public ResourceId answer(InvocationOnMock invocation) throws Throwable {
            final Path thisFile = (Path) invocation.getArguments()[0];
            if(fileSet.contains(thisFile))
               fileSet.remove(thisFile);
            else
               fail("File not found within set.");
            return new ResourceId(nextId.incrementAndGet(), "");
         }
      }); 
      
      fileWalker.setFileHandler(resumeHandler);
      fileWalker.walk();

      // Check the files have all been processed even with addition of directory
      assertEquals(fileSet.isEmpty(), true);
   }
}