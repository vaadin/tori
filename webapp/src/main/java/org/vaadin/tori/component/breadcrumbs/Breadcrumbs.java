package org.vaadin.tori.component.breadcrumbs;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriNavigator.ViewChangeListener;
import org.vaadin.tori.category.CategoryView;
import org.vaadin.tori.component.breadcrumbs.CategoryCrumb.CategorySelectionListener;
import org.vaadin.tori.component.breadcrumbs.ThreadCrumb.ThreadSelectionListener;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.View;
import org.vaadin.tori.thread.ThreadView;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
public class Breadcrumbs extends CustomComponent implements
        CategorySelectionListener, ThreadSelectionListener {

    static final String STYLE_CRUMB = "crumb";
    static final String STYLE_THREAD = "thread";
    static final String STYLE_CATEGORY = "category";
    static final String STYLE_UNCLICKABLE = "unclickable";

    private final HorizontalLayout layout;

    private transient final ViewChangeListener viewListener = new ViewChangeListener() {
        @Override
        public void navigatorViewChange(final View previous, final View current) {
            renderBreadCrumb();
        }
    };

    private final ToriNavigator navigator;

    public Breadcrumbs(final ToriNavigator navigator) {
        setStyleName("breadcrumbs");
        this.navigator = navigator;
        navigator.addListener(viewListener);
        setHeight("40px");

        setCompositionRoot(layout = new HorizontalLayout());
        renderBreadCrumb();
    }

    private void renderBreadCrumb() {
        layout.removeAllComponents();
        final Button dashboardButton = new Button("Dashboard");
        dashboardButton.setIcon(new ThemeResource("images/home.gif"));
        dashboardButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                navigator.navigateTo(ToriNavigator.ApplicationView.DASHBOARD);
            }
        });
        layout.addComponent(dashboardButton);

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
        final Component categoryCrumb = new CategoryCrumb.Clickable(
                currentThread.getCategory(), this);
        final Component threadCrumb = new ThreadCrumb(currentThread, this);
        layout.addComponent(categoryCrumb);
        layout.addComponent(threadCrumb);
    }

    private void paint(final CategoryView categoryView) {
        final Category currentCategory = categoryView.getCurrentCategory();
        final CategoryCrumb crumb = new CategoryCrumb.UnClickable(
                currentCategory, this);
        layout.addComponent(crumb);
    }

    @Override
    public void selectCategory(final Category selectedCategory) {
        final String catIdAsString = String.valueOf(selectedCategory.getId());
        navigator.navigateTo(ToriNavigator.ApplicationView.CATEGORIES,
                catIdAsString);
    }

    @Override
    public void selectThread(final DiscussionThread selectedThread) {
        final String threadIdAsString = String.valueOf(selectedThread.getId());
        navigator.navigateTo(ToriNavigator.ApplicationView.THREADS,
                threadIdAsString);
    }
}
