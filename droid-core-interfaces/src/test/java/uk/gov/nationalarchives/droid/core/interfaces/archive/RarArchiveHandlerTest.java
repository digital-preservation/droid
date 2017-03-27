package uk.gov.nationalarchives.droid.core.interfaces.archive;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;



/**
 * Created by rhubner on 3/21/17.
 */
public class RarArchiveHandlerTest {

    @Test
    public void simpleTest() throws IOException, RarException {

        FileVolumeManager manager = new FileVolumeManager(new File("/home/rhubner/Downloads/sample.rar"));
        Archive archive = new Archive(manager);

        List<FileHeader> headers = archive.getFileHeaders();

        assertEquals(1, headers.size());

        InputStream a = archive.getInputStream(headers.get(0));

        archive.close();


    }



}
