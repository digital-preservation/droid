package uk.gov.nationalarchives.droid.gui.help;

public final class AboutDialogData {
    private final String droidVersion;

    private final String javaVersion;

    private final String javaLocation;
    private final String operatingSystem;
    private final String droidFolder;

    /**
     * private constructor to force singleton
     * @param builder builer
     */
    private AboutDialogData(AboutDialogDataBuilder builder) {
        this.droidVersion = builder.builderDroidVersion;
        this.javaVersion = builder.builderJavaVersion;
        this.javaLocation = builder.builderJavaLocation;
        this.operatingSystem = builder.builderOperatingSystem;
        this.droidFolder = builder.builderDroidFolder;
    }

    public String getDroidVersion() {
        return droidVersion;
    }
    public String getJavaVersion() { return javaVersion;}
    public String getJavaLocation() { return javaLocation;}
    public String getOperatingSystem() { return operatingSystem;}
    public String getDroidFolder() { return droidFolder;}

    public static class AboutDialogDataBuilder {
        private String builderDroidVersion = "";
        private String builderJavaVersion = "";

        private String builderJavaLocation = "";
        private String builderOperatingSystem = "";
        private String builderDroidFolder = "";

        public AboutDialogDataBuilder() {
        }

        public AboutDialogDataBuilder withDroidVersion(String droidVersion) {
            this.builderDroidVersion = droidVersion;
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

        public AboutDialogData build() {
            return new AboutDialogData(this);
        }
    }
}
