/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
