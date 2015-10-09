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
import java.net.URISyntaxException;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;

/**
 * @author aretter/boreilly
 *
 */
public class UriTypeDescriptor extends AbstractTypeDescriptor<URI> {

    /**
     * UriTypeDescriptor.
     */
    public static final UriTypeDescriptor INSTANCE = new UriTypeDescriptor();

    private static final long serialVersionUID = -5394698215886892324L;

    /**
     * constructor.
     */
    public UriTypeDescriptor() {
        super(URI.class);
    }

    /**
     * @param value - the URI to convert to a String.
     * @return - the value converted to a string
     */
    public String toString(URI value) {
        /**
         * we copied Hibernate's UrlTypeDescriptor example, they were using URL.toExternalForm
         * not sure if URI.toString will cut-it here?
         */
        return value.toString();
    }

    /**
     * @param string - the string to convert to a URI.
     * @return - new URI from supplied string.
     */
    public URI fromString(String string) {
        try {
            return new URI(string);
        } catch (URISyntaxException e) {
            throw new HibernateException("Unable to convert string [" + string + "] to URI : " + e);
        }
    }

    /**
     * @param <X> - type to unwrap to, and return type of method.
     * @param value - value to unwrap .
     * @param type  - type to check if string is assignable.
     * @param options - not used.
     * @return - value unwrapped to string and then converted to type for generic param.
     */
    @SuppressWarnings("unchecked")
    public <X> X unwrap(URI value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (String.class.isAssignableFrom(type)) {
            return (X) toString(value);
        }
        throw unknownUnwrap(type);
    }

    /**
     * @param <X> - type to wrap to a URI.
     * @param value - value to wrap, of type X .
     * @param options - not used.
     * @return - URI from generic type.
     */
    public <X> URI wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (String.class.isInstance(value)) {
            return fromString((String) value);
        }
        throw unknownWrap(value.getClass());
    }
}
