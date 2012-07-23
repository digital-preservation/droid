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
package uk.gov.nationalarchives.droid.profile;

import java.sql.Connection;
import java.util.Properties;

/**
 * @author rflitcroft
 *
 */
public interface ProfileInstanceLocator {

    /**
     * Returns a profile instance manager which will manage the profile instance at the specified
     * location.
     * @param profile - the filesystem path where the profile instance is located.
     * @param properties - properties to be passed to the profile instance.
     * @return a profile instance manager
     */
    ProfileInstanceManager getProfileInstanceManager(ProfileInstance profile, Properties properties);

    /**
     * Closes the specified profile instrance an cleans up all associated resources.
     * @param location - the filesystem path where the profile instance is located.
     */
    void closeProfileInstance(String location);

    /**
     * Returns a profile instance manager which will manage the profile instance at the specified
     * location.
     * @param profileId the ID of the profile
     * @return a profile instance manager
     */
    ProfileInstanceManager getProfileInstanceManager(String profileId);

    /**
     * Returns a connection to the profile database.
     * @param profileId the ID of the profile.
     * @return A connection to the database.
     */
    Connection getConnection(String profileId);
    
    /**
     * Shuts down the profile database.
     * @param profileId the ID of the profile.
     */
    void shutdownDatabase(String profileId);

    /**
     * Starts a profile database.
     * @param profileId the profile ID
     */
    void bootDatabase(String profileId);
}
