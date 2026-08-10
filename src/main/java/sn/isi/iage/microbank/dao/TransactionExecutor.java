package sn.isi.iage.microbank.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.function.Function;

/**
 * Porte d'entree unique vers la persistance pour la couche service.
 * <p>
 * Les DAO de ce projet ne gerent volontairement ni EntityManager ni transaction :
 * un virement doit debiter un compte, en crediter un autre et journaliser
 * l'operation dans une seule et meme transaction. C'est donc ici que se trouvent
 * le {@code begin()}, le {@code commit()} et le {@code rollback()}.
 */
public class TransactionExecutor {

    private final EntityManagerFactory entityManagerFactory;

    public TransactionExecutor() {
        this(JpaUtil.getEntityManagerFactory());
    }

    public TransactionExecutor(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Execute un traitement qui modifie des donnees.
     * Tout ce qui est fait dans {@code travail} est valide ensemble, ou annule ensemble.
     */
    public <T> T executeInTransaction(Function<EntityManager, T> travail) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            T resultat = travail.apply(entityManager);
            entityManager.getTransaction().commit();
            return resultat;
        } catch (RuntimeException erreur) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw erreur;
        } finally {
            entityManager.close();
        }
    }

    /** Execute une lecture : aucune transaction d'ecriture n'est ouverte. */
    public <T> T executeQuery(Function<EntityManager, T> lecture) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return lecture.apply(entityManager);
        } finally {
            entityManager.close();
        }
    }
}
