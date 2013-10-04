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

import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;

/**
 * Font faces supported by Liferay forum.
 */
public enum LiferayFontFace implements FontFace {
    // @formatter:off
    ARIAL("Arial", "[font=Arial][/font]"),
    COMIC_SANS("Comic Sans MS", "[font=Comic Sans MS][/font]"),
    COURIER_NEW("Courier New", "[font=Courier New][/font]"),
    TAHOMA("Tahoma", "[font=Tahoma][/font]"),
    TIMES_NEW_ROMAN("Times New Roman", "[font=Times New Roman][/font]"),
    VERDANA("Verdana", "[font=Verdana][/font]");
    // @formatter:on

    private String name;
    private String syntax;

    private LiferayFontFace(final String name, final String syntax) {
        this.name = name;
        this.syntax = syntax;
    }

    @Override
    public String getFontName() {
        return name;
    }

    @Override
    public String getFontSyntax() {
        return syntax;
    }

}
