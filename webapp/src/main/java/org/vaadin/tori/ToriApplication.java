package org.vaadin.tori;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator.NavigableApplication;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.DebugDataSource;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.SignatureFormatter;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.RequestHandler;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.terminal.WrappedResponse;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.Root;
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

    @Override
    public void init() {

        final ToriApiLoader apiLoader = new ToriApiLoader();
        ds = apiLoader.createDataSource();
        postFormatter = apiLoader.createPostFormatter();
        signatureFormatter = apiLoader.createSignatureFormatter();
        authorizationService = apiLoader.createAuthorizationService();

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

    @Override
    public String getThemeForRoot(final Root root) {
        return getProperty("theme");
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
