package uk.gov.nationalarchives.droid.gui.filechooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class S3FileSystemView extends FileSystemView {

    private static final String HOME = "s3://";
    private static final String FORWARD_SLASH = "/";

    private final S3Client s3Client;
    private String currentBucket;

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    public S3FileSystemView(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    protected File createFileSystemRoot(File f) {
        return new VirtualFile(f.toString(), 0);
    }

    @Override
    public File getParentDirectory(final File dir) {
        return (dir == null || dir.getParentFile() == null) ? null : new VirtualFile(dir.getParentFile().getPath());
    }

    @Override
    public File[] getFiles(final File dir, boolean useFileHiding) {
        log.info("Trying to get files for {}", dir);
        try {
            return getFilesFromS3(dir);
        } catch (AbortedException e) {
            return new VirtualFile[0];
        }
    }


    private File[] getFilesFromS3(final File dir) throws AbortedException {

        if ("s3:".equals(dir.getName())) {
            log.info("Getting list of s3 buckets");
            try {
                return this.s3Client.listBuckets().buckets().stream().map(Bucket::name).toList().stream()
                        .map(bucketName -> new VirtualFile(bucketName + FORWARD_SLASH, 0))
                        .toArray(VirtualFile[]::new);
            } catch (SdkException e) {
                log.error(e.getMessage(), e);
            }


        }
        if (!dir.getPath().contains(FORWARD_SLASH)) {
            String rootName = getRootName(dir);
            log.info("Setting current bucket to {}", rootName);
            this.currentBucket = rootName;
        }

        log.info("Dir path is {}", dir.getPath());
        String prefix = dir.getPath().contains(FORWARD_SLASH) ? dir.getPath().substring(dir.getPath().indexOf(FORWARD_SLASH) + 1) + FORWARD_SLASH : null;
        ListObjectsV2Request.Builder listObjectsV2RequestBuilder = ListObjectsV2Request.builder()
                .bucket(this.currentBucket)
                .prefix(prefix)
                .delimiter(FORWARD_SLASH);

        if (prefix != null) {
            listObjectsV2RequestBuilder = listObjectsV2RequestBuilder.prefix(prefix);
        }

        ListObjectsV2Response listObjectsV2Response = this.s3Client.listObjectsV2(listObjectsV2RequestBuilder.build());

        Stream<VirtualFile> fileStream = listObjectsV2Response.contents()
                .stream()
                .map(this::fileFromObject);
        Stream<VirtualFile> folderStream =  listObjectsV2Response.commonPrefixes()
                .stream()
                .map(this::fileFromCommonPrefix);

        return Stream.concat(fileStream, folderStream).toArray(VirtualFile[]::new);
    }

    @Override
    public File[] getRoots() {
        return new File[]{new VirtualFile(HOME, 0)};
    }

    private String getRootName(File dir) {
        if (dir.getParentFile() == null) {
            return dir.getName();
        } else {
            return getRootName(dir.getParentFile());
        }
    }

    private record FileInfo(String parent, String file) {}

    private FileInfo getFileInfo(String name) {
        int idx = name.lastIndexOf(FORWARD_SLASH);
        if (idx == -1) {
            return new FileInfo(this.currentBucket, name);
        } else {
            String file = name.substring(idx);
            String parent = this.currentBucket + FORWARD_SLASH + name.substring(0, idx);
            return new FileInfo(parent, file);
        }
    }

    private VirtualFile fileFromCommonPrefix(CommonPrefix commonPrefix) {
        String name = commonPrefix.prefix().substring(0, commonPrefix.prefix().length() -1);
        FileInfo fileInfo = getFileInfo(name);
        return new VirtualFile(fileInfo.parent, fileInfo.file + FORWARD_SLASH, 0);
    }

    private VirtualFile fileFromObject(S3Object s3Object) {
        FileInfo fileInfo = getFileInfo(s3Object.key());
        return new VirtualFile(fileInfo.parent, fileInfo.file, s3Object.size());
    }

    @Override
    public File createFileObject(final String path) {
        return new VirtualFile(path);
    }

    @Override
    public File createFileObject(final File dir, final String filename) {
        Path fileObject;

        if (dir != null) {
            fileObject = Paths.get(dir.toPath().toString(), filename);
        } else {
            fileObject = Paths.get(filename);
        }
        return new VirtualFile(fileObject.toFile().getPath());
    }


    @Override
    public boolean isComputerNode(File dir) {
        return false;
    }

    @Override
    public Icon getSystemIcon(File f) {
        return null;
    }

    @Override
    public String getSystemTypeDescription(File f) {
        return f.toPath().toString();
    }

    @Override
    public String getSystemDisplayName(File f) {
        return f.getName();
    }

    @Override
    public File getDefaultDirectory() {
        return new VirtualFile("s3:/");
    }

    @Override
    public File getHomeDirectory() {
        return getDefaultDirectory();
    }

    @Override
    public boolean isFileSystemRoot(final File dir) {
        return hasBucket(dir);
    }

    @Override
    public boolean isHiddenFile(final File f) {
        return false;
    }

    @Override
    public boolean isFileSystem(final File f) {
        return !isFileSystemRoot(f);
    }

    @Override
    public File createNewFolder(File containingDir) throws IOException {
        return null;
    }

    @Override
    public File getChild(final File parent, final String fileName) {
        throw new UnsupportedOperationException("Not sure when this would make sense. Call getFiles instead.");
    }

    @Override
    public boolean isParent(final File folder, final File file) {
        return file.toPath().getParent().equals(folder.toPath());
    }

    @Override
    public Boolean isTraversable(final File f) {
        return f.isDirectory();
    }

    @Override
    public boolean isRoot(final File f) {
        return hasBucket(f);
    }

    private static boolean hasBucket(File f) {
        Pattern parseURI = Pattern.compile("^s3://?(?<bucket>[^/]+)/?(?<path>.*)$");
        return !parseURI.matcher(f.getPath()).matches();
    }

}
