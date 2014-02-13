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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vaadin.openesignforms.ckeditor.CKEditorConfig;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;
import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.VCKEditorTextField;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.util.PostFormatter.FontsInfo;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;

import com.vaadin.server.Page;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class BBCodeWysiwygEditor extends CKEditorTextField {

    public BBCodeWysiwygEditor(boolean autoGrow) {
        addStyleName("wysiwyg-editor");
        setHeight(null);
        setImmediate(true);

        CKEditorConfig config = new CKEditorConfig();
        configureTheme(config);
        configurePlugins(config, autoGrow);
        configureFonts(config);
        configureToolbar(config);

        config.setForcePasteAsPlainText(true);
        config.disableElementsPath();
        config.setEnterMode(1);

        setConfig(config);
    }

    private void configureToolbar(CKEditorConfig config) {
        config.addCustomToolbarLine("{ items: ['Font','FontSize'] },"
                + "{ items: ['Bold','Italic','Underline','Strike','TextColor','RemoveFormat'] },"
                + "{ items: ['codebutton'] },"
                + "{ items: ['Link','Image','NumberedList','BulletedList'] },"
                + "{ items: ['Source'] }");
    }

    private void configureFonts(CKEditorConfig config) {
        FontsInfo fontsInfo = ToriApiLoader.getCurrent().getPostFormatter()
                .getFontsInfo();
        List<String> fontNames = new ArrayList<String>();
        for (FontFace ff : fontsInfo.getFontFaces()) {
            fontNames.add(ff.getFontName());
        }
        config.setFontNames(fontNames);

        StringBuilder sb = new StringBuilder();
        for (FontSize fs : fontsInfo.getFontSizes()) {
            if (!sb.toString().isEmpty()) {
                sb.append(";");
            }
            sb.append(fs.getFontSizeName()).append("/")
                    .append(fs.getFontSizeValue());
        }
        sb.append("'").insert(0, "'");

        config.addExtraConfig("fontSize_sizes", sb.toString());
    }

    private void configurePlugins(CKEditorConfig config, boolean autoGrow) {
        config.addToExtraPlugins("custombbcode");
        config.addToExtraPlugins("codebutton");
        config.addToRemovePlugins("magicline");

        WebBrowser browser = Page.getCurrent().getWebBrowser();
        boolean disableResizer = true;
        if (autoGrow) {
            if (browser.isChrome() || browser.isSafari() || browser.isOpera()) {
                config.addToExtraPlugins("autogrow");
            } else {
                disableResizer = false;
            }
        }

        if (disableResizer) {
            config.disableResizeEditor();
        }
    }

    private void configureTheme(CKEditorConfig config) {
        String themesPath = VaadinService.getCurrentRequest().getContextPath()
                + "/VAADIN/themes/";
        String toriTheme = UI.getCurrent().getTheme() + "/";
        String toriCss = themesPath + toriTheme + "styles.css";
        String editorCss = themesPath + toriTheme + "editor/editor.css";

        WebBrowser browser = Page.getCurrent().getWebBrowser();
        if (browser.isIE()) {
            String ieCss = themesPath + toriTheme + "editor/ie.css";
            config.setContentsCss(toriCss, editorCss, ieCss);
        } else {
            config.setContentsCss(toriCss, editorCss);
        }
        config.setBodyClass("v-app v-widget authoring "
                + UI.getCurrent().getTheme());
    }

    // The following avoids repaints while the user is typing a message.
    private boolean ignoreNextRepaint;

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        if (variables.containsKey(VCKEditorTextField.VAR_TEXT)) {
            ignoreNextRepaint = true;
        }
        super.changeVariables(source, variables);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        if (ignoreNextRepaint) {
            ignoreNextRepaint = false;
        } else {
            super.paintContent(target);
        }
    }
}
