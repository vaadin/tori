package org.vaadin.tori.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vaadin.tori.widgetset.client.ui.lazylayout.VLazyLayout;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;

/**
 * Forked from {@link com.vaadin.ui.CssLayout} in Vaadin 6.7.3
 */
@ClientWidget(VLazyLayout.class)
public final class LazyLayout extends AbstractLayout {
    private static class ComponentDiff {
        private final Map<Component, Integer> additions;
        private final Map<Component, Integer> removes;
        private final Map<Component, Integer> moves;

        public ComponentDiff(final Map<Component, Integer> additions,
                final Map<Component, Integer> removes,
                final Map<Component, Integer> moves) {
            this.additions = additions;
            this.removes = removes;
            this.moves = moves;
        }

        public Map<Component, Integer> getAdditions() {
            return additions;
        }

        public Map<Component, Integer> getRemoves() {
            return removes;
        }

        public Map<Component, Integer> getMoves() {
            return moves;
        }

        public Integer[] getAdditionIndexes() {
            /*
             * this NEEDS to be in ascending order, or we'll add totally wrong
             * indexes
             */
            final List<Integer> list = new ArrayList<Integer>(
                    additions.values());
            Collections.sort(list);
            return toIntArray(list);
        }

        private static Integer[] toIntArray(final Collection<Integer> collection) {
            return collection.toArray(new Integer[collection.size()]);
        }

        public Integer[] getRemovesIndexes() {
            /*
             * this NEEDS to be in DESECNDING order, or we'll remove stuff out
             * of the index.
             */
            final List<Integer> list = new ArrayList<Integer>(removes.values());
            Collections.sort(list);
            Collections.reverse(list);
            return toIntArray(list);
        }

        public boolean hasChanges() {
            return !(additions.isEmpty() && removes.isEmpty() && moves
                    .isEmpty());
        }
    }

    private static final long serialVersionUID = -2190849834160200478L;

    /**
     * Custom layout slots containing the components.
     */
    private final ArrayList<Component> components = new ArrayList<Component>();

    private List<Component> clientComponents = new ArrayList<Component>();

    /**
     * The client side gives a list of indexes for components that should be
     * rendered.
     */
    private final List<Integer> componentIndexesToSend = new ArrayList<Integer>();
    private final Set<Component> componentsPaintedOnClientSide = new HashSet<Component>();

    private String placeholderWidth = "100%";
    private String placeholderHeight = "181px";

    private int distance = 1000;
    private int renderDelay = 500;

    private boolean hasBeenRenderedBefore;

    /**
     * Add a component into this container. The component is added to the right
     * or under the last component.
     * <p/>
     * The component will not be rendered directly, but fetched with a separate
     * round-trip only after it gets visible.
     * 
     * @param c
     *            the component to be added.
     * @see #addComponentEagerly(Component)
     */
    @Override
    public void addComponent(final Component c) {
        // Add to components before calling super.addComponent
        // so that it is available to AttachListeners
        components.add(c);
        try {
            super.addComponent(c);
            requestRepaint();
        } catch (final IllegalArgumentException e) {
            components.remove(c);
            throw e;
        }
    }

    /**
     * Add a component into this container. The component is added to the right
     * or under the last component.
     * <p/>
     * The component will be added and rendered directly with this round-trip.
     * Best used with small incremental changes.
     * 
     * @param c
     *            the component to be added.
     * @see #addComponent(Component)
     */
    public void addComponentEagerly(final Component c) {
        addComponent(c);
        componentIndexesToSend.add(components.indexOf(c));
    }

    @Override
    public void removeComponent(final Component c) {
        components.remove(c);
        super.removeComponent(c);
        requestRepaint();
    }

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
    public int getComponentCount() {
        return components.size();
    }

    /**
     * @deprecated Don't call this manually
     */
    @Deprecated
    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        super.paintContent(target);

        if (!hasBeenRenderedBefore || target.isFullRepaint()) {

            /*
             * non-incremental data - stuff that is always needed whenever
             * starting from scratch.
             */

            target.addAttribute(VLazyLayout.ATT_PLACEHOLDER_HEIGHT_STRING,
                    placeholderHeight);
            target.addAttribute(VLazyLayout.ATT_PLACEHOLDER_WIDTH_STRING,
                    placeholderWidth);
            target.addAttribute(VLazyLayout.ATT_TOTAL_COMPONENTS_INT,
                    components.size());
            target.addAttribute(VLazyLayout.ATT_DISTANCE_INT, distance);
            target.addAttribute(VLazyLayout.ATT_RENDER_DELAY, renderDelay);

            // [ add preloaded components here. ]

            resetComponentDiff();
            componentsPaintedOnClientSide.clear();
        }

