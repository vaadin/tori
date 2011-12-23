package org.vaadin.tori.data;

import org.vaadin.tori.data.spi.ServiceProvider;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.TestAuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.SignatureFormatter;
import org.vaadin.tori.util.TestPostFormatter;
import org.vaadin.tori.util.TestSignatureFormatter;

public class TestServiceProvider implements ServiceProvider {

    @Override
    public DataSource createDataSource() {
        try {
            return new TestDataSource();
        } catch (final DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PostFormatter createPostFormatter() {
        return new TestPostFormatter();
    }

    @Override
    public AuthorizationService createAuthorizationService() {
        return new TestAuthorizationService();
    }

    @Override
    public SignatureFormatter createSignatureFormatter() {
        return new TestSignatureFormatter();
    }

}
