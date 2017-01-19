/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.core.interfaces.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * Created by rhubner on 1/17/17.
 */
public final class DroidUrlFormat {

    private static final Pattern WINDOWS_DRIVER_LETTER = Pattern.compile("^[A-Z]:$");
    private static final String WINDOWS_UNC_REFIX = "//";
    private static final String PATH_SPLITER = "/";

    private DroidUrlFormat() {
    };

    /**
     * Format URI base on Droid specific rules.
     * <ul>
     *     <li>Works only for <i>file</i> scheme</li>
     *     <li>Other schemas use default {@link URI#toString()}</li>
     * </ul>
     * @param uri URI.
     * @return String where are all possible "dangerous" character replaced with %xx encoding.
     */
    public static String format(URI uri) {

        StringBuilder b = new StringBuilder();
        if (!"file".equals(uri.getScheme())) {
            return uri.toString();
        }

        b.append(uri.getScheme()).append(':');

        if (uri.getPath().startsWith(WINDOWS_UNC_REFIX)) {  //Windows UNC path
            b.append(WINDOWS_UNC_REFIX);
        }

        if (uri.getAuthority() != null) {
            b.append(uri.getRawAuthority());
        }
        b.append(encodePath(uri.getPath()));

        if (uri.getQuery() != null) {
            b.append(uri.getRawQuery());
        }
        if (uri.getFragment() != null) {
            b.append(uri.getRawFragment());
        }

        return b.toString();
    }

    private static String encodePath(String path) {
        String[] parts = path.split(PATH_SPLITER);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i == 1 && WINDOWS_DRIVER_LETTER.matcher(parts[i]).matches()) {
                b.append(parts[i]).append('/');
            } else {
                b.append(encode(parts[i])).append('/');
            }
        }
        if (!path.endsWith(PATH_SPLITER)) {
            b.deleteCharAt(b.length() - 1);
        }
        return b.toString();
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding", ex);
        }
    }
}
