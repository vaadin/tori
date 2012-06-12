package org.vaadin.tori.edit;

import java.util.Map;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public class EditPresenter extends Presenter<EditView> {

    public EditPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    @Override
    public final void init() {
        refreshView();
    }

    private void refreshView() {
        getView().setReplacements(dataSource.getPostReplacements());
        getView().setConvertMessageBoardsUrls(
                dataSource.getReplaceMessageBoardsLinks());
        getView().setGoogleAnalyticsTrackerId(
                dataSource.getGoogleAnalyticsTrackerId());
    }

    public final void savePreferences(final Map<String, String> replacements,
            final boolean replaceMessageBoardsLinks,
            final @CheckForNull String googleAnalyticsTrackerId) {
        try {
            dataSource.savePortletPreferences(replacements,
                    replaceMessageBoardsLinks, googleAnalyticsTrackerId);
            getView().showNotification("Preferences saved");
        } catch (final DataSourceException e) {
            getView()
                    .showNotification(
                            "There was an error while saving preferences. Please see the log.");
        }
        refreshView();
    }

}
