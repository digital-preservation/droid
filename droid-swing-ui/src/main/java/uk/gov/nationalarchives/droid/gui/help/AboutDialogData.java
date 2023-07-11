package uk.gov.nationalarchives.droid.gui.help;

public final class AboutDialogData {
    private final String droidVersion;
    private final String javaVersion;

    private final String javaLocation;
    private final String operatingSystem;

    /**
     * private constructor to force singleton
     * @param builder builer
     */
    private AboutDialogData(AboutDialogDataBuilder builder) {
        this.droidVersion = builder.builderDroidVersion;
        this.javaVersion = builder.builderJavaVersion;
        this.javaLocation = builder.javaLocation;
        this.operatingSystem = builder.operatingSystem;
    }

    public String getDroidVersion() {
        return droidVersion;
    }


    public static class AboutDialogDataBuilder {
        private String builderDroidVersion = "";
        private String builderJavaVersion = "";

        private String javaLocation = "";
        private String operatingSystem = "";

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

        public AboutDialogData build() {
            return new AboutDialogData(this);
        }
    }
}
