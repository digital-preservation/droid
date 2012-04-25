/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.signature;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;

/**
 * Proxy server configuration.
 * @author rflitcroft
 *
 */
public class ProxySettings implements ConfigurationListener {

    private final Log log = LogFactory.getLog(getClass());
    
    private String proxyHost;
    private int proxyPort;
    private boolean enabled;
    
    private Set<ProxySubscriber> proxySubscribers = new HashSet<ProxySubscriber>();
    
    /**
     * @return the proxyHost
     */
    public String getProxyHost() {
        return proxyHost;
    }
    /**
     * @return a nnew instance
     */
    public static ProxySettings newInstance() {
        return new ProxySettings();
    }
    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        
    }
    /**
     * @param proxyHost the proxyHost to set
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }
    /**
     * @return the proxyPort
     */
    public int getProxyPort() {
        return proxyPort;
    }
    /**
     * @param proxyPort the proxyPort to set
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
    
    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void configurationChanged(ConfigurationEvent event) {
        final String propertyName = event.getPropertyName();
        if (propertyName.startsWith("update.proxy")) {
            DroidGlobalProperty property = DroidGlobalProperty.forName(propertyName);
            switch (property) {
                case UPDATE_PROXY_HOST: 
                    setProxyHost((String) event.getPropertyValue());
                    break;
                case UPDATE_PROXY_PORT:
                    setProxyPort((Integer) event.getPropertyValue());
                    break;
                case UPDATE_USE_PROXY:
                    setEnabled((Boolean) event.getPropertyValue());
                    break;
                default:
                    log.error(String.format("Invalid proxy setting [%s]", propertyName));
            }
            
            notifyProxySubscribers();
        }            
    }
    
    /**
     * Notifies all proxy subscribers of a proxy setting change.
     */
    public void notifyProxySubscribers() {
        for (ProxySubscriber subscriber : proxySubscribers) {
            subscriber.onProxyChange(this);
        }
    }
    
    /**
     * Adds a service that subscribes to these proxy settings.
     * @param proxySubscriber the subscriber to add.
     */
    public void addProxySubscriber(ProxySubscriber proxySubscriber) {
        proxySubscribers.add(proxySubscriber);
    }
}
