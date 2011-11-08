package org.vaadin.tori.data.spi;

import org.vaadin.tori.data.DataSource;

/**
 * Each datasource is required to implement this interface and the
 * implementation must be named {@value #IMPLEMENTATION_CLASSNAME}. The class
 * name is reflectively looked up by the application.
 */
public interface DataSourceProvider {

    public static final String IMPLEMENTATION_CLASSNAME = "org.vaadin.tori.data.DataSourceProviderImpl";

    /**
     * Returns a new DataSource instance.
     * 
     * @return a new DataSource instance.
     */
    DataSource createDataSource();

}
