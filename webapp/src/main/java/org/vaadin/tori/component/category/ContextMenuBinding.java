package org.vaadin.tori.component.category;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ContextMenuBinding {

    CategoryContextMenuItem value();

}
