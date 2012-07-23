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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.type.StringType;

/**
 * @author rflitcroft
 *
 */
public class UriType extends StringType {

    private static final long serialVersionUID = -5484571563787862267L;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(ResultSet rs, String name) throws SQLException {
        return newUri(rs.getString(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<URI> getReturnedClass() {
        return URI.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(PreparedStatement st, Object value, int index) throws SQLException {
        st.setString(index, value == null ? null : value.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sqlType() {
        return Types.VARCHAR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "uri"; 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object stringToObject(String xml) {
        return newUri(xml);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromStringValue(String xml) {
        return newUri(xml);
    }
    
    private static URI newUri(String s) {
        try {
            return s == null ? null : new URI(s);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
}
