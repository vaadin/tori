package org.vaadin.tori.data;

import org.vaadin.tori.data.spi.ServiceProvider;
import org.vaadin.tori.util.LiferayPostFormatter;
import org.vaadin.tori.util.PostFormatter;

public class ServiceProviderImpl implements ServiceProvider {

    @Override
    public DataSource createDataSource() {
        return new LiferayDataSource();
    }

    @Override
    public PostFormatter createPostFormatter() {
        return new LiferayPostFormatter();
    }

}
