package uk.gov.nationalarchives.droid.profile;

import java.net.URI;
import java.nio.file.Path;

public class HttpProfileResource extends AbstractProfileResource {

    public HttpProfileResource(String uri) {
        super.setUri(URI.create(uri));
    }

    public static boolean isHttpUrl(String url) {
        try {
            String scheme = URI.create(url).getScheme();
            return scheme != null && (scheme.equals("http") || scheme.equals("https"));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isS3Object() {
        return false;
    }

    @Override
    public boolean isHttpObject() {
        return true;
    }

    @Override
    public void setSize(Path filePath) {}


    public void setUri(URI uri) {
        super.setUri(uri);
    }
}
