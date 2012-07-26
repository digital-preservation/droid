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
package uk.gov.nationalarchives.droid.command.action;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.command.filter.AntlrDqlParser;
import uk.gov.nationalarchives.droid.command.filter.CommandLineFilter;
import uk.gov.nationalarchives.droid.command.filter.CommandLineFilter.FilterType;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.export.interfaces.ExportManager;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author rflitcroft
 *
 */
public class ExportCommandTest {

    @Test
    public void testExportThreeProfiles() throws Exception {
        
        ExportManager exportManager = mock(ExportManager.class);
        ProfileManager profileManager = mock(ProfileManager.class);
        
        ProfileInstance profile1 = mock(ProfileInstance.class);
        when(profile1.getUuid()).thenReturn("profile1");
        
        ProfileInstance profile2 = mock(ProfileInstance.class);
        when(profile2.getUuid()).thenReturn("profile2");

        ProfileInstance profile3 = mock(ProfileInstance.class);
        when(profile3.getUuid()).thenReturn("profile3");

        when(profileManager.open(eq(new File("foo1")), any(ProgressObserver.class))).thenReturn(profile1);
        when(profileManager.open(eq(new File("foo2")), any(ProgressObserver.class))).thenReturn(profile2);
        when(profileManager.open(eq(new File("foo3")), any(ProgressObserver.class))).thenReturn(profile3);
        
        Future future = mock(Future.class);
        when(exportManager.exportProfiles(any(List.class), eq("destination"), (Filter) isNull(), eq(ExportOptions.ONE_ROW_PER_FORMAT))).thenReturn(future);
        
        ExportCommand command = new ExportCommand();
        
        String[] profileList = new String[] {"foo1", "foo2", "foo3"};
        command.setProfiles(profileList);
        command.setExportManager(exportManager);
        command.setProfileManager(profileManager);
        command.setDestination("destination");
        command.setExportOptions(ExportOptions.ONE_ROW_PER_FORMAT);
        
        command.execute();
        
        String[] expectedExportedProfiles = new String[] {
            "profile1", "profile2", "profile3",
        };
        
        verify(exportManager).exportProfiles(Arrays.asList(expectedExportedProfiles), "destination", null, ExportOptions.ONE_ROW_PER_FORMAT);
        
    }

    @Test
    public void testExportProfileWithNarrowFilter() throws Exception {
        
        ExportManager exportManager = mock(ExportManager.class);
        ProfileManager profileManager = mock(ProfileManager.class);
        
        ProfileInstance profile1 = mock(ProfileInstance.class);
        when(profile1.getUuid()).thenReturn("profile1");
        
        when(profileManager.open(eq(new File("foo1")), any(ProgressObserver.class))).thenReturn(profile1);
        
        Future future = mock(Future.class);
        when(exportManager.exportProfiles(any(List.class), eq("destination"), any(Filter.class), eq(ExportOptions.ONE_ROW_PER_FORMAT))).thenReturn(future);
        
        ExportCommand command = new ExportCommand();
        command.setDqlFilterParser(new AntlrDqlParser());
        
        String[] profileList = new String[] {"foo1"};
        command.setProfiles(profileList);
        command.setExportManager(exportManager);
        command.setProfileManager(profileManager);
        command.setDestination("destination");
        command.setExportOptions(ExportOptions.ONE_ROW_PER_FORMAT);
        
        CommandLineFilter cliFilter = new CommandLineFilter(
                new String[] {
                    "file_size = 720",
                    "puid any fmt/101 fmt/666",
                }, FilterType.ALL);
        
        command.setFilter(cliFilter);
        command.execute();
        
        
        String[] expectedExportedProfiles = new String[] {
            "profile1",
        };
        
        ArgumentCaptor<Filter> filterCaptor = ArgumentCaptor.forClass(Filter.class);
        verify(exportManager).exportProfiles(eq(Arrays.asList(expectedExportedProfiles)), 
                eq("destination"), filterCaptor.capture(), eq(ExportOptions.ONE_ROW_PER_FORMAT));

        
        Filter filter = filterCaptor.getValue();
        final List<FilterCriterion> criteria = filter.getCriteria();
        assertEquals(2, criteria.size());
        
        FilterCriterion sizeCriterion = criteria.get(0);
        FilterCriterion puidCriterion = criteria.get(1);
        
        assertEquals(CriterionOperator.EQ, sizeCriterion.getOperator());
        assertEquals(CriterionFieldEnum.FILE_SIZE, sizeCriterion.getField());
        assertEquals(Long.valueOf(720), sizeCriterion.getValue());
        
        assertEquals(CriterionOperator.ANY_OF, puidCriterion.getOperator());
        assertEquals(CriterionFieldEnum.PUID, puidCriterion.getField());
        assertArrayEquals(new Object[] {"fmt/101", "fmt/666"}, (Object[]) puidCriterion.getValue());
    }
}
