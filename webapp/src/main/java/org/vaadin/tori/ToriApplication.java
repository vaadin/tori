package org.vaadin.tori;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator.NavigableApplication;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.spi.DataSourceProvider;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.Window;

/**
 * ToriApplication implements the ThreadLocal pattern where you can always get a
 * reference to the application instance by calling {@link #getCurrent()}
 * method.
 */
@SuppressWarnings("serial")
public class ToriApplication extends Application implements
        NavigableApplication, HttpServletRequestListener,
        PortletRequestListener {

    private static Logger log = Logger.getLogger(ToriApplication.class);
    private static ThreadLocal<ToriApplication> currentApplication = new ThreadLocal<ToriApplication>();

    private DataSource ds;

    @Override
    public void init() {
        checkThatCommonIsLoaded();
        resolveDataSource();
        setCurrentInstance();

        setTheme("tori");

        final Window mainWindow = new ToriWindow();
        setMainWindow(mainWindow);
    }

    /**
     * Verifies that the common project is in the classpath
     * 
     * @throws RuntimeException
     *             if Common is not in the classpath
     */
    private void checkThatCommonIsLoaded() {
        try {
            Class.forName("org.vaadin.tori.data.spi.DataSourceProvider");
        } catch (final ClassNotFoundException e) {
            log.error("Your project was apparently deployed without "
                    + "the Common project (common.jar) in its classpath");
            throw new RuntimeException(e);
        }
    }

    private void resolveDataSource() {
        try {
            final DataSourceProvider dsFactory = (DataSourceProvider) Class
                    .forName(DataSourceProvider.IMPLEMENTATION_CLASSNAME)
                    .newInstance();
            ds = dsFactory.createDataSource();
        } catch (final InstantiationException e) {
            log.error("Can't use the constructor for the current datasource's "
                    + DataSourceProvider.IMPLEMENTATION_CLASSNAME + ".");
            log.error("Make sure it is a non-abstract class with a public no-argument constructor.");
            throw new RuntimeException(e);
        } catch (final IllegalAccessException e) {
            log.error("DataSource's no-argument constructor seems non-public. It needs to be publicly accessible.");
            throw new RuntimeException(e);
        } catch (final ClassNotFoundException e) {
            log.error("It seems you don't have a DataSource in your classpath, "
                    + "or the added data source is misconfigured (see JavaDoc for "
                    + DataSourceProvider.class.getName() + ")");
            throw new RuntimeException(e);
        }
        log.info("Using DataSource implementation: " + ds.getClass().getName());
    }

    @Override
    public Window getWindow(final String name) {
        // Delegate the multiple browser window/tab handling to Navigator
        return ToriNavigator.getWindow(this, name, super.getWindow(name));
    }

    @Override
    public Window createNewWindow() {
        return new ToriWindow();
    }

    /**
     * Returns the ToriApplication instance that is bound to the currently
     * running Thread.
     * 
     * @return ToriApplication instance for the current Thread.
     */
    public static ToriApplication getCurrent() {
        return currentApplication.get();
    }

    /**
     * Returns the {@link DataSource} of this application.
     * 
     * @return {@link DataSource} of this application
     */
    public DataSource getDataSource() {
        return ds;
    }

    private void setCurrentInstance() {
        currentApplication.set(this);
    }

    private void removeCurrentInstance() {
        currentApplication.remove();
    }

    @Override
    public void onRequestStart(final HttpServletRequest request,
            final HttpServletResponse response) {
        setCurrentInstance();
    }

    @Override
    public void onRequestStart(final javax.portlet.PortletRequest request,
            final javax.portlet.PortletResponse response) {
        setCurrentInstance();
    }

    @Override
    public void onRequestEnd(final HttpServletRequest request,
            final HttpServletResponse response) {
        removeCurrentInstance();
    }

    @Override
    public void onRequestEnd(final javax.portlet.PortletRequest request,
            final javax.portlet.PortletResponse response) {
        removeCurrentInstance();
    }
}
