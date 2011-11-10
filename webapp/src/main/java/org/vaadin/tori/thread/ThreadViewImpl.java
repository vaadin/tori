package org.vaadin.tori.thread;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    @Override
    public void initView() {
        // TODO Auto-generated method stub

    }

    @Override
    protected Component createCompositionRoot() {
        // TODO
        return new Label("Unimplemented");
    }

    @Override
    protected ThreadPresenter createPresenter() {
        // TODO
        return new ThreadPresenter();
    }

    @Override
    public DiscussionThread getCurrentThread() {
        // TODO Auto-generated method stub
        final DiscussionThread thread = new DiscussionThread();
        thread.setCategory(new Category());
        return thread;
    }
}