        else {

            /*
             * incremental data - stuff that is requested after the first
             * request.
             */

            final ComponentDiff diff = getAndResetComponentDiff();

            if (diff.hasChanges()) {
                componentsPaintedOnClientSide.removeAll(diff.getRemoves()
                        .keySet());

                target.addAttribute(VLazyLayout.ATT_ADD_PLACEHOLDERS_INTARR,
                        diff.getAdditionIndexes());

                target.addAttribute(VLazyLayout.ATT_REMOVE_COMPONENTS_INTARR,
                        diff.getRemovesIndexes());

                target.addAttribute(VLazyLayout.ATT_MOVE_COMPONENTS_MAP,
                        diff.getMoves());
            }
        }
        if (componentIndexesToSend != null) {
            Collections.sort(componentIndexesToSend);

            final Map<Component, Integer> componentOrder = new HashMap<Component, Integer>();
            for (final int sendIndex : componentIndexesToSend) {
                final Component component = components.get(sendIndex);
                component.paint(target);
                componentOrder.put(component, sendIndex);
                componentsPaintedOnClientSide.add(component);
            }

            target.addAttribute(VLazyLayout.ATT_PAINT_INDICES_MAP,
                    componentOrder);

            componentIndexesToSend.clear();
        }

        hasBeenRenderedBefore = true;
    }

    @SuppressWarnings("unchecked")
    private void resetComponentDiff() {
        clientComponents = (List<Component>) components.clone();
    }

    private ComponentDiff getAndResetComponentDiff() {

        final LinkedHashMap<Component, Integer> additions = new LinkedHashMap<Component, Integer>();
        for (int i = 0; i < components.size(); i++) {
            final Component c = components.get(i);
            if (!clientComponents.contains(c)) {
                additions.put(c, i);
            }
        }

        final Map<Component, Integer> removes = new HashMap<Component, Integer>();
        for (int i = 0; i < clientComponents.size(); i++) {
            final Component c = clientComponents.get(i);
            if (!components.contains(c)) {
                removes.put(c, i);
            }
        }

        /*
         * to reliably know how the components have been moved around, we need
         * to add the added and remove the removed. Thus we get all the properly
         * shifted indices too
         */

        clientComponents.removeAll(removes.keySet());

        // linkedhashmap preserves order
        for (final Entry<Component, Integer> entry : additions.entrySet()) {
            final Component component = entry.getKey();
            final Integer index = entry.getValue();
            clientComponents.add(index, component);
        }

        /*
         * Now clientComponents should have exactly the same elements, no more,
         * no less, than components. Some components may be ordered differently.
         * So we'll investigate that.
         */

        final Map<Component, Integer> moves = new HashMap<Component, Integer>();
        for (int i = 0; i < components.size(); i++) {
            final Component componentInNew = components.get(i);
            final Component componentInOld = clientComponents.get(i);

            if (!componentInNew.equals(componentInOld)
                    && !moves.containsKey(componentInNew)) {
                moves.put(componentInNew, i);
            }
        }

        resetComponentDiff();
        return new ComponentDiff(additions, removes, moves);
    }

    @Override
    public void replaceComponent(final Component oldComponent,
            final Component newComponent) {

        // Gets the locations
        int oldLocation = -1;
        int newLocation = -1;
        int location = 0;
        for (final Component component : components) {
            if (component == oldComponent) {
                oldLocation = location;
            }
            if (component == newComponent) {
                newLocation = location;
            }

            location++;
        }

        if (oldLocation == -1) {
            addComponent(newComponent);
        } else if (newLocation == -1) {
            removeComponent(oldComponent);
            addComponent(newComponent, oldLocation);
        } else {
            if (oldLocation > newLocation) {
                components.remove(oldComponent);
                components.add(newLocation, oldComponent);
                components.remove(newComponent);
                components.add(oldLocation, newComponent);
            } else {
                components.remove(newComponent);
                components.add(oldLocation, newComponent);
                components.remove(oldComponent);
                components.add(newLocation, oldComponent);
            }

            requestRepaint();
        }
    }

    /**
     * Adds a component into indexed position in this container.
     * 
     * @param c
     *            the component to be added.
     * @param index
     *            the index of the component position. The components currently
     *            in and after the position are shifted forwards.
     */
    public void addComponent(final Component c, int index) {
        // If c is already in this, we must remove it before proceeding
        // see ticket #7668
        if (c.getParent() == this) {
            // When c is removed, all components after it are shifted down
            if (index > components.indexOf(c)) {
                index--;
            }
            removeComponent(c);
        }
        components.add(index, c);
        try {
            super.addComponent(c);
            requestRepaint();
        } catch (final IllegalArgumentException e) {
            components.remove(c);
            throw e;
        }
    }

    @Override
    public void changeVariables(final Object source,
            final Map<String, Object> variables) {
        super.changeVariables(source, variables);

        if (variables.containsKey(VLazyLayout.VAR_LOAD_INDEXES_INTARR)) {
            for (final Object o : (Object[]) variables
                    .get(VLazyLayout.VAR_LOAD_INDEXES_INTARR)) {
                componentIndexesToSend.add((Integer) o);
            }
            requestRepaint();
        }
    }

    public void setPlaceholderSize(final String height, final String width) {
        placeholderWidth = width;
        placeholderHeight = height;
        requestRepaint();
    }

    /**
     * 
     * @param primary
     * @param secondary
     */
    public void setRenderDistance(final int pixels) {
        distance = pixels;
        requestRepaint();
    }

    /**
     * @param renderDelayMillis
     * @throws IllegalArgumentException
     *             if <code>renderDelayMillis</code> is negative.
     */
    public void setRenderDelay(final int renderDelayMillis) {
        if (renderDelayMillis < 0) {
            throw new IllegalArgumentException("delay can't be negative");
        }
        renderDelay = renderDelayMillis;
        requestRepaint();
    }
}
