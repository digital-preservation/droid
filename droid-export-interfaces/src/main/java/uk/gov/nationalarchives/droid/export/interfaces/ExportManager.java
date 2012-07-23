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
package uk.gov.nationalarchives.droid.export.interfaces;

import java.util.List;
import java.util.concurrent.Future;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;


/**
 * @author rflitcroft
 *
 */
public interface ExportManager {

    /**
     * Exports one or more profiles to a CSV file.
     * 
     * FIXME:
     * The only reason this interface takes an optional filter
     * is so that the command line can pass in one of its own 
     * filters.  This is because various parts of the code
     * use a particular implementation of Filter (FilterImpl)
     * rather than using the Filter interface (and modifying
     * it appropriately to be generally useful).
     * The upshot is that you cannot set a generic Filter 
     * on a given profile, only a FilterImpl, so if you want
     * to use a different kind of filter (as the command line 
     * does), you have to override the use of it in each profile,
     * rather than just setting it on the profile directly.
     * 
     * @param profileIds the list of profiles to export.
     * @param destination the destination filename
     * @param filter optional filter
     * @param options the options for export.
     * @return future for cancelling the task. 
     */
    Future<?> exportProfiles(List<String> profileIds, String destination, Filter filter, ExportOptions options);

}
