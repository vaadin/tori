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

package org.vaadin.tori.view.thread.newthread;

import java.util.Map;

import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.exception.FileNameException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.util.UserBadgeProvider;
import org.vaadin.tori.view.thread.AuthoringData;
import org.vaadin.tori.view.thread.newthread.NewThreadView.ViewData;

public class NewThreadPresenter extends Presenter<NewThreadView> {

    private Long categoryId;

    public NewThreadPresenter(final NewThreadView view) {
        super(view);
    }

    public void saveNewThread(final String topic, final String rawBody,
            final Map<String, byte[]> attachments, final boolean follow) {
        if (topic.isEmpty() || rawBody.isEmpty()) {
            view.showError("Thread topic and body needed");
        } else {
            try {
                Post post = dataSource.saveNewThread(topic, rawBody,
                        attachments, categoryId);
                if (follow) {
                    dataSource.followThread(post.getThread().getId());
                }
                messaging.sendUserAuthored(post.getId(), post.getThread()
                        .getId());
                view.newThreadCreated(post.getThread().getId());
            } catch (final FileNameException e) {
                log.error(e);
                e.printStackTrace();
                view.showError("Invalid file names");
                view.authoringFailed();
            } catch (final DataSourceException e) {
                log.error(e);
                e.printStackTrace();
                view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
                view.authoringFailed();
            }
        }
    }

    private AuthoringData getAuthoringData() {
        final User currentUser = dataSource.getCurrentUser();
        return new AuthoringData() {
            @Override
            public boolean mayAddFiles() {
                return authorizationService.mayAddFilesInCategory(categoryId);
            }

            @Override
            public int getMaxFileSize() {
                return dataSource.getAttachmentMaxFileSize();
            }

            @Override
            public String getCurrentUserName() {
                return currentUser.getDisplayedName();
            }

            @Override
            public String getCurrentUserAvatarUrl() {
                return currentUser.getAvatarUrl();
            }

            @Override
            public String getCurrentUserBadgeHTML() {
                String result = null;
                final UserBadgeProvider badgeProvider = ToriApiLoader
                        .getCurrent().getUserBadgeProvider();
                if (badgeProvider != null) {
                    result = badgeProvider.getHtmlBadgeFor(currentUser);
                }
                return result;
            }

            @Override
            public String getCurrentUserLink() {
                return currentUser.getUserLink();
            }
        };
    }

    @Override
    public void navigationTo(final String[] args) {
        try {
            String categoryString = args[0];
            categoryId = categoryString.isEmpty() ? null : Long
                    .parseLong(categoryString);
            NewThreadView.ViewData viewData = new ViewData() {
                @Override
                public Long getCategoryId() {
                    return categoryId;
                }
            };
            view.setViewData(viewData, getAuthoringData());
        } catch (final NumberFormatException e) {
            view.showError("Category not found");
            view.redirectToDashboard();
        }
    }

}
