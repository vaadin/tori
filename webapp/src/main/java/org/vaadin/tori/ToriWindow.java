package org.vaadin.tori;

import org.vaadin.tori.component.DebugControlPanel;
import org.vaadin.tori.component.breadcrumbs.Breadcrumbs;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.DebugAuthorizationService;

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

        addControlPanelIfInDevelopment();
        addComponent(new Breadcrumbs(navigator));
        addComponent(navigator);
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
