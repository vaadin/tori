package org.vaadin.tori;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
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
import com.vaadin.RootRequiresMoreInformationException;
import com.vaadin.terminal.CombinedRequest;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.RequestHandler;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.terminal.WrappedResponse;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.terminal.gwt.server.WrappedPortletRequest;
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

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "ignoring serialization")
    private final ToriApiLoader apiLoader = new ToriApiLoader();

    @Override
    public void init() {
        if (ToriApplication.getCurrent().getDataSource() instanceof DebugDataSource) {
            addAttachmentDownloadHandler();
        }
    }

    @Override
    protected String getRootClassName(final WrappedRequest request) {
        return ToriRoot.class.getName();
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
        return apiLoader.getDs();
    }

    public AuthorizationService getAuthorizationService() {
        return apiLoader.getAuthorizationService();
    }

    public PostFormatter getPostFormatter() {
        return apiLoader.getPostFormatter();
    }

    public SignatureFormatter getSignatureFormatter() {
        return apiLoader.getSignatureFormatter();
    }

    @Override
    public void onRequestStart(final HttpServletRequest request,
            final HttpServletResponse response) {
        apiLoader.setRequest(request);
    }

    @Override
    public void onRequestStart(final javax.portlet.PortletRequest request,
            final javax.portlet.PortletResponse response) {
        apiLoader.setRequest(request);
    }

    @Override
    public void onRequestEnd(final HttpServletRequest request,
            final HttpServletResponse response) {
    }

    @Override
    public void onRequestEnd(final javax.portlet.PortletRequest request,
            final javax.portlet.PortletResponse response) {
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

    /** @see org.vaadin.tori.data.LiferayDataSource.TORI_CATEGORY_ID */
    private static final String TORI_CATEGORY_ID = "toriCategoryId";
    /** @see org.vaadin.tori.data.LiferayDataSource.TORI_THREAD_ID */
    private static final String TORI_THREAD_ID = "toriThreadId";
    /** @see org.vaadin.tori.data.LiferayDataSource.TORI_MESSAGE_ID */
    private static final String TORI_MESSAGE_ID = "toriMessageId";

    @Override
    public Root getRootForRequest(final WrappedRequest request)
            throws RootRequiresMoreInformationException {
        final Root root = super.getRootForRequest(request);
        if (root != null) {
            // Acquire the portletRequest if possible
            PortletRequest portletRequest = null;
            if (request instanceof WrappedPortletRequest) {
                portletRequest = ((WrappedPortletRequest) request)
                        .getPortletRequest();
            } else if (request instanceof CombinedRequest) {
                final WrappedRequest wrappedRequest = ((CombinedRequest) request)
                        .getSecondRequest();
                if (wrappedRequest instanceof WrappedPortletRequest) {
                    portletRequest = ((WrappedPortletRequest) wrappedRequest)
                            .getPortletRequest();
                }
            }

            if (portletRequest != null) {
                // Check if category/thread/message was determined from the
                // original request attributes (Liferay message boards type url)

                final PortletSession session = portletRequest
                        .getPortletSession();

                final Object categoryId = session
                        .getAttribute(TORI_CATEGORY_ID);
                final Object threadId = session.getAttribute(TORI_THREAD_ID);
                final Object messageId = session.getAttribute(TORI_MESSAGE_ID);

                String fragment = null;

                if (categoryId != null) {
                    fragment = ToriNavigator.ApplicationView.CATEGORIES
                            .getUrl() + "/" + categoryId;
                    session.removeAttribute(TORI_CATEGORY_ID);
                } else if (threadId != null) {
                    fragment = ToriNavigator.ApplicationView.THREADS.getUrl()
                            + "/" + threadId;
                    session.removeAttribute(TORI_THREAD_ID);
                    if (messageId != null) {
                        fragment = fragment + "/" + messageId;
                        session.removeAttribute(TORI_MESSAGE_ID);
                    }
                }

                if (fragment != null) {
                    // Apply changes to Tori root's fragment
                    root.setFragment(fragment);
                }

                ((ToriRoot) root).setPortletMode(portletRequest
                        .getPortletMode());
            }
        }
        return root;
    }
}
