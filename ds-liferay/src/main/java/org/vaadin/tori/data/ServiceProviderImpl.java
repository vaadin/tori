package org.vaadin.tori.data;

import org.vaadin.tori.data.spi.ServiceProvider;

public class ServiceProviderImpl implements ServiceProvider {

    @Override
    public DataSource createDataSource() {
        return new LiferayDataSource();
    }

}
