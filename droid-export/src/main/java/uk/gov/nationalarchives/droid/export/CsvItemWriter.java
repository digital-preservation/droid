/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.export;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.com.bytecode.opencsv.CSVWriter;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;
import uk.gov.nationalarchives.droid.profile.NodeMetaData;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * @author rflitcroft
 *
 */
public class CsvItemWriter implements ItemWriter<ProfileResourceNode> {

    private static final String FILE_URI_SCHEME = "file";

    private static final String[] HEADERS = {
        "ID",
        "PARENT_ID",
        "URI",
        "FILE_PATH",
        "NAME",
        "METHOD",
        "STATUS",
        "SIZE",
        "TYPE",
        "EXT",
        "LAST_MODIFIED",
        "EXTENSION_MISMATCH",
        "MD5_HASH",
        "FORMAT_COUNT",
        "PUID",
        "MIME_TYPE",
        "FORMAT_NAME",
        "FORMAT_VERSION",
    };
    
    private final Log log = LogFactory.getLog(getClass());

    private CSVWriter csvWriter;
    private DroidGlobalConfig config;
    private FastDateFormat dateFormat = DateFormatUtils.ISO_DATETIME_FORMAT;
    private ExportOptions options = ExportOptions.ONE_ROW_PER_FILE;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void write(List<? extends ProfileResourceNode> nodes) {
        switch (options) {
            case ONE_ROW_PER_FILE: {
                writeOneRowPerFile(nodes);
                break;
            }
            case ONE_ROW_PER_FORMAT: {
                writeOneRowPerFormat(nodes);
                break;
            }
            default: {
                writeOneRowPerFile(nodes);
            }
        }
//        if (config.getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)) {
//            writeOneRowPerFormat(nodes);
//        } else {
//            writeOneRowPerFile(nodes);
//        }
    }
    
    private void writeOneRowPerFile(List<? extends ProfileResourceNode> nodes) {
        try {
            for (ProfileResourceNode node : nodes) {
                NodeMetaData metaData = node.getMetaData();
                List<String> nodeEntries = new ArrayList<String>();
                nodeEntries.add(nullSafeNumber(node.getId()));
                //ProfileResourceNode parent = node.getParent();
                //nodeEntries.add(parent != null ? nullSafeNumber(parent.getId()) : "");
                nodeEntries.add(nullSafeNumber(node.getParentId()));
                nodeEntries.add(node.getUri().toString());
                nodeEntries.add(toFilePath(node.getUri()));
                nodeEntries.add(toFileName(metaData.getName()));
                nodeEntries.add(nullSafeName(metaData.getIdentificationMethod()));
                nodeEntries.add(metaData.getNodeStatus().getStatus());
                nodeEntries.add(nullSafeNumber(metaData.getSize()));
                nodeEntries.add(metaData.getResourceType().getResourceType());
                nodeEntries.add(metaData.getExtension());
                nodeEntries.add(nullSafeDate(metaData.getLastModifiedDate(), dateFormat));
                nodeEntries.add(node.getExtensionMismatch().toString());
                nodeEntries.add(metaData.getHash());
                nodeEntries.add(nullSafeNumber(node.getIdentificationCount()));
                
                for (Format format : node.getFormatIdentifications()) {
                    nodeEntries.add(format.getPuid());
                    nodeEntries.add(format.getMimeType());
                    nodeEntries.add(format.getName());
                    nodeEntries.add(format.getVersion());
                }
                

                csvWriter.writeNext(nodeEntries.toArray(new String[0]));
            }
            csvWriter.flush();
            
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private void writeOneRowPerFormat(List<? extends ProfileResourceNode> nodes) {
        try {
            for (ProfileResourceNode node : nodes) {
                NodeMetaData metaData = node.getMetaData();
                //ProfileResourceNode parent = node.getParent();
                for (Format format : node.getFormatIdentifications()) {
                    String[] nodeEntries = new String[] {
                        nullSafeNumber(node.getId()),
                        //parent != null ? nullSafeNumber(parent.getId()) : "",
                        nullSafeNumber(node.getParentId()),
                        node.getUri().toString(),
                        toFilePath(node.getUri()),
                        toFileName(metaData.getName()),
                        nullSafeName(metaData.getIdentificationMethod()),
                        metaData.getNodeStatus().getStatus(),
                        nullSafeNumber(metaData.getSize()),
                        metaData.getResourceType().getResourceType(),
                        metaData.getExtension(),
                        nullSafeDate(metaData.getLastModifiedDate(), dateFormat),
                        node.getExtensionMismatch().toString(),
                        metaData.getHash(),
                        nullSafeNumber(node.getIdentificationCount()),
                        format.getPuid(),
                        format.getMimeType(),
                        format.getName(),
                        format.getVersion(),
                    };
                    csvWriter.writeNext(nodeEntries);
                }
            }
            csvWriter.flush();
            
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param csvWriter the csvWriter to write to.
     */
    void setCsvWriter(CSVWriter csvWriter) {
        this.csvWriter = csvWriter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(Writer writer) {
        csvWriter = new CSVWriter(writer);
        csvWriter.writeNext(HEADERS);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void setOptions(ExportOptions options) {
        this.options = options;
    }
    
    /**
     * Closes the CSV writer.
     */
    @Override
    public void close() {
        try {
            csvWriter.close();
        } catch (IOException e) {
            log.error("Error closing CSV output file.", e);
        }
    }
    
    private static String nullSafeName(Enum<?> value) {
        return value == null ? "" : value.toString();
    }
    
    private static String nullSafeNumber(Number number) {
        return number == null ? "" : number.toString();
    }
    
    private static String nullSafeDate(Date date, FastDateFormat format) {
        return date == null ? "" : format.format(date);
    }
    
    private static String toFilePath(URI uri) {
        if (FILE_URI_SCHEME.equals(uri.getScheme())) {
            return new File(uri).getAbsolutePath();
        }
        
        return null;
    }

    private static String toFileName(String name) {
        return FilenameUtils.getName(name);
    }
    
    /**
     * @param config the config to set
     */
    public void setConfig(DroidGlobalConfig config) {
        this.config = config;
    }

}
