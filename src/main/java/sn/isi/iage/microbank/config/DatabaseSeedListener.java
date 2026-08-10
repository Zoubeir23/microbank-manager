package sn.isi.iage.microbank.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import sn.isi.iage.microbank.dao.JpaUtil;
import sn.isi.iage.microbank.dao.TransactionExecutor;
import sn.isi.iage.microbank.entity.Agency;
import sn.isi.iage.microbank.entity.User;
import sn.isi.iage.microbank.enums.Role;
import sn.isi.iage.microbank.enums.Statut;
import sn.isi.iage.microbank.util.PasswordHasher;

import java.util.logging.Logger;

/**
 * Cycle de vie de l'application.
 * <p>
 * Au demarrage : ouvre l'EntityManagerFactory et cree les comptes de demonstration
 * si la table des utilisateurs est vide, pour que l'application soit utilisable
 * immediatement apres une installation.
 * A l'arret : ferme proprement l'EntityManagerFactory et son pool de connexions.
 */
@WebListener
public class DatabaseSeedListener implements ServletContextListener {

    private static final Logger JOURNAL = Logger.getLogger(DatabaseSeedListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent evenement) {
        TransactionExecutor transactionExecutor =
                new TransactionExecutor(JpaUtil.getEntityManagerFactory());

        transactionExecutor.executeInTransaction(entityManager -> {
            long nombreDUtilisateurs = entityManager
                    .createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();

            if (nombreDUtilisateurs == 0) {
                entityManager.persist(creerUtilisateur(
                        "GAYE", "Abdoulaye", "admin", "admin123", Role.ADMIN));
                entityManager.persist(creerUtilisateur(
                        "DIOP", "Awa", "agent", "agent123", Role.AGENT));
                JOURNAL.info("Comptes de demonstration crees : admin / agent");
            }

            long nombreDAgences = entityManager
                    .createQuery("SELECT COUNT(a) FROM Agency a", Long.class)
                    .getSingleResult();

            if (nombreDAgences == 0) {
                entityManager.persist(creerAgence("AG-DKR", "Agence de Dakar", "Dakar"));
                entityManager.persist(creerAgence("AG-THS", "Agence de Thies", "Thies"));
            }
            return null;
        });
    }

    @Override
    public void contextDestroyed(ServletContextEvent evenement) {
        JpaUtil.close();
    }

    private User creerUtilisateur(String nom, String prenom, String login,
                                  String motDePasse, Role role) {
        User utilisateur = new User();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setLogin(login);
        utilisateur.setMotDePasse(PasswordHasher.hacher(motDePasse));
        utilisateur.setRole(role);
        utilisateur.setStatut(Statut.ACTIF);
        return utilisateur;
    }

    private Agency creerAgence(String code, String nom, String ville) {
        Agency agence = new Agency();
        agence.setCode(code);
        agence.setNom(nom);
        agence.setVille(ville);
        return agence;
    }
}
