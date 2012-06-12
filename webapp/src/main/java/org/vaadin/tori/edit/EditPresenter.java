package org.vaadin.tori.edit;

import java.util.Map;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;

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
    }

    public final void savePreferences(final Map<String, String> replacements,
            final boolean replaceMessageBoardsLinks) {
        try {
            dataSource.savePortletPreferences(replacements,
                    replaceMessageBoardsLinks);
            getView().showNotification("Preferences saved");
        } catch (final DataSourceException e) {
            getView()
                    .showNotification(
                            "There was an error while saving preferences. Please see the log.");
        }
        refreshView();
    }

}
