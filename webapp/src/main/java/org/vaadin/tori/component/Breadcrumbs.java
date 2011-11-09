package org.vaadin.tori.component;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriNavigator.ViewChangeListener;
import org.vaadin.tori.category.CategoryView;
import org.vaadin.tori.mvp.View;
import org.vaadin.tori.thread.ThreadView;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class Breadcrumbs extends CustomComponent {

    private final CssLayout layout;

    private transient final ViewChangeListener viewListener = new ViewChangeListener() {
        @Override
        public void navigatorViewChange(final View previous, final View current) {
            renderBreadCrumb();
        }
    };

    private final ToriNavigator navigator;

    public Breadcrumbs(final ToriNavigator navigator) {
        this.navigator = navigator;
        navigator.addListener(viewListener);

        setCompositionRoot(layout = new CssLayout());
        layout.setWidth("100%");
        setWidth("100%");
        renderBreadCrumb();
    }

    private void renderBreadCrumb() {
        layout.removeAllComponents();
        layout.addComponent(new Label("Dashboard"));

        final View currentView = navigator.getCurrentView();
        if (currentView instanceof CategoryView) {
            final CategoryView categoryView = (CategoryView) currentView;
            paint(categoryView);
        } else if (currentView instanceof ThreadView) {
            final ThreadView threadView = (ThreadView) currentView;
            paint(threadView);
        }
    }

    private void paint(final ThreadView threadView) {
        // TODO
        layout.removeAllComponents();
        layout.addComponent(new Label("Dashboard > Category > Thread"));
    }

    private void paint(final CategoryView categoryView) {
        // TODO
        layout.removeAllComponents();
        layout.addComponent(new Label("Dashboard > Category"));
    }
}
