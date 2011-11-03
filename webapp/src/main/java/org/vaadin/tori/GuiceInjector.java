package org.vaadin.tori;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.TestDataSource;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Holder for a singleton {@link Injector} instance that is used by the
 * {@code ToriNavigator} to instantiate views enabling dependency injection in
 * views and presenters.
 */
class GuiceInjector {

    private static Injector injector;

    public static Injector getInstance() {
        if (injector == null) {
            injector = Guice.createInjector(new Module() {

                @Override
                public void configure(final Binder binder) {
                    binder.bind(DataSource.class).to(TestDataSource.class);
                }

            });
        }
        return injector;
    }

}
