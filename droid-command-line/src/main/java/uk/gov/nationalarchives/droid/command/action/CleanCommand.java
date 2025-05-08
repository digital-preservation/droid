package uk.gov.nationalarchives.droid.command.action;

import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.util.stream.Stream;

public class CleanCommand implements DroidCommand {

    private final Path droidHome;
    private final Stream<String> toDelete;

    public CleanCommand(final Path droidHome) {
        this.droidHome = droidHome;
        this.toDelete = Stream.of(
                "container_sigs",
                "signature_files",
                "profiles",
                "profile_templates",
                "droid.properties",
                "log4j2.properties",
                "tmp"
        );
    }

    @Override
    public void execute() throws CommandExecutionException {
        toDelete.forEach(this::delete);
    }

    private void delete(final String path) {
        FileUtils.deleteQuietly(droidHome.resolve(path).toFile());
    }
}
