/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.filter.action;

import uk.gov.nationalarchives.droid.gui.filter.domain.DummyMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.FileExtensionMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.FileFormatMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.FileNameMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.FileSizeMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.FilterDomain;
import uk.gov.nationalarchives.droid.gui.filter.domain.FormatCountMetaData;
import uk.gov.nationalarchives.droid.gui.filter.domain.IdentificationMethodMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.JobStatusMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.LastModifiedDateMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.MimeTypeMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.PUIDMetadata;
import uk.gov.nationalarchives.droid.gui.filter.domain.ResourceTypeMetadata;
import uk.gov.nationalarchives.droid.profile.ProfileManager;

/**
 * @author Alok Kumar Dash
 * 
 */
public class InitialiseFilterAction {

    /**
     * Initialises Filter.
     * @param profileManager  ProfileManager of GUI
     * @param filterDomain FilterDomain (Meta-data Information)
     * @param profileId  selected Profile Id from GUI. 
     */

    public void initialiseFilter(String profileId,
            ProfileManager profileManager, FilterDomain filterDomain) {
        FileNameMetadata fileNameMetadata = new FileNameMetadata();
        FileSizeMetadata fileSizeMetadata = new FileSizeMetadata();
        FileExtensionMetadata fileExtensionMetaData = new FileExtensionMetadata();
        LastModifiedDateMetadata lastModifiedDateMetadata = new LastModifiedDateMetadata();
        ResourceTypeMetadata resourceTypeMetadata = new ResourceTypeMetadata();
        
        MimeTypeMetadata mimeTypeMetadata = new MimeTypeMetadata(profileManager
                .getReferenceData(profileId).getFormats());
        PUIDMetadata pUIDMetadata = new PUIDMetadata(profileManager
                .getReferenceData(profileId).getFormats());
        FileFormatMetadata fileFormatMetaData = new FileFormatMetadata(
                profileManager.getReferenceData(profileId).getFormats());
        IdentificationMethodMetadata identificationMethodMetadata = new IdentificationMethodMetadata();
        JobStatusMetadata jobStatusMetadata = new JobStatusMetadata();
        FormatCountMetaData formatCountMetaData = new FormatCountMetaData();
        
        DummyMetadata dummyMetadata = new DummyMetadata();

        filterDomain.addFilterCondition(dummyMetadata);
        filterDomain.addFilterCondition(fileNameMetadata);
        filterDomain.addFilterCondition(fileSizeMetadata);
        filterDomain.addFilterCondition(fileExtensionMetaData);
        filterDomain.addFilterCondition(lastModifiedDateMetadata);
        filterDomain.addFilterCondition(resourceTypeMetadata);
        filterDomain.addFilterCondition(mimeTypeMetadata);
        filterDomain.addFilterCondition(pUIDMetadata);
        filterDomain.addFilterCondition(fileFormatMetaData);
        filterDomain.addFilterCondition(identificationMethodMetadata);
        filterDomain.addFilterCondition(jobStatusMetadata);
        filterDomain.addFilterCondition(formatCountMetaData);
    }

}
