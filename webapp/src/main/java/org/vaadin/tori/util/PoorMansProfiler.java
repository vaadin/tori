package org.vaadin.tori.util;

public class PoorMansProfiler {
    private final String name;
    private double baseTime;

    public PoorMansProfiler(final String name) {
        this.name = name;
        System.out.println(String.format("** Starting %s [%s]", getClass()
                .getSimpleName(), name));
        baseTime = System.currentTimeMillis();
    }

    public void mark() {
        mark(null);
    }

    public void mark(final String name) {
        final double offset = System.currentTimeMillis();

        if (name == null) {
            System.out.println(String.format("** %2$s mark: %1$.2f",
                    (offset - baseTime) / 1000, this.name));
        } else {
            System.out.println(String.format("** %3$s mark [%2$s]: %1$.2f",
                    (offset - baseTime) / 1000, name, this.name));
        }

        // account for the time this method has run.
        baseTime += System.currentTimeMillis() - offset;
    }

    public void stop() {
        final long stopTime = System.currentTimeMillis();
        System.out.println(String.format("** %1$s stopped, total: %2$.2f",
                name, (stopTime - baseTime) / 1000));
    }
}
