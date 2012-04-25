/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.command.action.ReportCommand;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.report.interfaces.CancellableProgressObserver;
import uk.gov.nationalarchives.droid.report.interfaces.Report;
import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;
import uk.gov.nationalarchives.droid.report.interfaces.ReportRequest;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpec;
import uk.gov.nationalarchives.droid.report.interfaces.ReportXmlWriter;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author Alok Kumar Dash
 */
//@Ignore("these tests take too long")
public class ReportCommandTest {

    private ReportCommand reportCommand;
    
    private ProfileManager profileManager;
    private ReportManager reportManager;

    @Before
    public void setup() {
        reportCommand = new ReportCommand();

        profileManager = mock(ProfileManager.class);
        reportManager = mock(ReportManager.class);
        
        reportCommand.setProfileManager(profileManager);
        reportCommand.setReportManager(reportManager);
        
    }


    @Test
    public void testReportCommandWithNonExistentCustomReportName() throws Exception {
        final String profileLocation = "profiles/12345.droid";
        final String destination = "destination.xml";
        final String profileId = "12345";
        
        final ProfileInstance profile = mock(ProfileInstance.class);
        when(profile.getUuid()).thenReturn(profileId);
        
        when(profileManager.open(eq(new File(profileLocation)), any(ProgressObserver.class)))
            .thenReturn(profile);
        
        reportCommand.setReportType("MyCustomReport");
        reportCommand.setProfiles(new String[] {profileLocation});
        reportCommand.setDestination(destination);
        
        try {
            reportCommand.execute();
            fail("Expected Command Line execeution exception.");
        } catch (CommandExecutionException e) {
            assertEquals("Report [MyCustomReport] not found.", e.getMessage());
        }
        
        verify(reportManager, never()).generateReport(any(ReportRequest.class), (Filter) isNull(), any(CancellableProgressObserver.class));
    }

    @Test
    public void testReportCommandWithCustomReportName() throws Exception {
        final String profileLocation = "profiles/12345.droid";
        final String destination = "destination.xml";
        final String profileId = "12345";
        
        final ProfileInstance profile = mock(ProfileInstance.class);
        when(profile.getUuid()).thenReturn(profileId);
        
        when(profileManager.open(eq(new File(profileLocation)), any(ProgressObserver.class)))
            .thenReturn(profile);
        
        reportCommand.setReportType("MyCustomReport");
        reportCommand.setProfiles(new String[] {profileLocation});
        reportCommand.setDestination(destination);
        reportCommand.setReportOutputType("DROID Report XML");
        
        ReportXmlWriter reportXmlWriter = mock(ReportXmlWriter.class);
        reportCommand.setReportXmlWriter(reportXmlWriter);
        
        final ReportSpec reportSpec = new ReportSpec();
        reportSpec.setName("MyCustomReport");
        
        when(reportManager.listReportSpecs()).thenReturn(Arrays.asList(new ReportSpec[] {reportSpec}));
        Report report = new Report();
        
        ArgumentCaptor<ReportRequest> requestCaptor = ArgumentCaptor.forClass(ReportRequest.class);
        when(reportManager.generateReport(requestCaptor.capture(), (Filter) isNull(), (CancellableProgressObserver) isNull()))
            .thenReturn(report);
        
        reportCommand.execute();

        ReportRequest reportRequest = requestCaptor.getValue();
        
        assertEquals(1, reportRequest.getProfileIds().size());
        assertEquals("12345", reportRequest.getProfileIds().get(0));
        
        verify(reportXmlWriter).writeReport(eq(report), any(FileWriter.class));
    }
}
