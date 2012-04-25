/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile.referencedata;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * @author Alok Kumar Dash
 * 
 */

public class ReferenceDataDaoImpl implements ReferenceDataDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Format> getFormats() {
        Query q = null;
        List<Format> formats = null;

        String query = "from Format order by name";
        q = entityManager.createQuery(query);

        formats = q.getResultList();

        return formats;
    }

}
