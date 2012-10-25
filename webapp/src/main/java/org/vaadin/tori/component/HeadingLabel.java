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

package org.vaadin.tori.component;

import com.vaadin.ui.Label;

/**
 * Label for displaying HTML headings ({@code h1, h2, etc.}).
 */
@SuppressWarnings("serial")
public class HeadingLabel extends Label {

    public enum HeadingLevel {
        H1, H2, H3, H4
    }

    public HeadingLabel(final String content, final HeadingLevel level) {
        super(content);
        addStyleName(level.name().toLowerCase());
    }

}
