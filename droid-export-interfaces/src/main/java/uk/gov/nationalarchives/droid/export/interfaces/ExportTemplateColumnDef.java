package uk.gov.nationalarchives.droid.export.interfaces;

public interface ExportTemplateColumnDef {
    String getHeaderLabel();
    Boolean isProfileNodeColumn();
    String getOriginalColumnName();
    String getDataValue();
}
