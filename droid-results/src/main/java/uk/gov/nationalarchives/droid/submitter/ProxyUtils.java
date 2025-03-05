package uk.gov.nationalarchives.droid.submitter;

import org.apache.commons.configuration.PropertiesConfiguration;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;

public class ProxyUtils {

    private DroidGlobalConfig config;

    public ProxyUtils(final DroidGlobalConfig config) {
        this.config = config;
    }

    public ProxySettings getProxySettings(AbstractProfileResource profileResource) {
        ProxySettings proxySettings = new ProxySettings();
        PropertiesConfiguration configProperties = config.getProperties();
        boolean proxyEnabledGlobally = configProperties.getBoolean(DroidGlobalProperty.UPDATE_USE_PROXY.getName());

        if (proxyEnabledGlobally) {
            proxySettings.setEnabled(true);
            proxySettings.setProxyHost(configProperties.getString(DroidGlobalProperty.UPDATE_PROXY_HOST.getName()));
            proxySettings.setProxyPort(configProperties.getInt(DroidGlobalProperty.UPDATE_PROXY_PORT.getName()));
            return proxySettings;
        } else if (profileResource.getProxy() != null) {
            proxySettings.setEnabled(true);
            proxySettings.setProxyHost(profileResource.getProxy().getHost());
            proxySettings.setProxyPort(profileResource.getProxy().getPort());
            return proxySettings;
        } else {
            proxySettings.setEnabled(false);
            return proxySettings;
        }
    }
}
