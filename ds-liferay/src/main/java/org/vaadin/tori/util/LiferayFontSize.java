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

import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;

public enum LiferayFontSize implements FontSize {
    // @formatter:off
    SIZE1("1", "1"),
    SIZE2("2", "2"),
    SIZE3("3", "3"),
    SIZE4("4", "4"),
    SIZE5("5", "5"),
    SIZE6("6", "6"),
    SIZE7("7", "7");
    // @formatter:on

    private String name;
    private String syntax;

    private LiferayFontSize(final String name, final String syntax) {
        this.name = name;
        this.syntax = syntax;
    }

    @Override
    public String getFontSizeName() {
        return name;
    }

    @Override
    public String getFontSizeSyntax() {
        return syntax;
    }

}
