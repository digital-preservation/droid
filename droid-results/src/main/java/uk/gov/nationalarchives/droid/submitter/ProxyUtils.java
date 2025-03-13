/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
