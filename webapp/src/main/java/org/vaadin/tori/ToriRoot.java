package org.vaadin.tori;

import org.vaadin.tori.ToriNavigator.ViewChangeListener;
import org.vaadin.tori.component.DebugControlPanel;
import org.vaadin.tori.component.breadcrumbs.Breadcrumbs;
import org.vaadin.tori.mvp.View;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.DebugAuthorizationService;

import com.vaadin.annotations.Theme;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.ui.Root;
import com.vaadin.ui.VerticalLayout;

@Theme("tori")
@SuppressWarnings("serial")
public class ToriRoot extends Root {

    private final ToriNavigator navigator = new ToriNavigator(this);

    @Override
    protected void init(final WrappedRequest request) {
        setCaption("Tori");
        final VerticalLayout windowLayout = new VerticalLayout();
        windowLayout.setMargin(false);
        setContent(windowLayout);

        final Breadcrumbs breadcrumbs = new Breadcrumbs(navigator);
        addControlPanelIfInDevelopment();
        addComponent(breadcrumbs);
        addComponent(navigator);

        navigator.init(request.getBrowserDetails().getUriFragment());

        navigator.addListener(new ViewChangeListener() {
            @Override
            public void navigatorViewChange(final View previous,
                    final View current) {
                // scroll to top when the view is changed
                scrollIntoView(breadcrumbs);
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
