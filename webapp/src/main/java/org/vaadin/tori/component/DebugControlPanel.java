package org.vaadin.tori.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityEvent;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityListener;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.service.DebugAuthorizationService;

import com.google.common.collect.Lists;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class DebugControlPanel extends CustomComponent implements
        PopupVisibilityListener {

    private final DebugAuthorizationService authorizationService;
    private final ToriNavigator navigator;

    private final ClickListener checkboxClickListener = new ClickListener() {
        @Override
        public void buttonClick(final ClickEvent event) {
            try {
                final Button button = event.getButton();
                final Method setter = (Method) button.getData();
                final boolean newValue = button.booleanValue();
                setter.invoke(authorizationService, newValue);
                navigator.recreateCurrentView();
            } catch (final IllegalArgumentException e) {
                getApplication().getMainWindow().showNotification(
                        e.getClass().getSimpleName());
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                getApplication().getMainWindow().showNotification(
                        e.getClass().getSimpleName());
                e.printStackTrace();
            } catch (final InvocationTargetException e) {
                getApplication().getMainWindow().showNotification(
                        e.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    };

    public DebugControlPanel(
            final DebugAuthorizationService authorizationService,
            final ToriNavigator navigator) {
        this.authorizationService = authorizationService;
        this.navigator = navigator;

        final PopupButton popupButton = new PopupButton("Debug Control Panel");
        popupButton.setIcon(new ThemeResource("images/icon-settings.png"));
        popupButton.addComponent(new Label());
        popupButton.addPopupVisibilityListener(this);
        setCompositionRoot(popupButton);
    }

    @Override
    public void popupVisibilityChange(final PopupVisibilityEvent event) {
        if (event.isPopupVisible()) {
            final PopupButton popupButton = event.getPopupButton();
            popupButton.removeAllComponents();
            popupButton.setComponent(createControlPanel());
        }
    }

    private Component createControlPanel() {
        final Panel panel = new Panel();
        panel.setStyleName(Reindeer.PANEL_LIGHT);
        panel.setScrollable(true);
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
                final CheckBox checkbox = new CheckBox(setter.getName());
                checkbox.setData(setter);
                checkbox.setValue(getGetterFrom(setter).invoke(
                        authorizationService));
                checkbox.addListener(checkboxClickListener);
                checkbox.setImmediate(true);
                panel.addComponent(checkbox);
            }
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
            panel.addComponent(new Label(e.toString()));
        } catch (final ReadOnlyException e) {
            e.printStackTrace();
            panel.addComponent(new Label(e.toString()));
        } catch (final ConversionException e) {
            e.printStackTrace();
            panel.addComponent(new Label(e.toString()));
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
            panel.addComponent(new Label(e.toString()));
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
            panel.addComponent(new Label(e.toString()));
        } catch (final SecurityException e) {
            e.printStackTrace();
            panel.addComponent(new Label(e.toString()));
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
            panel.addComponent(new Label(e.toString()));
        }

        return panel;
    }

    private Method getGetterFrom(final Method setter) throws SecurityException,
            NoSuchMethodException {
        final String getterSubString = setter.getName().substring(3);
        final String getterName = getterSubString.substring(0, 1).toLowerCase()
                + getterSubString.substring(1);
        return authorizationService.getClass().getMethod(getterName);
    }

    private static Set<Method> getSettersByReflection(
            final DebugAuthorizationService object) {
        final Set<Method> setters = new HashSet<Method>();

        for (final Method method : object.getClass().getMethods()) {
            final boolean soundsLikeASetter = method.getName()
                    .startsWith("set");

            if (soundsLikeASetter) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                final boolean hasExactlyOneParameter = parameterTypes.length == 1;
                final boolean parameterTypeIsBoolean = parameterTypes[0] == boolean.class;

                if (hasExactlyOneParameter && parameterTypeIsBoolean) {
                    setters.add(method);
                } else {
                    // if this happens, it's actually our own fault. Oops.
                    throw new IllegalStateException("method " + method
                            + " sounds like a setter, but doesn't have "
                            + "exactly one boolean parameter");
                }
            }
        }

        return setters;
    }
}
