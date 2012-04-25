/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile.export;

import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;

/**
 * @author rflitcroft
 *
 */
public final class HibernateSessionFactoryLocator {

    private HibernateSessionFactoryLocator() { }
    
    //private HibernateEntityManagerFactory entityManagerFactory;
    
    /**
     * Gets the hibernate session factory from the entitymanager factory.
     * @param emf an entity manager factory (must a hibernate one!)
     * @return hibernate session factory
     */
    public static SessionFactory getSessionFactory(HibernateEntityManagerFactory emf) {
        return emf.getSessionFactory();
    }

//    /**
//     * @param entityManagerFactory the entityManagerFactory to set
//     */
//    public void setEntityManagerFactory(
//            HibernateEntityManagerFactory entityManagerFactory) {
//        this.entityManagerFactory = entityManagerFactory;
//    }
//    
//    /**
//     * @return the hibernate session factory
//     */
//    public SessionFactory getSessionFactory() {
//        return entityManagerFactory.getSessionFactory();
//    }
}
