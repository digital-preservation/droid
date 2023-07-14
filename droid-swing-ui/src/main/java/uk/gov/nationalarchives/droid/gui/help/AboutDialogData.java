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
package uk.gov.nationalarchives.droid.gui.help;

public final class AboutDialogData {
    private final String droidVersion;
    private final String buildTimeStamp;
    private final String javaVersion;
    private final String javaLocation;
    private final String operatingSystem;
    private final String droidFolder;
    private final String logFolder;

    /**
     * private constructor used by the accompanying builder class
     * @param builder builer
     */
    private AboutDialogData(AboutDialogDataBuilder builder) {
        this.droidVersion = builder.builderDroidVersion;
        this.buildTimeStamp = builder.builderBuildTimeStamp;
        this.javaVersion = builder.builderJavaVersion;
        this.javaLocation = builder.builderJavaLocation;
        this.operatingSystem = builder.builderOperatingSystem;
        this.droidFolder = builder.builderDroidFolder;
        this.logFolder = builder.builderLogFolder;
    }

    public String getDroidVersion() {
        return droidVersion;
    }
    public String getBuildTimeStamp() {
        return buildTimeStamp;
    }
    public String getJavaVersion() { return javaVersion; }
    public String getJavaLocation() { return javaLocation; }
    public String getOperatingSystem() { return operatingSystem; }
    public String getDroidFolder() { return droidFolder; }
    public String getLogFolder() { return logFolder; }

    /**
     * Builder class for building the data related to about dialog.
     */
    public static class AboutDialogDataBuilder {
        private String builderDroidVersion = "";
        private String builderBuildTimeStamp = "";
        private String builderJavaVersion = "";
        private String builderJavaLocation = "";
        private String builderOperatingSystem = "";
        private String builderDroidFolder = "";
        private String builderLogFolder = "";


        public AboutDialogDataBuilder withDroidVersion(String droidVersion) {
            this.builderDroidVersion = droidVersion;
            return this;
        }
        public AboutDialogDataBuilder withBuildTimeStamp(String buildTimestamp) {
            this.builderBuildTimeStamp = buildTimestamp;
            return this;
        }

        public AboutDialogDataBuilder withJavaVersion(String javaVersion) {
            this.builderJavaVersion = javaVersion;
            return this;
        }

        public AboutDialogDataBuilder withJavaLocation(String javaLocation) {
            this.builderJavaLocation = javaLocation;
            return this;
        }

        public AboutDialogDataBuilder withOsName(String osName) {
            this.builderOperatingSystem = osName;
            return this;
        }

        public AboutDialogDataBuilder withDroidFolder(String droidFolder) {
            this.builderDroidFolder = droidFolder;
            return this;
        }

        public AboutDialogDataBuilder withLogFolder(String logFolder) {
            this.builderLogFolder = logFolder;
            return this;
        }

        public AboutDialogData build() {
            return new AboutDialogData(this);
        }
    }
}
