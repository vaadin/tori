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

import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.view.thread.newthread.NewThreadView.ViewPermissions;

public class NewThreadPresenter extends Presenter<NewThreadView> {

    private long categoryId;

    public NewThreadPresenter(NewThreadView view) {
        super(view);
    }

    public void createNewThread(final String topic, final String rawBody,
            Map<String, byte[]> attachments) {
        StringBuilder errorMessages = new StringBuilder();
        if (topic.isEmpty()) {
            errorMessages.append("You need a topic<br/>");
        }
        if (rawBody.isEmpty()) {
            errorMessages.append("You need a thread body<br/>");
        }

        if (errorMessages.toString().isEmpty()) {

            try {
                Post post = dataSource.saveNewThread(topic, rawBody,
                        attachments, categoryId);
                messaging.sendUserAuthored(post.getId(), post.getThread()
                        .getId());
                view.newThreadCreated(post.getThread().getId());
            } catch (final DataSourceException e) {
                log.error(e);
                e.printStackTrace();
                view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
            }
        } else {
            view.showError(errorMessages.toString());
        }

    }

    @Override
    public void navigationTo(String[] args) {
        if (args.length > 0) {
            try {
                categoryId = Long.parseLong(args[0]);
                view.setViewPermissions(new ViewPermissions() {
                    @Override
                    public boolean mayAddFiles() {
                        return authorizationService
                                .mayAddFilesInCategory(categoryId);
                    }

                    @Override
                    public int getMaxFileSize() {
                        return dataSource.getAttachmentMaxFileSize();
                    }
                });
            } catch (final NumberFormatException IGNORE) {
                categoryNotFound();
            }
        } else {
            categoryNotFound();
        }
    }

    private void categoryNotFound() {
        view.showError("Category not found");
        view.redirectToDashboard();
    }

}
