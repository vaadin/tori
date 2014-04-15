/*
 * Copyright 2013 Vaadin Ltd.
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

package org.vaadin.tori;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.vaadin.hene.popupbutton.widgetset.client.ui.PopupButtonConnector;
import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.CKEditorConnector;
import org.vaadin.tori.widgetset.client.ui.ToriUIConnector;
import org.vaadin.tori.widgetset.client.ui.floatingcomponent.FloatingComponentConnector;
import org.vaadin.tori.widgetset.client.ui.post.PostComponentConnector;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingConnector;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.vaadin.client.JavaScriptExtension;
import com.vaadin.client.extensions.javascriptmanager.JavaScriptManagerConnector;
import com.vaadin.client.ui.button.ButtonConnector;
import com.vaadin.client.ui.checkbox.CheckBoxConnector;
import com.vaadin.client.ui.csslayout.CssLayoutConnector;
import com.vaadin.client.ui.customcomponent.CustomComponentConnector;
import com.vaadin.client.ui.image.ImageConnector;
import com.vaadin.client.ui.label.LabelConnector;
import com.vaadin.client.ui.link.LinkConnector;
import com.vaadin.client.ui.menubar.MenuBarConnector;
import com.vaadin.client.ui.optiongroup.OptionGroupConnector;
import com.vaadin.client.ui.orderedlayout.HorizontalLayoutConnector;
import com.vaadin.client.ui.orderedlayout.VerticalLayoutConnector;
import com.vaadin.client.ui.panel.PanelConnector;
import com.vaadin.client.ui.table.TableConnector;
import com.vaadin.client.ui.tabsheet.TabsheetConnector;
import com.vaadin.client.ui.textarea.TextAreaConnector;
import com.vaadin.client.ui.textfield.TextFieldConnector;
import com.vaadin.client.ui.tree.TreeConnector;
import com.vaadin.client.ui.treetable.TreeTableConnector;
import com.vaadin.client.ui.upload.UploadConnector;
import com.vaadin.client.ui.window.WindowConnector;
import com.vaadin.server.widgetsetutils.ConnectorBundleLoaderFactory;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.Connect.LoadStyle;

public class ToriConnectorBundleLoaderFactory extends
        ConnectorBundleLoaderFactory {

    private static final Map<String, Boolean> CONNECTORS = new HashMap<String, Boolean>();

    static {
        addConnector(ToriUIConnector.class, false);
        addConnector(VerticalLayoutConnector.class, false);
        addConnector(HorizontalLayoutConnector.class, false);
        addConnector(LinkConnector.class, false);
        addConnector(CustomComponentConnector.class, false);
        addConnector(CssLayoutConnector.class, false);
        addConnector(PostComponentConnector.class, false);
        addConnector(JavaScriptManagerConnector.class, false);
        addConnector(JavaScriptExtension.class, false);
        addConnector(FloatingComponentConnector.class, false);
        addConnector(LabelConnector.class, false);

        addConnector(WindowConnector.class, true);
        addConnector(TabsheetConnector.class, true);
        addConnector(TreeConnector.class, true);
        addConnector(MenuBarConnector.class, true);
        addConnector(PanelConnector.class, true);
        addConnector(PopupButtonConnector.class, true);
        addConnector(CKEditorConnector.class, true);
        addConnector(TreeTableConnector.class, true);
        addConnector(ThreadListingConnector.class, true);
        addConnector(UploadConnector.class, true);
        addConnector(ButtonConnector.class, true);
        addConnector(TextFieldConnector.class, true);
        addConnector(TextAreaConnector.class, true);
        addConnector(CheckBoxConnector.class, true);
        addConnector(OptionGroupConnector.class, true);
        addConnector(TableConnector.class, true);
        addConnector(ImageConnector.class, true);
    }

    private static void addConnector(final Class<? extends Connector> clazz,
            final boolean lazy) {
        CONNECTORS.put(clazz.getName(), lazy);
    }

    @Override
    protected Collection<JClassType> getConnectorsForWidgetset(
            final TreeLogger logger, final TypeOracle typeOracle)
            throws UnableToCompleteException {
        Collection<JClassType> connectorsForWidgetset = super
                .getConnectorsForWidgetset(logger, typeOracle);
        ArrayList<JClassType> arrayList = new ArrayList<JClassType>();
        for (JClassType jClassType : connectorsForWidgetset) {
            String qualifiedSourceName = jClassType.getQualifiedSourceName();
            if (CONNECTORS.containsKey(qualifiedSourceName)) {
                arrayList.add(jClassType);
            }
        }

        return arrayList;
    }

    @Override
    protected LoadStyle getLoadStyle(final JClassType connectorType) {
        LoadStyle loadStyle = super.getLoadStyle(connectorType);

        if (CONNECTORS.containsKey(connectorType.getQualifiedSourceName())
                && CONNECTORS.get(connectorType.getQualifiedSourceName())) {
            loadStyle = LoadStyle.LAZY;
        }
        return loadStyle;
    }
}
