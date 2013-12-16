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
    SIZE1("1", "0.7em"),
    SIZE2("2", "0.8em"),
    SIZE3("3", "0.9em"),
    SIZE4("4", "1.0em"),
    SIZE5("5", "1.1em"),
    SIZE6("6", "1.3em"),
    SIZE7("7", "1.5em");
    // @formatter:on

    private String name;
    private String value;

    private LiferayFontSize(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getFontSizeName() {
        return name;
    }

    @Override
    public String getFontSizeValue() {
        return value;
    }

}
