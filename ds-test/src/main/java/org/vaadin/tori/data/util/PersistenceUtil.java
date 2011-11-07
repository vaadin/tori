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
