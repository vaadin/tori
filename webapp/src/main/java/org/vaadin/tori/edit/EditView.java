package org.vaadin.tori.edit;

import java.util.Map;

import org.vaadin.tori.mvp.View;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public interface EditView extends View {

    void setReplacements(Map<String, String> postReplacements);

    void setConvertMessageBoardsUrls(boolean convert);

    void showNotification(String notification);

    void setGoogleAnalyticsTrackerId(
            @CheckForNull String googleAnalyticsTrackerId);

}
