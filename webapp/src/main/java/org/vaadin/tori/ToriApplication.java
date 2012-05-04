package org.vaadin.tori;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ServiceLoader;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator.NavigableApplication;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.DebugDataSource;
import org.vaadin.tori.data.spi.ServiceProvider;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.util.EventBus;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.SignatureFormatter;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.RequestHandler;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.terminal.WrappedResponse;
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
    private static ThreadLocal<HttpServletRequest> currentHttpServletRequest = new ThreadLocal<HttpServletRequest>();
    private static ThreadLocal<PortletRequest> currentPortletRequest = new ThreadLocal<PortletRequest>();

    private DataSource ds;
    private PostFormatter postFormatter;
    private SignatureFormatter signatureFormatter;
    private AuthorizationService authorizationService;
    private final EventBus eventBus = new EventBus();

    @Override
    public void init() {
        checkThatCommonIsLoaded();

        final ServiceProvider spi = createServiceProvider();
        ds = createDataSource(spi);
        postFormatter = createPostFormatter(spi);
        signatureFormatter = createSignatureFormatter(spi);
        authorizationService = createAuthorizationService(spi);

        if (ToriApplication.getCurrent().getDataSource() instanceof DebugDataSource) {
            addAttachmentDownloadHandler();
        }

        setRequestForDataSource();
    }

    @Override
    protected String getRootClassName(final WrappedRequest request) {
        return ToriRoot.class.getName();
    }

    private void setRequestForDataSource() {
        if (ds instanceof PortletRequestAware) {
            setRequest((PortletRequestAware) ds);
        } else if (ds instanceof HttpServletRequestAware) {
            setRequest((HttpServletRequestAware) ds);
        }

        if (authorizationService instanceof PortletRequestAware) {
            setRequest((PortletRequestAware) authorizationService);
        } else if (authorizationService instanceof HttpServletRequestAware) {
            setRequest((HttpServletRequestAware) authorizationService);
        }
    }

    private void setRequest(final PortletRequestAware target) {
        if (currentPortletRequest.get() != null) {
            target.setRequest(currentPortletRequest.get());
        } else {
            log.warn("No PortletRequest set.");
        }
    }

    private void setRequest(final HttpServletRequestAware target) {
        if (currentHttpServletRequest.get() != null) {
            target.setRequest(currentHttpServletRequest.get());
        } else {
            log.warn("No HttpServletRequest set.");
        }
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
            throw new RuntimeException("Your project was "
                    + "apparently deployed without the Common "
                    + "project (common.jar) in its classpath", e);
        }
    }

    private static ServiceProvider createServiceProvider() {
        final ServiceLoader<ServiceProvider> loader = ServiceLoader
                .load(ServiceProvider.class);
        if (loader.iterator().hasNext()) {
            return loader.iterator().next();
        } else {
            throw new RuntimeException(
                    "It seems you don't have a DataSource in your classpath, "
                            + "or the added data source is misconfigured (see JavaDoc for "
                            + ServiceProvider.class.getName() + ").");
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

    private static SignatureFormatter createSignatureFormatter(
            final ServiceProvider spi) {
        final SignatureFormatter signatureFormatter = spi
                .createSignatureFormatter();
        log.info(String.format("Using %s implementation: %s",
                SignatureFormatter.class.getSimpleName(), signatureFormatter
                        .getClass().getName()));
        return signatureFormatter;
    }

    private static AuthorizationService createAuthorizationService(
            final ServiceProvider spi) {
        final AuthorizationService authorizationService = spi
                .createAuthorizationService();
        log.info(String.format("Using %s implementation: %s",
                PostFormatter.class.getSimpleName(), authorizationService
                        .getClass().getName()));
        return authorizationService;
    }

    /**
     * Returns the ToriApplication instance that is bound to the currently
     * running Thread.
     * 
     * @return ToriApplication instance for the current Thread.
     */
    public static ToriApplication getCurrent() {
        return (ToriApplication) Application.getCurrentApplication();
    }

    /**
     * Returns the {@link DataSource} of this application.
     * 
     * @return {@link DataSource} of this application
     */
    public DataSource getDataSource() {
        return ds;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    public PostFormatter getPostFormatter() {
        return postFormatter;
    }

    public SignatureFormatter getSignatureFormatter() {
        return signatureFormatter;
    }

    @Override
    public void onRequestStart(final HttpServletRequest request,
            final HttpServletResponse response) {
        currentHttpServletRequest.set(request);
        setRequestForDataSource();
    }

    @Override
    public void onRequestStart(final javax.portlet.PortletRequest request,
            final javax.portlet.PortletResponse response) {
        currentPortletRequest.set(request);
        setRequestForDataSource();
    }

    @Override
    public void onRequestEnd(final HttpServletRequest request,
            final HttpServletResponse response) {
        currentHttpServletRequest.remove();
    }

    @Override
    public void onRequestEnd(final javax.portlet.PortletRequest request,
            final javax.portlet.PortletResponse response) {
        currentPortletRequest.remove();
    }

    @Override
    public Window createNewWindow() {
        return new Window("Tori");
    }

    private void addAttachmentDownloadHandler() {
        addRequestHandler(new RequestHandler() {
            @Override
            public boolean handleRequest(final Application application,
                    final WrappedRequest request, final WrappedResponse response)
                    throws IOException {

                final String requestPathInfo = request.getRequestPathInfo();
                if (requestPathInfo
                        .startsWith(DebugDataSource.ATTACHMENT_PREFIX)) {
                    final String[] data = requestPathInfo.substring(
                            DebugDataSource.ATTACHMENT_PREFIX.length()).split(
                            "/");
                    final long dataId = Long.parseLong(data[0]);
                    final String fileName = data[1];

                    final byte[] attachmentData = ((DebugDataSource) ToriApplication
                            .getCurrent().getDataSource())
                            .getAttachmentData(dataId);

                    final ByteArrayInputStream is = new ByteArrayInputStream(
                            attachmentData);
                    final DownloadStream stream = new DownloadStream(is, null,
                            fileName);
                    stream.writeTo(response);
                    return true;
                }
                return false;
            }
        });
    }
}
