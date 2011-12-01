package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.category.CategoryListing;
import org.vaadin.tori.component.category.CategoryListing.Mode;
import org.vaadin.tori.component.thread.ThreadListing;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class CategoryViewImpl extends
        AbstractView<CategoryView, CategoryPresenter> implements CategoryView {

    private VerticalLayout layout;
    private Component threadListing;
    private CategoryListing categoryListing;
    private VerticalLayout categoryLayout;
    private HeadingLabel noThreadsInfo;
    private HeadingLabel threadsLabel;
    private Button newThreadButton1;
    private Button newThreadButton2;
    private ToriNavigator navigator;

    @Override
    protected Component createCompositionRoot() {
        return layout = new VerticalLayout();
    }

    @Override
    public void init(final ToriNavigator navigator,
            final Application application) {
        this.navigator = navigator;
        super.init(navigator, application);
    }

    @Override
    public void initView() {
        categoryLayout = new VerticalLayout();
        layout.addComponent(categoryLayout);

        categoryLayout.addComponent(new HeadingLabel("Contained Categories",
                HeadingLevel.H2));
        categoryLayout.addComponent(categoryListing = new CategoryListing(
                Mode.SINGLE_COLUMN));

        final VerticalLayout threadLayout = new VerticalLayout();
        layout.addComponent(threadLayout);

        final HorizontalLayout threadHeaderLayout = new HorizontalLayout();
        threadHeaderLayout.setWidth("100%");
        threadLayout.addComponent(threadHeaderLayout);

        threadsLabel = new HeadingLabel("Threads", HeadingLevel.H2);
        threadHeaderLayout.addComponent(threadsLabel);

        newThreadButton1 = createNewThreadButton();
        threadHeaderLayout.addComponent(newThreadButton1);
        threadHeaderLayout.setComponentAlignment(newThreadButton1,
                Alignment.MIDDLE_RIGHT);

        threadLayout.addComponent(threadListing = new Label("placeholder"));

        noThreadsInfo = new HeadingLabel(
                "There are no threads in this category", HeadingLevel.H1);
        layout.addComponent(noThreadsInfo);

        newThreadButton2 = createNewThreadButton();
        threadLayout.addComponent(newThreadButton2);
    }

    private Button createNewThreadButton() {
        final Button button = new Button("Start a new thread");
        button.setIcon(new ThemeResource("images/icon-newthread.png"));
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                navigator.navigateTo(String.format("%s/new/%s",
                        ToriNavigator.ApplicationView.THREADS.getUrl(),
                        getPresenter().getCurrentCategory().getId()));
            }
        });
        button.setWidth("163px"); // same as
                                  // org.vaadin.tori.component.thread.ThreadListing.PROPERTY_ID_LATESTPOST
                                  // with padding and all
        return button;
    }

    @Override
    protected CategoryPresenter createPresenter() {
        final ToriApplication app = ToriApplication.getCurrent();
        final CategoryPresenter categoryPresenter = new CategoryPresenter(
                app.getDataSource(), app.getAuthorizationService());

        final ComponentContainer parent = (ComponentContainer) threadListing
                .getParent();
        parent.replaceComponent(threadListing,
                threadListing = new ThreadListing(categoryPresenter));

        newThreadButton1.setEnabled(categoryPresenter.userMayStartANewThread());
        newThreadButton2.setEnabled(categoryPresenter.userMayStartANewThread());

        return categoryPresenter;
    }

    @Override
    public void displaySubCategories(final List<Category> subCategories) {
        // show contained categories only if there are any
        categoryLayout.setVisible(!subCategories.isEmpty());
        categoryListing.setCategories(subCategories);
    }

    @Override
    public void displayThreads(final List<DiscussionThread> threadsInCategory) {
        // show contained threads only if there are any
        threadListing.setVisible(!threadsInCategory.isEmpty());
        threadsLabel.setVisible(!threadsInCategory.isEmpty());
        noThreadsInfo.setVisible(threadsInCategory.isEmpty());

        ((ThreadListing) threadListing).setThreads(threadsInCategory);
    }

    @Override
    protected void navigationTo(final String requestedDataId) {
        super.getPresenter().setCurrentCategoryById(requestedDataId);
    }

    @Override
    public void displayCategoryNotFoundError(final String requestedCategoryId) {
        getWindow().showNotification(
                "No category found for " + requestedCategoryId,
                Notification.TYPE_ERROR_MESSAGE);
    }

    @Override
    public Category getCurrentCategory() {
        return getPresenter().getCurrentCategory();
    }

    @Override
    public void confirmFollowing() {
        getWindow().showNotification("following thread");
    }

    @Override
    public void confirmUnfollowing() {
        getWindow().showNotification("unfollowed thread");
    }

    @Override
    public void confirmThreadMoved() {
        getWindow().showNotification("thread moved");
    }

    @Override
    public void confirmThreadStickied() {
        getWindow().showNotification("thread stickied");
        // TODO make visual adjustments
    }

    @Override
    public void confirmThreadUnstickied() {
        getWindow().showNotification("thread unstickied");
        // TODO make visual adjustments
    }

    @Override
    public void confirmThreadLocked() {
        getWindow().showNotification("thread locked");
        // TODO make visual adjustments
    }

    @Override
    public void confirmThreadUnlocked() {
        getWindow().showNotification("thread unlocked");
        // TODO make visual adjustments
    }

    @Override
    public void confirmThreadDeleted() {
        getWindow().showNotification("thread deleted");
    }
}
