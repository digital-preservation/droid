package uk.gov.nationalarchives.droid.export.template;

import uk.gov.nationalarchives.droid.export.interfaces.ExportTemplateColumnDef;

public class ConstantStringColumnDef implements ExportTemplateColumnDef {

    private final String headerLabel;
    private String dataValue;

    public ConstantStringColumnDef(String dataValue, String headerLabel) {
        this.dataValue = dataValue;
        this.headerLabel = headerLabel;
    }

    @Override
    public String getHeaderLabel() {
        return headerLabel;
    }

    @Override
    public Boolean isProfileNodeColumn() {
        return false;
    }

    @Override
    public String getOriginalColumnName() {
        throw new RuntimeException("Constant String Columns do not have an associated original column name");
    }

    @Override
    public String getDataValue() {
        return dataValue;
    }
}
