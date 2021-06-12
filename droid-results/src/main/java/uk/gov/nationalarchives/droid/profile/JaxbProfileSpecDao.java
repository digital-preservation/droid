/*
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
package uk.gov.nationalarchives.droid.profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author rflitcroft
 *
 */
public class JaxbProfileSpecDao implements ProfileSpecDao {

    private static final String PROFILE_XML = "profile.xml";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final JAXBContext context;
    
    /**
     * @throws JAXBException if the JAXBContext could not be instantiated.
     */
    public JaxbProfileSpecDao() throws JAXBException {
        context = JAXBContext.newInstance(ProfileInstance.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileInstance loadProfile(final InputStream in) {
        
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            final ProfileInstance profile = (ProfileInstance) unmarshaller.unmarshal(in);
            return profile;
        } catch (final JAXBException e) {
            log.error(e.getErrorCode(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProfile(final ProfileInstance profile, final Path profileHomeDir) {
        final Path profileXml = profileHomeDir.resolve(PROFILE_XML);
        try (final Writer out = Files.newBufferedWriter(profileXml, UTF_8, StandardOpenOption.CREATE)) {
            final Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(profile, out);
        } catch (final IOException | JAXBException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
