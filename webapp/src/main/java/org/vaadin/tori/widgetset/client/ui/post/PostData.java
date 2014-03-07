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

package org.vaadin.tori.widgetset.client.ui.post;

import java.util.Map;

import com.vaadin.shared.Connector;

public class PostData {

    public static class PostPrimaryData {
        public String authorName;
        public String authorLink;
        public String postBody;
        public String authorAvatarUrl;
        public Map<String, String> attachments;
    }

    public static class PostAdditionalData {
        public String prettyTime;
        public String permaLink;
        public String badgeHTML;
        public Connector settings;
        public Connector footer;
    }
}
