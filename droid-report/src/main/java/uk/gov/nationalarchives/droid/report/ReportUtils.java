/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.RestrictionFactory;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Criterion;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Junction;
import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Restrictions;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.report.dao.ReportLineItem;
import uk.gov.nationalarchives.droid.report.interfaces.ProfileReportData;

/**
 * @author rflitcroft
 *
 */
public final class ReportUtils {

    private static final String ROOT_FOLDER = "/";
    
    // This map of resources is only used to copy in the pre-built reports and transforms
    // to the droid user folder.  Reports and transforms used by the application are dynamically
    // picked up from the droid user folder, so new reports and transforms can be added by the user.
    private static final Map<String, List<String>> REPORT_DEFS;
    static {
        List<String> noTransforms = new ArrayList<String>();
        List<String> generalFiles = new ArrayList<String>();
        List<String> planetTransforms = new ArrayList<String>();
        generalFiles.add("Web page.html.xsl");
        generalFiles.add("Text.txt.xsl");
        //generalFiles.add("droidlogo.gif");
        planetTransforms.add("Planets XML.xml.xsl");
        REPORT_DEFS = new HashMap<String, List<String>>();        
        REPORT_DEFS.put(ROOT_FOLDER, generalFiles);
        REPORT_DEFS.put("Total count of files and folders.xml", noTransforms);
        REPORT_DEFS.put("Total unreadable files.xml", noTransforms);
        REPORT_DEFS.put("Total unreadable folders.xml", noTransforms);
        REPORT_DEFS.put("File count and sizes.xml", noTransforms);
        REPORT_DEFS.put("File count and sizes by file extension.xml", noTransforms);
        REPORT_DEFS.put("File count and sizes by file format PUID.xml", noTransforms);
        REPORT_DEFS.put("File count and sizes by mime type.xml", noTransforms);
        REPORT_DEFS.put("File count and sizes by year last modified.xml", noTransforms);
        REPORT_DEFS.put("File count and sizes by month last modified.xml", noTransforms);
        REPORT_DEFS.put("File count and sizes by year and month last modified.xml", noTransforms);
        REPORT_DEFS.put("Comprehensive breakdown.xml", planetTransforms);
    }
    
    private ReportUtils() { }
    
    /**
     * Transforms a List of resources to a List of resource paths.
     * @param resources the resources to transform
     * @return List of resource Paths
     */
    static List<String> toResourcePaths(List<AbstractProfileResource> resources) {
        List<String> profileResourcePaths = new ArrayList<String>();

        for (AbstractProfileResource resource : resources) {
            profileResourcePaths.add(new File(resource.getUri()).getPath());
        }

        return profileResourcePaths;
    }
    
    /**
     * Builds a new ProfileReportData object from a ReportLineItem.
     * @param profile the profile
     * @param reportLineItem the report line item.
     * @return a new ProfileReportData object
     */
    static ProfileReportData buildProfileReportData(ProfileInstance profile, ReportLineItem reportLineItem) {
        ProfileReportData data = new ProfileReportData();
        data.setProfileName(profile.getName());
        data.setProfileId(profile.getUuid());
        data.setCount(reportLineItem.getCount());
        data.setSum(reportLineItem.getSum());
        data.setAverage(reportLineItem.getAverage());
        data.setMin(reportLineItem.getMinimum());
        data.setMax(reportLineItem.getMaximum());

        return data;
    }
    
    /**
     * Builds a criterion filter from the filters supplied.
     * @param f1 filter 1
     * @param f2 filter 2
     * @return a criterion
     */
    static Criterion buildFilter(Filter f1, Filter f2) {
        Junction outerConjunction = Restrictions.conjunction();

        // Add the profile filter criteria
        if (f1 != null && f1.isEnabled()) {
            Junction profileCriteria = f1.isNarrowed() ? Restrictions.conjunction() 
                    : Restrictions.disjunction();
            for (FilterCriterion profileCriterion : f1.getCriteria()) {
                profileCriteria.add(RestrictionFactory.forFilterCriterion(profileCriterion));
            }
            outerConjunction.add(profileCriteria);
        }
        
        // Add the profile filter criteria
        if (f2 != null) {
            Junction reportItemCriteria = f2.isNarrowed() ? Restrictions.conjunction() 
                    : Restrictions.disjunction();
            for (FilterCriterion profileCriterion : f2.getCriteria()) {
                reportItemCriteria.add(RestrictionFactory.forFilterCriterion(profileCriterion));
            }
            outerConjunction.add(reportItemCriteria);
        }
        
        return outerConjunction;
        
    }
    
    
    
    /**
     * Populates the destinationDirectoey with the resources specified, using the classloader given.
     * @param destinationDir destination directory
     * @param classLoader the classloader used to locate resources
     * @throws IOException if a resource could not be copied
     */
    static void populateReportDefinitionsDirectory(File destinationDir, ClassLoader classLoader) 
        throws IOException {
        
        for (String reportDefFilename : REPORT_DEFS.keySet()) {
            List<String> transformList = REPORT_DEFS.get(reportDefFilename);
            // Root - no reports, just transforms:
            if (ROOT_FOLDER.equals(reportDefFilename)) {
                copyTransforms(destinationDir, transformList, classLoader);
            } else {
                String reportDirName = FilenameUtils.getBaseName(reportDefFilename);
                File reportDir = new File(destinationDir, reportDirName);
                reportDir.mkdir();
                copyResourceToFile(reportDefFilename, reportDir, classLoader);
                copyTransforms(reportDir, transformList, classLoader);
            }
        }
    }

    private static void copyTransforms(File destinationDir, 
            List<String> transformNames, ClassLoader classLoader) throws IOException {
        for (String transformName : transformNames) {
            copyResourceToFile(transformName, destinationDir, classLoader);
        }
    }
    
    // Copies a resource if the file doesn't already exist.
    private static void copyResourceToFile(String resourceName,
            File destinationDir, ClassLoader classLoader) throws IOException {
        File reportDefFile = new File(destinationDir, resourceName);
        if (!reportDefFile.exists()) {
            reportDefFile.createNewFile();
            OutputStream out = new FileOutputStream(reportDefFile);
            InputStream in = classLoader.getResourceAsStream(resourceName);
            IOUtils.copy(in, out);
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
   
}
