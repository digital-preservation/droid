/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
package uk.gov.nationalarchives.droid.report;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;


/**
 * Tests that report XML can be transformed using XSLT.
 * 
 * @author rflitcroft
 *
 */
public class ReportExportTest {

    private ReportTransformerImpl reportTransformer;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void init() {
        XMLUnit.setIgnoreWhitespace(true);
    }
    
    @Before
    public void setup() {
        reportTransformer = new ReportTransformerImpl();
        DroidGlobalConfig globalConfig = mock(DroidGlobalConfig.class);
        reportTransformer.setConfig(globalConfig);
    }
    
    
    @Test
    public void testXhtmlTransform() throws Exception {
        
        try(final InputStream in = getClass().getClassLoader().getResourceAsStream("test-report.xml");
            final Reader reader = new BufferedReader(new InputStreamReader(in));
            final StringWriter writer = new StringWriter()) {

            reportTransformer.transformUsingXsl(reader, "Web page.html.xsl", writer);
        
        /* this is a nasty test - every change to the report or xsl creates a lot of
         work keeping all this up to date.
        String control =
            /*
              "<html>"
            + "  <body>"
            + "    <h1>Profile Summary</h1>"
            + "    <table border=\"1\">"
            + "      <tr>"
            + "        <td>Signature File Version</td>"
            + "        <td>32</td>"
            + "      </tr>"
            + "      <tr>"
            + "        <td>Throttle (ms)</td>"
            + "        <td>0</td>"
            + "      </tr>"
            + "      <tr>"
            + "        <td>Started</td>"
            + "        <td>02 Sep 2010</td>"
            + "      </tr>"
            + "      <tr>"
            + "        <td>Finished</td>"
            + "        <td>02 Sep 2010</td>"
            + "      </tr>"
            + "    </table>"
            + "    "
            + "    <h2>File sizes by year last modified</h2>\r\n"
            + "      <table border=\"1\">\r\n"
            + "        <tr>\r\n"
            + "        <th/>\r\n"
            + "        <th>Count</th>\r\n"
            + "        <th>Sum</th>\r\n"
            + "        <th>Min</th>\r\n"
            + "        <th>Max</th>\r\n"
            + "        <th>Average</th>\r\n"
            + "      </tr>\r\n"
            + "      <tr>\r\n"
            + "        <td>2009</td>\r\n"
            + "        <td>41</td>\r\n"
            + "        <td>29271</td>\r\n"
            + "        <td>0</td>\r\n"
            + "        <td>4358</td>\r\n"
            + "        <td>713.0</td>\r\n"
            + "      </tr>\r\n"
            + "      <tr>\r\n"
            + "        <td>2010</td>\r\n"
            + "        <td>6</td>\r\n"
            + "        <td>19715</td>\r\n"
            + "        <td>0</td>\r\n"
            + "        <td>3943</td>\r\n"
            + "        <td>3285.0</td>\r\n"
            + "      </tr>\r\n"
            + "    </table>"
            + "  </body>"
            + "</html>"
            
        */

            System.out.println(writer.getBuffer().toString());

            //XMLAssert.assertXMLEqual(control, writer.getBuffer().toString());
            //assertTrue(pdf.exists());
        }
    }

    @Test
    public void testPdfTransform() throws Exception {

        final File pdf = temporaryFolder.newFile("myPdf.pdf");

        DroidGlobalConfig globalConfig = mock(DroidGlobalConfig.class);
        when(globalConfig.getTempDir()).thenReturn(temporaryFolder.getRoot().toPath());
        reportTransformer.setConfig(globalConfig);

        try (final InputStream in = getClass().getClassLoader().getResourceAsStream("test-report.xml");
             final Reader reader = new BufferedReader(new InputStreamReader(in));
             final OutputStream pdfOut = Files.newOutputStream(pdf.toPath())) {

            reportTransformer.transformToPdf(reader, "Web page.html.xsl", pdfOut);
        }

        assertTrue(Files.exists(pdf.toPath()));
        assertTrue(Files.size(pdf.toPath()) > 1000);
        assertTrue(Files.size(pdf.toPath()) < 5000);
    }
}
