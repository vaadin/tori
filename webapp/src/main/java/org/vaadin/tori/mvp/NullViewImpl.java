package org.vaadin.tori.mvp;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * This class is only for placeholder purposes for development.
 */
@SuppressWarnings("serial")
public class NullViewImpl extends AbstractView<NullView, NullPresenter>
        implements NullView {

    @Override
    public void initView() {
        // NOP
    }

    @Override
    protected Component createCompositionRoot() {
        return new Label("Nothing here!");
    }

    @Override
    protected NullPresenter createPresenter() {
        return new NullPresenter();
    }

}
