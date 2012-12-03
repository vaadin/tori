/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.edit;

import java.util.Map;

import org.vaadin.tori.Configuration;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public class EditPresenter extends Presenter<EditView> {

    public EditPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    @Override
    public final void init() {
        refreshView();
    }

    @SuppressWarnings("deprecation")
    private void refreshView() {
        getView().setReplacements(dataSource.getPostReplacements());
        getView().setConvertMessageBoardsUrls(
                dataSource.getReplaceMessageBoardsLinks());
        getView().setGoogleAnalyticsTrackerId(
                dataSource.getGoogleAnalyticsTrackerId());
        getView().setPathRoot(dataSource.getPathRoot());
    }

    public final void savePreferences(final Map<String, String> replacements,
            final boolean replaceMessageBoardsLinks,
            final @CheckForNull String googleAnalyticsTrackerId,
            @NonNull final String pathRoot) {
        try {
            final Configuration config = new Configuration();
            config.setReplaceMessageBoardsLinks(replaceMessageBoardsLinks);
            config.setReplacements(replacements);
            config.setGoogleAnalyticsTrackerId(googleAnalyticsTrackerId);
            config.setPathRoot(pathRoot);
            dataSource.save(config);
            getView().showNotification("Preferences saved");
        } catch (final DataSourceException e) {
            getView()
                    .showNotification(
                            "There was an error while saving preferences. Please see the log.");
        }
        refreshView();
    }

}
