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
