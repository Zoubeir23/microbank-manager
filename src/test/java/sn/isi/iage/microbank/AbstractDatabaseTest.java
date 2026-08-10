package sn.isi.iage.microbank;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import sn.isi.iage.microbank.model.Account;
import sn.isi.iage.microbank.model.Client;
import sn.isi.iage.microbank.model.User;
import sn.isi.iage.microbank.enums.Role;
import sn.isi.iage.microbank.enums.Statut;
import sn.isi.iage.microbank.enums.StatutCompte;
import sn.isi.iage.microbank.enums.TypeCompte;
import sn.isi.iage.microbank.util.PasswordHasher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Socle des tests qui touchent la base : ouvre une base H2 en memoire et remet les
 * tables a zero avant chaque test pour que les tests restent independants entre eux.
 */
public abstract class AbstractDatabaseTest {

    protected static EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void ouvrirBaseDeTest() {
        entityManagerFactory = Persistence.createEntityManagerFactory("microbank-test-pu");
    }

    @AfterAll
    static void fermerBaseDeTest() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void viderLesTables() {
        executerDansUneTransaction(entityManager -> {
            entityManager.createQuery("DELETE FROM Operation").executeUpdate();
            entityManager.createQuery("DELETE FROM ClientDocument").executeUpdate();
            entityManager.createQuery("DELETE FROM Account").executeUpdate();
            entityManager.createQuery("DELETE FROM Client").executeUpdate();
            entityManager.createQuery("DELETE FROM Agency").executeUpdate();
            entityManager.createQuery("DELETE FROM User").executeUpdate();
        });
    }

    protected void executerDansUneTransaction(Consumer<EntityManager> travail) {
        lireOuEcrire(entityManager -> {
            travail.accept(entityManager);
            return null;
        });
    }

    protected <T> T lireOuEcrire(Function<EntityManager, T> travail) {
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

    protected <T> T lire(Function<EntityManager, T> lecture) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return lecture.apply(entityManager);
        } finally {
            entityManager.close();
        }
    }

    // ===================== JEUX DE DONNEES =====================

    protected User creerAgent(String login) {
        return lireOuEcrire(entityManager -> {
            User agent = new User();
            agent.setNom("DIOP");
            agent.setPrenom("Awa");
            agent.setLogin(login);
            agent.setMotDePasse(PasswordHasher.hacher("agent123"));
            agent.setRole(Role.AGENT);
            agent.setStatut(Statut.ACTIF);
            entityManager.persist(agent);
            return agent;
        });
    }

    protected Client creerClient(String nom, String prenom, String numeroPiece) {
        return lireOuEcrire(entityManager -> {
            Client client = new Client();
            client.setNom(nom);
            client.setPrenom(prenom);
            client.setTelephone("770000000");
            client.setEmail(prenom.toLowerCase() + "@example.com");
            client.setNumeroPiece(numeroPiece);
            client.setDateCreation(LocalDate.now());
            client.setStatut(Statut.ACTIF);
            entityManager.persist(client);
            return client;
        });
    }

    protected Account creerCompte(Client client, String numeroCompte, BigDecimal solde,
                                  StatutCompte statut) {
        return lireOuEcrire(entityManager -> {
            Account compte = new Account();
            compte.setNumeroCompte(numeroCompte);
            compte.setType(TypeCompte.COURANT);
            compte.setSolde(solde);
            compte.setDateOuverture(LocalDate.now());
            compte.setStatut(statut);
            compte.setClient(entityManager.getReference(Client.class, client.getId()));
            entityManager.persist(compte);
            return compte;
        });
    }

    protected BigDecimal soldeDuCompte(Long compteId) {
        return lire(entityManager -> entityManager.find(Account.class, compteId).getSolde());
    }
}
