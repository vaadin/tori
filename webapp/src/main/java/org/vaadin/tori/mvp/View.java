package org.vaadin.tori.mvp;

import org.vaadin.tori.ToriNavigator;

import com.vaadin.Application;

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
     * @param application
     *            Application instance this view lives in.
     */
    public void init(ToriNavigator navigator, Application application);

    /**
     * This view is navigated to.
     * 
     * This method is always called before the view is shown on screen. If there
     * is any additional id to data what should be shown in the view, it is also
     * optionally passed as parameter.
     * 
     * @param requestedDataId
     *            Id of the data extracted from URI fragment or null if not
     *            given. This is the string that appeards in URI after
     *            #viewname/
     */
    public void navigateTo(String requestedDataId);

    /**
     * Get a warning that should be shown to user before navigating away from
     * the view.
     * 
     * If the current view is in state where navigating away from it could lead
     * to data loss, this method should return a message that will be shown to
     * user before he confirms that he will leave the screen. If there is no
     * need to ask questions from user, this should return null.
     * 
     * @return Message to be shown or null if no message should be shown.
     */
    public String getWarningForNavigatingFrom();
}
