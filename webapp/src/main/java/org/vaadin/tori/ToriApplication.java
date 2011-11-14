package org.vaadin.tori;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator.NavigableApplication;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.data.spi.ServiceProvider;
import org.vaadin.tori.util.PostFormatter;

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
    private PostFormatter postFormatter;

    @Override
    public void init() {
        checkThatCommonIsLoaded();

        final ServiceProvider spi = getServiceProvider();
        ds = createDataSource(spi);
        postFormatter = createPostFormatter(spi);

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
            Class.forName("org.vaadin.tori.data.spi.ServiceProvider");
        } catch (final ClassNotFoundException e) {
            log.error("Your project was apparently deployed without "
                    + "the Common project (common.jar) in its classpath");
            throw new RuntimeException(e);
        }
    }

    private static ServiceProvider getServiceProvider() {
        try {
            final ServiceProvider dsFactory = (ServiceProvider) Class.forName(
                    ServiceProvider.IMPLEMENTING_CLASSNAME).newInstance();
            return dsFactory;
        } catch (final InstantiationException e) {
            log.error("Can't use the constructor for the current datasource's "
                    + ServiceProvider.IMPLEMENTING_CLASSNAME + ".");
            log.error("Make sure it is a non-abstract class with a public no-argument constructor.");
            throw new RuntimeException(e);
        } catch (final IllegalAccessException e) {
            log.error("DataSource's no-argument constructor seems non-public. It needs to be publicly accessible.");
            throw new RuntimeException(e);
        } catch (final ClassNotFoundException e) {
            log.error("It seems you don't have a DataSource in your classpath, "
                    + "or the added data source is misconfigured (see JavaDoc for "
                    + ServiceProvider.class.getName() + ")");
            throw new RuntimeException(e);
        }
    }

    private static DataSource createDataSource(final ServiceProvider spi) {
        final DataSource ds = spi.createDataSource();
        log.info(String.format("Using %s implementation: %s",
                DataSource.class.getSimpleName(), ds.getClass().getName()));
        return ds;
    }

    private static PostFormatter createPostFormatter(final ServiceProvider spi) {
        final PostFormatter postFormatter = spi.createPostFormatter();
        log.info(String.format("Using %s implementation: %s",
                PostFormatter.class.getSimpleName(), postFormatter.getClass()
                        .getName()));
        return postFormatter;
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
     * Returns the current {@link User} of the application or {@code null} if no
     * user is logged in.
     * 
     * @return the current {@link User} of the application or {@code null}.
     */
    public static User getCurrentUser() {
        return (User) currentApplication.get().getUser();
    }

    /**
     * Returns the {@link DataSource} of this application.
     * 
     * @return {@link DataSource} of this application
     */
    public DataSource getDataSource() {
        return ds;
    }

    public PostFormatter getPostFormatter() {
        return postFormatter;
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
