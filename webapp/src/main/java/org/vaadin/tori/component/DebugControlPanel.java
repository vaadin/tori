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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityEvent;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityListener;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.category.CategoryView;
import org.vaadin.tori.dashboard.DashboardView;
import org.vaadin.tori.data.entity.AbstractEntity;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.service.DebugAuthorizationService;
import org.vaadin.tori.thread.ThreadView;

import com.google.gwt.thirdparty.guava.common.base.Joiner;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class DebugControlPanel extends CustomComponent implements
        PopupVisibilityListener {

    private static class CheckBoxShouldBeDisabledException extends Exception {
    }

    @SuppressWarnings("unused")
    private static class ContextData {
        private Category category;
        private DiscussionThread thread;
        private List<Post> posts;

        public void setCategory(final Category category) {
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }

        public void setThread(final DiscussionThread thread) {
            this.thread = thread;
        }

        public DiscussionThread getThread() {
            return thread;
        }

        public void setPosts(final List<Post> posts) {
            this.posts = posts;
        }

        public List<Post> getPosts() {
            return posts;
        }
    }

    private class CheckboxListener implements ValueChangeListener {
        private final ContextData data;
        private final Method setter;

        public CheckboxListener(final ContextData data, final Method setter) {
            this.data = data;
            this.setter = setter;
        }

        @Override
        public void valueChange(final ValueChangeEvent event) {
            final boolean newValue = ((CheckBox) event.getProperty())
                    .getValue();
            callSetter(newValue);
            navigator.navigateTo(navigator.getState());
        }

        private void callSetter(final boolean newValue) {
            try {

                if (methodHasArguments(setter, 1)) {
                    setter.invoke(authorizationService, newValue);
                }

                else {
                    final Class<?> paramClass = setter.getParameterTypes()[0];
                    if (paramClass == Post.class) {
                        throw new IllegalStateException("Setters for "
                                + Post.class.getName()
                                + " should be handled by "
                                + "another piece of code. MAJOR BUG!");
                    } else {
                        final Object setterParam = getCorrectTypeOfDataFrom(
                                paramClass, data);
                        setter.invoke(authorizationService, setterParam,
                                newValue);
                    }
                }

            } catch (final Exception e) {
                Notification.show(e.getClass().getSimpleName());
                e.printStackTrace();
            }

        }
    }

    private class PostCheckboxListener implements ValueChangeListener {

        private final Post post;
        private final Method setter;

        public PostCheckboxListener(final Post post, final Method setter) {
            this.post = post;
            this.setter = setter;
        }

        @Override
        public void valueChange(final ValueChangeEvent event) {
            final boolean newValue = ((CheckBox) event.getProperty())
                    .getValue();
            callSetter(newValue);
            navigator.navigateTo(navigator.getState());
        }

        private void callSetter(final boolean newValue) {
            try {
                setter.invoke(authorizationService, post, newValue);
            } catch (final Exception e) {
                Notification.show(e.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }

    private final DebugAuthorizationService authorizationService;
    private final ToriNavigator navigator;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "we don't care about serialization")
    private ContextData data;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "we don't care about serialization")
    private final Logger log = Logger.getLogger(getClass());
    protected com.vaadin.navigator.View currentView;

    public DebugControlPanel(
            final DebugAuthorizationService authorizationService,
            final ToriNavigator navigator) {
        this.authorizationService = authorizationService;
        this.navigator = navigator;
        navigator.addViewChangeListener(new ViewChangeListener() {
            @Override
            public void afterViewChange(ViewChangeEvent event) {
                currentView = event.getNewView();
            }

            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                return true;
            }
        });

        final PopupButton popupButton = new PopupButton("Debug Control Panel");
        popupButton.setIcon(new ThemeResource("images/icon-settings.png"));
        popupButton.addPopupVisibilityListener(this);
        setCompositionRoot(popupButton);
    }

    @Override
    public void popupVisibilityChange(final PopupVisibilityEvent event) {
        final ContextData data = getContextData();
        if (event.isPopupVisible()) {
            final PopupButton popupButton = event.getPopupButton();
            popupButton.setContent(createControlPanel(data));
        }
    }

    private ContextData getContextData() {
        final ContextData data = new ContextData();
        if (currentView instanceof DashboardView) {
            // NOP - no context data, can't populate
        } else if (currentView instanceof CategoryView) {
            final CategoryView categoryView = (CategoryView) currentView;
            data.setCategory(categoryView.getCurrentCategory());
        } else if (currentView instanceof ThreadView) {
            final ThreadView threadView = (ThreadView) currentView;
            final DiscussionThread currentThread = threadView
                    .getCurrentThread();

            if (currentThread != null) {
                data.setCategory(currentThread.getCategory());
                data.setThread(currentThread);
                data.setPosts(currentThread.getPosts());
            } else {
                log.warn("currentThread was null");
            }
        }

        return data;
    }

    private Component createControlPanel(final ContextData data) {
        this.data = data;

        final Panel panel = new Panel();
        panel.setStyleName(Reindeer.PANEL_LIGHT);
        panel.setWidth("300px");
        panel.setHeight("300px");

        final Set<Method> setters = getSettersByReflection(authorizationService);

        final List<Method> orderedSetters = Lists.newArrayList(setters);
        Collections.sort(orderedSetters, new Comparator<Method>() {
            @Override
            public int compare(final Method o1, final Method o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        try {
            for (final Method setter : orderedSetters) {
                if (isForPosts(setter)) {
                    panel.setContent(createPostControl(setter, data.getPosts()));
                } else {
                    panel.setContent(createRegularControl(setter));
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            panel.setContent(new Label(e.toString()));
        }

        return panel;
    }

    private Component createPostControl(final Method setter,
            final List<Post> posts) throws Exception {
        if (posts == null || posts.isEmpty()) {
            final Label label = new Label(getNameForCheckBox(setter));
            label.setEnabled(false);
            return label;
        }

        final PopupView popup = new PopupView(getNameForCheckBox(setter),
                new CustomComponent() {
                    {
                        final CssLayout root = new CssLayout();
                        setCompositionRoot(root);
                        root.setWidth("100%");
                        setWidth("400px");

                        root.addComponent(new Label(setter.getName()));

                        for (final Post post : posts) {
                            final Method getter = getGetterFrom(setter);
                            final boolean getterValue = (Boolean) getter
                                    .invoke(authorizationService, post);

                            final String authorName = post.getAuthor()
                                    .getDisplayedName();
                            final String postBody = post.getBodyRaw()
                                    .substring(0, 20);

                            final CheckBox checkbox = new CheckBox(authorName
                                    + " :: " + postBody);
                            checkbox.setValue(getterValue);
                            checkbox.addValueChangeListener(new PostCheckboxListener(
                                    post, setter));
                            checkbox.setImmediate(true);
                            checkbox.setWidth("100%");
                            root.addComponent(checkbox);
                        }
                    }
                });
        popup.setHideOnMouseOut(false);
        return popup;
    }

    private Component createRegularControl(final Method setter)
            throws SecurityException, NoSuchMethodException, Exception {
        final CheckBox checkbox = new CheckBox(getNameForCheckBox(setter));
        try {
            final boolean getterValue = callGetter(getGetterFrom(setter));
            checkbox.setValue(getterValue);
            checkbox.addValueChangeListener(new CheckboxListener(data, setter));
            checkbox.setImmediate(true);
        } catch (final CheckBoxShouldBeDisabledException e) {
            /*
             * Because our context doesn't have data to set up this object, we
             * disable the checkbox. E.g. DashboardView doesn't have context
             * data on a particular Category or Thread
             */

            checkbox.setEnabled(false);
        }
        return checkbox;
    }

    private String getNameForCheckBox(final Method setter) {
        if (setter.getParameterTypes().length == 1) {
            return setter.getName();
        } else {
            final List<String> typeNames = Lists.newArrayList();
            for (final Class<?> type : setter.getParameterTypes()) {
                typeNames.add(type.getSimpleName());
            }
            typeNames.remove(typeNames.size() - 1); // the last boolean
            final String params = Joiner.on(", ").join(typeNames);
            return setter.getName() + "(" + params + ")";
        }
    }

    private static boolean isForPosts(final Method setter) {
        return setter.getParameterTypes()[0] == Post.class;
    }

    private boolean callGetter(final Method getter)
            throws CheckBoxShouldBeDisabledException, Exception {
        if (methodHasArguments(getter, 0)) {
            return (Boolean) getter.invoke(authorizationService);
        } else if (methodHasArguments(getter, 1)) {
            final Class<?> paramClass = getter.getParameterTypes()[0];
            final Object entityParameter = getCorrectTypeOfDataFrom(paramClass,
                    data);

            if (entityParameter != null) {
                return (Boolean) getter.invoke(authorizationService,
                        entityParameter);
            } else {
                throw new CheckBoxShouldBeDisabledException();
            }
        } else {
            throw new IllegalArgumentException("Getter has too many parameters");
        }
    }

    private Method getGetterFrom(final Method setter) throws SecurityException,
            NoSuchMethodException {
        if (setter.getParameterTypes().length == 1) {
            // this is a simple, global access right.

            final String getterSubString = setter.getName().substring(3);
            final String getterName = getterSubString.substring(0, 1)
                    .toLowerCase() + getterSubString.substring(1);

            for (final Method method : authorizationService.getClass()
                    .getMethods()) {
                if (method.getName().equals(getterName)
                        && method.getParameterTypes().length == 0) {
                    return method;
                }
            }

            throw new NoSuchMethodException("No expected method " + getterName
                    + "() was found in "
                    + authorizationService.getClass().getName());
        } else if (setter.getParameterTypes().length == 2) {
            // this is a access right to a certain object.

            final Class<?> type = setter.getParameterTypes()[0];
            final String getterSubString = setter.getName().substring(3);
            final String getterName = getterSubString.substring(0, 1)
                    .toLowerCase() + getterSubString.substring(1);

            for (final Method method : authorizationService.getClass()
                    .getMethods()) {
                if (method.getName().equals(getterName)
                        && method.getParameterTypes().length == 1
                        && method.getParameterTypes()[0] == type) {
                    return method;
                }
            }

            throw new NoSuchMethodException("No expected method " + getterName
                    + "(" + type.getSimpleName() + ") was found in "
                    + authorizationService.getClass().getName());
        } else {
            throw new RuntimeException(
                    "Setter has an unexpected amount of parameters. ARCHITECTURE BUG!");
        }
    }

    private static Set<Method> getSettersByReflection(
            final DebugAuthorizationService object) {
        final Set<Method> setters = new HashSet<Method>();

        for (final Method method : object.getClass().getMethods()) {
            final boolean soundsLikeASetter = method.getName()
                    .startsWith("set");

            if (soundsLikeASetter) {
                if (isAnAcceptableSetter(method)) {
                    setters.add(method);
                } else {
                    // if this happens, it's actually our own fault. Oops.
                    throw new IllegalStateException("method " + method
                            + " sounds like a setter, but doesn't "
                            + "conform to our format");
                }
            }
        }

        return setters;
    }

    private static boolean methodHasArguments(final Method method,
            final int amount) {
        return method.getParameterTypes().length == amount;
    }

    private static boolean isAnAcceptableSetter(final Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();

        if (methodHasArguments(method, 1)) {
            return parameterTypes[0] == boolean.class;
        } else if (methodHasArguments(method, 2)) {
            return AbstractEntity.class.isAssignableFrom(parameterTypes[0])
                    && parameterTypes[1] == boolean.class;
        } else {
            throw new IllegalArgumentException(
                    "Given method has 0 or more than 2 parameters");
        }
    }

    private static <T extends Object> T getCorrectTypeOfDataFrom(
            final Class<T> paramClass, final ContextData data) {
        try {
            for (final Method method : data.getClass().getMethods()) {
                if (method.getReturnType() == paramClass
                        && method.getParameterTypes().length == 0) {
                    @SuppressWarnings("unchecked")
                    final T value = (T) method.invoke(data);
                    return value;
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        throw new RuntimeException(ContextData.class.getName()
                + " does not have a no-arg method that "
                + "would return the data type " + paramClass);
    }
}
