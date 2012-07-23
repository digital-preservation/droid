/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
