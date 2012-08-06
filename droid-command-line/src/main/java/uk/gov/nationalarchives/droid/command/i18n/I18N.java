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
package uk.gov.nationalarchives.droid.command.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author rflitcroft
 *
 */
public final class I18N {
    
    /** Options header. */
    public static final String OPTIONS_HEADER = "options.header";
    /** Help help. */
    public static final String HELP_HELP = "help.help";
    /** Version help. */
    public static final String VERSION_HELP = "version.help";
    /** Bad options. */
    public static final String BAD_OPTIONS = "invalid.options";
    
    /** Export description. */
    public static final String EXPORT_FILE_HELP = "export.file.help";

    /** Export description. */
    public static final String EXPORT_FORMAT_HELP = "export.format.help";
    
    
    /** Profiles description. */
    public static final String PROFILES_HELP = "profiles.help";
    /** Report description. */
    public static final String REPORT_HELP = "report.help";

    /** Report name. */
    public static final String REPORT_NAME_HELP = "report.name.help";
    
    /** Report type. */
    public static final String REPORT_TYPE_HELP = "report.type.help";
    
    /** Narrowing filter. */
    public static final String ALL_FILTER = "all.filter.help";
    /** Any filter (widening). */
    public static final String ANY_FILTER = "any.filter.help";
    
    /** List filter fields. */
    public static final String LIST_FILTER_FIELD = "filter.field.help";
    
    /** List reports. */
    public static final String LIST_REPORTS_HELP = "report.list.help";    
    
    /** Run a profile. */
    public static final String RUN_PROFILE_HELP = "profile.run.help";
    
    /** Run without a profile. */
    public static final String RUN_NO_PROFILE_HELP = "no_profile.run.help";
    
    /** Help for signature file. */
    public static final String SIGNATURE_FILE_HELP = "signature_file.help";
    
    /** Help for container signature file. */
    public static final String CONTAINER_SIGNATURE_FILE_HELP = "container_signature_file.help";
    
    /** Help for extension list. */
    public static final String EXTENSION_LIST_HELP = "extension_list.help";
    
    /** Help for archives. */
    public static final String ARCHIVES_HELP = "archives.help";
    
    /** Recurse subdirectories. */
    public static final String RECURSE_HELP = "recurse.help";
    
    /** Only log at ERROR and above to the console. */
    public static final String QUIET_HELP = "quiet.help";
    
    /** Check for signature update. */
    public static final String CHECK_SIGNATURE_UPDATE_HELP = "signature_update.check.help";
    
    /** Check signature updates - none available. */
    public static final String CHECK_SIGNATURE_UPDATE_UNAVAILABLE = "signature_update.check.unavailable";

    /** Check signature updates - success. */
    public static final String CHECK_SIGNATURE_UPDATE_SUCCESS = "signature_update.check.success";

    /** Check signature updates - error. */
    public static final String CHECK_SIGNATURE_UPDATE_ERROR = "signature_update.check.error";
    
    /** Download the latest signature update. */
    public static final String DOWNLOAD_SIGNATURE_UPDATE_HELP = "signature_update.download.help";

    /** Download the latest signature update. */
    public static final String DOWNLOAD_SIGNATURE_UPDATE_SUCCESS = "signature_update.download.success";

    /** Download the latest signature update. */
    public static final String DOWNLOAD_SIGNATURE_UPDATE_ERROR = "signature_update.download.error";
    
    /** Display the default signature file version help. */
    public static final String DEFAULT_SIGNATURE_VERSION_HELP = "signature.display_default.help";

    /** Display the default signature file version. */
    public static final String DEFAULT_SIGNATURE_VERSION = "signature.display";
    
    /** Configure default signature file version help. */
    public static final String CONFIGURE_DEFAULT_SIGNATURE_VERSION_HELP = "signature.configure_default.help";
    
    /** "Version". */
    public static final String VERSION = "version";
    
    /** Configure default signature file success message. */
    public static final String CONFIGURE_SIGNATURE_FILE_VERSION_SUCCESS = "signature.configure_default.success";
    
    /** Configure default signature file invalid message. */
    public static final String CONFIGURE_SIGNATURE_FILE_VERSION_INVALID = "signature.configure_default.invalid";
    
    /** List all signature file versions help. */
    public static final String LIST_SIGNATURE_VERSIONS_HELP = "signature.list_all.help";
    
    /** No Signature files available. */
    public static final String NO_SIG_FILES_AVAILABLE = "signature.none";
    
    /** No reports are defined. */
    public static final String NO_REPORTS_DEFINED = "reports.none";

    private I18N() { }
    
    /**
     * Resolves a key to an internationalised String.
     * @param key the key
     * @return the internationalised String
     */
    public static String getResource(final String key) {
        return ResourceBundle.getBundle("options").getString(key);
    }
    
    /**
     * Resolves a key to an internationalised String with replacement parameters.
     * @param key the key
     * @param params the parameters
     * @return the internationalised String
     */
    public static String getResource(final String key, Object... params) {
        String pattern = getResource(key);
        return MessageFormat.format(pattern, params);
    }

}
