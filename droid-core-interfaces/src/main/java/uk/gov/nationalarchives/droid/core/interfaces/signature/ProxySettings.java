/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
