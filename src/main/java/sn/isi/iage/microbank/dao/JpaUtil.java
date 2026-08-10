package sn.isi.iage.microbank.dao;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Fabrique unique d'{@link EntityManagerFactory} pour toute l'application.
 * La creation d'une EntityManagerFactory est couteuse : elle lit persistence.xml,
 * analyse les entites et ouvre le pool de connexions. On la cree donc une seule fois
 * et on la partage, alors qu'un EntityManager est cree puis ferme a chaque requete.
 */
public final class JpaUtil {

    public static final String PERSISTENCE_UNIT = "microbank-pu";

    private static EntityManagerFactory entityManagerFactory;

    private JpaUtil() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
        return entityManagerFactory;
    }

    public static synchronized void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        entityManagerFactory = null;
    }
}
