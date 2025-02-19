package uk.gov.nationalarchives.droid.core.interfaces.http;

import org.apache.commons.configuration.Configuration;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySubscriber;

import java.net.URI;

public class S3ClientFactory implements ProxySubscriber {

    private S3Client s3Client;

    private ProxySettings proxySettings = new ProxySettings();

    public S3ClientFactory(DroidGlobalConfig config) {
        config.getProperties().addConfigurationListener(proxySettings);
        Configuration configuration = config.getProperties();

        proxySettings = new ProxySettings();
        proxySettings.setEnabled(configuration.getBoolean(DroidGlobalProperty.UPDATE_USE_PROXY.getName()));
        proxySettings.setProxyHost(configuration.getString(DroidGlobalProperty.UPDATE_PROXY_HOST.getName()));
        proxySettings.setProxyPort(configuration.getInt(DroidGlobalProperty.UPDATE_PROXY_PORT.getName()));
        proxySettings.setEnabled(configuration.getBoolean(DroidGlobalProperty.UPDATE_USE_PROXY.getName()));

        config.getProperties().addConfigurationListener(proxySettings);
        proxySettings.addProxySubscriber(this);
        setS3Client(proxySettings);
    }

    @Override
    public void onProxyChange(ProxySettings changedProxySettings) {
        setS3Client(changedProxySettings);
    }

    public S3Client getS3Client() {
        return s3Client;
    }

    private void setS3Client(ProxySettings clientProxySettings) {
        S3ClientBuilder builder = S3Client.builder().region(Region.EU_WEST_2);
        if (clientProxySettings.isEnabled()) {
            ProxyConfiguration proxyConfiguration = ProxyConfiguration.builder()
                    .endpoint(URI.create("http://" + clientProxySettings.getProxyHost() + ":" + clientProxySettings.getProxyPort()))
                    .build();
            SdkHttpClient httpClient = ApacheHttpClient.builder()
                    .proxyConfiguration(proxyConfiguration)
                    .build();
            this.s3Client = builder.httpClient(httpClient).build();
        } else {
            this.s3Client = builder.build();
        }
    }
}
