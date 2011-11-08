package org.vaadin.tori.data;

import org.vaadin.tori.data.spi.DataSourceProvider;

public class DataSourceProviderImpl implements DataSourceProvider {

    @Override
    public DataSource createDataSource() {
        return new LiferayDataSource();
    }

}
