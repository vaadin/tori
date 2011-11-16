package org.vaadin.tori.data;

import org.vaadin.tori.data.spi.ServiceProvider;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.TestAuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.TestPostFormatter;

public class ServiceProviderImpl implements ServiceProvider {

    @Override
    public DataSource createDataSource() {
        return new TestDataSource();
    }

    @Override
    public PostFormatter createPostFormatter() {
        return new TestPostFormatter();
    }

    @Override
    public AuthorizationService createAuthorizationService() {
        return new TestAuthorizationService();
    }

}
