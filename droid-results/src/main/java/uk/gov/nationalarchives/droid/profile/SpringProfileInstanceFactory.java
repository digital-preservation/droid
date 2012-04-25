/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import uk.gov.nationalarchives.droid.profile.datasource.DerbyPooledDataSource;

/**
 * @author rflitcroft
 * 
 */
public class SpringProfileInstanceFactory implements ProfileInstanceLocator {

    /**
     * 
     */
    private static final String DATA_SOURCE_BEAN_NAME = "dataSource";

    private static final String PROFILE_MANAGER = "profileManager";

    private Map<String, GenericApplicationContext> profileInstanceManagers =
         new HashMap<String, GenericApplicationContext>();

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ProfileInstanceManager getProfileInstanceManager(
            ProfileInstance profile, Properties properties) {

        if (!profileInstanceManagers.containsKey(profile.getUuid())) {
            GenericApplicationContext ctx = new GenericApplicationContext();
            XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
            xmlReader.loadBeanDefinitions(new ClassPathResource[] {
                new ClassPathResource("META-INF/spring-jpa.xml"),
                new ClassPathResource("META-INF/spring-results.xml"), });
            xmlReader.setResourceLoader(ctx);
            xmlReader.setEntityResolver(new ResourceEntityResolver(ctx));

            PropertyPlaceholderConfigurer config = new PropertyPlaceholderConfigurer();
            config.setLocalOverride(true);
            config.setProperties(properties);
            config.setLocations(new ClassPathResource[] {
                new ClassPathResource("jpa.properties"),
                new ClassPathResource("archive-puids.properties"), });
            
            ctx.addBeanFactoryPostProcessor(config);
            ctx.refresh();
            //ctx.registerShutdownHook();
            profileInstanceManagers.put(profile.getUuid(), ctx);
        }

        ApplicationContext ctx = profileInstanceManagers.get(profile.getUuid());
        ProfileInstanceManager profileManager = (ProfileInstanceManager) ctx
                .getBean(PROFILE_MANAGER, ProfileInstanceManager.class);
        profileManager.setProfile(profile);
        return profileManager;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdownDatabase(String profileId) {
        ApplicationContext ctx = profileInstanceManagers.get(profileId);
        DerbyPooledDataSource dataSource = (DerbyPooledDataSource) ctx
                .getBean(DATA_SOURCE_BEAN_NAME);
        try {
            dataSource.shutdown();
            //CHECKSTYLE:OFF no choice here - the datasource throws Exception
        } catch (Exception e) {
            // CHECKSTYLE:ON
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void bootDatabase(String profileId) {
        ApplicationContext ctx = profileInstanceManagers.get(profileId);
        DerbyPooledDataSource dataSource = (DerbyPooledDataSource) ctx
                .getBean(DATA_SOURCE_BEAN_NAME);
        try {
            dataSource.init();
        } catch (SQLException e) {
            throw new ProfileException(e.getMessage(), e);
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection(String profileId) {
        ApplicationContext ctx = profileInstanceManagers.get(profileId);
        DerbyPooledDataSource dataSource = (DerbyPooledDataSource) ctx
                .getBean(DATA_SOURCE_BEAN_NAME);
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new ProfileException(e.getMessage(), e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void closeProfileInstance(String location) {
        GenericApplicationContext ctx = profileInstanceManagers
                .remove(location);
        if (ctx != null) {
            ctx.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ProfileInstanceManager getProfileInstanceManager(
            String profileId) {

        if (!profileInstanceManagers.containsKey(profileId)) {
            throw new IllegalArgumentException(String.format(
                    "No profile instance exists [%s]", profileId));
        }
        return (ProfileInstanceManager) profileInstanceManagers.get(profileId)
                .getBean(PROFILE_MANAGER);
    }

}
