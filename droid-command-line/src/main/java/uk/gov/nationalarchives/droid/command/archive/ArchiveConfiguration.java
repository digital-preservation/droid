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
package uk.gov.nationalarchives.droid.command.archive;

/**
 * configuration to expand web archives and archives.
 */
public class ArchiveConfiguration {
    private Boolean expandAllArchives;
    private String[] expandArchiveTypes;
    private Boolean expandAllWebArchives;
    private String[] expandWebArchiveTypes;

    /**
     *
     * @param expandAllArchives whether to expand all archives
     * @param expandArchiveTypes list of archive types to expand
     * @param expandAllWebArchives whether to expand all web archives
     * @param expandWebArchiveTypes list of web archive types to expand
     */
    public ArchiveConfiguration(Boolean expandAllArchives, String[] expandArchiveTypes, Boolean expandAllWebArchives, String[] expandWebArchiveTypes) {
        this.expandAllArchives = expandAllArchives;
        this.expandArchiveTypes = expandArchiveTypes;
        this.expandAllWebArchives = expandAllWebArchives;
        this.expandWebArchiveTypes = expandWebArchiveTypes;
    }

    /**
     * @return whether to expand all web archives
     */
    public Boolean getExpandAllWebArchives() {
        return expandAllWebArchives;
    }

    /**
     * @param expandAllWebArchives whether to expand all web archives
     */
    public void setExpandAllWebArchives(Boolean expandAllWebArchives) {
        this.expandAllWebArchives = expandAllWebArchives;
    }

    /**
     *
     * @return list of web archive types to expand
     */
    public String[] getExpandWebArchiveTypes() {
        return expandWebArchiveTypes;
    }

    /**
     *
     * @param expandWebArchiveTypes list of web archive types to expand
     */
    public void setExpandWebArchiveTypes(String[] expandWebArchiveTypes) {
        this.expandWebArchiveTypes = expandWebArchiveTypes;
    }

    /**
     * @return whether to expand all archives
     */
    public Boolean getExpandAllArchives() {
        return expandAllArchives;
    }
    /**
     * @param expandAllArchives whether to expand all archives
     */
    public void setExpandAllArchives(Boolean expandAllArchives) {
        this.expandAllArchives = expandAllArchives;
    }

    /**
     *
     * @return list of archive types to expand
     */
    public String[] getExpandArchiveTypes() {
        return expandArchiveTypes;
    }
    /**
     *
     * @param expandArchiveTypes list of archive types to expand
     */
    public void setExpandArchiveTypes(String[] expandArchiveTypes) {
        this.expandArchiveTypes = expandArchiveTypes;
    }
}
