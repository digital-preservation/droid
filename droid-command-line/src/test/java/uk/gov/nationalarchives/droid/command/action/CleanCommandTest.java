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
