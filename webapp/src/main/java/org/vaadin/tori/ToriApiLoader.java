package org.vaadin.tori;

import java.util.ServiceLoader;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.spi.ServiceProvider;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.SignatureFormatter;

public class ToriApiLoader {

    private final ServiceProvider spi;

    public ToriApiLoader() {
        checkThatCommonIsLoaded();
        spi = newServiceProvider();
    }

    /**
     * Verifies that the common project is in the classpath
     * 
     * @throws RuntimeException
     *             if Common is not in the classpath
     */
    private static void checkThatCommonIsLoaded() {
        try {
            Class.forName("org.vaadin.tori.data.spi.ServiceProvider");
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException("Your project was "
                    + "apparently deployed without the Common "
                    + "project (common.jar) in its classpath", e);
        }
    }

    private static ServiceProvider newServiceProvider() {
        final ServiceLoader<ServiceProvider> loader = ServiceLoader
                .load(ServiceProvider.class);
        if (loader.iterator().hasNext()) {
            return loader.iterator().next();
        } else {
            throw new RuntimeException(
                    "It seems you don't have a DataSource in your classpath, "
                            + "or the added data source is misconfigured (see JavaDoc for "
                            + ServiceProvider.class.getName() + ").");
        }
    }

    public DataSource createDataSource() {
        final DataSource ds = spi.createDataSource();
        getLogger().info(
                String.format("Using %s implementation: %s", DataSource.class
                        .getSimpleName(), ds.getClass().getName()));
        return ds;
    }

    public PostFormatter createPostFormatter() {
        final PostFormatter postFormatter = spi.createPostFormatter();
        getLogger().info(
                String.format("Using %s implementation: %s",
                        PostFormatter.class.getSimpleName(), postFormatter
                                .getClass().getName()));
        return postFormatter;
    }

    public SignatureFormatter createSignatureFormatter() {
        final SignatureFormatter signatureFormatter = spi
                .createSignatureFormatter();
        getLogger().info(
                String.format("Using %s implementation: %s",
                        SignatureFormatter.class.getSimpleName(),
                        signatureFormatter.getClass().getName()));
        return signatureFormatter;
    }

    public AuthorizationService createAuthorizationService() {
        final AuthorizationService authorizationService = spi
                .createAuthorizationService();
        getLogger().info(
                String.format("Using %s implementation: %s",
                        PostFormatter.class.getSimpleName(),
                        authorizationService.getClass().getName()));
        return authorizationService;
    }

    private static Logger getLogger() {
        return Logger.getLogger(ToriApiLoader.class);
    }

}
