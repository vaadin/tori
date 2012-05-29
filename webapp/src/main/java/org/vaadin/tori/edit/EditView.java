package org.vaadin.tori.edit;

import java.util.Map;

import org.vaadin.tori.mvp.View;

public interface EditView extends View {

    void setReplacements(Map<String, String> postReplacements);

    void setConvertMessageBoardsUrls(boolean convert);

    void showNotification(String notification);

}
