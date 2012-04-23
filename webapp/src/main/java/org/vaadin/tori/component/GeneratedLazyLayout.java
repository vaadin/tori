package org.vaadin.tori.component;

import java.util.List;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class GeneratedLazyLayout extends LazyLayout2 {
    /**
     * The ComponentGenerator is an interface that allows the client code to
     * generate the used components lazily.
     * <p/>
     * <strong>Note: Using this does not support the modification of existing
     * components.</strong> So this interface works best with an immutable
     * amount of lazily generated components.
     * <p/>
     * {@link LazyLayout#replaceComponent(Component, Component)
     * replaceComponent()} and {@link LazyLayout#removeComponent(Component)
     * removeComponent()} might work, but you need to handle the bookkeeping
     * yourself.
     */
    public interface ComponentGenerator {
        /**
         * Get the Components found at places between (and including) two index
         * points.
         * 
         * @param from
         *            The zero-based index of the first component to return
         * @param to
         *            The zero-based index of the last component to return
         * @return The components between (and including) <code>from</code> and
         *         <code>to</code> such that they are in displayed order.
         */
        List<Component> getComponentsAtIndexes(int from, int to);

        /**
         * This method is called only once, and it needs to contain the amount
         * of components shown throughout the lifetime of the LazyLayout.
         */
        int getAmountOfComponents();
    }

    private final ComponentGenerator generator;

    public GeneratedLazyLayout(final ComponentGenerator generator) {
        this.generator = generator;
        getState()
                .setTotalAmountOfComponents(generator.getAmountOfComponents());
    }

    /**
     * @deprecated
     * @throws UnsupportedOperationException
     *             always
     */
    @Deprecated
    @Override
    public void addComponent(final Component c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     * @throws UnsupportedOperationException
     *             always
     */
    @Deprecated
    @Override
    public void addComponentEagerly(final Component c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     * @throws UnsupportedOperationException
     *             always
     */
    @Deprecated
    @Override
    public void removeAllComponents() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     * @throws UnsupportedOperationException
     *             always
     */
    @Deprecated
    @Override
    public void removeComponent(final Component c) {
        throw new UnsupportedOperationException();
    }
}
