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

import org.vaadin.tori.mvp.View;
import org.vaadin.tori.view.thread.AuthoringData;

public interface NewThreadView extends View {
    void showNotification(String message);

    void showError(String message);

    void redirectToDashboard();

    void setViewData(ViewData viewData, AuthoringData authoringData);

    void newThreadCreated(long threadId);

    interface ViewData {
        Long getCategoryId();
    }

    void authoringFailed();

}
