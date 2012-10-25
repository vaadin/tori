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

package org.vaadin.tori.util;

import org.vaadin.tori.util.PostFormatter.FormatInfo;

public enum LiferayFormatInfo implements FormatInfo {
    // @formatter:off
    BOLD("Bold", "[b][/b]", "bold.png"), 
    ITALIC("Italic", "[i][/i]", "italic.png"),
    UNDERLINE("Underline", "[u][/u]", "underline.png"),
    STRIKETHROUGH("Strikethrough", "[s][/s]", "strikethrough.png");
    // @formatter:on

    private String name;
    private String syntax;
    private String icon;

    private LiferayFormatInfo(final String name, final String syntax,
            final String icon) {
        this.name = name;
        this.syntax = syntax;
        this.icon = icon;
    }

    @Override
    public String getFormatName() {
        return name;
    }

    @Override
    public String getFormatSyntax() {
        return syntax;
    }

    @Override
    public String getFormatIcon() {
        return icon;
    }

}
