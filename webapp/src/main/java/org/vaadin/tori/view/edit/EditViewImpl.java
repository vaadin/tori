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

package org.vaadin.tori.view.edit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.tori.Configuration;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.util.ComponentUtil;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class EditViewImpl extends AbstractView<EditView, EditPresenter>
        implements EditView {

    private final VerticalLayout mainLayout = new VerticalLayout();
    private final TabSheet tabSheet = new TabSheet();
    private final BeanFieldGroup<Configuration> fieldGroup = new BeanFieldGroup<Configuration>(
            Configuration.class);

    private Component generalPreferencesLayout;
    private Component emailPreferencesLayout;

    @PropertyId("replaceMessageBoardsLinks")
    private CheckBox replaceMessageBoardsLinks;
    @PropertyId("updatePageTitle")
    private CheckBox updatePageTitle;
    @PropertyId("showThreadsOnDashboard")
    private CheckBox showThreadsOnDashboard;
    @PropertyId("pageTitlePrefix")
    private TextField pageTitlePrefix;
    @PropertyId("googleAnalyticsTrackerId")
    private TextField googleAnalyticsTrackerId;
    @PropertyId("mayNotReplyNote")
    private TextField mayNotReplyNote;
    private Table replacementsTable;

    @PropertyId("useToriMailService")
    private CheckBox useToriMailService;
    @PropertyId("emailFromAddress")
    private TextField emailFromAddress;
    @PropertyId("emailFromName")
    private TextField emailFromName;
    @PropertyId("emailReplyToAddress")
    private TextField emailReplyToAddress;
    @PropertyId("emailHeaderImageUrl")
    private TextField emailHeaderImageUrl;

    public EditViewImpl(final DataSource dataSource,
            final AuthorizationService authorizationService) {
    }

    @Override
    protected Component createCompositionRoot() {
        return mainLayout;
    }

    @Override
    public void initView() {
        setStyleName("editview");
        mainLayout.addComponent(tabSheet);
        mainLayout.setSpacing(true);

        generalPreferencesLayout = buildGeneralPreferences();
        tabSheet.addTab(generalPreferencesLayout, "General preferences");

        emailPreferencesLayout = buildEmailPreferences();
        tabSheet.addTab(emailPreferencesLayout, "Email preferences");

        Component saveButton = buildSaveButton();
        mainLayout.addComponent(saveButton);
        mainLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);

        getPresenter().init();
    }

    private Component buildEmailPreferences() {
        VerticalLayout result = new VerticalLayout();
        result.addStyleName("preferenceslayout");
        result.setSpacing(true);
        result.setMargin(true);

        result.addComponent(buildUseToriMailService());
        result.addComponent(buildEmailFromAddress());
        result.addComponent(buildEmailFromName());
        result.addComponent(buildEmailReplyToAddress());
        result.addComponent(buildEmailHeaderImageURL());

        return result;
    }

    private Component buildUseToriMailService() {
        useToriMailService = new CheckBox("Use Tori mail service");
        useToriMailService.setImmediate(true);
        useToriMailService.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                updateEmailPreferencesEnabled();
            }
        });
        return getFieldWrapper(
                "Use Tori notification email functionality instead of "
                        + "the default one provided by the Message Boards portlet",
                useToriMailService);
    }

    private void updateEmailPreferencesEnabled() {
        emailFromAddress.setEnabled(useToriMailService.getValue());
        emailFromName.setEnabled(useToriMailService.getValue());
        emailReplyToAddress.setEnabled(useToriMailService.getValue());
        emailHeaderImageUrl.setEnabled(useToriMailService.getValue());
    }

    private Component buildEmailFromAddress() {
        emailFromAddress = getTextField("Default");
        return getFieldWrapper("Email from address", emailFromAddress);
    }

    private Component buildEmailFromName() {
        emailFromName = getTextField("Default");
        return getFieldWrapper("Email from name", emailFromName);
    }

    private Component buildEmailReplyToAddress() {
        emailReplyToAddress = getTextField("Default");
        return getFieldWrapper("Reply to address", emailReplyToAddress);
    }

    private Component buildEmailHeaderImageURL() {
        emailHeaderImageUrl = getTextField("Default (Tori-icon)");
        return getFieldWrapper("URL of the image displayed in email header",
                emailHeaderImageUrl);
    }

    private Component buildGeneralPreferences() {
        VerticalLayout result = new VerticalLayout();
        result.addStyleName("preferenceslayout");
        result.setSpacing(true);
        result.setMargin(true);

        result.addComponent(buildForumTitle());
        result.addComponent(buildUpdateTitle());
        result.addComponent(buildGAField());
        result.addComponent(buildShowThreadsOnDashboard());
        result.addComponent(buildMayNotreplyNote());
        result.addComponent(buildReplaceLinks());
        result.addComponent(buildReplacements());
        return result;
    }

    private Component buildShowThreadsOnDashboard() {
        showThreadsOnDashboard = new CheckBox("Show threads on dashboard");
        return getFieldWrapper(
                "Let Tori show threads/topics added to the root category on the front page.",
                showThreadsOnDashboard);
    }

    private Component buildSaveButton() {
        Button saveButton = new Button("Save preferences",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        try {
                            fieldGroup.commit();
                            Configuration config = fieldGroup
                                    .getItemDataSource().getBean();

                            final Map<String, String> replacements = new HashMap<String, String>();
                            for (final Object itemId : replacementsTable
                                    .getItemIds()) {
                                final Item item = replacementsTable
                                        .getItem(itemId);
                                replacements.put(
                                        (String) item.getItemProperty("regex")
                                                .getValue(), (String) item
                                                .getItemProperty("replacement")
                                                .getValue());

                            }
                            config.setReplacements(replacements);

                            getPresenter().savePreferences(config);
                        } catch (CommitException e) {
                            Notification.show("Invalid values",
                                    Notification.Type.ERROR_MESSAGE);
                        }
                    }
                });
        return saveButton;
    }

    private Component buildMayNotreplyNote() {
        mayNotReplyNote = getTextField("Default");
        return getFieldWrapper(
                "Message displayed in the end of the thread when user is not allowed to reply",
                mayNotReplyNote);
    }

    private Component buildUpdateTitle() {
        updatePageTitle = new CheckBox("Update the page title");
        return getFieldWrapper(
                "Allow Tori to update the browser title while navigating between different views.",
                updatePageTitle);
    }

    private Component buildForumTitle() {
        pageTitlePrefix = getTextField("Default");
        return getFieldWrapper("Forum title.", pageTitlePrefix);
    }

    private Component buildReplaceLinks() {
        replaceMessageBoardsLinks = new CheckBox(
                "Replace message boards link data with tori format");
        return getFieldWrapper(
                "Check the box beneath to let Tori scan post content render-time for links "
                        + "intended for Liferay message boards portlet and convert them to tori-format for display.",
                replaceMessageBoardsLinks);
    }

    private Component buildReplacements() {
        VerticalLayout result = new VerticalLayout();
        result.addComponent(getSubTitle("Define post body regex-patterns/replacements to be "
                + "applied whenever posts are being displayed/previewed."));

        replacementsTable = new Table();
        replacementsTable.setWidth("100%");
        replacementsTable.setHeight("10em");
        replacementsTable.setSelectable(true);
        replacementsTable.setImmediate(true);
        replacementsTable.setEditable(true);
        replacementsTable.setTableFieldFactory(new TableFieldFactory() {

            @Override
            public Field<?> createField(final Container container,
                    final Object itemId, final Object propertyId,
                    final Component uiContext) {
                final TextField textField = new TextField();
                textField.setWidth(100.0f, Unit.PERCENTAGE);
                textField.addFocusListener(new FocusListener() {
                    @Override
                    public void focus(final FocusEvent event) {
                        replacementsTable.setValue(itemId);
                    }
                });
                return textField;
            }
        });

        final Button removeButton = ComponentUtil.getSecondaryButton("Remove",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(final ClickEvent event) {
                        replacementsTable.removeItem(replacementsTable
                                .getValue());
                    }
                });

        replacementsTable
                .addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(final ValueChangeEvent event) {
                        removeButton
                                .setEnabled(event.getProperty().getValue() != null);
                    }
                });

        replacementsTable.addContainerProperty("regex", String.class, "",
                "Regex", null, null);
        replacementsTable.setSortContainerPropertyId("regex");
        replacementsTable.addContainerProperty("replacement", String.class, "",
                "Replacement", null, null);
        result.addComponent(replacementsTable);

        removeButton.setEnabled(false);

        Button newButton = ComponentUtil.getSecondaryButton("New",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        final Object itemId = replacementsTable.addItem();
                        replacementsTable.setValue(itemId);
                    }
                });

        final HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(removeButton);
        buttonsLayout.addComponent(newButton);
        result.addComponent(buttonsLayout);

        return result;
    }

    private Component buildGAField() {
        googleAnalyticsTrackerId = getTextField("Not enabled");
        return getFieldWrapper("Google Analytics Tracker id",
                googleAnalyticsTrackerId);
    }

    private Component getFieldWrapper(final String subTitle,
            final Component field) {
        VerticalLayout result = new VerticalLayout();
        result.setSpacing(true);
        result.addComponents(getSubTitle(subTitle), field);
        result.addComponent(new Label("<hr />", ContentMode.HTML));
        return result;
    }

    private TextField getTextField(final String inputPrompt) {
        TextField result = new TextField();
        result.setNullRepresentation("");
        result.setNullSettingAllowed(true);
        result.setInputPrompt(inputPrompt);
        return result;
    }

    private Component getSubTitle(final String string) {
        Label titleLabel = new Label(string);
        titleLabel.addStyleName("subtitle");
        return titleLabel;
    }

    @Override
    protected EditPresenter createPresenter() {
        return new EditPresenter(this);
    }

    @Override
    public void setConfiguration(final Configuration configuration) {
        fieldGroup.setItemDataSource(configuration);
        fieldGroup.bindMemberFields(this);
        setReplacements(configuration.getReplacements());
        updateEmailPreferencesEnabled();
    }

    public void setReplacements(final Map<String, String> postReplacements) {
        replacementsTable.removeAllItems();
        for (final Entry<String, String> entry : postReplacements.entrySet()) {
            final Item item = replacementsTable.addItem(entry);
            item.getItemProperty("regex").setValue(entry.getKey());
            item.getItemProperty("replacement").setValue(entry.getValue());
        }
        replacementsTable.sort();
    }

    @Override
    public void showNotification(final String notification) {
        Notification.show(notification);
    }

    @Override
    public String getTitle() {
        return "Preferences View";
    }

}
