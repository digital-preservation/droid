/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;

import uk.gov.nationalarchives.droid.core.interfaces.filter.expressions.Criterion;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReader;
import uk.gov.nationalarchives.droid.planet.xml.dao.PlanetsXMLData;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;
import uk.gov.nationalarchives.droid.profile.referencedata.ReferenceData;
import uk.gov.nationalarchives.droid.report.dao.GroupByField;
import uk.gov.nationalarchives.droid.report.dao.ReportFieldEnum;
import uk.gov.nationalarchives.droid.report.dao.ReportLineItem;
import uk.gov.nationalarchives.droid.results.handlers.ProgressMonitor;

/**
 * @author rflitcroft
 * 
 */
public interface ProfileInstanceManager {

    /**
     * Populates a prfil ewith format data from a signature file.
     * @param signatureFileUri the URi of the signature file
     * 
     * @throws SignatureFileException
     *             if the signature file could not be read
     */
    void initProfile(URI signatureFileUri) throws SignatureFileException;

    /**
     * Starts a profile, which will complete in the future.
     * 
     * @return a future object for controlling the job.
     * @throws IOException file replay failed.
     */
    Future<?> start() throws IOException;

    /**
     * Cancels a running profile.
     */
    void cancel();

    /**
     * Pauses a running profile.
     */
    void pause();

    /**
     * Finds all root nodes.
     * 
     * @return root nodes
     */
    List<ProfileResourceNode> findRootProfileResourceNodes();

    /**
     * Finds the children of a profile resource node with the given parentId.
     * 
     * @param parentId
     *            the ID of the node to search under
     * @return Profile resource containing its immediate children
     */
    List<ProfileResourceNode> findAllProfileResourceNodes(Long parentId);

    /**
     * @return the progress moniot for this profile manager.
     */
    ProgressMonitor getProgressMonitor();

    /**
     * @param observer
     *            an object to be notified when a result is available.
     */
    void setResultsObserver(ProfileResultObserver observer);

    /**
     * @param profile
     *            the profile to be managed.
     */
    void setProfile(ProfileInstance profile);

    /**
     * Retrieves all the formats.
     * 
     * @return list of formats
     */
    List<Format> getAllFormats();

    /**
     * Loads the reference data for the selected profile.
     *            the profile to start
     * @return the new Profile Instance.
     */
    ReferenceData getReferenceData();

    /**
     * Sets the throttle value for this profile instance.
     * @param throttleValue the new Throttle value
     */
    void setThrottleValue(int throttleValue);
    
    /**
     * Gets a resource node item reader.
     * via a method-lookup
     * @return a new ItemReader
     */
    ItemReader<ProfileResourceNode> getNodeItemReader();
    
    /**
     * @deprecated Planets xml is generated using xslt transforms from normal reports now.
     * Gets data required for planets.
     * @return Planet xml data.
     */
    @Deprecated
    PlanetsXMLData getPlanetsData();
    
    /**
     * Gets data required for planets.
     * @param filter Filter to be applied to the report data.
     * @param  reportField reported field.
     * @param groupByFields
     *            Fields to group by, including any grouping functions associated with them.
     * @return Report xml data.
     */
    
    List<ReportLineItem> getReportData(Criterion filter, ReportFieldEnum reportField, 
            List<GroupByField> groupByFields);

}
