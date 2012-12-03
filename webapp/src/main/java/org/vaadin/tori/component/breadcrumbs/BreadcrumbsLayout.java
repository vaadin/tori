package org.vaadin.tori.component.breadcrumbs;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.CustomLayout;

@JavaScript({ "makesurethecrumbsfit.js" })
public class BreadcrumbsLayout extends CustomLayout {
    private static final long serialVersionUID = 1085239071591295689L;

    public BreadcrumbsLayout() {
        super("breadcrumbslayout");
    }
}
