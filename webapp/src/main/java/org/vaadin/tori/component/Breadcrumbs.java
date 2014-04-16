/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.component;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.util.ComponentUtil;
import org.vaadin.tori.util.ToriScheduler;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.view.listing.ListingView;
import org.vaadin.tori.view.thread.ThreadView;
import org.vaadin.tori.view.thread.newthread.NewThreadView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class Breadcrumbs extends CustomComponent implements ViewChangeListener {

    static final String STYLE_CRUMB = "crumb";
    static final String STYLE_THREAD = "thread";
    static final String STYLE_CATEGORY = "category";

    private HorizontalLayout crumbsLayout;
    private Label viewCaption;
    private final DataSource dataSource = ToriApiLoader.getCurrent()
            .getDataSource();
    private final String pageTitlePrefix = dataSource.getConfiguration()
            .getPageTitlePrefix();
    private final AuthorizationService authorizationService = ToriApiLoader
            .getCurrent().getAuthorizationService();

    private Button followButton;
    private long threadId;
    private Label iconsComponent;

    public Breadcrumbs() {
        setStyleName("tori-breadcrumbs");
        ToriNavigator.getCurrent().addViewChangeListener(this);

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(false, true, false, true));
        mainLayout.addComponent(buildCrumbsLayout());
        mainLayout.addComponent(buildCaptionLayout());
        setCompositionRoot(mainLayout);
    }

    private Component buildCaptionLayout() {
        viewCaption = new Label("");
        viewCaption.addStyleName("viewcaption");
        viewCaption.setSizeUndefined();

        iconsComponent = new Label("");
        iconsComponent.setSizeUndefined();

        final HorizontalLayout captionLayout = new HorizontalLayout(
                viewCaption, iconsComponent);
        captionLayout.setWidth(100.0f, Unit.PERCENTAGE);
        captionLayout.setExpandRatio(iconsComponent, 1.0f);

        followButton = buildFollowButton();
        captionLayout.addComponent(followButton);

        return captionLayout;
    }

    private Button buildFollowButton() {
        Button result = new Button();
        result.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                try {
                    if (dataSource.isFollowingThread(threadId)) {
                        dataSource.unfollowThread(threadId);
                        Notification.show("Topic unfollowed");
                    } else {
                        dataSource.followThread(threadId);
                        Notification.show("Following topic");
                    }
                    updateFollowButtonStyle();
                } catch (Exception e) {
                    e.printStackTrace();
                    Notification.show(
                            DataSourceException.GENERIC_ERROR_MESSAGE,
                            Type.ERROR_MESSAGE);
                }
            }
        });

        return result;
    }

    public void updateFollowButtonStyle() {
        followButton.setVisible(authorizationService.mayFollowThread(threadId));
        followButton
                .setStyleName(dataSource.isFollowingThread(threadId) ? "followed"
                        : "notfollowed");
    }

    private Component buildCrumbsLayout() {
        crumbsLayout = new HorizontalLayout();
        crumbsLayout.setHeight(22.0f, Unit.PIXELS);
        crumbsLayout.setStyleName("breadcrumbs-layout");
        return crumbsLayout;
    }

    @Override
    public void afterViewChange(final ViewChangeEvent event) {
        viewCaption.setValue(null);
        iconsComponent.setStyleName("icons");
        followButton.setVisible(false);

        final View view = event.getNewView();

        if (view instanceof AbstractView) {
            String viewTitle = ((AbstractView<?, ?>) view).getTitle();
            final Long urlParameterId = ((AbstractView<?, ?>) view)
                    .getUrlParameterId();
            viewCaption.setValue(viewTitle);
            if (view instanceof NewThreadView) {
                crumbsLayout.removeAllComponents();
                Long categoryId = ((AbstractView) view).getUrlParameterId();
                try {
                    Category category = dataSource.getCategory(categoryId);
                    prependLink(category);
                } catch (DataSourceException e) {
                    e.printStackTrace();
                }
            } else if (urlParameterId == null) {
                crumbsLayout.removeAllComponents();
                viewCaption.setValue(getDashboardTitle());
            } else {
                ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        crumbsLayout.removeAllComponents();
                        buildCrumbs(view, urlParameterId);
                    }
                });
            }
        }

    }

    private void buildCrumbs(final View view, final Long urlParameterId) {
        Category parentCategory = null;
        if (view instanceof ThreadView) {
            try {
                DiscussionThread thread = dataSource.getThread(urlParameterId);
                if (thread.isLocked()) {
                    iconsComponent.addStyleName("lockedthread");
                }
                if (thread.isSticky()) {
                    iconsComponent.addStyleName("stickythread");
                }

                followButton.setVisible(true);
                threadId = thread.getId();
                updateFollowButtonStyle();
                parentCategory = thread.getCategory();
            } catch (Exception e) {
                ((ThreadView) view).showError("No topic found");
                e.printStackTrace();
                ToriNavigator.getCurrent().navigateToDashboard();
            }
        } else if (view instanceof ListingView) {
            if (urlParameterId != 0) {
                try {
                    Category category = dataSource.getCategory(urlParameterId);
                    parentCategory = category.getParentCategory();
                } catch (DataSourceException e) {
                    e.printStackTrace();
                }
            }
        }

        prependLink(parentCategory);

        for (Iterator<Component> iter = crumbsLayout.iterator(); iter.hasNext();) {
            crumbsLayout.setComponentAlignment(iter.next(),
                    Alignment.MIDDLE_CENTER);
        }
    }

    private void prependLink(final Category category) {
        if (category == null) {
            crumbsLayout.addComponent(getDashboardLink(), 0);
        } else {
            crumbsLayout.addComponent(getCategoryLink(category), 0);
            prependLink(category.getParentCategory());
        }
    }

    private Component getDashboardLink() {
        Link link = new Link(getDashboardTitle(), new ExternalResource("#"
                + ToriNavigator.ApplicationView.DASHBOARD.getUrl()));
        link.setHeight(100.0f, Unit.PERCENTAGE);
        return link;
    }

    private String getDashboardTitle() {
        return pageTitlePrefix != null ? pageTitlePrefix : "Tori";
    }

    private Component getCategoryLink(final Category category) {
        HorizontalLayout result = new HorizontalLayout();
        result.setSpacing(true);
        result.setHeight(100.0f, Unit.PERCENTAGE);
        result.addStyleName("categorylink");
        final Link crumb = new Link(category.getName(), new ExternalResource(
                "#" + ToriNavigator.ApplicationView.CATEGORIES.getUrl() + "/"
                        + category.getId()));
        crumb.setHeight(100.0f, Unit.PERCENTAGE);
        result.addComponent(crumb);
        result.setComponentAlignment(crumb, Alignment.MIDDLE_CENTER);
        Component siblingMenu = getSiblingMenuBar(category);
        siblingMenu.setHeight(100.0f, Unit.PERCENTAGE);
        result.addComponent(siblingMenu);
        result.setComponentAlignment(siblingMenu, Alignment.MIDDLE_CENTER);
        return result;
    }

    private Component getSiblingMenuBar(final Category category) {
        final MenuBar menuBar = ComponentUtil.getDropdownMenu();
        final MenuItem topItem = menuBar.getMoreMenuItem();
        // Lazily populate the menubar
        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                if (!topItem.hasChildren()) {
                    populateSiblingMenu(topItem, category);
                }
            }
        });
        return menuBar;
    }

    protected void populateSiblingMenu(final MenuItem topItem,
            final Category category) {
        try {
            Category parent = category.getParentCategory();
            Long parentId = parent != null ? parent.getId() : null;
            final List<Category> siblings = dataSource
                    .getSubCategories(parentId);
            for (final Category sibling : siblings) {
                if (authorizationService.mayViewCategory(sibling.getId())) {
                    topItem.addItem(sibling.getName(), new Command() {
                        @Override
                        public void menuSelected(final MenuItem selectedItem) {
                            ToriNavigator.getCurrent().navigateToCategory(
                                    sibling.getId());
                        }
                    });
                }
            }
        } catch (final DataSourceException e) {
            getLogger().error(e);
            e.printStackTrace();
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(Breadcrumbs.class);
    }

    @Override
    public boolean beforeViewChange(final ViewChangeEvent event) {
        return true;
    }

    public static Breadcrumbs getCurrent() {
        return ToriUI.getCurrent().getBreadcrumbs();
    }
}
