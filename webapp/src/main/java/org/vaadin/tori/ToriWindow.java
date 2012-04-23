package org.vaadin.tori;

import java.io.ByteArrayInputStream;
import java.net.URL;

import org.vaadin.tori.ToriNavigator.ViewChangeListener;
import org.vaadin.tori.component.DebugControlPanel;
import org.vaadin.tori.component.breadcrumbs.Breadcrumbs;
import org.vaadin.tori.data.DebugDataSource;
import org.vaadin.tori.mvp.View;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.DebugAuthorizationService;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The main window of Tori application. There may be several {@code ToriWindow}
 * instances within one {@link ToriApplication} as the application supports
 * multiple browser windows/tabs.
 */
@SuppressWarnings("serial")
public class ToriWindow extends Window {

    private final ToriNavigator navigator = new ToriNavigator();

    public ToriWindow() {
        super("Tori");
        final VerticalLayout windowLayout = new VerticalLayout();
        windowLayout.setMargin(false);
        setContent(windowLayout);

        final Breadcrumbs breadcrumbs = new Breadcrumbs(navigator);
        addControlPanelIfInDevelopment();
        addComponent(breadcrumbs);
        addComponent(navigator);

        navigator.addListener(new ViewChangeListener() {
            @Override
            public void navigatorViewChange(final View previous,
                    final View current) {
                // scroll to top when the view is changed
                scrollIntoView(breadcrumbs);
            }
        });

        if (ToriApplication.getCurrent().getDataSource() instanceof DebugDataSource) {
            addAttachmentDownloadHandler();
        }

    }

    private void addAttachmentDownloadHandler() {
        addURIHandler(new URIHandler() {

            @Override
            public DownloadStream handleURI(final URL context,
                    final String relativeUri) {
                DownloadStream stream = null;
                if (context.getPath().endsWith(
                        DebugDataSource.ATTACHMENT_PREFIX)) {
                    final String[] data = relativeUri.split("/");
                    final long dataId = Long.parseLong(data[0]);
                    final String fileName = data[1];

                    final byte[] attachmentData = ((DebugDataSource) ToriApplication
                            .getCurrent().getDataSource())
                            .getAttachmentData(dataId);

                    final ByteArrayInputStream is = new ByteArrayInputStream(
                            attachmentData);
                    stream = new DownloadStream(is, null, fileName);
                }
                return stream;
            }
        });
    }

    private void addControlPanelIfInDevelopment() {
        final AuthorizationService authorizationService = ToriApplication
                .getCurrent().getAuthorizationService();
        if (authorizationService instanceof DebugAuthorizationService) {
            addComponent(new DebugControlPanel(
                    (DebugAuthorizationService) authorizationService, navigator));
        }
    }
}
