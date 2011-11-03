package org.vaadin.tori.data;

public class DataSourceProvider implements
        org.vaadin.tori.data.spi.DataSourceProvider {

    @Override
    public DataSource createDataSource() {
        return new TestDataSource();
    }

}
