/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.signature;

/**
 * @author rflitcroft
 *
 */
public interface ProxySubscriber {

    /**]
     * Invoked when proxy settings change.
     * @param proxySettings the changed proxy settings.
     */
    void onProxyChange(ProxySettings proxySettings);
}
