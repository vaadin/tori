package org.vaadin.tori.data.spi;

import java.util.ServiceLoader;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.SignatureFormatter;

/**
 * This interface needs to be implemented for the datasource project included in
 * Tori's WAR. Otherwise, errors will ensue upon launch. Each datasource
 * provider must place a configuration file in {@code META-INF/services}
 * directory as defined in the documentation of {@link ServiceLoader} class.
 * 
 * <br />
 * <br />
 * All factory methods in this interface are called only once per Application
 * instance (i.e. session) and the instance is reused by that instance. This
 * means that the returned service implementations are allowed to be stateful.
 * 
 * @see ServiceLoader
 */
public interface ServiceProvider {

    public static final String IMPLEMENTING_CLASSNAME = "org.vaadin.tori.data.ServiceProviderImpl";

    /**
     * Returns a new {@link DataSource} instance.
     */
    DataSource createDataSource();

    /**
     * Returns a new {@link PostFormatter} instance.
     */
    PostFormatter createPostFormatter();

    /**
     * Returns a new {@link AuthorizationService} instance.
     */
    AuthorizationService createAuthorizationService();

    /**
     * Returns a new {@link SignatureFormatter} instance.
     */
    SignatureFormatter createSignatureFormatter();

}
