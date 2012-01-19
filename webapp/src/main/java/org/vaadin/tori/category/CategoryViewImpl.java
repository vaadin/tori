package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.component.category.CategoryListing;
import org.vaadin.tori.component.category.CategoryListing.Mode;
import org.vaadin.tori.component.thread.ThreadListing;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.thread.ThreadPresenter;

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

import edu.umd.cs.findbugs.annotations.CheckForNull;

@SuppressWarnings("serial")
public class CategoryViewImpl extends
        AbstractView<CategoryView, CategoryPresenter> implements CategoryView {

    private static final HeadingLabel NO_THREADS = new HeadingLabel(
            "There are no threads in this category", HeadingLevel.H1);
    private static final HeadingLabel THREADS = new HeadingLabel("Threads",
            HeadingLevel.H2);
    private VerticalLayout layout;
    private Component threadListingPlaceHolder;
    private ThreadListing threadListing;
    private CategoryListing categoryListing;
    private VerticalLayout categoryLayout;
    private HeadingLabel threadsLabel;
    private Button newThreadButton1;
    private Button newThreadButton2;

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

        final VerticalLayout threadLayout = new VerticalLayout();
        layout.addComponent(threadLayout);

        final HorizontalLayout threadHeaderLayout = new HorizontalLayout();
        threadHeaderLayout.setWidth("100%");
        threadLayout.addComponent(threadHeaderLayout);

        threadsLabel = THREADS;
        threadHeaderLayout.addComponent(threadsLabel);

        newThreadButton1 = createNewThreadButton();
        threadHeaderLayout.addComponent(newThreadButton1);
        threadHeaderLayout.setComponentAlignment(newThreadButton1,
                Alignment.MIDDLE_RIGHT);

        threadLayout.addComponent(threadListingPlaceHolder = new Label(
                "placeholder"));

        newThreadButton2 = createNewThreadButton();
        threadLayout.addComponent(newThreadButton2);
    }

    private Button createNewThreadButton() {
        final Button button = new Button("Start a new thread");
        button.setIcon(new ThemeResource("images/icon-newthread.png"));
        button.addListener(new Button.ClickListener() {
            @Override
            @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "This method is never called if the category isn't set")
            public void buttonClick(final ClickEvent event) {
                getNavigator().navigateTo(
                        String.format("%s/%s/%s",
                                ToriNavigator.ApplicationView.THREADS.getUrl(),
                                ThreadPresenter.NEW_THREAD_ARGUMENT,
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

        final ComponentContainer parent = (ComponentContainer) threadListingPlaceHolder
                .getParent();
        parent.replaceComponent(threadListingPlaceHolder,
                threadListing = new ThreadListing(categoryPresenter));

        return categoryPresenter;
    }

    @Override
    public void displaySubCategories(final List<Category> subCategories) {
        // show contained categories only if there are any
        categoryLayout.setVisible(!subCategories.isEmpty());
        categoryListing.setCategories(subCategories);
    }

    // @Override
    // public void displayThreads(final List<DiscussionThread>
    // threadsInCategory) {
    // if (threadsInCategory.isEmpty()) {
    // threadListing.addStyleName(StyleConstants.HIDDEN);
    // } else {
    // threadListing.removeStyleName(StyleConstants.HIDDEN);
    // }
    //
    // final ComponentContainer parent = (ComponentContainer) threadsLabel
    // .getParent();
    // if (threadsInCategory.isEmpty()) {
    // parent.replaceComponent(threadsLabel, threadsLabel = NO_THREADS);
    // } else {
    // parent.replaceComponent(threadsLabel, threadsLabel = THREADS);
    // }
    //
    // newThreadButton1.setEnabled(getPresenter().userMayStartANewThread());
    // newThreadButton2.setEnabled(getPresenter().userMayStartANewThread());
    //
    // threadListing.setThreads(threadsInCategory);
    // }

    @Override
    protected void navigationTo(final String[] arguments) {
        super.getPresenter().setCurrentCategoryById(arguments[0]);
    }

    @Override
    public void displayCategoryNotFoundError(final String requestedCategoryId) {
        log.error("No such category: " + requestedCategoryId);
        getWindow().showNotification(
                "No category found for " + requestedCategoryId,
                Notification.TYPE_ERROR_MESSAGE);
    }

    @Override
    @CheckForNull
    public Category getCurrentCategory() {
        return getPresenter().getCurrentCategory();
    }

    @Override
    public void confirmFollowing(final DiscussionThread thread) {
        threadListing.refreshStyles(thread);
        getWindow().showNotification("Following thread");
    }

    @Override
    public void confirmUnfollowing(final DiscussionThread thread) {
        threadListing.refreshStyles(thread);
        getWindow().showNotification("Unfollowed thread");
    }

    @Override
    public void confirmThreadMoved() {
        getWindow().showNotification("Thread moved");
    }

    @Override
    public void confirmThreadStickied(final DiscussionThread thread) {
        getWindow().showNotification("Thread stickied");
        // presenter will reset the view here
    }

    @Override
    public void confirmThreadUnstickied(final DiscussionThread thread) {
        getWindow().showNotification("Thread unstickied");
        // presenter will reset the view here
    }

    @Override
    public void confirmThreadLocked(final DiscussionThread thread) {
        getWindow().showNotification("Thread locked");
        refreshVisually(thread);
    }

    @Override
    public void confirmThreadUnlocked(final DiscussionThread thread) {
        getWindow().showNotification("Thread unlocked");
        refreshVisually(thread);
    }

    private void refreshVisually(final DiscussionThread thread) {
        threadListing.refresh(thread);
    }

    @Override
    public void confirmThreadDeleted() {
        getWindow().showNotification("Thread deleted");
    }

    @Override
    public void panic() {
        layout.removeAllComponents();
        layout.addComponent(new PanicComponent());
    }
}
