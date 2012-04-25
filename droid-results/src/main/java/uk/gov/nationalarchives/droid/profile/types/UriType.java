/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
