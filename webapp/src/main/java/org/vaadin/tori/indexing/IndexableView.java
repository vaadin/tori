package org.vaadin.tori.indexing;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.DataSource;

public abstract class IndexableView {
    protected final List<String> arguments;
    protected final DataSource ds;

    public IndexableView(final List<String> arguments, final DataSource ds) {
        this.arguments = arguments;
        this.ds = ds;
    }

    abstract public String getXhtml();

    public static <T extends IndexableView> T newInstance(
            final Class<T> viewClass, final List<String> arguments,
            final DataSource dataSource) {
        try {
            final Constructor<T> constructor = viewClass.getConstructor(
                    List.class, DataSource.class);
            return constructor.newInstance(arguments, dataSource);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        } catch (final SecurityException e) {
            e.printStackTrace();
        }

        Logger.getLogger(IndexableView.class).error(
                "Improperly constructed " + IndexableView.class.getSimpleName()
                        + ": " + viewClass.getClass().getName());
        return null;
    }

    protected Logger getLogger() {
        return Logger.getLogger(getClass());
    }
}
