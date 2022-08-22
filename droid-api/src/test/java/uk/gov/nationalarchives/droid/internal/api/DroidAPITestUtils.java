package uk.gov.nationalarchives.droid.internal.api;

import uk.gov.nationalarchives.droid.core.SignatureParseException;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class DroidAPITestUtils {
    public static DroidAPI createApi() throws SignatureParseException {
        Path signaturePath = Paths.get("../droid-results/custom_home/signature_files/DROID_SignatureFile_V96.xml");
        Path containerPath = Paths.get("../droid-results/custom_home/container_sigs/container-signature-20200121.xml");
        return DroidAPI.getInstance(signaturePath, containerPath);  //Create only once instance of Droid.
    }
}
