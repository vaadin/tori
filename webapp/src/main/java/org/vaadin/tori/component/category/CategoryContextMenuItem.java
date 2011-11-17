package org.vaadin.tori.component.category;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;

public enum CategoryContextMenuItem {
    // @formatter:off
    EDIT_CATEGORY(new ThemeResource("images/icon-edit.png"), "Edit category"),
    DELETE_CATEGORY(new ThemeResource("images/icon-delete.png"), "Delete category"),
    FOLLOW_CATEGORY(new ThemeResource("images/icon-edit.png"), "Follow category");
    // @formatter:on

    private Resource icon;
    private String caption;

    private CategoryContextMenuItem(final Resource icon, final String caption) {
        this.icon = icon;
        this.caption = caption;
    }

    public Resource getIcon() {
        return icon;
    }

    public String getCaption() {
        return caption;
    }
}
