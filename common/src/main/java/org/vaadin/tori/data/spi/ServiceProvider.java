package org.vaadin.tori.data.spi;

import org.vaadin.tori.data.DataSource;

/**
 * This interface needs to be implemented for the datasource project included in
 * Tori's WAR. Otherwise, errors will ensue upon launch.
 */
public interface ServiceProvider {

    public static final String IMPLEMENTING_CLASSNAME = "org.vaadin.tori.data.ServiceProviderImpl";

    /**
     * Returns a new {@link DataSource} instance.
     */
    DataSource createDataSource();

}
