/*
 * Copyright 2011 Vaadin Ltd.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.vaadin.tori.widgetset.client.ui.lazylayout.LazyLayoutClientRpc;
import org.vaadin.tori.widgetset.client.ui.lazylayout.LazyLayoutServerRpc;
import org.vaadin.tori.widgetset.client.ui.lazylayout.LazyLayoutState;

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class LazyLayout2 extends AbstractLayout {

    /**
     * Custom layout slots containing the components.
     */
    protected final List<Component> components = new LinkedList<Component>();
    protected final Set<Component> loadedComponents = new HashSet<Component>();

    private final LazyLayoutServerRpc rpc = new LazyLayoutServerRpc() {
        @Override
        public void fetchComponentsForIndices(final List<Integer> indicesToFetch) {
            for (final Integer index : indicesToFetch) {
                loadedComponents.add(getComponent(index));
            }

            requestRepaintAll();
            getRpcProxy(LazyLayoutClientRpc.class).renderComponents(
                    indicesToFetch);
        }
    };

    public LazyLayout2() {
        registerRpc(rpc);
        getState().setComponents(components);
    }

    @Override
    public boolean isComponentVisible(final Component childComponent) {
        return loadedComponents.contains(childComponent);
    }

    /**
     * Add a component into this container. The component is added to the right
     * or under the previous component.
     * 
     * @param c
     *            the component to be added.
     */
    @Override
    public void addComponent(final Component c) {
        // Add to components before calling super.addComponent
        // so that it is available to AttachListeners
        components.add(c);
        try {
            super.addComponent(c);
            getState().setTotalAmountOfComponents(getComponentCount());

            requestRepaint();
        } catch (final IllegalArgumentException e) {
            components.remove(c);
            throw e;
        }
    }

    public void addComponentEagerly(final Component c) {
        addComponent(c);
        loadedComponents.add(c);
    }

    /**
     * Removes the component from this container.
     * 
     * @param c
     *            the component to be removed.
     */
    @Override
    public void removeComponent(final Component c) {
        components.remove(c);
        super.removeComponent(c);
        getState().setTotalAmountOfComponents(getComponentCount());
        requestRepaint();
    }

    /**
     * Gets the component container iterator for going trough all the components
     * in the container.
     * 
     * @return the Iterator of the components inside the container.
     */
    @Override
    public Iterator<Component> getComponentIterator() {
        return components.iterator();
    }

    /**
     * Gets the number of contained components. Consistent with the iterator
     * returned by {@link #getComponentIterator()}.
     * 
     * @return the number of contained components
     */
    @Override
    public int getComponentCount() {
        return components.size();
    }

    @Override
    public LazyLayoutState getState() {
        return (LazyLayoutState) super.getState();
    }

    /**
     * Returns styles to be applied to given component. Override this method to
     * inject custom style rules to components.
     * 
     * <p>
     * Note that styles are injected over previous styles before actual child
     * rendering. Previous styles are not cleared, but overridden.
     * 
     * <p>
     * Note that one most often achieves better code style, by separating
     * styling to theme (with custom theme and {@link #addStyleName(String)}.
     * With own custom styles it is also very easy to break browser
     * compatibility.
     * 
     * @param c
     *            the component
     * @return css rules to be applied to component
     */
    protected String getCss(final Component c) {
        return null;
    }

    /**
     * Don't use it.
     * 
     * @throws UnsupportedOperationException
     * @deprecated not supported
     */
    @Deprecated
    @Override
    public void replaceComponent(final Component oldComponent,
            final Component newComponent) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the index of the given component.
     * 
     * @param component
     *            The component to look up.
     * @return The index of the component or -1 if the component is not a child.
     */
    public int getComponentIndex(final Component component) {
        return components.indexOf(component);
    }

    /**
     * Returns the component at the given position.
     * 
     * @param index
     *            The position of the component.
     * @return The component at the given index.
     * @throws IndexOutOfBoundsException
     *             If the index is out of range.
     */
    public Component getComponent(final int index)
            throws IndexOutOfBoundsException {
        return components.get(index);
    }

    public void setPlaceholderSize(final String placeholderHeight,
            final String placeholderWidth) {
        getState().setPlaceholderHeight(placeholderHeight);
        getState().setPlaceholderWidth(placeholderWidth);
    }

    public void setRenderDistance(final int renderDistancePx) {
        getState().setRenderDistance(renderDistancePx);
    }

    public void setRenderDelay(final int renderDelayMillis) {
        getState().setRenderDelay(renderDelayMillis);
    }
}
