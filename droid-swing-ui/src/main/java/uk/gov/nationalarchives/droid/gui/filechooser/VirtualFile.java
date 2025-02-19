package uk.gov.nationalarchives.droid.gui.filechooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Serial;
import java.net.URI;

public class VirtualFile extends File {
    @Serial
    private static final long serialVersionUID = -1218203508098312763L;
    private static final String FORWARD_SLASH = "/";

    private final boolean isDir;
    private final long length;
    private long lastModified;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public VirtualFile(String pathName) {
        super(pathName);
        log.info("Creating virtual file {}", pathName);
        isDir = pathName.endsWith(FORWARD_SLASH);
        this.length = 0;

    }

    public VirtualFile(String pathName, long length) {
        super(pathName);
        log.info("Creating virtual file {} with length {}", pathName, length);
        isDir = pathName.endsWith(FORWARD_SLASH);
        this.length = length;
    }

    public VirtualFile(String parent, String child, long length) {
        super(parent, child);
        log.info("Creating virtual file with parent {} child {} and length {}", parent, child, length);
        isDir = child.endsWith(FORWARD_SLASH);
        this.length = length;
    }

    @Override
    public boolean setLastModified(long t) {
        this.lastModified = t;
        return true;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return isDir;
    }

    @Nonnull
    @Override
    public File getCanonicalFile() {
        return this;
    }

    @Nonnull
    @Override
    public URI toURI() {
        return URI.create("s3://" + getPath());
    }

    @Nonnull
    @Override
    public String getPath() {
        return super.getPath().replaceAll("\\\\", FORWARD_SLASH);
    }

    @Nonnull
    @Override
    public File getAbsoluteFile() {
        return this;
    }

    @Override
    public boolean isFile() {
        return !isDir;
    }

    @Override
    public File getParentFile() {
        final int lastIndex = this.toString().lastIndexOf('/');

        if (lastIndex <= 0) {
            return null;
        }

        String parent = this.toString().substring(0, lastIndex + 1);

        return new VirtualFile(parent, 1);
    }

    @Override
    public String toString() {
        final int lastIndex = getPath().lastIndexOf('/');

        if (lastIndex <= 0) {
            return this.getPath();
        }

        return this.getPath().substring(lastIndex + 1);
    }
}
