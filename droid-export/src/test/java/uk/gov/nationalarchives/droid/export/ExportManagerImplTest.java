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
package uk.gov.nationalarchives.droid.export;

import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReader;
import uk.gov.nationalarchives.droid.export.interfaces.ItemReaderCallback;
import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;
import uk.gov.nationalarchives.droid.export.interfaces.JobCancellationException;
import uk.gov.nationalarchives.droid.profile.ProfileContextLocator;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileInstanceManager;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * @author rflitcroft
 *
 */
public class ExportManagerImplTest {

    private ExportManagerImpl exportManager;
    
    private ItemReader<ProfileResourceNode> reader;
    private ItemWriter<ProfileResourceNode> writer;

    private ProfileContextLocator profileContextLocator;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        reader = mock(ItemReader.class);
        writer = mock(ItemWriter.class);
        
        profileContextLocator = mock(ProfileContextLocator.class);

        exportManager = new ExportManagerImpl();
        exportManager.setItemWriter(writer);
        exportManager.setProfileContextLocator(profileContextLocator);
    }

    @Test
    public void testExportNodesToCsv() throws Exception {
        
        ProfileInstanceManager profileInstanceManager = mock(ProfileInstanceManager.class);
        ProfileInstance profile1 = mock(ProfileInstance.class);
        
        when(profileContextLocator.getProfileInstance("profile1")).thenReturn(profile1);
        when(profileContextLocator.hasProfileContext(anyString())).thenReturn(true);
        when(profileInstanceManager.getNodeItemReader()).thenReturn(reader);
        when(profileContextLocator.openProfileInstanceManager(profile1)).thenReturn(profileInstanceManager);
        
        ProfileResourceNode node1 = new ProfileResourceNode(new URI("node1"));
        ProfileResourceNode node2 = new ProfileResourceNode(new URI("node2"));
        ProfileResourceNode node3 = new ProfileResourceNode(new URI("node3"));
       
//        FormatIdentification fi1 = new FormatIdentification();
//        FormatIdentification fi2 = new FormatIdentification();
//        FormatIdentification fi3 = new FormatIdentification();

        final List<ProfileResourceNode> fis = new ArrayList<ProfileResourceNode>();
        fis.add(node1);
        fis.add(node2);
        fis.add(node3);
        
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                ItemReaderCallback<ProfileResourceNode> callback = 
                    (ItemReaderCallback<ProfileResourceNode>) invocation.getArguments()[0];
                try {
                    callback.onItem(fis);
                } catch (JobCancellationException e) {
                    fail(e.getMessage());
                }
                return null;
            }
        }).when(reader).readAll(any(ItemReaderCallback.class), any(Filter.class));

        List<String> profileIdList = new ArrayList<String>();
        profileIdList.add("profile1");
        
        exportManager.exportProfiles(profileIdList, "destination", null, ExportOptions.ONE_ROW_PER_FILE).get();

        verify(writer).open(any(Writer.class));
        verify(writer).write(fis);
        verify(writer).close();
    }

}
