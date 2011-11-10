package org.vaadin.tori.component;

import org.vaadin.hene.splitbutton.SplitButton;
import org.vaadin.hene.splitbutton.SplitButton.SplitButtonPopupVisibilityEvent;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriNavigator.ViewChangeListener;
import org.vaadin.tori.category.CategoryView;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.View;
import org.vaadin.tori.thread.ThreadView;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class Breadcrumbs extends CustomComponent {

    private final HorizontalLayout layout;

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

        setCompositionRoot(layout = new HorizontalLayout());
        renderBreadCrumb();
    }

    private void renderBreadCrumb() {
        layout.removeAllComponents();
        layout.addComponent(new Button("Dashboard", new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                navigator.navigateTo(ToriNavigator.ApplicationView.DASHBOARD);
            }
        }));

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
        final DiscussionThread currentThread = threadView.getCurrentThread();
        final Component categoryCrumb = getCategoryCrumb(currentThread
                .getCategory());
        final Component threadCrumb = getThreadCrumb(currentThread);
        layout.addComponent(categoryCrumb);
        layout.addComponent(threadCrumb);
    }

    private void paint(final CategoryView categoryView) {
        final Category currentCategory = categoryView.getCurrentCategory();
        final Component crumb = getCategoryCrumb(currentCategory);
        layout.addComponent(crumb);
    }

    /**
     * @throws IllegalArgumentException
     *             if <code>thread</code> is <code>null</code>.
     */
    private Component getThreadCrumb(final DiscussionThread thread)
            throws IllegalArgumentException {
        if (thread == null) {
            throw new RuntimeException("Trying to render the thread part of "
                    + "the breadcrumbs, but the given thread was null");
        }

        final SplitButton crumb = new SplitButton(thread.getTopic());
        crumb.addPopupVisibilityListener(new SplitButton.SplitButtonPopupVisibilityListener() {
            @Override
            public void splitButtonPopupVisibilityChange(
                    final SplitButtonPopupVisibilityEvent event) {
                event.getSplitButton().setComponent(new Label("bar"));
            }
        });

        return crumb;
    }

    /**
     * @throws IllegalArgumentException
     *             if <code>category</code> is <code>null</code>.
     */
    private Component getCategoryCrumb(final Category category)
            throws IllegalArgumentException {
        if (category == null) {
            throw new IllegalArgumentException("Trying to render the category "
                    + "part of the breadcrumbs, "
                    + "but the given category was null");
        }

        final SplitButton crumb = new SplitButton(category.getName());
        crumb.addPopupVisibilityListener(new SplitButton.SplitButtonPopupVisibilityListener() {
            @Override
            public void splitButtonPopupVisibilityChange(
                    final SplitButtonPopupVisibilityEvent event) {
                if (event.isPopupVisible()) {
                    event.getSplitButton().setComponent(new Label("foo"));
                }
            }
        });
        return crumb;
    }
}
