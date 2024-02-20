package uk.gov.nationalarchives.droid.export.template;

import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplateColumnDef;

public class ProfileResourceNodeColumnDef implements ExportTemplateColumnDef {

    private final String originalHeaderLabel;
    private final String headerLabel;

    public ProfileResourceNodeColumnDef(String originalHeaderLabel, String headerLabel) {
        this.originalHeaderLabel = originalHeaderLabel;
        this.headerLabel = headerLabel;
    }

    @Override
    public String getHeaderLabel() {
        return headerLabel;
    }

    @Override
    public Boolean isProfileNodeColumn() {
        return true;
    }

    @Override
    public String getOriginalColumnName() {
        return originalHeaderLabel;
    }

    @Override
    public String getDataValue() {
        throw new RuntimeException("Profile resource node column uses data from the profile results");
    }
}
