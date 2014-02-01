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

package org.vaadin.tori.data;

import org.vaadin.tori.data.spi.ServiceProvider;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.TestAuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.SignatureFormatter;
import org.vaadin.tori.util.TestPostFormatter;
import org.vaadin.tori.util.TestSignatureFormatter;
import org.vaadin.tori.util.ToriActivityMessaging;

public class TestServiceProvider implements ServiceProvider {

    @Override
    public DataSource createDataSource() {
        try {
            return new TestDataSource();
        } catch (final DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PostFormatter createPostFormatter() {
        return new TestPostFormatter();
    }

    @Override
    public AuthorizationService createAuthorizationService() {
        return new TestAuthorizationService();
    }

    @Override
    public SignatureFormatter createSignatureFormatter() {
        return new TestSignatureFormatter();
    }

    @Override
    public ToriActivityMessaging createToriActivityMessaging() {
        return new ToriActivityMessaging() {

            @Override
            public void addUserTypingListener(UserTypingListener listener) {
                // TODO Auto-generated method stub

            }

            @Override
            public void addUserAuthoredListener(UserAuthoredListener listener) {
                // TODO Auto-generated method stub

            }

            @Override
            public void removeUserTypingListener(UserTypingListener listener) {
                // TODO Auto-generated method stub

            }

            @Override
            public void removeUserAuthoredListener(UserAuthoredListener listener) {
                // TODO Auto-generated method stub

            }

            @Override
            public void sendUserTyping(long threadId) {
                // TODO Auto-generated method stub

            }

            @Override
            public void sendUserAuthored(long postId, long threadId) {
                // TODO Auto-generated method stub

            }

        };
    }

}
