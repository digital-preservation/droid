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
package uk.gov.nationalarchives.droid.container;

import java.util.List;
import java.util.stream.Stream;

public class ContainerMatchUtils {


    public record ContainerTestData(String containerName, String pattern, List<String> willMatch,
                                    List<String> willNotMatch) {
    }


    public static Stream<ContainerTestData> getContainerTestData() {
        String containerName = "test.zip";
        return Stream.of(
                new ContainerTestData(
                        containerName,
                        "*.txt",
                        List.of("file.txt", "notes.txt"),
                        List.of("file.md", "image.jpg")
                ),
                new ContainerTestData(
                        containerName,
                        "*.{jpg,png,gif}",
                        List.of("photo.jpg", "graphic.png", "animation.gif"),
                        List.of("document.pdf", "image.bmp")
                ),
                new ContainerTestData(containerName,
                        "file*",
                        List.of("file1", "file2.txt", "file_test.md"),
                        List.of("afile.txt", "otherfile.md")
                ),
                new ContainerTestData(containerName,
                        "file?.txt",
                        List.of("file1.txt", "file2.txt"),
                        List.of("file10.txt", "fileAB.txt")
                ),
                new ContainerTestData(containerName,
                        "test/**",
                        List.of("test/file.txt", "test/subdir/file.txt"),
                        List.of("notest/file.txt", "testfile.txt")
                ),
                new ContainerTestData(containerName,
                        "**/*.scala",
                        List.of("/Main.scala", "src/main/Example.scala"),
                        List.of("Main.java", "notes.txt")
                ),
                new ContainerTestData(containerName,
                        "**/test_*.py",
                        List.of("/dir1/test_abc.py", "subdir/test_xyz.py"),
                        List.of("my_test.py", "testsuite.py")
                ),
                new ContainerTestData(containerName,
                        "src/**/main.*",
                        List.of("src/main/main.scala", "src/app/main.py"),
                        List.of("src/app/mainfile.py")
                ),
                new ContainerTestData(containerName,
                        "logs/**/*2024*.log",
                        List.of("logs/jan/2024_error.log", "logs/feb/2024_info.log"),
                        List.of("logs/2023_info.log", "logs/future_2025.log")
                ),
                new ContainerTestData(
                        "test.zip",
                        "{containerFileName}.xml",
                        List.of("test.xml"),
                        List.of("test.txt", "test.pdf", "test2.xml")
                ),
                new ContainerTestData("test2.7z",
                        "{containerFileName}",
                        List.of("test2"),
                        List.of("test2.txt", "test2.xml", "test")
                ),
                new ContainerTestData("test3",
                        "{containerFileName}.pdf",
                        List.of("test3.pdf"),
                        List.of("test3.txt", "test3.xml", "test3")
                ),
                new ContainerTestData("test3.tar.zip",
                        "{containerFileName}.txt",
                        List.of("test3.tar.txt"),
                        List.of("test3.txt", "test3.xml", "test3.tar")
                )
        );
    }
}
