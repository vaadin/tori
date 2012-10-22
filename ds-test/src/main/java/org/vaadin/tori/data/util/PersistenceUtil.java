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

package org.vaadin.tori.data.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceUtil {

    public static final String PERSISTENCE_UNIT_NAME = "tori-testdata";
    private static EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Returns a new EntityManager instance for the persistence unit named
     * {@value #PERSISTENCE_UNIT_NAME}.
     * 
     * @return a new EntityManager instance.
     */
    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }
}
