package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.category.CategoryListing;
import org.vaadin.tori.component.category.CategoryListing.Mode;
import org.vaadin.tori.component.thread.ThreadListing;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.ui.Component;
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
    private VerticalLayout threadLayout;
    private HeadingLabel noThreadsInfo;

    @Override
    protected Component createCompositionRoot() {
        return layout = new VerticalLayout();
    }

    @Override
    public void initView() {
        categoryLayout = new VerticalLayout();
        layout.addComponent(categoryLayout);

        categoryLayout.addComponent(new HeadingLabel("Contained Categories",
                HeadingLevel.H2));
        categoryLayout.addComponent(categoryListing = new CategoryListing(
                Mode.SINGLE_COLUMN));

        threadLayout = new VerticalLayout();
        layout.addComponent(threadLayout);

        threadLayout.addComponent(new HeadingLabel("Threads", HeadingLevel.H2));
        threadLayout.addComponent(threadListing = new Label("placeholder"));

        noThreadsInfo = new HeadingLabel(
                "There are no threads in this category", HeadingLevel.H1);
        layout.addComponent(noThreadsInfo);
    }

    @Override
    protected CategoryPresenter createPresenter() {
        final ToriApplication app = ToriApplication.getCurrent();
        final CategoryPresenter categoryPresenter = new CategoryPresenter(
                app.getDataSource(), app.getAuthorizationService());

        threadLayout.replaceComponent(threadListing,
                threadListing = new ThreadListing(categoryPresenter));

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
        threadLayout.setVisible(!threadsInCategory.isEmpty());
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
