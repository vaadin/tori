package org.vaadin.tori.mvp;

import org.vaadin.tori.ToriNavigator;

public interface View {

    /**
     * Initializes the UI components used by this View.
     */
    void initView();

    /**
     * Init view.
     * 
     * Convenience method which navigator call before slightly before the view
     * is first time rendered. This is called only once in the lifetime of each
     * view instance. In many cases it is better to construct UI within this
     * method than in constructor as you are guaranteed to get references to
     * application and navigator here.
     * 
     * @param navigator
     *            Reference to navigator that controls the window where this
     *            view is attached to.
     */
    public void init(ToriNavigator navigator);

    /**
     * This view is navigated to.
     * 
     * This method is always called before the view is shown on screen. If there
     * is any additional id to data what should be shown in the view, it is also
     * optionally passed as parameter.
     * 
     * @param arguments
     *            The arguments passed to this view. If no arguments are passed,
     *            the array is empty.
     */
    public void navigateTo(String[] arguments);
}
