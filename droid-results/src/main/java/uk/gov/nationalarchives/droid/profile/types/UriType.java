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
package uk.gov.nationalarchives.droid.profile.types;

import java.net.URI;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.StringType;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

/**
 * @author rflitcroft
 *
 */
public class UriType extends AbstractSingleColumnStandardBasicType<URI> implements DiscriminatorType<URI> {

    /**
     * .
     */  
    public static final UriType INSTANCE = new UriType();
    private static final long serialVersionUID = -9191945677317066123L;

    /**
     * constructor.
     */
    public UriType() {
        super(VarcharTypeDescriptor.INSTANCE, UriTypeDescriptor.INSTANCE);
    }

    /**
     * @return - the literal "uri".
     */    
    public String getName() {
        return "uri";
    }

    /**
     * @return - true.
     */
    @Override
    protected boolean registerUnderJavaType() {
        return true;
    }

    /**
     * @param value - the URI to convert to string.
     * @return - URI to String.
     */
    @Override
    public String toString(URI value) {
        return UriTypeDescriptor.INSTANCE.toString(value);
    }

    /**
     * @param value - the URI to convert to string.
     * @param dialect - the dialect to use.
     * @return - SQL String
     * @throws Exception if conversion fails
     */
    public String objectToSQLString(URI value, Dialect dialect) throws Exception {

        return StringType.INSTANCE.objectToSQLString(toString(value), dialect);
    }

    /**
     * @param xml - the xml string
     * @return - URI from string.
     */
    public URI stringToObject(String xml) {
        return UriTypeDescriptor.INSTANCE.fromString(xml);
    }
}
