package org.vaadin.tori.util.vaadin;

import org.vaadin.tori.util.PageTitleUpdater;

import com.vaadin.server.Page;
import com.vaadin.ui.JavaScript;

public class VaadinPageTitleUpdater implements PageTitleUpdater {

    @Override
    public void updatePageTitle(String title) {
        String pageTitle = "Vaadin > Forum";
        if (title != null && !title.isEmpty()) {
            pageTitle = pageTitle.concat(" > ").concat(title);
        }

        Page.getCurrent().setTitle(pageTitle);
        // Liferay.Session.extend() resets the document.title to _originalTitle
        // after every request (when logged in). This is a workaround for the
        // issue.
        JavaScript
                .eval("if (Liferay.Session) {Liferay.Session._originalTitle = '"
                        + pageTitle + "'};");

    }
}