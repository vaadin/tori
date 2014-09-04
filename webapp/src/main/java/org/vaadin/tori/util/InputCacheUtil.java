/*
 * Copyright 2014 Vaadin Ltd.
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

package org.vaadin.tori.util;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;

import com.github.wolfie.clientstorage.ClientStorage;
import com.github.wolfie.clientstorage.ClientStorage.ClientStorageSupportListener;
import com.github.wolfie.clientstorage.ClientStorage.Closure;
import com.vaadin.server.Extension;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

/**
 * Provides an api for caching/fetching unfinished post in session and local
 * storage if available.
 */
public class InputCacheUtil implements ClientStorageSupportListener {

    private static final String INPUT_CACHE = "inputcache_";

    private final ClientStorage clientStorage = new ClientStorage(this);
    private Boolean clientStorageSupported;

    public Extension getExtension() {
        return clientStorage;
    }

    @Override
    public void clientStorageIsSupported(final boolean supported) {
        clientStorageSupported = supported;
    }

    @SuppressWarnings("unchecked")
    private Map<Object, String> getSessionCache() {
        VaadinSession session = UI.getCurrent().getSession();
        if (session.getAttribute(INPUT_CACHE) == null) {
            session.setAttribute(INPUT_CACHE, new HashMap<Object, String>());
        }
        return (Map<Object, String>) session.getAttribute(INPUT_CACHE);
    }

    public static void put(final String id, final String value) {
        ToriUI.getCurrent().getInputCacheUtil().doPut(id, value);
    }

    private void doPut(final String id, final String value) {
        String key = getCacheKey(id);
        getSessionCache().put(key, value);
        if (clientStorageSupported) {
            clientStorage.setLocalItem(key,
                    DatatypeConverter.printBase64Binary(value.getBytes()));
        }
    }

    public static void remove(final String id) {
        ToriUI.getCurrent().getInputCacheUtil().doRemove(id);
    }

    private void doRemove(final String id) {
        String key = getCacheKey(id);
        getSessionCache().remove(key);
        if (clientStorageSupported) {
            clientStorage.removeLocalItem(key);
        }
    }

    public static void get(final String id, final Callback callback) {
        ToriUI.getCurrent().getInputCacheUtil().doGet(id, callback);
    }

    private void doGet(final String id, final Callback callback) {
        String key = getCacheKey(id);

        final String sessionCacheValue = getSessionCache().get(key);
        if (sessionCacheValue != null) {
            callback.execute(sessionCacheValue);
        } else if (clientStorageSupported == null) {
            // ClientStorage support not determined yet....
            ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    doGet(id, callback);
                }
            });
        } else if (clientStorageSupported) {
            // Value not found in session cache. Try local storage.
            clientStorage.getLocalItem(key, new Closure() {
                @Override
                public void execute(final String value) {
                    if (value != null) {
                        try {
                            callback.execute(new String(DatatypeConverter
                                    .parseBase64Binary(value)));
                        } catch (StringIndexOutOfBoundsException e) {
                            // Invalid formatting on the encoded
                            // data. Ignore.
                        }
                    }
                }
            });
        }
    }

    private String getCacheKey(final String id) {
        long userId = ToriApiLoader.getCurrent().getDataSource()
                .getCurrentUser().getId();
        return INPUT_CACHE + userId + "_" + id;
    }

    public interface Callback {
        void execute(String value);
    }

}
